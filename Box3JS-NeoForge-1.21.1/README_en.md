# Box3JS — Minecraft Scripting Engine

> **Beta** — This project is in early beta. APIs may change. Feedback is welcome.

[简体中文](Readme.md) | [English](README_en.md)

**No Java knowledge required. Build unlimited Minecraft gameplay with TypeScript.**

Box3JS is a server-side scripting engine (Mozilla Rhino) built into a NeoForge mod. Forget complex Java mod development — write TypeScript, hot-reload instantly, see changes live. PvP arenas, RPG dungeons, party games, world management, social tools — all achievable with scripts.

## Installation

1. Place `box3js-<version>.jar` into your server's `mods/` directory
2. For SQLite database support (`db` API), also install [`minecraft-sqlite-jdbc`](https://modrinth.com/mod/minecraft-sqlite-jdbc)
3. Start the server

## 5-Minute Quick Start

In-game (requires OP level ≥ 2):

```
/box3script create mygame
```

This creates a TypeScript project:

```
config/box3/script/mygame/
├── package.json          ← npm dependencies (esbuild, Babel, TypeScript)
├── tsconfig.json
├── build.mjs             ← build script (esbuild → Babel → Rhino)
├── eslint.config.mjs
├── types/
│   └── globals.d.ts      ← full API type declarations (IDE autocomplete)
└── src/
    └── app.ts            ← entry point — write your code here
```

Build and start:

```bash
cd config/box3/script/mygame
npm install && npm run build
```

```
/box3script sandbox mygame     # (recommended) enable sandbox for safe testing
/box3script start mygame       # start the script
```

Edit `src/app.ts`, re-run `npm run build`, then `/box3script reload mygame` — changes take effect **without restarting the server**.

## Why Box3JS?

| Feature | Description |
|---------|-------------|
| **Zero barrier** | Know JS/TS? You can build. No Gradle, no IDE, no restarts |
| **Hot reload** | Edit → build → reload in seconds. Enable `watch` for auto-reload |
| **Sandbox** | Toggle sandbox to track all script changes; disable to fully roll back |
| **TypeScript** | Full `.d.ts` type declarations, esbuild + Babel pipeline, IDE IntelliSense |
| **17 events** | onTick, onPlayerJoin, onChat, onEntityDeath, onBlockActivate, onButtonPressed... |
| **Visual effects** | 13+ particles, fireworks, lightning, explosions, sounds |
| **Game systems** | Scoreboards, BossBar, teams, world border, cross-script messaging |
| **Custom items** | JSON-configured items (food, rarity, glint), dynamic recipe management |
| **Data persistence** | JSON storage + SQLite database (leaderboards, economy, player data) |

## Commands

| Command | Description |
|---------|-------------|
| `/box3script` | Show project status overview |
| `/box3script create <name>` | Create a new TypeScript project |
| `/box3script start [project\|all]` | Enable and load projects |
| `/box3script stop [project\|all]` | Disable and unload projects |
| `/box3script reload [project]` | Reload scripts (for development) |
| `/box3script watch` | Toggle file watching (auto hot-reload) |
| `/box3script sandbox <project>` | Toggle sandbox (on=track / off=rollback) |
| `/box3script compile <project>` | Compile to standalone JAR (no Box3JS needed) |

All `<project>` arguments support **Tab completion**. [Full command reference →](docs/api/commands_en.md)

## API Overview

| Global | Purpose |
|--------|---------|
| `world` | World state, events, particles, fireworks, lightning, sounds, scoreboards, BossBar, teams, border, custom items |
| `entity` | Entity properties, AI pathfinding, equipment, potion effects, tags, navigation |
| `player` | Inventory, flight, game mode, teleport, messaging, XP, sounds |
| `voxels` | Block read/write, region fill, spawner control |
| `storage` | JSON data persistence |
| `db` | SQLite database — SQL queries, leaderboards, player data |
| `console` | Server console logging (`log`/`warn`/`error`/`debug`) |
| `GameVector3` | 3D vector (coordinate math) |
| `GameBounds3` | Bounding box |
| `GameRGBColor` / `GameRGBAColor` | RGB / RGBA color |
| `GameQuaternion` | Quaternion (rotation math) |

[API Overview →](docs/api/README_en.md) · [Find by Task →](docs/api/README_en.md#find-by-task--i-want-to)

## Tutorials

From zero to full mini-games. Every example is TypeScript-compiled and ESLint-verified:

| # | Tutorial | Time | What you'll learn |
|---|----------|------|-------------------|
| 1 | [Getting Started](docs/tutorial/01-basics.md) | 10 min | Project setup, first script, chat commands, timers |
| 2 | [Players & Items](docs/tutorial/02-player-items.md) | 15 min | Teleport, flight, items, enchantments, potions, custom items |
| 3 | [Events & Entities](docs/tutorial/03-events-entities.md) | 15 min | Event callbacks, entity spawning, AI, combat, patrols |
| 4 | [Advanced Systems](docs/tutorial/04-advanced-systems.md) | 15 min | Scoreboards, BossBar, teams, world border, cross-script messaging |
| 5 | [Mini-Games](docs/tutorial/05-examples.md) | 20 min | PvP arena, particles & fireworks, wave mobs, visual effects |

[Tutorial overview →](docs/tutorial/README.md)

## Documentation Structure

```
docs/
├── api/                   ← API Reference
│   ├── README.md          Overview + find by task
│   ├── world.md           World API (events, particles, fireworks, scoreboards...)
│   ├── entity.md          Entity API (properties, AI, equipment, effects...)
│   ├── player.md          Player API (inventory, messaging, flight, teleport...)
│   ├── voxels.md          Voxels API (read/write, fill, spawner)
│   ├── storage.md         Storage API (JSON persistence)
│   ├── database.md        Database API (SQLite)
│   ├── math.md            Math API (Vector3, Color, Quaternion)
│   └── commands.md        /box3script command reference
├── tutorial/              ← Tutorials
│   ├── README.md          Learning path overview
│   ├── 01-basics.md       Getting started
│   ├── 02-player-items.md Players & items
│   ├── 03-events-entities.md Events & entities
│   ├── 04-advanced-systems.md Advanced systems
│   └── 05-examples.md     Mini-games
└── BOX3_API_COMPARISON.md ← Box3 platform vs Box3JS API comparison
```

## Example Project

`run/config/box3/script/colorzone/` contains a complete Territory Rush game and 7 verified feature examples covering every tutorial scenario.

## Dependencies

| Feature | Requirement |
|---------|-------------|
| Script engine core | Rhino 1.9.1 bundled — no extra install needed |
| `db` API (SQLite) | Requires [`minecraft-sqlite-jdbc`](https://modrinth.com/mod/minecraft-sqlite-jdbc) mod |
| All other APIs | No additional dependencies |

> Without `minecraft-sqlite-jdbc`, all APIs except `db` work normally. Only calling `db.sql()` triggers an error asking you to install it.

## Tech Stack

- **Runtime:** Mozilla Rhino 1.9.1 (embedded JS engine for JVM)
- **Build tools:** esbuild bundle → Babel transpile (Rhino target) → regex sanitize
- **Language:** TypeScript, compiled to ES5-compatible JS
- **Platform:** NeoForge 1.21.1, Java 21

## License

Apache License 2.0
