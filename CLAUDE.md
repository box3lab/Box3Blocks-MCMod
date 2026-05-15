# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Box3Blocks is a Minecraft mod that imports 372 decorative blocks from the Box3 platform into Minecraft, supporting terrain file import/export and model items. It also includes **Box3JS**, a dual-side (server + client) TypeScript/JavaScript scripting engine (Rhino) for creating custom gameplay, mini-games, GUIs, and world interactions.

The repository is a **multi-project monorepo** with 7 independent subprojects targeting different mod loaders and Minecraft versions. There is no root build system — each subproject has its own Gradle wrapper and `build.gradle`.

## Subprojects

| Directory          | Loader   | MC Version | Java | Notes                                    |
| ------------------ | -------- | ---------- | ---- | ---------------------------------------- |
| `Fabric-1.20.1/`   | Fabric   | 1.20.1     | 17   | `fabric-loom-remap`                      |
| `Fabric-1.21.1/`   | Fabric   | 1.21.1     | 21   | `fabric-loom-remap`                      |
| `Fabric-1.21.11/`  | Fabric   | 1.21.11    | 21   | `fabric-loom-remap`                      |
| `Fabric-26.1/`     | Fabric   | 26.1       | 25   | `fabric-loom`                            |
| `Forge-1.20.1/`    | Forge    | 1.20.1     | 17   | `net.minecraftforge.gradle` v6.x         |
| `NeoForge-1.21.1/` | NeoForge | 1.21.1     | 21   | **Box3JS lives here** — NeoForge ModDevGradle |
| `NeoForge-26.1/`   | NeoForge | 26.1       | 25   | NeoForge ModDevGradle                    |

Only NeoForge-1.21.1 has the Box3JS scripting engine. The other 6 subprojects are purely the Box3Blocks decorative block mod.

## Build Commands

```bash
# Build a single subproject
cd NeoForge-1.21.1 && ./gradlew build

# Clean build artifacts
cd NeoForge-1.21.1 && ./gradlew clean

# Build Box3JS scripts
cd run/config/box3/script/colorzone && npm run build
cd run/config/box3/script/mygame && npm run build

# Run project verification (Java ↔ DTS ↔ docs consistency)
cd NeoForge-1.21.1 && node tools/verify-box3js-project.mjs
```

**Important:** Forge-1.20.1 requires Java 17. All other subprojects use Java 21+. NeoForge-26.1 uses Java 25.

There are no existing tests (`src/test` directories are empty).

## Shared Resources Architecture

Shared resources are centralized to avoid ~20,000 duplicate asset files:

- **`shared-resources/`** — used by ALL subprojects: block textures, models, blockstates, item models, worldgen data, `block-id.json`, `block-spec.json`
- **`shared-resources-fabric/`** — used by all 4 Fabric subprojects: `models/item/` JSONs + lang files
- **`shared-resources-forge/`** — used by Forge + both NeoForge subprojects: `models/item/` JSONs + lang files

## Block Mod Architecture

All subprojects (including NeoForge-1.21.1) share the block mod's runtime generation architecture:

- `BlockIndexData` / `BlockIndexUtil` reads `block-id.json` and `block-spec.json` at registration time
- `VoxelBlockFactories` creates `Block` instances dynamically (no per-block Java classes)
- Only 6 special blocks have dedicated Java classes: `VoxelBlock`, `GlassVoxelBlock`, `BarrierVoxelBlock`, `BouncePadBlock`, `ConveyorBlock`, `SpiderWebBlock`

## Box3JS Scripting Engine (NeoForge-1.21.1 only)

Box3JS uses Mozilla Rhino to run **dual-side** JavaScript/TypeScript — server scripts (`src/server/`) run on the server thread, client scripts (`src/client/`) run on each player's client. Scripts live in `run/config/box3/script/<project>/`. Each project has its own isolated scope, callbacks, and tracked state.

### Package Structure

| Package | Role |
|---------|------|
| `script/` | Server-side API bindings, engine, event bus, sandbox |
| `client/` | Client-side engine, API bindings (input, ui, gui, audio, chat, storage, db, http) |
| `registries/` | Recipe manager |
| `standalone/` | JS→JAR compiler, standalone bootstrap, registry code-gen |

