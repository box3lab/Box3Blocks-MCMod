# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

Box3Blocks is a Minecraft mod that imports 372 decorative blocks from the Box3 platform into Minecraft, supporting terrain file import/export and model items. It also includes **Box3JS**, a server-side TypeScript/JavaScript scripting engine (Rhino) for creating custom gameplay, mini-games, and world interactions.

The repository is a **multi-project monorepo** with 8 independent subprojects targeting different mod loaders and Minecraft versions. There is no root build system — each subproject has its own Gradle wrapper and `build.gradle`.

## Subprojects

| Directory          | Loader   | MC Version | Java | Notes                                    |
| ------------------ | -------- | ---------- | ---- | ---------------------------------------- |
| `Fabric-1.20.1/`   | Fabric   | 1.20.1     | 17   | `fabric-loom-remap`                      |
| `Fabric-1.21.1/`   | Fabric   | 1.21.1     | 21   | `fabric-loom-remap`                      |
| `Fabric-1.21.11/`  | Fabric   | 1.21.11    | 21   | `fabric-loom-remap`                      |
| `Fabric-26.1/`     | Fabric   | 26.1       | 25   | `fabric-loom`                            |
| `Fabric-26.2/`     | Fabric   | 26.2       | 25   | `fabric-loom`                            |
| `Forge-1.20.1/`    | Forge    | 1.20.1     | 17   | `net.minecraftforge.gradle` v6.x         |
| `NeoForge-1.21.1/` | NeoForge | 1.21.1     | 21   | **Box3JS lives here** — NeoForge ModDevGradle |
| `NeoForge-26.1/`   | NeoForge | 26.1       | 25   | NeoForge ModDevGradle                    |

Only NeoForge-1.21.1 has the Box3JS scripting engine. The other 7 subprojects are purely the Box3Blocks decorative block mod.

## Build Commands

```bash
# Build a single subproject
cd NeoForge-1.21.1 && ./gradlew build

# Clean build artifacts
cd NeoForge-1.21.1 && ./gradlew clean

# Build Box3JS script (in run/config/box3/script/<project>/)
cd run/config/box3/script/colorzone
npm install && npm run build       # esbuild → Babel → Rhino target
```

**Important:** Forge-1.20.1 requires Java 17. Fabric-1.21.1 and Fabric-1.21.11 use Java 21. Fabric-26.1, Fabric-26.2, and NeoForge-26.1 use Java 25.

There are no existing tests (`src/test` directories are empty).

## Shared Resources Architecture

Shared resources are centralized to avoid ~20,000 duplicate asset files:

- **`shared-resources/`** — used by ALL subprojects: block textures, models, blockstates, item models, worldgen data, `block-id.json`, `block-spec.json`
- **`shared-resources-fabric/`** — used by all 5 Fabric subprojects: `models/item/` JSONs + lang files
- **`shared-resources-forge/`** — used by Forge + both NeoForge subprojects: `models/item/` JSONs + lang files

## Block Mod Architecture

All subprojects (including NeoForge-1.21.1) share the block mod's runtime generation architecture:

- `BlockIndexData` / `BlockIndexUtil` reads `block-id.json` and `block-spec.json` at registration time
- `VoxelBlockFactories` creates `Block` instances dynamically (no per-block Java classes)
- Only 6 special blocks have dedicated Java classes: `VoxelBlock`, `GlassVoxelBlock`, `BarrierVoxelBlock`, `BouncePadBlock`, `ConveyorBlock`, `SpiderWebBlock`

## Box3JS Scripting Engine (NeoForge-1.21.1 only)

Box3JS uses Mozilla Rhino to run server-side JavaScript/TypeScript. Scripts live in `run/config/box3/script/<project>/`. Each project has its own isolated scope, callbacks, and tracked state.

### Java Package: `com.box3lab.box3js`

