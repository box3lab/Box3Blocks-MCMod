# Box3JS and Box3 (Shenqi Code Island)

This page explains where Box3JS comes from, why it exists, and what unique advantages it offers.

## What is Box3 (神奇代码岛)

[Box3](https://box3.fun) (also known as 神奇代码岛 or "Code Island 3.0") is a **multiplayer 3D game creation platform** developed by Shenzhen Qimengdao Technology Co., Ltd. (深圳奇梦岛科技有限公司), a brand under Codemao (编程猫). Users create racing games, PvP arenas, RPGs, FPS shooters, and even MOBAs — all in a browser, using nothing but JavaScript.

Key features:

- **JavaScript programming** — Write game logic in JS/TypeScript with zero game engine experience
- **Real-time multiplayer** — Platform provides servers, rooms, and network sync out of the box
- **Cross-platform** — Works on PC, mobile, and tablet with no downloads
- **Community UGC** — All games are created by users; the platform itself ships no preset content

Box3 has given thousands of young creators their first experience of "making a game." Its API design has been battle-tested by a large community over years, resulting in a clean, intuitive, and efficient programming model.

## What is Box3JS

Box3JS is a **community-driven Minecraft mod** (NeoForge 1.21.1) that embeds a JavaScript engine (Mozilla Rhino) inside the Minecraft server, enabling developers to write server-side gameplay logic in TypeScript, with optional client-side scripts for key listeners, screen UI, and local audio.

**Box3JS is not an official product of Box3.** It was created by community developers familiar with the Box3 ecosystem, continuing Box3's API design philosophy and naming conventions.

## Why Box3JS for Minecraft Exists

### The Idea

Box3's API design is genuinely excellent — refined through years of community use:

- Global object injection — no `import`/`require` needed
- Tick-based timers — perfectly synced with the game world
- Universal cancellation pattern (`GameEventHandlerToken`)
- Per-project scope isolation — multiple scripts never interfere

But Box3 runs on its own closed platform. Creators can't reach the Minecraft ecosystem — which has a larger player community, richer block mechanics, and a mature mod distribution system.

**Box3JS's core mission: bring the Box3-level developer experience into Minecraft.**

### Who Box3JS is For

| User | Why |
|------|-----|
| Box3 platform developers | Same API style — reuse existing skills with zero learning curve |
| MC server owners wanting custom gameplay | No Java, Gradle, or Mixin needed — just write JS |
| Programming education | TypeScript + hot reload + sandbox rollback = ideal teaching environment |
| Developers who don't want to write Java mods | Ready-to-use APIs cover common needs with no build pipeline burden |

## Unique Advantages of Box3JS

### 1. Box3-Compatible API Design

If you've written scripts for Box3, Box3JS code looks immediately familiar:

```js
// This code looks almost identical on Box3 and Box3JS
world.onPlayerJoin((entity) => {
  entity.player.directMessage(`§aWelcome ${entity.player.name}!`);
});

world.onChat((entity, message) => {
  if (message === "!hello") {
    entity.player.directMessage("Hello!");
    return false;
  }
  return true;
});
```

For a detailed API comparison, see [Box3 API vs Box3JS](../BOX3_API_COMPARISON.md).

### 2. Real Minecraft World

Box3JS directly operates the Minecraft world — real blocks, vanilla entities, complete game mechanics. You can:

- Manipulate real MC blocks (`voxels.setVoxel`, `voxels.fillVoxel`)
- Spawn vanilla entities with AI, equipment, and potion effects
- Use Minecraft's scoreboards, BossBars, and team system
- Execute vanilla commands (`player.runCommand`)
- Control weather, time, world borders, and game rules

### 3. Hot Reload + Sandbox

- **Save-and-reload** — Edit → `npm run build` → `/box3script reload` — no server restart
- **Sandbox protection** — Enable sandbox to track all world changes; disable to roll back everything
- **Auto hot-reload** — Enable `watch` to auto-reload on build output changes

### 4. Standalone Distribution

When development is done, compile into a standalone JAR with one command:

```
/box3script compile mygame
```

Generates `mygame-1.0.0.jar` — drop it into any NeoForge 1.21.1 server's `mods/` directory. No source code needed, no build tools required. Ready for CurseForge and Modrinth distribution.

### 5. Dual-Side Architecture

```
Server (authoritative)       Client (presentation)
world.* / voxels.*           client.* / input.*
entity.* / player.*   ←→     ui.* / audio.* / gui.*
storage / db / http          storage / db / http
     └── remoteChannel ──────┘
```

Server handles authoritative game logic; client handles local presentation (UI, audio, input). Bidirectional communication via `remoteChannel`.

### 6. Full TypeScript Experience

- Complete `.d.ts` type declarations for VS Code IntelliSense
- esbuild + Babel build pipeline with modern syntax support (`const`, arrow functions, template literals, `async/await`)
- ESLint code checking
- Mutually exclusive `tsconfig.server.json` / `tsconfig.client.json` to prevent API misuse

## Differences from Box3 Platform

Box3JS is not a 1:1 copy of Box3's API. Differences stem from the fundamental differences between the two platforms:

| Area | Box3 Platform | Box3JS (MC) |
|------|--------------|-------------|
| Rendering | Custom 3D engine | Minecraft vanilla renderer |
| Physics | Custom physics engine | Minecraft vanilla physics |
| Weather | Independent rain/snow/fog (rich parameter control) | MC vanilla weather (rainDensity/thunderDensity) |
| Lighting | Manual/natural light modes (lightMode/sunFrequency) | MC vanilla lighting |
| Custom models | Built-in editor | Requires Resource Pack (MC mechanism) |
| Custom blocks/items | Runtime registration | Requires JAR compilation (`registries` + `/box3script compile`) |
| Database | Built-in KV storage | JSON storage + SQLite (requires sqlite-jdbc mod) |
| Networking | Platform-managed | `remoteChannel` custom payloads |

**Design principle:** Keep API naming and semantics consistent where possible, but don't force-fit MC-incompatible features. For a detailed comparison, see [Box3 API vs Box3JS](../BOX3_API_COMPARISON.md).

---

## Next Steps

- **Get started**: [Quick Start Guide](getting-started_en.md) — write your first MC script in 10 minutes
- **How it works**: [Architecture](architecture_en.md) — Rhino engine, scope isolation, build pipeline
- **API reference**: [API by Task](../api/README_en.md) — find APIs by "I want to..."
