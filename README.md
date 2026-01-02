**MegaTorch** is a simple yet effective plugin for server owners and players, to allow one single torch to disable monster spawning in a large area around the torch.

When a **MegaTorch** is placed, in the defined radius in the config file (default radius of 64 blocks) it prevents monsters from spawning inside of that radius.

Commands & Permissions:
/megatorch reload - megatorch.reload
/megatorch give <player> [amount] - megatorch.give

Config Settings:

```yaml
settings:
  radius: 64 # The radius in blocks from the torch to prevent monsters from spawning.
  item:
    material: TORCH # The material for the torch
    max_stack_size: 64 # Control how large of a stack size these items can be
    name: '&6Mega Torch' # The name of the torch, supports color codes
    # The lore on the torch, supports multiple lines and colors.
    lore:
      - '&eWhen placed prevents mobs from spawning'
      - '&eup to 64 blocks around it.'
```