| File | Role |
|------|------|
| `Box3JS.java` | `@Mod` entry point, subscribes to NeoForge events, fires callbacks into JS |
| `script/Box3ScriptEngine.java` | Singleton Rhino engine: load/reload/stop scripts, fire events, manage scopes |
| `script/Box3ScriptCommand.java` | `/box3script` command handler |
| `script/Box3ScriptConfig.java` | Config: enabled projects, sandbox state, file watcher |
| `script/Box3ScriptSandbox.java` | Tracks block/entity/player/world mutations for rollback |
| `script/Box3ScriptTemplate.java` | Template for `/box3script create` |
| `script/Box3ScriptWatcher.java` | File watching + auto-reload on `.js` change |
| `script/Box3JSWorld.java` | `world.*` API: events, entity queries, scoreboard, BossBar, teams, border, particles, fireworks, recipes, structures, custom items |
| `script/Box3JSEntity.java` | `entity.*` API: position, velocity, HP, tags, AI, equipment, effects |
| `script/Box3JSPlayer.java` | `player.*` API: inventory, flight, game mode, teleport, XP, food, advancements, tab list |
| `script/Box3JSVoxels.java` | `voxels.*` API: get/set voxel, fill region, spawner control |
| `script/Box3JSQuery.java` | `world.querySelectorAll()` / `entitiesInRadius()` etc. |
| `script/Box3JSEventBus.java` | Per-project callback storage with isolation |
| `script/Box3JSCallbacks.java` | Callback interface definitions |
| `script/Box3JSScoreboard.java` | Scoreboard CRUD |
| `script/Box3JSBossbar.java` | BossBar CRUD |
| `script/Box3JSTeam.java` | Team CRUD |
| `script/Box3JSStorage.java` | Per-project JSON file persistence |
| `script/Box3ScriptUtils.java` | Shared helpers: sound playing, raycast, entity lookAt |
| `script/GameVector3.java` | 3D vector exposed to JS (`new GameVector3(x, y, z)`) |
| `script/GameBounds3.java` | AABB bounds |
| `script/GameRGBColor.java` / `GameRGBAColor.java` | Color types |
| `script/GameQuaternion.java` | Quaternion math |
| `script/GameEventHandlerToken.java` | Returned by `world.onXxx()` — has `cancel()` and `active()` |
| `registries/Box3JSCustomItems.java` | Custom items via Minecraft data components on `minecraft:paper` carrier |
| `registries/Box3JSRecipeManager.java` | Recipe blacklist via `RecipeManager.replaceRecipes()` |

### DTS Type Constraints

`world.currentTick` and `world.projectName` are **methods** in `globals.d.ts`, not properties:
```ts
world.currentTick()  // ✅ returns number
world.projectName()  // ✅ returns string
```

### Script Build Pipeline

`build.mjs` in each script project does: `esbuild bundle` → `Babel` (target Rhino 1.9.1) → regex sanitize for Rhino. Entry is always `src/app.ts`, output is `dist/app.js`. Supports `--watch` for hot reload.

### Custom Items System

Uses `minecraft:paper` as carrier with `DataComponents` (CUSTOM_NAME, LORE, CUSTOM_MODEL_DATA, MAX_STACK_SIZE, ENCHANTMENT_GLINT_OVERRIDE, RARITY, FOOD). Client-side textures via resource pack `paper.json` with `custom_model_data` overrides. **No DeferredRegister** — no registry sync needed.

Config: `resourcepacks/box3js-items/items.json` + textures + model JSONs. Loaded via `world.loadCustomItems("box3js-items")`.

Consumable/Cooldown/Enchantable/JukeboxPlayable components are NOT available in NeoForge 21.1.220 (need MC 1.21.2+).

### Recipe Manager

`Box3JSRecipeManager` uses `RecipeManager.replaceRecipes()` (public API, no reflection):
- `removeRecipe(id)` — filters via replaceRecipes
- `clearRecipes()` — restores full original list
- `listRecipes(filter)` — searches by keyword

### Documentation

- `docs/api/` — Full API reference for world, entity, player, voxels, storage, math, commands (Chinese + English)
- `docs/tutorial/` — 5-part tutorial series (01-basics → 05-examples) with complete PvP arena and parkour game examples

## Version Differences

- `VoxelExport` only in Fabric-1.21.11, Fabric-26.1, Fabric-26.2, and Forge/NeoForge variants
- `VoxelFluidRenderHandler` only in Fabric-1.21.11
- NeoForge-26.1 moved client code to `src/client/java`
- Fabric-26.1 and Fabric-26.2 use `fabric-loom` (not `fabric-loom-remap`) and Java 25
- Fabric-26.2 uses `EntityTypes.ITEM_DISPLAY` (not `EntityType.ITEM_DISPLAY`) and `Vec3.atCenterOf()` (not `BlockPos.getCenter()`)

## Tools

- **`tools/generate_blocks_fabric.py`** / **`tools/generate_blocks_forge.py`** — generates block registration code
- **`tools/box3-texture-cut/`** — TypeScript tool for cutting sprite sheets into textures
