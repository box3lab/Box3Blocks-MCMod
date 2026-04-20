# Box3Blocks（神岛材质包）-- Minecraft Mod

![Modrinth Downloads](https://img.shields.io/modrinth/dt/iG3hRUix?logo=modrinth)
![CurseForge Downloads](https://img.shields.io/curseforge/dt/1456138?logo=curseforge)

[简体中文](README.md) | [English](README_en.md)

Import 373 blocks from the Box3 (Magic Code Island) platform into Minecraft, so you can build with the same familiar blocks inside MC.  
You can also migrate structures from Box3 directly into your Minecraft world, preserving the original building style.

## 🌟 Features

### 🎨 Rich Block Library

- **373 blocks**: including letters, numbers, symbols, colors, elements, etc.
- **9 creative tabs**: organized for easy search and use
  - Box3: Letters – A-Z letter blocks
  - Box3: Numbers – 0-9 number blocks
  - Box3: Symbols – various symbol blocks
  - Box3: Colors – colorful blocks
  - Box3: Elements – chemical element blocks
  - Box3: Food – food-related blocks
  - Box3: Lights – emissive / light blocks
  - Box3: Nature – natural-texture blocks
  - Box3: Building – building material blocks

### 🏗 Importing Box3 Structures

- **Terrain file import**: supports importing compressed terrain files (`.gz`) from the `config/box3/` directory into your world.
- **Structure migration**: migrate structures from Box3 into Minecraft while keeping the same block appearance.
- **Get terrain files**: visit https://box3lab.com/build2mc to download Box3 building terrain files (`.gz`).
- **Import commands**:
  - `/box3import`  
    List all available terrain files (`.gz`) under `config/box3/`.
  - `/box3import <fileName>`  
    Import a structure from `config/box3/<fileName>.gz`.  
    (You don’t need to type the `.gz` extension in the command.)
  - `/box3import <fileName> <offsetY>`  
    Apply an additional vertical offset of `offsetY` blocks relative to your current position (positive = up, negative = down).
  - `/box3import <fileName> <offsetY> <ignoreBarrier>`  
    When `ignoreBarrier = true`, barrier blocks will be skipped (they will not be placed in the world).
  - `/box3import <fileName> <offsetY> <ignoreBarrier> <ignoreWater>`  
    When `ignoreWater = true`, all fluids are uniformly replaced with air.

### 📤 Exporting Minecraft Regions to Box3

- **Export commands**:
  - `/box3export <fileName>`  
    Automatically search for the two nearest `Redstone Block` markers (`minecraft:redstone_block`) around the player,
    treat them as opposite corners of the export region, and export that region to `config/box3/<fileName>.gz`.
  - **Search rules**: chunks are scanned from near to far until two marker blocks are found, then the search stops.  
    The maximum search radius is `1024` blocks.

### 🧩 Importing Box3 Model Items

- **Resource file import**: Supports importing resource packs from the `resourcepacks/` directory.
- **Resource pack model loading**: Put models into a resource pack to auto-register them in Creative.
- **Model creative tab**: `Box3:Models` tab for model items.
- **Model Destroyer**: Right-click a model to delete it (item name: Model Destroyer).
- **Generate model resource pack**: Visit https://box3lab.com/mc-resource-pack to get a pack compatible with this mod.

#### ✨ Model Operation Guide

- **Interactive parameter tuning**:
  - Right-click the model with an `empty hand`: switch mode (`Scale / Offset X / Offset Y / Offset Z / Rotation`)
  - Right-click the model with a `Stick`: increase the current mode value
  - Right-click the model with a `Blaze Rod`: decrease the current mode value
- **Copy & paste parameters**:
  - Right-click the model with `Paper`: copy current model parameters
  - Right-click the model with a `Book`: paste parameters to the target model

### 🔍 Barrier Visibility Toggle

- **Barrier visibility command `/box3barrier`**:
  - `/box3barrier`  
    Show whether barriers are currently visible.
  - `/box3barrier <bool>`  
    Turn barrier rendering on or off (barriers always have collision; this only controls rendering).
  - `/box3barrier toggle`  
    Quickly toggle barrier visibility.  
    The state is saved to a local config file and will be applied automatically next time you enter the world.

### 🔒 Command Permission Management

- `/box3import`, `/box3barrier`, and `/box3export` require different permission levels depending on configuration (default level `0`).
- Server owners can use `/box3perm` to view or adjust the required level:
  - `/box3perm` – show the current required permission level. Valid values range from `0-4`, matching Minecraft op levels.
  - `/box3perm <level>` – set the `requireOpForCommands` level to the specified value (`0-4`).

�📋 **Full block list**: see [block_id.md](block_id.md) for all block IDs, registry keys, and Chinese–English name mapping.

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).

## 🙏 Acknowledgements

- Blocks provided by Box3, and the mod developed by Box3Lab
- FabricMC team for the Fabric mod loader
- NeoForged team for the NeoForge mod loader

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=box3lab/Box3Blocks-MCMod&type=date&legend=top-left)](https://www.star-history.com/#box3lab/Box3Blocks-MCMod&type=date&legend=top-left)