### Server-side Java (`script/`)

| File | Role |
|------|------|
| `Box3ScriptEngine.java` | Singleton Rhino engine: load/reload/stop scripts, fire events, manage scopes |
| `Box3ScriptCommand.java` | `/box3script` command handler |
| `Box3ScriptConfig.java` | Config: enabled projects, sandbox state, file watcher |
| `Box3ScriptSandbox.java` | Tracks block/entity/player/world mutations for rollback |
| `Box3ScriptTemplate.java` | Template for `/box3script create` |
| `Box3ScriptWatcher.java` | File watching + auto-reload on `.js` change |
| `Box3JSEventBus.java` | Per-project callback storage with isolation |
| `Box3JSCallbacks.java` | Callback interface definitions |
| `Box3JSWorld.java` | `world.*` API: events, entity queries, scoreboard, BossBar, teams, border, particles, fireworks, recipes, structures |
| `Box3JSEntity.java` | `entity.*` API: position, velocity, HP, tags, AI, equipment, effects |
| `Box3JSPlayer.java` | `player.*` API: inventory, flight, game mode, teleport, XP, food, advancements, tab list |
| `Box3JSVoxels.java` | `voxels.*` API: get/set voxel, fill region, spawner control |
| `Box3JSRemoteChannel.java` | `remoteChannel.*` cross-side event communication |
| `Box3JSStorage.java` | Per-project JSON file persistence + `GameDataStorage` inner class |
| `Box3JSHttp.java` | Server-side HTTP API (`http.*`) |
| `Box3DatabaseBase.java` | Shared SQLite database base class (used by server + client db) |
| `Box3JSQueryResult.java` | SQL query result wrapper exposed to JS |
| `Box3JSResponse.java` | HTTP response wrapper (`GameHttpFetchResponse`) |
| `Box3JSConsole.java` | `console.*` (log/warn/error/debug/clear/assert) |
| `Box3JSGuiServerHandler.java` | Handles C→S GUI packets (slot clicks, close) on server thread |
| `Box3JSGuiController.java` | Side-agnostic GUI controller (callbacks via Consumer/Runnable) |
| `Box3JSScriptContainerMenu.java` | `AbstractContainerMenu` subclass using vanilla `MenuType.GENERIC_9xN` |
| `Box3ScriptUtils.java` | Shared helpers: sound, raycast, lookAt, stringify |
| `Box3JSScoreboard.java` / `Box3JSBossbar.java` / `Box3JSTeam.java` | Scoreboard / BossBar / Team CRUD |
| `GameVector3.java` / `GameBounds3.java` / `GameRGBColor.java` / `GameRGBAColor.java` / `GameQuaternion.java` | Math types exposed to JS |
| `GameEventHandlerToken.java` | Returned by all `onXxx()` — has `cancel()` and `active()` |

### Client-side Java (`client/`)

| File | Role |
|------|------|
| `Box3JSClientEngine.java` | Client-side Rhino engine; wires `client`, `audio`, `input`, `ui`, `chat`, `gui`, `remoteChannel`, `storage`, `db`, `http` globals |
| `Box3JSGuiProxy.java` | Returned by `gui.openGUI()` — stores callbacks, sends C→S packets |
| `Box3JSClientStorage.java` | Client-side JSON storage + `GameDataStorage` |
| `Box3JSClientDatabase.java` | Client-side SQLite with graceful-fallback reminder |
| `Box3JSClientHttp.java` | Client-side HTTP API |
| `screen/Box3JSScriptContainerScreen.java` | **DELETED** — vanilla `ChestScreen` renders the container now |

### Network Layer

`Box3JSNetwork.java` defines all custom payloads (C↔S):
- **GUI**: `GUIServerboundPayload` (open/click/close), `GUIClientboundPayload` (slot update/close)
- **RemoteChannel**: `RemoteChannelPayload` (server→client event), `RemoteChannelServerboundPayload` (client→server event)

Payloads are registered with `optional()` — vanilla clients silently ignore them.

