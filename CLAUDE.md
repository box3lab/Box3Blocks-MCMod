# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Box3Blocks is a Minecraft mod that imports 372 decorative blocks from the Box3 platform into Minecraft, supporting terrain file import/export and model items. The repository is a **multi-project monorepo** with 7 independent subprojects targeting different mod loaders and Minecraft versions. There is no root build system — each subproject has its own Gradle wrapper and `build.gradle`.

## Subprojects

| Directory          | Loader   | MC Version | Java | Key Plugin                       |
| ------------------ | -------- | ---------- | ---- | -------------------------------- |
| `Fabric-1.20.1/`   | Fabric   | 1.20.1     | 17   | `fabric-loom-remap`              |
| `Fabric-1.21.1/`   | Fabric   | 1.21.1     | 21   | `fabric-loom-remap`              |
| `Fabric-1.21.11/`  | Fabric   | 1.21.11    | 21   | `fabric-loom-remap`              |
| `Fabric-26.1/`     | Fabric   | 26.1       | 25   | `fabric-loom`                    |
| `Forge-1.20.1/`    | Forge    | 1.20.1     | 17   | `net.minecraftforge.gradle` v6.x |
| `NeoForge-1.21.1/` | NeoForge | 1.21.1     | 21   | NeoForge ModDevGradle            |
| `NeoForge-26.1/`   | NeoForge | 26.1       | 25   | NeoForge ModDevGradle            |

## Build Commands

Each subproject is built independently. The Gradle wrapper exists in each subproject directory:

```bash
# Build a single subproject
cd Fabric-26.1 && ./gradlew build

# Clean build artifacts
cd Fabric-26.1 && ./gradlew clean

# Build all in sequence (bash loop)
for d in Fabric-1.20.1 Fabric-1.21.1 Fabric-1.21.11 Fabric-26.1; do
  (cd "$d" && ./gradlew build) || break
done
```

**Important:** Forge-1.20.1 requires Java 17 (ForgeGradle 6.x doesn't support Java 21+). All other subprojects use their respective Java versions as noted in the table above.

There are no existing tests (`src/test` directories are empty).

## Shared Resources Architecture

To avoid ~20,000 duplicate asset files across subprojects, shared resources are centralized into three directories. Each subproject's `build.gradle` pulls from the appropriate sources:

- **`shared-resources/`** — used by ALL subprojects: 2,324 block textures (PNG + mcmeta), 372 block models, 372 blockstates, 372 item model definitions, `icon.png`, worldgen data, `block-id.json`, `block-spec.json`
- **`shared-resources-fabric/`** — used by all 4 Fabric subprojects: 372 `models/item/` JSONs + lang files (`en_us.json`, `zh_cn.json`)
- **`shared-resources-forge/`** — used by Forge + both NeoForge subprojects: 372 `models/item/` JSONs + lang files

Resource merging uses `DuplicatesStrategy.EXCLUDE` in Fabric/Forge `processResources` (subproject-local files take precedence over shared), and NeoForge uses `srcDir()` in source sets.

Per-subproject files that remain in `src/main/resources/`:

- `fabric.mod.json` (Fabric variants — metadata + mixin configs)
- `META-INF/mods.toml`, `pack.mcmeta` (Forge/NeoForge)
- `data/box3/worldgen/visible.json` override (Fabric-1.20.1 only)
- `assets/box3/lang/` overrides (Fabric-1.21.11, Fabric-26.1 — newer lang format differs from the older Fabric group)

## Code Architecture

### Two Package Trees

Fabric subprojects use the **`com.box3lab`** package. Forge/NeoForge subprojects use **`com.box3lab.box3`**. The Java source under each is structurally similar but uses loader-specific APIs (Fabric's `Registry` vs Forge/NeoForge's `DeferredRegister`).

### Runtime Block Generation

This mod does **not** define each of the 372 blocks as individual Java classes. Instead, blocks are generated programmatically at registration time:

1. `BlockIndexData` / `BlockIndexUtil` reads `block-id.json` and `block-spec.json` from resources — these define every block's ID, name, category, light level, opacity, etc.
2. `VoxelBlockFactories` / `VoxelBlockPropertiesFactory` creates `Block` instances dynamically from the spec data.
3. `ModBlocks` (Fabric) or `Box3Blocks` (Forge/NeoForge) orchestrates registration into Minecraft's registry system.
4. `CreativeTabRegistrar` groups blocks into 9 creative mode tabs based on category.

Only 6 special blocks have dedicated Java classes: `VoxelBlock`, `GlassVoxelBlock`, `BarrierVoxelBlock`, `BouncePadBlock`, `ConveyorBlock`, `SpiderWebBlock`.

### Key Source Files (in every subproject)

- **Entry point**: `Box3.java` (Fabric, implements `ModInitializer`) or `Box3Blocks.java` (Forge/NeoForge, annotated `@Mod`)
- **Client entry**: `Box3Client.java` (Fabric) or `Box3BlocksClient.java` (Forge/NeoForge)
- **Commands**: `ModCommands.java` — `/box3import`, `/box3export`, `/box3barrier`, `/box3perm`
- **Config**: `ConfigUtil.java` (Fabric) or `Box3Config.java` (Forge/NeoForge) — permission level, barrier visibility
- **Import/Export**: `Box3ImportFiles.java` / `VoxelImport.java` / `VoxelExport.java` — terrain `.gz` file handling
- **Model items**: `PackModelBlockEntity.java` / `PackModelEntityBlock.java` — resource-pack-loaded custom models

### Version Differences Worth Noting

- `VoxelExport` only exists in Fabric-1.21.11, Fabric-26.1, and all Forge/NeoForge variants (not in older Fabric)
- `VoxelFluidRenderHandler` only in Fabric-1.21.11
- NeoForge-26.1 moved client code from `src/main/java` to `src/client/java`
- Fabric-26.1 uses `fabric-loom` (not `fabric-loom-remap`) and Java 25

## Tools

- **`tools/generate_blocks_fabric.py`** — generates Fabric block registration code
- **`tools/generate_blocks_forge.py`** — generates Forge/NeoForge block registration code
- **`tools/strength_blocks.py`** — block property utilities shared by both generators
- **`tools/box3-texture-cut/`** — TypeScript tool for cutting sprite sheets into individual block textures
