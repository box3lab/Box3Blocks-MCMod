---
layout: home

hero:
  name: "Box3JS"
  text: "Minecraft JS/TS Scripting Engine"
  tagline: Same programming experience as Box3 — build mini-games in Minecraft with JS/TS
  actions:
    - theme: brand
      text: Get Started
      link: /en/guide/getting-started

features:
  - icon: 🎮
    title: Server & Client Dual-Side Scripting
    details: "Server: world manipulation, entities, recipes. Client: keyboard input, screen UI, audio, SQLite storage, HTTP requests."
  - icon: 📦
    title: TypeScript-First
    details: DTS type definitions for all 17 global objects. Built-in esbuild + Babel pipeline transpiles modern TS to Rhino-compatible ES5.
  - icon: 🔄
    title: Hot Reload
    details: Script changes take effect instantly — no server restart needed. File watcher auto-reloads on save.
  - icon: 🌐
    title: Bidirectional Communication
    details: remoteChannel for server↔client event messaging. Server broadcasts to all players, clients reply independently.
  - icon: 🗄️
    title: Dual-Side Storage & Database
    details: JSON file persistence and SQLite on both server and client. Paginated queries, atomic updates, counters, tagged templates.
  - icon: 🧩
    title: Custom Blocks & Items
    details: Block textures, item models, equipment, sounds, creative tabs — all configured via JSON registries (standalone/JAR mode).
  - icon: 📚
    title: Comprehensive Docs
    details: 50+ pages covering API reference, progressive tutorials, recipes, architecture deep-dive, and FAQ — bilingual CN/EN.
  - icon: 🚀
    title: Standalone JAR Mode
    details: Compile script projects into independent JAR mods with zero runtime dependencies — drop into mods folder and go.

---

## Quick Start

```bash
# In-game: create a new project
/box3script create mygame

# Build & watch
cd config/box3/script/mygame
npm install
npm run build -- --watch

# TypeScript type check
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

[View Full Docs →](/en/guide/getting-started)

## Version Info

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| Mod Loader | NeoForge |
| Java | 21 |
| JS Engine | Mozilla Rhino 1.9.1 (ES5) |
| TypeScript | Via Babel → ES5 |
