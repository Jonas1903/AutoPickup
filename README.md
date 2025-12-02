# AutoPickup

Minecraft Paper 1.21.8 Auto Pickup & Auto Smelt Plugin with GUI configuration.

## Features

### Auto Pickup System
- Items from broken blocks go directly into your inventory
- If inventory is full, items drop normally on the ground
- Toggle on/off per player

### Auto Smelt System
- Ores automatically smelt into their smelted form (e.g., Iron Ore → Iron Ingot)
- Only applies to smeltable items configured by server admins
- Toggle on/off per player

### Player Toggle GUI
- Access via `/autopickup` or `/ap`
- Toggle Auto Pickup ON/OFF
- Toggle Auto Smelt ON/OFF
- Visual indicators showing current status

### Custom Ore Converter Item
- Special item that converts ores into configurable output items
- Hold in **offhand** while mining to activate
- Example: 64 Diamonds → 10 Amethyst Shards
- Obtain via `/ap give <player>` command

### Admin Configuration GUI
- Access via `/autopickup admin` or `/ap admin`
- Drag-and-drop functionality to set input/output items
- Configurable conversion ratios
- Toggle which items can be auto-smelted

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/autopickup` or `/ap` | Opens player toggle GUI | `autopickup.use` |
| `/ap admin` | Opens admin configuration GUI | `autopickup.admin` |
| `/ap give <player>` | Gives ore converter item to a player | `autopickup.admin` |
| `/ap reload` | Reloads configuration | `autopickup.admin` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `autopickup.use` | Allows using auto pickup/smelt features | `true` |
| `autopickup.admin` | Access to admin configuration GUI | `op` |
| `autopickup.converter` | Allows using the ore converter item | `true` |

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart or reload your server
4. Configure using `/ap admin` or by editing `config.yml`

## Building from Source

### Requirements
- Java 22
- Maven 3.6+

### Build Commands
```bash
# Clone the repository
git clone https://github.com/Jonas1903/AutoPickup.git
cd AutoPickup

# Build with Maven
mvn clean package

# The JAR file will be in target/AutoPickup-1.0.0.jar
```

## Configuration

### config.yml
```yaml
# Default smelting recipes (can be modified via admin GUI)
auto-smelt:
  enabled-items:
    - IRON_ORE
    - DEEPSLATE_IRON_ORE
    - GOLD_ORE
    - DEEPSLATE_GOLD_ORE
    - COPPER_ORE
    - DEEPSLATE_COPPER_ORE
    - ANCIENT_DEBRIS

# Ore converter settings
ore-converter:
  input-item: DIAMOND
  input-amount: 64
  output-item: AMETHYST_SHARD
  output-amount: 10

# Messages (supports & color codes)
messages:
  prefix: "&8[&6AutoPickup&8] "
  auto-pickup-enabled: "&aAuto Pickup enabled!"
  auto-pickup-disabled: "&cAuto Pickup disabled!"
  auto-smelt-enabled: "&aAuto Smelt enabled!"
  auto-smelt-disabled: "&cAuto Smelt disabled!"
  no-permission: "&cYou don't have permission to do that!"
  converter-received: "&aYou received the Ore Converter item!"
  converter-given: "&aGave Ore Converter to %player%!"
  config-reloaded: "&aConfiguration reloaded!"
  player-not-found: "&cPlayer not found!"
  usage: "&cUsage: /autopickup [admin|give <player>|reload]"
```

## Supported Smeltable Items

The plugin supports automatic smelting for the following items:
- Iron Ore → Iron Ingot
- Deepslate Iron Ore → Iron Ingot
- Gold Ore → Gold Ingot
- Deepslate Gold Ore → Gold Ingot
- Copper Ore → Copper Ingot
- Deepslate Copper Ore → Copper Ingot
- Raw Iron/Gold/Copper → Ingots
- Ancient Debris → Netherite Scrap
- Sand → Glass
- Cobblestone → Stone
- And many more...

## License

MIT License - See LICENSE file for details.

## Author

Jonas1903
