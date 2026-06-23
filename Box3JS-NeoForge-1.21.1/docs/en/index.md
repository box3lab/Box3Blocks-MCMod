---
layout: home

hero:
  name: "Box3JS"
  text: "TypeScript Scripting Engine for Minecraft"
  tagline: No Java. No Gradle. No restarts. Write TypeScript, hot-reload in seconds.
  actions:
    - theme: brand
      text: Get Started
      link: /en/guide/getting-started
    - theme: alt
      text: Why Box3JS?
      link: /en/guide/about-box3js

features:
  - icon: ⚡
    title: TypeScript, Zero Java
    details: Full DTS types with bilingual JSDoc for every API. esbuild + Babel pipeline compiles modern TS to ES5 automatically. No JDK, no Gradle, no IDE setup.
  - icon: 🔄
    title: Hot Reload in Seconds
    details: Edit → save → see changes instantly. No server restarts. Built-in file watcher auto-reloads on every save.
  - icon: 🎮
    title: Box3-Compatible API
    details: Same clean API style as Box3 (Shenqi Code Island). World, Entity, Player, Voxels, Storage, Database, HTTP, remoteChannel — all as global objects.
  - icon: 🧩
    title: 110+ Minecraft Extensions
    details: Scoreboards, BossBars, teams, world border, particles, fireworks, lightning, potions, custom blocks/items/sounds, and more.
  - icon: 🖥️
    title: Server & Client Scripting
    details: Server handles game logic and world manipulation. Client handles keyboard input, HUD, audio, custom GUIs. Bidirectional events via remoteChannel.
  - icon: 🛡️
    title: Sandbox Protection
    details: Enable sandbox to auto-track all script changes. Disable to fully roll back. Test fearlessly — your map is always safe.
  - icon: 📦
    title: Standalone JAR Distribution
    details: /box3script compile packages your scripts into a standalone mod. Drop it into mods/ — no Box3JS dependency required.
  - icon: 🗄️
    title: Built-in Persistence
    details: JSON file storage and SQLite database on both server and client. Leaderboards, player saves, economies — all built-in.

---

## Quick Start

```bash
/box3script create mygame       # scaffold a TypeScript project
```

```bash
cd config/box3/script/mygame
npm install && npm run build     # install deps + build
```

```bash
/box3script sandbox mygame       # enable sandbox (recommended)
/box3script start mygame         # start the script
```

Open `src/server/app.ts`, write your game logic, save, then `/box3script reload mygame`. Enable `/box3script watch` and it auto-reloads on every save.

[Full documentation →](/en/guide/getting-started)

