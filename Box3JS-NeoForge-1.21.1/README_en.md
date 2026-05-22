# Box3JS — Minecraft Scripting Engine

> **v0.1.0** — First public beta release. APIs may change. Feedback is welcome.

[简体中文](Readme.md) | [English](README_en.md)

**No Java knowledge required. Build unlimited Minecraft gameplay with TypeScript.**

Box3JS embeds the Mozilla Rhino JavaScript engine in a Minecraft mod. Its API design is inspired by [Box3](https://dao3.fun)  — bringing Box3's clean, efficient developer experience into Minecraft. No Gradle, no server restarts, no complex toolchains. PvP arenas, tower defense, RPG dungeons, party games — write TypeScript, load instantly, see changes live.

> Box3JS & the Box3 platform? → [About Box3JS](docs/en/guide/about-box3js.md)

```ts
// On player join: welcome dialog + particles + sound
world.onPlayerJoin((player) => {
  player.player.dialog({ content: "Welcome to the Box3JS server!" });
  const pos = new GameVector3(player.position.x, player.position.y + 1.8, player.position.z);
  world.spawnParticle("minecraft:happy_villager", pos, 10, 0.5, 0.5, 0.5, 0.1);
  world.playSound("minecraft:entity.player.levelup", pos, 1.0, 1.0);
});

// Chat commands: !heal to restore HP, !firework to launch a firework
world.onChat((player, message) => {
  if (message === "!heal") {
    player.hp = player.maxHp;
    player.player.actionBar("Healed!");
    return false; // cancel broadcast
  }
  if (message === "!firework") {
    const pos = new GameVector3(player.position.x, player.position.y + 1, player.position.z);
    world.launchFirework(pos, "green", "large_ball");
    return false;
  }
});
```

<details>
<summary>Versus: the same feature in a traditional Java mod</summary>

```java
// Java — ~60 lines, 3 classes for the same result
@Mod.EventBusSubscriber(modid = "mymod")
public class PlayerJoinListener {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        // Dialogs require client-side handling; server-side can only send chat
        player.sendSystemMessage(
            Component.literal("Welcome to the server!").withStyle(ChatFormatting.GREEN)
        );
        // Particles need ServerLevel and ParticleOptions
        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position().add(0, 1.8, 0);
        level.sendParticles(
            ParticleTypes.HAPPY_VILLAGER,
            pos.x, pos.y, pos.z,
            10, 0.5, 0.5, 0.5, 0.1
        );
        // Same for sound
        level.playSound(
            null, player.blockPosition(),
            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
            1.0f, 1.0f
        );
    }
}

@Mod.EventBusSubscriber(modid = "mymod")
public class ChatListener {
    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getMessage().getString();
        if ("!heal".equals(message)) {
            player.setHealth(player.getMaxHealth());
            player.displayClientMessage(
                Component.literal("Healed!"), true
            );
            event.setCanceled(true);
        }
        if ("!firework".equals(message)) {
            // Fireworks need manual entity construction
            FireworkRocketEntity firework = new FireworkRocketEntity(
                player.level(),
                player.getX(), player.getY() + 1, player.getZ(),
                createFireworkItem("green", FireworkRocketEntity.Shape.LARGE_BALL)
            );
            player.level().addFreshEntity(firework);
            event.setCanceled(true);
        }
    }
    // + ~20 lines for the firework item builder...
}
```
</details>

## Why Box3JS?

**Zero barrier to entry** — If you know JS/TS, you can build mods. No Gradle, no IDE, no server restarts. `/box3script create` scaffolds a complete TypeScript project in one command.

**Instant hot reload** — Edit → build → reload takes seconds. Enable `watch` mode and skip the build step too — save and it reloads automatically.

**Box3-like, Minecraft-native** — The API design mirrors Box3's developer experience. Creators familiar with Box3 can transition seamlessly. Plus full access to Minecraft's native capabilities: mob AI, particles, structures, advancements, recipes.

**Sandbox protection** — Enable sandbox to automatically track all script changes. Disable it to fully roll back. Test fearlessly — your map is always safe.

**First-class TypeScript** — Complete `.d.ts` type declarations with bilingual JSDoc. Build pipeline: esbuild bundle → Babel transpile to ES5 → regex sanitize. Full IDE IntelliSense.

**Dual-side scripting** — Server handles game logic, entity AI, block manipulation. Client handles keyboard input, screen UI, chat, audio. Bidirectional real-time communication via `remoteChannel`.

**Built-in persistence** — JSON file storage and SQLite database for leaderboards, economies, player saves — all accessible from both server and client scripts.

**Standalone distribution** — `/box3script compile` packages your scripts into a standalone JAR mod. Drop it into `mods/` like any other mod — no Box3JS required.

## Installation

1. Place `box3js-<version>.jar` into your server's `mods/` directory
2. For SQLite database support (`db` API), also install [`minecraft-sqlite-jdbc`](https://modrinth.com/mod/minecraft-sqlite-jdbc)
3. Start the server

## Quick Start

In chat (requires OP level ≥ 2):

```
/box3script create mygame       # scaffold a TypeScript project
```

Then build and start:

```bash
cd config/box3/script/mygame
npm install && npm run build     # install deps + build
```

```
/box3script sandbox mygame       # enable sandbox (recommended)
/box3script start mygame         # start the script
```

Open `src/server/app.ts`, write game logic, `npm run build`, then `/box3script reload mygame` — **no server restart needed**. Enable `/box3script watch` and it auto-reloads on every save.

> [Full getting-started guide →](docs/en/guide/getting-started.md)

## Commands

| Command | Description |
|---------|-------------|
| `/box3script` | Show all project statuses |
| `/box3script create <name>` | Scaffold a new TypeScript project |
| `/box3script start [project\|all]` | Enable and load projects |
| `/box3script stop [project\|all]` | Disable and unload projects |
| `/box3script reload [project]` | Reload scripts (development) |
| `/box3script watch` | Toggle file watching (auto hot-reload) |
| `/box3script sandbox <project>` | Toggle sandbox (on=track / off=rollback) |
| `/box3script compile <project>` | Compile to standalone JAR mod |

All `<project>` arguments support **Tab completion**. [Full command reference →](docs/en/api/commands.md)

## API Overview

| Global | Purpose |
|--------|---------|
| `world` | World events, entity queries, particles, fireworks, lightning, sounds, scoreboards, BossBar, teams, border |
| `entity` | Entity properties, AI pathfinding/attack, equipment, potion effects, attribute modification |
| `player` | Inventory, flight, game mode, teleport, messaging, XP |
| `voxels` | Block read/write, region fill, replace, spawner control |
| `remoteChannel` | Server ↔ client bidirectional event channel |
| `client` · `input` · `ui` · `chat` · `audio` · `gui` | Client APIs: lifecycle, keyboard, screen text, chat, audio, custom containers |
| `http` | HTTP requests (sync + async) |
| `storage` · `db` | JSON persistence / SQLite database (server & client) |
| `registries` | Custom blocks, items & sounds (compiled JAR mode) |
| `GameVector3` · `GameBounds3` · `GameRGBColor` · `GameQuaternion` | Math types: vectors, bounding boxes, colors, quaternions |

[API Overview →](docs/en/api/README.md) · [Find by Task →](docs/en/api/README.md#find-by-task--i-want-to) · [Full Docs →](docs/en/README.md)

## Tutorials

From zero to full mini-games. Every example is TypeScript-compiled and ESLint-verified:

| # | Tutorial | Time | What you'll learn |
|---|----------|------|-------------------|
| 1 | [Getting Started](docs/en/tutorial/01-basics.md) | 10 min | Project setup, first script, chat commands, timers |
| 2 | [Players & Items](docs/en/tutorial/02-player-items.md) | 15 min | Teleport, flight, items, enchantments, potion effects |
| 3 | [Events & Entities](docs/en/tutorial/03-events-entities.md) | 15 min | All event callbacks, entity spawning, AI control, patrol guards |
| 4 | [Advanced Systems](docs/en/tutorial/04-advanced-systems.md) | 15 min | Scoreboards, BossBar, teams, world border, cross-script messaging |
| 5 | [Mini-Games](docs/en/tutorial/05-examples.md) | 20 min | PvP arena, particles & fireworks, wave mob spawning |
| 6 | [Client Scripting](docs/en/tutorial/06-client-scripting.md) | 15 min | Keyboard input, screen UI, audio/music, local storage, remoteChannel |

[Tutorial Overview →](docs/en/tutorial/README.md)

## Example Projects

`run/config/box3/script/` contains multiple full game projects ready to run or study:

| Project | Genre | Highlights |
|---------|-------|------------|
| `patdef` | Tower Defense | 4 tower types (arrow/ice/fire/lightning), wave spawning, GUI shop, attack beam VFX |
| `bedwar` | Team PvP | Two-team combat, resource generators, gear upgrades, traps, bed destruction |
| `coredf` | Core Defense | Chinese-localized, multi-phase waves, economy system |
| `az` | Multi-Phase | Complex state machine, phase transitions, mixed gameplay |
| `colorzone` | Parkour + Demo | Bidirectional client-server communication, UI examples, 7 feature demos |
| `mygame` | API Tests | Functional test cases covering every Box3JS API |

Each project has its own client `dist/client.js` and server `dist/server.js` — drop in and play.

## Dependencies

| Feature | Requirement |
|---------|-------------|
| Script engine core | Rhino 1.9.1 bundled — no extra install needed |
| `db` API (SQLite) | Requires [`minecraft-sqlite-jdbc`](https://modrinth.com/mod/minecraft-sqlite-jdbc) |
| All other APIs | No additional dependencies |

> Without the SQLite mod, everything except `db` works normally. Only calling `db.sql()` triggers a reminder.

## License

Apache License 2.0
