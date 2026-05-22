# Box3JS Documentation

Box3JS is a JavaScript/TypeScript scripting engine mod for Minecraft NeoForge 1.21.1. Write server-side gameplay scripts and client-side UI scripts in JS/TS — no JDK, no Java compilation required.

## Documentation Navigation

### Getting Started

Start from zero: what Box3JS is, how to use it, and how it works under the hood.

| Doc | Content |
|-----|---------|
| [Getting Started](guide/getting-started.md) | Setup → first script → dev loop → debugging → deployment |
| [Common Recipes](guide/recipes.md) | Templates: economy, teleport, shop, daily rewards, leaderboard, webhook |
| [Architecture](guide/architecture.md) | Rhino engine, scope management, build pipeline, network communication |
| [JS vs Java](guide/js-vs-java.md) | Advantages and trade-offs of Box3JS scripting vs native Java modding |
| [FAQ](guide/faq.md) | Loading, build, runtime, database, HTTP, client-side, deployment |

### Tutorials

6 progressive tutorials, 10-15 minutes each, with complete runnable code.

| # | Tutorial | What you'll learn |
|---|----------|-------------------|
| 1 | [Basics](tutorial/01-basics.md) | Create a project → build → first script → chat commands → timers |
| 2 | [Players & Items](tutorial/02-player-items.md) | Teleport, flight, items, enchantments, potion effects, game mode |
| 3 | [Events & Entities](tutorial/03-events-entities.md) | All event callbacks, entity spawning, AI control, patrol guards, collision |
| 4 | [Advanced Systems](tutorial/04-advanced-systems.md) | Scoreboard rankings, BossBar countdown, teams, world border, cross-script messaging |
| 5 | [Mini-Games](tutorial/05-examples.md) | PvP arena (fully playable), particle effects, fireworks, wave-based mob spawning |
| 6 | [Client Scripting](tutorial/06-client-scripting.md) | Keyboard input, screen UI, audio/music, local storage, SQLite, HTTP, remoteChannel |
| 📋 | [Tutorial Overview](tutorial/README.md) | Learning path, prerequisites, development tips |

### API Reference

Complete API documentation organized by function. One doc per global object/namespace.

| Category | Doc | Global Object |
|----------|-----|---------------|
| **Server Overview** | [server](api/server.md) | Server runtime boundaries, events, players/entities, blocks, data, cross-side communication |
| **World** | [world](api/world.md) | `world` — events, particles, fireworks, sounds, scoreboards |
| **Entity** | [entity](api/entity.md) | `entity` — properties, AI, equipment, effects |
| **Player** | [player](api/player.md) | `player` — inventory, messaging, flight, teleport |
| **Voxels** | [voxels](api/voxels.md) | `voxels` — read/write blocks, region fill |
| **Storage** | [storage](api/storage.md) | `storage` — JSON persistence |
| **Database** | [database](api/database.md) | `db` — SQLite database |
| **HTTP** | [http](api/http.md) | `http` — HTTP requests |
| **Client** | [client](api/client.md) | `audio` `client` `input` `ui` `chat` `gui` `remoteChannel` |
| **Registries** | [registries](api/registries.md) | `registries` — custom blocks/items/sounds |
| **Math** | [math](api/math.md) | `GameVector3` `GameBounds3` `GameRGBColor` `GameRGBAColor` `GameQuaternion` |
| **Commands** | [commands](api/commands.md) | `/box3script` CLI reference |
| **Quick Search** | [Find by Task](api/README.md) | "I want to do X" → find the right API |
| **Comparison** | [Box3 API Comparison](../BOX3_API_COMPARISON.md) | Side-by-side comparison of Box3 platform APIs vs Box3JS (Chinese) |

### Version & Compatibility

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| Mod Loader | NeoForge |
| Java | 21 |
| JS Engine | Mozilla Rhino 1.9.1 (ES5 compatible) |
| TypeScript | Compiled to ES5 via Babel |

## Quick Links

- **5-minute quickstart**: [Getting Started →](guide/getting-started.md)
- **"I want to do X, what API?"**: [API Quick Search →](api/README.md)
- **Why Box3JS over Java mods?**: [JS vs Java →](guide/js-vs-java.md)
- **How Box3JS works internally**: [Architecture →](guide/architecture.md)
- **Learn Box3JS from scratch**: [Tutorial 1 →](tutorial/01-basics.md)
