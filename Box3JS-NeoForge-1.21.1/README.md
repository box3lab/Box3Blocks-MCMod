# Box3JS — Minecraft 脚本引擎

> **Beta** — 本项目处于早期测试阶段，API 可能变动，欢迎反馈。

[简体中文](Readme.md) | [English](README_en.md)

**无需 Java 知识，用 TypeScript 为你的 Minecraft 服务器创造无限玩法。**

Box3JS 是一个内置于 NeoForge 模组的服务端脚本引擎（Mozilla Rhino），延续了神奇代码岛的 API 风格。告别复杂的 Java 模组开发——写 TypeScript，一键热重载，即时生效。PvP 竞技场、RPG 副本、派对小游戏、世界管理、社交工具，全能用脚本快速实现。

## 安装

1. 将 `box3js-<version>.jar` 放入服务端 `mods/` 目录
2. 如需使用 SQLite 数据库（`db` API），还需安装 [`minecraft-sqlite-jdbc`](https://modrinth.com/mod/minecraft-sqlite-jdbc)
3. 启动服务器

## 5 分钟快速开始

在游戏中（需要 OP 权限等级 ≥ 2）：

```
/box3script create mygame
```

这会创建 TypeScript 项目：

```
config/box3/script/mygame/
├── package.json          ← npm 依赖（esbuild、Babel、TypeScript）
├── tsconfig.json
├── build.mjs             ← 构建脚本（esbuild → Babel → Rhino）
├── eslint.config.mjs
├── types/
│   └── globals.d.ts      ← 完整 API 类型声明（IDE 自动补全）
└── src/
    └── app.ts            ← 入口文件，代码写在这里
```

构建并启动：

```bash
cd config/box3/script/mygame
npm install && npm run build
```

```
/box3script sandbox mygame     # (推荐) 开启沙盒，放心测试
/box3script start mygame       # 启动脚本
```

打开 `src/app.ts` 写入代码，保存后 `npm run build`，再 `/box3script reload mygame` 即时生效——**不需要重启服务器**。

## 为什么选择 Box3JS？

| 特性 | 说明 |
|------|------|
| **零门槛** | 会 JS/TS 就能写，无需 Gradle、无需 IDE、无需重启 |
| **热重载** | 改代码 → build → reload，秒级生效。开启 `watch` 自动重载 |
| **沙盒保护** | 开启沙盒自动追踪所有脚本修改，关闭时完整回滚，服务器不留痕迹 |
| **TypeScript** | 完整 `.d.ts` 类型声明，esbuild + Babel 编译管线，享受智能提示 |
| **17 种事件** | onTick、onPlayerJoin、onChat、onEntityDeath、onBlockActivate、onButtonPressed... |
| **视觉效果** | 13+ 粒子、烟花、闪电、爆炸、音效 |
| **游戏系统** | 计分板、BossBar、队伍、世界边界、跨脚本通信 |
| **自定义物品** | JSON 配置注册自定义物品（食物、稀有度、附魔光效），动态管理配方 |
| **数据持久化** | JSON 存储 + SQLite 数据库（排行榜、经济、玩家数据） |

## 命令

| 命令 | 说明 |
|------|------|
| `/box3script` | 查看项目状态总览 |
| `/box3script create <name>` | 创建新 TypeScript 项目 |
| `/box3script start [project\|all]` | 启用并加载项目 |
| `/box3script stop [project\|all]` | 禁用并卸载项目 |
| `/box3script reload [project]` | 重载脚本（开发用） |
| `/box3script watch` | 切换文件监控（自动热重载） |
| `/box3script sandbox <project>` | 切换沙盒模式（开=追踪 / 关=回滚） |

所有 `<project>` 参数支持 **Tab 自动补全**。[完整命令文档 →](docs/api/commands.md)

## API 速览

| 全局对象 | 用途 |
|----------|------|
| `world` | 世界状态、事件回调、粒子、烟花、闪电、音效、计分板、BossBar、队伍、边界、自定义物品 |
| `entity` | 实体属性、AI 寻路、装备、药水效果、标签、导航 |
| `player` | 背包、飞行、游戏模式、传送、消息、经验、音效 |
| `voxels` | 方块读写、区域填充、刷怪笼 |
| `storage` | JSON 数据持久化 |
| `db` | SQLite 数据库 — SQL 查询、排行榜、玩家数据 |
| `console` | 控制台日志输出（`log`/`warn`/`error`/`debug`） |
| `GameVector3` | 三维向量（坐标运算） |
| `GameBounds3` | 包围盒 |
| `GameRGBColor` / `GameRGBAColor` | RGB/RGBA 颜色 |
| `GameQuaternion` | 四元数（旋转运算） |

[API 总览 →](docs/api/README.md) · [按任务速查 →](docs/api/README.md#功能速查---我想) · [English](docs/api/README_en.md)

## 教程

从零到完整小游戏，每个示例均经过 TypeScript 编译 + ESLint 验证：

| # | 教程 | 时长 | 学什么 |
|---|------|------|--------|
| 1 | [从零开始](docs/tutorial/01-basics.md) | 10 min | 创建项目、第一个脚本、聊天命令、定时任务 |
| 2 | [玩家操控与物品](docs/tutorial/02-player-items.md) | 15 min | 传送、飞行、物品、附魔、药水、自定义物品 |
| 3 | [事件系统与实体](docs/tutorial/03-events-entities.md) | 15 min | 事件回调、实体生成、AI、战斗、巡逻 |
| 4 | [高级游戏系统](docs/tutorial/04-advanced-systems.md) | 15 min | 计分板、BossBar、队伍、边界、跨脚本通信 |
| 5 | [实战小游戏](docs/tutorial/05-examples.md) | 20 min | PvP 竞技场、粒子烟花、波次刷怪、特效大全 |

[教程总览 →](docs/tutorial/README.md)

## 文档结构

```
docs/
├── api/                   ← API 参考
│   ├── README.md          总览 + 功能速查
│   ├── world.md           世界 API（事件、粒子、烟花、计分板...）
│   ├── entity.md          实体 API（属性、AI、装备、效果...）
│   ├── player.md          玩家 API（背包、消息、飞行、传送...）
│   ├── voxels.md          方块 API（读写、填充、刷怪笼）
│   ├── storage.md         存储 API（JSON 持久化）
│   ├── database.md        数据库 API（SQLite）
│   ├── math.md            数学 API（Vector3、Color、Quaternion）
│   └── commands.md        /box3script 命令参考
├── tutorial/              ← 入门教程
│   ├── README.md          教程总览
│   ├── 01-basics.md       从零开始
│   ├── 02-player-items.md 玩家与物品
│   ├── 03-events-entities.md 事件与实体
│   ├── 04-advanced-systems.md 高级系统
│   └── 05-examples.md     实战小游戏
└── BOX3_API_COMPARISON.md ← Box3 平台 vs Box3JS API 对照表
```

## 示例项目

`run/config/box3/script/colorzone/` 包含完整的领地争夺战游戏和 7 个功能示例，涵盖 Hello World 到波次刷怪的全部教学场景。

## 依赖说明

| 功能 | 依赖 |
|------|------|
| 脚本引擎核心 | 内嵌 Rhino 1.9.1，无需额外安装 |
| `db` API（SQLite） | 需安装 [`minecraft-sqlite-jdbc`](https://modrinth.com/mod/minecraft-sqlite-jdbc) 模组 |
| 其他 API | 无额外依赖 |

> 未安装 `minecraft-sqlite-jdbc` 时，`db` 以外的所有 API 正常工作。只有调用 `db.sql()` 才会提示需要安装。

## 技术栈

- **运行时：** Mozilla Rhino 1.9.1（Java 嵌入式 JS 引擎）
- **编译工具：** esbuild 打包 → Babel 转译（Rhino 目标） → 正则清理
- **语言：** TypeScript 编写，编译为 ES5 兼容 JS
- **平台：** NeoForge 1.21.1，Java 21

## 许可证

Apache License 2.0
