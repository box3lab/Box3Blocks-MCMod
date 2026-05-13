# Box3JS 文档

Box3JS 是 Minecraft NeoForge 1.21.1 的 JavaScript/TypeScript 脚本引擎模组。你可以在不安装 JDK、不编译 Java 代码的情况下，用 JS/TS 编写服务端玩法脚本和客户端 UI 脚本。

## 文档导航

### 入门指南

从零开始，了解 Box3JS 是什么、怎么用、以及背后的原理。

| 文档 | 内容 |
|------|------|
| [快速开始](guide/getting-started.md) | 环境搭建 → 第一个脚本 → 开发循环 → 调试 → 发布 |
| [常用配方](guide/recipes.md) | 功能模板：经济系统、传送、商店、每日奖励、排行榜、Webhook |
| [运行原理](guide/architecture.md) | Rhino 引擎、作用域管理、构建管线、网络通信 |
| [JS vs Java 对比](guide/js-vs-java.md) | Box3JS 脚本开发 vs 原生 Java 模组开发的优势与劣势 |
| [常见问题](guide/faq.md) | 加载、构建、运行时、数据库、HTTP、客户端、部署 |

### 教程

5 个渐进式教程，每个 10-15 分钟，包含可运行的完整代码。

| # | 教程 | 你会学到 |
|---|------|---------|
| 1 | [从零开始](tutorial/01-basics.md) | 创建项目 → 构建 → 第一个脚本 → 聊天命令 → 定时任务 |
| 2 | [玩家操控与物品](tutorial/02-player-items.md) | 传送、飞行、物品给予、附魔、药水效果、游戏模式 |
| 3 | [事件系统与实体操控](tutorial/03-events-entities.md) | 全部事件回调、生成实体、AI 控制、巡逻守卫、碰撞检测 |
| 4 | [高级游戏系统](tutorial/04-advanced-systems.md) | 计分板排名、BossBar 倒计时、队伍分组、世界边界缩圈、跨脚本通信 |
| 5 | [实战小游戏](tutorial/05-examples.md) | PvP 竞技场（完整可玩）、粒子特效大全、烟花秀、波次刷怪 |
| 6 | [客户端脚本开发](tutorial/06-client-scripting.md) | 键盘输入、屏幕 UI、音效/音乐、本地存储、SQLite、HTTP、remoteChannel |
| 📋 | [教程总览](tutorial/README.md) | 学习路径图、前置知识、开发技巧 |

### API 参考

按功能分类的完整 API 文档。每个全局对象/命名空间一个文档。

| 分类 | 文档 | 全局对象 |
|------|------|---------|
| **服务端总览** | [server](api/server.md) | 服务端运行边界、事件、玩家/实体、方块、数据、跨端通信 |
| **世界** | [world](api/world.md) | `world` — 事件、粒子、烟花、音效、计分板 |
| **实体** | [entity](api/entity.md) | `entity` — 属性、AI、装备、效果 |
| **玩家** | [player](api/player.md) | `player` — 背包、消息、飞行、传送 |
| **方块** | [voxels](api/voxels.md) | `voxels` — 读写方块、区域填充 |
| **存储** | [storage](api/storage.md) | `storage` — JSON 持久化 |
| **数据库** | [database](api/database.md) | `db` — SQLite 数据库 |
| **网络** | [http](api/http.md) | `http` — HTTP 请求 |
| **客户端** | [client](api/client.md) | `audio` `client` `input` `ui` `chat` `gui` `remoteChannel` |
| **注册表** | [registries](api/registries.md) | `registries` — 自定义方块/物品/音效 |
| **数学** | [math](api/math.md) | `GameVector3` `GameBounds3` `GameRGBColor` `GameRGBAColor` `GameQuaternion` |
| **命令** | [commands](api/commands.md) | `/box3script` CLI 命令 |
| **速查** | [API 功能速查](api/README.md) | 按"我想做什么"查找对应 API |
| **对照** | [Box3 API 对照](BOX3_API_COMPARISON.md) | Box3 平台 API 与 Box3JS 实现逐一对比 |

### 版本与兼容性

| 项目 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| 模组加载器 | NeoForge |
| Java | 21 |
| JS 引擎 | Mozilla Rhino 1.9.1（ES5 兼容） |
| TypeScript | 通过 Babel 编译为 ES5 |

## 快速链接

- **5 分钟上手**: [快速开始 →](guide/getting-started.md)
- **我想做 X，用什么 API**: [API 功能速查 →](api/README.md)
- **为什么选 Box3JS 而不是写 Java 模组**: [JS vs Java 对比 →](guide/js-vs-java.md)
- **Box3JS 内部怎么运作的**: [运行原理 →](guide/architecture.md)
- **从零学 Box3JS 脚本**: [教程一 →](tutorial/01-basics.md)