`Box3JS.java` (`@Mod` class) registers all payload handlers, subscribes to NeoForge events, and fires callbacks into JS.

### key constraints

- **No custom `MenuType`**: GUI uses vanilla `MenuType.GENERIC_9x1` through `GENERIC_9x6`. This means vanilla clients never see unknown registry keys and can connect safely.
- `world.currentTick` and `world.projectName` are **methods**, not properties: `world.currentTick()`, `world.projectName()`
- All `onXxx()` event registration methods return `GameEventHandlerToken` (has `.cancel()` and `.active()`)

### DTS Structure

Template types live in `src/main/resources/assets/box3js/template/types/`:

```
types/
  shared.d.ts          — math types, console, storage, db, http, remoteChannel
  server/
    index.d.ts         — references: shared, world, voxels, entity, player
    server.d.ts        — world, voxels, registries, server-specific remoteChannel/storage
    world.d.ts         — GameWorld interface
    entity.d.ts        — GameEntity + GamePlayerEntity type
    player.d.ts        — GamePlayer interface
    voxels.d.ts        — GameVoxels interface
  client/
    index.d.ts         — references: shared, client, audio, input, ui, chat, gui
    client.d.ts        — GameClient, RemoteChannel (client-side)
    audio.d.ts         — GameAudio
    input.d.ts         — GameInput
    ui.d.ts            — GameUI
    chat.d.ts          — GameChat
    gui.d.ts           — GameGUI + GuiController
```

**Template sync rule**: When changing template DTS, also sync to `colorzone/types/` and `mygame/types/`.

### Script Build Pipeline

`build.mjs` in each project: `esbuild bundle` → `Babel` (target Rhino 1.9.1) → regex sanitize. Two entry points:

```
src/server/app.ts → dist/server.js   (server-side, runs on server thread)
src/client/app.ts → dist/client.js   (client-side, runs on each player's client)
```

Supports `--watch` for hot reload. ESLint uses split tsconfig: `tsconfig.server.json` + `tsconfig.client.json` (no root tsconfig).

### ESLint Config

Projects with split tsconfig (no root `tsconfig.json`) must explicitly list them:

```js
// eslint.config.mjs
export default [
  {
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.server.json', './tsconfig.client.json'],
      },
    },
  },
];
```

### Registries System (Standalone/JAR mode)

`Box3JSRegistryGen.java` reads JSON config files (`registries/blocks.json`, `items.json`, `creativeTabs.json`, `sounds.json`) and generates Java registration code injected into the compiled `@Mod` class. `Box3ScriptCompiler.java` bundles JS source into a JAR; `Box3StandaloneBootstrap.java` launches it.

### Recipe Manager

`Box3JSRecipeManager` uses `RecipeManager.replaceRecipes()` (public API, no reflection):
- `removeRecipe(id)` / `clearRecipes()` / `listRecipes(filter)`

### Documentation

- `docs/api/` — Full API reference: world, entity, player, voxels, storage, database, http, server, client, math, commands (CN + EN)
- `docs/guide/` — Getting started, architecture, JS vs Java comparison, FAQ, cookbook
- `docs/tutorial/` — 6-part tutorial (01-basics → 06-client-scripting)

## Version Differences

- `VoxelExport` only in Fabric-1.21.11, Fabric-26.1, and Forge/NeoForge variants
- `VoxelFluidRenderHandler` only in Fabric-1.21.11
- NeoForge-26.1 moved client code to `src/client/java`
- Fabric-26.1 uses `fabric-loom` (not `fabric-loom-remap`) and Java 25

## Tools

- **`tools/verify-box3js-project.mjs`** — 7-check project integrity: template files, type split, event tokens, globals, Java↔DTS parity, DTS docs, docs↔API sync
- **`tools/box3js-api-manifest.json`** — Source of truth: 17 globals + 32 API groups with Java/DTS/docs paths, accessor property rules, ignore lists
- **`tools/generate_blocks_fabric.py`** / **`tools/generate_blocks_forge.py`** — Block registration code generators
- **`tools/box3-texture-cut/`** — TypeScript tool for cutting sprite sheets into textures
