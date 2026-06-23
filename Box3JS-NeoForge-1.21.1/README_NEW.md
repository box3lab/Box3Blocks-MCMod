# Box3JS — Minecraft Scripting Engine

> **Beta** — APIs may change during early development. Feedback is welcome.

**No Java, just TypeScript.** Build Minecraft minigames, RPG systems, and world mechanics with scripts that hot-reload in seconds.

Built on Mozilla Rhino, Box3JS brings the Box3 coding style to Minecraft servers. Write TypeScript, run it instantly — no JDK, no Gradle, no server restarts.

## Features

- **TypeScript-first** — Full type declarations (`.d.ts`) with bilingual JSDoc, auto-complete in any editor
- **Box3-compatible API** — World, Entity, Player, Voxels, Storage, Database, HTTP, remoteChannel
- **110+ Minecraft extensions** — Scoreboards, BossBars, teams, world border, particles, fireworks, potions, custom blocks/items/sounds registration, and more
- **Client-side scripting** — Custom HUD, keyboard input, audio playback, client-side SQLite & HTTP
- **Project isolation** — Run multiple script projects independently, each with its own scope and callbacks
- **Sandbox mode** — Test safely; roll back all script-made changes on stop
- **Standalone compilation** — `/box3script compile` packages your script into a distributable JAR

## Quick Start

```
/box3script create mygame
```

```
npm install && npm run build
```

```
/box3script sandbox mygame
/box3script start mygame
```

Full guide: [https://docs.box3lab.com/box3js-mc](https://docs.box3lab.com/box3js-mc)

## License

Apache License 2.0

---

# Box3JS（神岛代码）— Minecraft 脚本引擎

> **Beta** — 早期测试阶段，API 可能变动，欢迎反馈。

**不写 Java，只用 TypeScript。** 在 Minecraft 里用脚本开发小游戏、RPG 玩法、世界机制，热重载秒级生效。

Box3JS 基于 Mozilla Rhino 引擎，延续了神奇代码岛的 API 风格。无需 JDK、无需 Gradle、无需重启服务器，写 TypeScript 即写即跑。

## 特性

- **TypeScript 优先** — 完整类型声明（`.d.ts`），双语 JSDoc，任意编辑器均有自动补全
- **Box3 兼容 API** — World / Entity / Player / Voxels / Storage / Database / HTTP / remoteChannel
- **110+ MC 扩展** — 记分板、Bossbar、队伍、世界边界、粒子、烟花、药水、自定义方块/物品/音效注册等
- **客户端脚本** — 自定义 HUD、键盘输入、音效播放、客户端 SQLite & HTTP
- **项目隔离** — 多脚本项目独立运行，各自拥有独立作用域和回调
- **沙盒模式** — 放心测试，停止后回滚一切脚本改动
- **独立编译** — `/box3script compile` 一键打包为可分发 JAR

## 快速开始

```
/box3script create mygame
```

```
npm install && npm run build
```

```
/box3script sandbox mygame
/box3script start mygame
```

完整教程：[https://docs.box3lab.com/box3js-mc](https://docs.box3lab.com/box3js-mc)

## 许可证

Apache License 2.0
