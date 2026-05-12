# Box3JS Documentation

Box3JS is a JavaScript/TypeScript scripting engine mod for Minecraft NeoForge 1.21.1. Write server-side gameplay scripts and client-side UI scripts without installing a JDK or compiling Java code.

## Navigation

### Getting Started

Learn what Box3JS is, how to use it, and the principles behind it — from zero.

| Doc | Content |
|------|------|
| [Quick Start](guide/getting-started_en.md) | Setup → first script → dev cycle → debugging → deployment |
| [Architecture](guide/architecture_en.md) | Rhino engine, scope management, build pipeline, network communication |
| [JS vs Java](guide/js-vs-java_en.md) | Box3JS scripting vs native Java modding — pros, cons & when to choose |

### Tutorials

5 progressive tutorials, each 10–15 minutes with complete runnable code.

| # | Tutorial | You'll learn |
|---|---------|-------------|
| 1 | [From Zero](tutorial/01-basics.md) | Create project → build → first script → chat commands → timers |
| 2 | [Players & Items](tutorial/02-player-items.md) | Teleport, flight, give items, enchantments, potion effects, game modes |
| 3 | [Events & Entities](tutorial/03-events-entities.md) | All event callbacks, spawn entities, AI control, patrol guards, collision |
| 4 | [Advanced Systems](tutorial/04-advanced-systems.md) | Scoreboards, BossBars, teams, world border, cross-script messaging |
| 5 | [Real Mini-Games](tutorial/05-examples.md) | Full PvP arena, particle effects, fireworks, wave spawning |
| 📋 | [Tutorial Overview](tutorial/README_en.md) | Learning roadmap, prerequisites, pro tips |

### API Reference

Complete API docs organized by functional category. One document per global object/namespace.

| Category | Doc | Globals |
|----------|-----|---------|
| **World** | [world](api/world_en.md) | `world` — events, particles, fireworks, sound, scoreboards |
| **Entity** | [entity](api/entity_en.md) | `entity` — properties, AI, equipment, effects |
| **Player** | [player](api/player_en.md) | `player` — inventory, messages, flight, teleport |
| **Voxels** | [voxels](api/voxels_en.md) | `voxels` — block read/write, region fill |
| **Storage** | [storage](api/storage_en.md) | `storage` — JSON persistence |
| **Database** | [database](api/database_en.md) | `db` — SQLite database |
| **HTTP** | [http](api/http_en.md) | `http` — HTTP requests |
| **Client** | [client](api/client_en.md) | `audio` `client` `input` `ui` `chat` `remoteChannel` |
| **Registries** | [registries](api/registries_en.md) | `registries` — custom blocks/items/sounds |
| **Math** | [math](api/math_en.md) | `GameVector3` `GameBounds3` `GameRGBColor` `GameRGBAColor` `GameQuaternion` |
| **Commands** | [commands](api/commands_en.md) | `/box3script` CLI commands |
| **Task Lookup** | [API by Task](api/README_en.md) | Find APIs by "I want to..." |
| **Comparison** | [Box3 API Comparison](BOX3_API_COMPARISON.md) | Box3 platform API vs Box3JS implementation |

### Version Info

| Item | Version |
|------|---------|
| Minecraft | 1.21.1 |
| Mod Loader | NeoForge |
| Java | 21 |
| JS Engine | Mozilla Rhino 1.9.1 (ES5 compatible) |
| TypeScript | Compiled to ES5 via Babel |

## Quick Links

- **5-minute quickstart**: [Quick Start →](guide/getting-started_en.md)
- **I want to do X, which API?**: [API by Task →](api/README_en.md)
- **Why Box3JS over Java modding?**: [JS vs Java →](guide/js-vs-java_en.md)
- **How does Box3JS work internally?**: [Architecture →](guide/architecture_en.md)
- **Learn Box3JS scripting from zero**: [Tutorial 1 →](tutorial/01-basics.md)
