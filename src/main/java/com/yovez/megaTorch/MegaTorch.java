package com.yovez.megaTorch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MegaTorch extends JavaPlugin implements Listener {

    private List<Location> megaTorchLocations;

    @Override
    public void onEnable() {
        // Plugin startup logic
        megaTorchLocations = new ArrayList<>();
        saveDefaultConfig();
        if (getConfig().isConfigurationSection("megatorches")) {
            for (String s : getConfig().getConfigurationSection("megatorches").getKeys(false)) {
                megaTorchLocations.add(getConfig().getLocation("megatorches." + s));
            }
        }
        getCommand("megatorch").setExecutor((CommandSender sender, Command cmd, String label, String[] args) -> {
            if (args.length == 0) {
                if (!sender.hasPermission("megatorch.reload")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have permission to do that."));
                    return true;
                }
                sender.sendMessage("MegaTorch Usage:");
                sender.sendMessage("/megatorch reload");
                sender.sendMessage("/megatorch give <player> [amount]");
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("megatorch.give")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have permission to do that."));
                    return true;
                }
                reloadConfig();
                sender.sendMessage("Reloaded MegaTorch config.");
                return true;
            }
            if (args[0].equalsIgnoreCase("give")) {
                if (!sender.hasPermission("megatorch.give")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have permission to do that."));
                    return true;
                }
                if (args.length == 2 || args.length == 3) {
                    if (args[0].equalsIgnoreCase("give")) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage("Player not found");
                            return true;
                        }
                        int amount = args.length == 3 ? Integer.parseInt(args[2]) : 1;
                        for (int i = 0; i < amount; i++) {
                            target.getInventory().addItem(getMegaTorchItemStack());
                        }
                        sender.sendMessage("Gave " + amount + " Mega Torches to " + target.getName());
                        return true;
                    }
                }
                sender.sendMessage("Try /megatorch give <player> [amount]");
                return true;
            }
            return true;
        });
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void placeTorch(Location location) {
        megaTorchLocations.add(location);
        int torchId = getConfig().isConfigurationSection("megatorches") ? getConfig().getConfigurationSection("megatorches").getKeys(false).size() : 0;
        getConfig().set("megatorches." + torchId, location);
        saveConfig();
    }

    public void breakTorch(Location location) {
        megaTorchLocations.remove(location);
        for (String id : getConfig().getConfigurationSection("megatorches").getKeys(false)) {
            if (getConfig().getLocation("megatorches." + id).equals(location)) {
                getConfig().set("megatorches." + id, null);
                saveConfig();
                return;
            }
        }
    }

    public ItemStack getMegaTorchItemStack() {
        ItemStack item = new ItemStack(Material.TORCH);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setMaxStackSize(getConfig().getInt("settings.item.max_stack_size", 64));
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString("settings.item.name", "&6Mega Torch")));
        List<String> configLore = new ArrayList<>(getConfig().getStringList("settings.item.lore"));
        List<String> itemLore = new ArrayList<>();
        if (!configLore.isEmpty()) {
            for (String s : configLore) {
                itemLore.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            meta.setLore(itemLore);
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent e) {
        boolean isHostile = (e.getEntity() instanceof Monster);
        if (isHostile) {
            if (!megaTorchLocations.isEmpty()) {
                for (Location torchLocation : megaTorchLocations) {
                    if (torchLocation != null && torchLocation.getWorld() != null && torchLocation.getWorld().equals(e.getLocation().getWorld())) {
                        if (e.getLocation().distance(torchLocation) <= getConfig().getInt("settings.radius", 64)) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTorchPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        if (e.getItemInHand().isSimilar(getMegaTorchItemStack())) {
            placeTorch(e.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onTorchBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        if (megaTorchLocations.contains(e.getBlock().getLocation())) {
            e.setDropItems(false);
            e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), getMegaTorchItemStack());
            breakTorch(e.getBlock().getLocation());
        }
    }
}
