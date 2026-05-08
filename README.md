# Box3Blocks（神岛材质包）-- Minecraft Mod

![Modrinth Downloads](https://img.shields.io/modrinth/dt/iG3hRUix?logo=modrinth)
![CurseForge Downloads](https://img.shields.io/curseforge/dt/1456138?logo=curseforge)

[简体中文](README.md) | [English](README_en.md)

导入神奇代码岛的372个方块到我的世界，让你在MC中也能使用熟悉的方块进行创作，还支持将神奇代码岛中的建筑/模型结构完整迁移到Minecraft世界中，保持原汁原味的建造风格。

## 🌟 主要功能

### 🎨 丰富的方块

- **372种方块**：包括字母、数字、符号、颜色、元素等
- **9个创造标签**：分类整理，方便查找
  - Box3:字母 - A-Z字母方块
  - Box3:数字 - 0-9数字方块
  - Box3:符号 - 各种符号方块
  - Box3:颜色 - 彩色方块
  - Box3:元素 - 化学元素方块
  - Box3:食物 - 食物相关方块
  - Box3:灯光 - 发光方块
  - Box3:自然 - 自然材质方块
  - Box3:建筑 - 建筑材料方块

### 🏗 导入神奇代码岛建筑

- **地形文件导入**：支持从 `config/box3/` 目录中的压缩地形文件（`.gz`）导入方块地图。
- **建筑迁移**：可将神奇代码岛中的建筑结构迁移到 Minecraft 世界中，保持方块外观一致。
- **获取建筑文件**：访问 https://box3lab.com/build2mc 获取神奇代码岛建筑的地形文件（`.gz`）。
- **导入指令**：
  - `/box3import`  
    列出 `config/box3/` 目录下所有可导入的地形文件（`.gz`）。
  - `/box3import <fileName>`  
    从 `config/box3/<fileName>.gz` 导入建筑（命令中不需要带后缀，会自动补 `.gz`）。
  - `/box3import <fileName> <offsetY>`  
    在当前位置基础上在 Y 轴方向额外偏移 `offsetY` 格（正数向上，负数向下）。
  - `/box3import <fileName> <offsetY> <ignoreBarrier>`  
    当 `ignoreBarrier = true` 时，跳过屏障方块（不会在世界中放置这些方块）。
  - `/box3import <fileName> <offsetY> <ignoreBarrier> <ignoreWater>`  
    当 `ignoreWater = true` 时，所有流体统一替换为空气。
- **导出指令**：
  - `/box3export <fileName>`  
    自动搜索附近最近的两个 `红石块`（`minecraft:redstone_block`）作为导出区域对角点，并导出到 `config/box3/<fileName>.gz`。
  - 搜索规则：从近到远扫描，找到两个标记点就停止；最大搜索半径为 `1024`。

### 🔍 屏障可见性切换

- **屏障可见性切换 `/box3barrier`**：
  - `/box3barrier`：查看当前屏障是否可见。
  - `/box3barrier <bool>`：开启/关闭屏障显示（屏障始终有碰撞，只是是否渲染）。
  - `/box3barrier toggle`：在开启/关闭之间快速切换。状态会保存到本地配置文件，下次进入世界自动沿用。

### 🧩 导入神奇代码岛的模型物品

- **资源文件导入**：支持从 `resourcepacks/` 目录文件导入资源包。
- **资源包加载模型**：将模型放入资源包即可自动注册到创造模式。
- **模型物品标签页**：`Box3:模型` 标签页用于管理模型物品。
- **生成模型资源包**：访问 https://box3lab.com/mc-resource-pack 获取适用于本模组的资源包文件。

#### ✨ 操作模型说明

- **交互调参**：
  - `空手`右键模型：切换模式（缩放 / X偏移 / Y偏移 / Z偏移 / 旋转）
  - `木棍`右键模型：当前模式参数增加
  - `烈焰棒`右键模型：当前模式参数减少
- **参数复制粘贴**：
  - `纸`右键模型：复制当前模型参数
  - `书`右键模型：粘贴参数到目标模型模型

### 🧪 Box3JS — 服务端脚本引擎

**无需 Java 知识，用 TypeScript 为你的服务器创造无限玩法。**

Box3JS 是内置于模组的服务端脚本引擎（Mozilla Rhino），让你像写网页一样为 Minecraft 服务器编写小游戏、自定义机制和世界交互。告别复杂的 Java 模组开发 — 写 TypeScript，一键热重载，即时生效。

**为什么选择 Box3JS？**

- **零门槛** — 会写 TypeScript/JavaScript 就能开发 Minecraft 玩法，无需 Gradle、无需 IDE、无需重启服务器
- **热重载** — 修改代码后自动编译重载（`--watch`），迭代速度秒杀传统模组开发
- **沙盒保护** — 一键开启沙盒模式，自动追踪所有世界修改；关闭时完整回滚，服务器不留痕迹。适合活动服、小游戏轮换
- **TypeScript 全流程** — esbuild 打包 + Babel 转译 + 类型声明文件，享受完整的类型检查和智能提示
- **16 种事件回调** — 玩家加入/离开、聊天、方块交互、实体死亡/受伤、玩家重生……覆盖所有玩法需求
- **丰富的视觉 API** — 粒子效果 (13+ 种)、烟花 (5 种形状)、闪电、爆炸、音效，打造沉浸式体验
- **完整游戏系统** — 计分板、BossBar 倒计时、队伍系统、世界边界缩圈、跨脚本通信，开箱即用
- **自定义物品/配方** — JSON 配置即可注册自定义物品（支持食物、稀有度、光效），动态管理合成配方

**快速开始：**

```bash
/box3script create mygame        # 创建 TypeScript 脚手架项目
cd config/box3/script/mygame
npm install && npm run build     # 安装依赖并编译
/box3script sandbox mygame       # (推荐) 开启沙盒，放心测试
/box3script start mygame         # 启动脚本
```

**命令一览：**

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

> 所有 `<project>` 参数均支持 **Tab 自动补全**。

**你能用它做什么？**

| 玩法类型 | 示例 |
|---|---|
| 竞技对抗 | PvP 竞技场、队伍对战、缩圈毒圈、击杀积分榜 |
| 休闲派对 | 领地圈地竞速、跑酷计时、大厅欢迎礼包 |
| RPG 机制 | 波次刷怪、精英 Boss 战、自定义物品/药水、巡逻守卫 |
| 社交工具 | 彩色聊天弹幕、家传送、坐标分享、随机传送 |
| 世界管理 | 方块区域填充、天气/时间控制、游戏规则切换 |

**沙盒系统：**

沙盒模式开启后自动追踪脚本对世界的所有修改，**持久化**保存（跨 stop/reload 保持），仅手动 `/box3script sandbox <project>` 关闭时才回滚。追踪内容包括：

- **方块修改** — `setVoxel`/`setVoxelId`/`fillVoxel`（上限 500 万块，90% 时日志警告）
- **实体状态** — 血量、AI、隐身、发光、无敌、着火、药水效果、标签等
- **玩家状态** — 游戏模式、飞行能力、移动速度、跳跃力、经验、饱食度、物品栏、护甲、药水效果、位置、维度、重生点
- **世界状态** — 天气、时间、难度、游戏规则、世界边界

关闭沙盒时自动回滚全部修改，并在聊天栏输出恢复摘要：`"restored: 23417 blocks, 83 entities, 2 players, world state"`。

**已实现 API：**

- `world` — 世界控制、16 种事件回调、记分板、BossBar、队伍、边界、粒子、烟花、闪电、爆炸、抛射物、射线检测、跨脚本通信
- `entity` — 实体属性、AI 寻路、装备、药水效果、标签、导航
- `player` — 玩家专属：背包、飞行、游戏模式、传送、消息、经验、成就
- `voxels` — 方块读写、区域填充、刷怪笼
- `storage` — JSON 数据持久化
- `console` / `require()` / `sleep()` / `GameVector3` / `GameBounds3` / `GameRGBColor` 等

完整 API 文档见 `docs/api/`，从零开始的开发教程见 `docs/tutorial/`（[第一课：Hello World →](Box3JS-NeoForge-1.21.1/docs/tutorial/01-basics.md)），全部示例代码均已通过 TypeScript 编译 + ESLint 验证。

### 🔒 命令权限管理

- `/box3import`、`/box3barrier`、`/box3export` 会根据配置要求不同的权限等级才能执行，默认为 `0` 权限等级。
- 也可以使用服主专用命令 `/box3perm` 动态查看与调整：
  - `/box3perm`：查看当前要求的权限等级，取值范围 `0-4` 对应 Minecraft 的权限等级。
  - `/box3perm <level>`：将 `requireOpForCommands` 设置为 `level`（0-4）。

📋 **完整方块列表**：查看 [block_id.md](block_id.md) 获取所有方块的 ID、注册 Key 和中英文名称对照表。

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

## 🙏 致谢

- 神奇代码岛提供的方块，神岛实验室开发模组
- FabricMC 团队提供的 Fabric 模组加载器
- NeoForged 团队提供的 NeoForge 模组加载器
- MinecraftForge 团队提供的 Forge 模组加载器

## 星历史

[![Star History Chart](https://api.star-history.com/svg?repos=box3lab/Box3Blocks-MCMod&type=date&legend=top-left)](https://www.star-history.com/#box3lab/Box3Blocks-MCMod&type=date&legend=top-left)
