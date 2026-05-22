---
layout: home

hero:
  name: "Box3JS"
  text: "JS/TS Scripting Engine for Minecraft"
  tagline: Build custom gameplay, mini-games, and UIs — no JDK, no Java compilation required.
  actions:
    - theme: brand
      text: Get Started
      link: /en/guide/getting-started

features:
  - icon: 🎮
    title: Server & Client Scripting
    details: Server-side world manipulation, entities, recipes. Client-side keyboard input, screen UI, sounds, SQLite storage, and HTTP requests.
  - icon: 📦
    title: TypeScript First
    details: Full DTS type definitions for all 17 global objects. Built-in esbuild + Babel pipeline transpiles modern TS to Rhino-compatible ES5.
  - icon: 🔄
    title: Hot Reload
    details: Edit scripts and see changes instantly without restarting the server. File watcher auto-reloads on save.
  - icon: 🌐
    title: Bidirectional Communication
    details: remoteChannel enables server↔client event messaging. Server broadcasts to all players; clients reply independently.
  - icon: 🗄️
    title: Dual-Side Storage & Database
    details: JSON file persistence and SQLite on both server and client. Pagination, atomic updates, counters, and tagged-template queries.
  - icon: 🧩
    title: Custom Blocks & Items
    details: Block textures, item models, equipment, sounds, and creative tabs — all registered from JSON configs (standalone/JAR mode).
  - icon: 📚
    title: Comprehensive Docs
    details: 50+ pages across API reference, progressive tutorials, cookbook recipes, architecture deep-dive, and FAQ — in Chinese and English.
  - icon: 🚀
    title: Standalone JAR Mode
    details: Compile your script project into a self-contained JAR mod. No runtime dependency on Box3JS — just drop it in your mods folder.

---

## Quick Start

```bash
# In-game: create a new project
/box3script create mygame

# Build and watch
cd config/box3/script/mygame
npm install
npm run build -- --watch

# TypeScript type checking
npm run check
```

```ts
// src/server/app.ts — your first script
world.onChat((entity, message) => {
  if (message === "!hello") {
    entity.player.directMessage(`Hello, ${entity.player.name}!`);
    return false;
  }
  return true;
});
```

[Read full docs →](/en/guide/getting-started)

## Version Info

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| Mod Loader | NeoForge |
| Java | 21 |
| JS Engine | Mozilla Rhino 1.9.1 (ES5) |
| TypeScript | via Babel → ES5 |

