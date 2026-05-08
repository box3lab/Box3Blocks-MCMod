# Box3JS（神岛代码）-- Minecraft Mod

> **测试版（Beta）** — 本项目处于早期测试阶段，API 可能变动，可能存在未发现的缺陷。欢迎反馈问题。

[简体中文](README.md) | [English](README_en.md)

**无需 Java 知识，用 TypeScript 为你的 Minecraft 服务器创造无限玩法。**

Box3JS 是一个内置于模组的服务端脚本引擎（Mozilla Rhino），延续了神奇代码岛的代码风格。告别复杂的 Java 模组开发——写 TypeScript，一键热重载，即时生效。无论是 PvP 竞技场、RPG 副本、派对小游戏，还是世界管理和社交工具，都能用脚本快速实现。

## 为什么选择 Box3JS？

- **零门槛** — 会写 TypeScript/JavaScript 就能开发 Minecraft 玩法，无需 Gradle、无需 IDE、无需重启服务器
- **热重载** — 修改代码后自动编译重载（`--watch`），迭代速度秒杀传统模组开发
- **沙盒保护** — 一键开启沙盒模式，自动追踪所有世界修改；关闭时完整回滚，服务器不留痕迹
- **TypeScript 全流程** — esbuild 打包 + Babel 转译（Rhino 1.9.1 目标），内置完整类型声明文件，享受类型检查和智能提示
- **16 种事件回调** — 玩家加入/离开、聊天、方块交互、实体死亡/受伤、玩家重生、按钮按下、跨脚本消息……覆盖所有玩法需求
- **丰富的视觉 API** — 13+ 种粒子效果、5 种烟花形状、闪电、爆炸、音效，打造沉浸式体验
- **完整游戏系统** — 计分板、BossBar 倒计时、队伍系统、世界边界缩圈、跨脚本通信，开箱即用
- **自定义物品/配方** — JSON 配置即可注册自定义物品（支持食物、稀有度、光效），动态管理合成配方

## 快速开始

在游戏中（需要 OP 权限，等级 ≥ 2）：

```
/box3script create mygame
```

这会创建一个 TypeScript 脚手架项目：

```
config/box3/script/mygame/
├── .gitignore
├── package.json          ← npm 依赖（esbuild、Babel、TypeScript）
├── tsconfig.json
├── build.mjs             ← 构建脚本（esbuild → Babel → Rhino）
├── types/
│   └── globals.d.ts      ← Box3JS 完整类型声明
└── src/
    └── app.ts            ← 入口（含 Hello World 示例）
```

然后构建：

```bash
cd config/box3/script/mygame
npm install
npm run build          # 输出 dist/app.js
```

回到游戏启用：

```
/box3script sandbox mygame     # (推荐) 开启沙盒，放心测试
/box3script start mygame       # 启动脚本
```

## 命令

| 命令 | 说明 |
|---|---|
| `/box3script` | 列出所有项目及启用/沙盒状态 |
| `/box3script create <name>` | 创建新脚本项目 (TypeScript 脚手架) |
| `/box3script start <project>` | 启动指定项目 |
| `/box3script start all` | 一键启动所有项目 |
| `/box3script stop <project>` | 停止指定项目（沙盒项目保留追踪） |
| `/box3script stop all` | 一键停止所有项目 |
| `/box3script reload <project>` | 重载指定项目（开发调试用） |
| `/box3script reload` | 重载所有已启用项目 |
| `/box3script watch` | 切换文件监控（`.js` 变化自动热重载） |
| `/box3script sandbox <project>` | 切换沙盒模式（开启追踪 / 关闭回滚） |

> 所有 `<project>` 参数均支持 **Tab 自动补全**。完整命令文档见 [commands.md](docs/api/commands.md)。

## 教程

从零基础到完整小游戏，每个示例均经过 TypeScript 编译 + ESLint 验证：

1. [从零开始](docs/tutorial/01-basics.md) — 创建项目、控制台、聊天命令、定时器、粒子特效
2. [玩家与物品](docs/tutorial/02-player-items.md) — 传送、飞行、游戏模式、药水、附魔、自定义物品
3. [事件与实体](docs/tutorial/03-events-entities.md) — 方块交互、实体生成/AI、受伤/死亡、巡逻守卫
4. [高级游戏系统](docs/tutorial/04-advanced-systems.md) — 计分板、BossBar、队伍、世界边界、跨脚本通信
5. [可视化与实战](docs/tutorial/05-examples.md) — 粒子、烟花、闪电、音效、PvP 竞技场、领地争夺战

## 示例项目

`run/config/box3/script/colorzone/` 包含一个完整的领地争夺战（Territory Rush）游戏和 7 个已验证的功能示例，涵盖从 Hello World 到波次刷怪的全部教学场景。

## 可用 API

| 模块 | 功能 |
|------|------|
| `world` | 世界控制、16 种事件回调、计分板、BossBar、队伍、边界、粒子、烟花、闪电、爆炸、抛射物、射线检测、跨脚本通信、自定义物品/配方 |
| `entity` | 实体属性、AI 寻路、装备、药水效果、标签、导航 |
| `player` | 背包、飞行、游戏模式、传送、消息、经验、成就、音效、标题、BossBar |
| `voxels` | 方块读写、区域填充、刷怪笼 |
| `storage` | JSON 数据持久化 |
| `db` | SQLite 数据库 — SQL 查询、排行榜、玩家数据 |
| `console` | 日志输出、`require()`、`sleep()` |
| `GameVector3` / `GameBounds3` / `GameRGBColor` | 数学与颜色类型 |

[API 总览 →](docs/api/README.md) ([English](docs/api/README_en.md))

## 许可证

Apache License 2.0
