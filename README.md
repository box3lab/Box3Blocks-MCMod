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

### 🧪 Box3JS 脚本引擎 (Beta)

Box3JS 是一个内置于模组的 JavaScript 脚本引擎（Rhino 引擎），允许服主编写服务端脚本来创建自定义玩法、小游戏和世界交互。所有脚本位于 `config/box3/script/<项目名>/`。

**快速开始：**

```bash
/box3script create mygame        # 创建 TypeScript 脚手架
cd config/box3/script/mygame
npm install && npm run build     # 安装依赖并编译
/box3script sandbox mygame       # (推荐) 开启沙盒模式
/box3script on mygame            # 启用并运行脚本
```

**命令一览：**

| 命令 | 说明 |
|---|---|
| `/box3script` | 列出所有项目及启用/沙盒状态 |
| `/box3script create <name>` | 创建新脚本项目 (TypeScript 脚手架) |
| `/box3script on <project>` | 启用并加载指定项目 |
| `/box3script on all` | 一键启用所有项目 |
| `/box3script off <project>` | 禁用指定项目 |
| `/box3script off all` | 一键禁用所有项目 |
| `/box3script stop` | 停止所有脚本（沙盒项目保留追踪状态） |
| `/box3script stop <project>` | 停止指定项目（沙盒项目保留追踪状态） |
| `/box3script reload` | 重载所有已启用项目 |
| `/box3script reload <project>` | 重载指定项目（开发调试用） |
| `/box3script watch` | 切换文件监控（`.js` 变化自动热重载） |
| `/box3script sandbox <project>` | 切换沙盒模式（开启追踪 / 关闭回滚） |

> 所有 `<project>` 参数均支持 **Tab 自动补全**。

**沙盒系统：**

沙盒模式开启后自动追踪脚本对世界的所有修改，**持久化**保存（跨 stop/reload 保持），仅手动 `/box3script sandbox <project>` 关闭时才回滚。追踪内容包括：

- **方块修改** — `setVoxel`/`setVoxelId`/`fillVoxel`（上限 500 万块，90% 时日志警告）
- **实体状态** — 血量、AI、隐身、发光、无敌、着火、药水效果、标签等
- **玩家状态** — 游戏模式、飞行能力、移动速度、跳跃力、经验、饱食度、物品栏、护甲、药水效果、位置、维度、重生点
- **世界状态** — 天气、时间、难度、游戏规则、世界边界

关闭沙盒时自动回滚全部修改，并在聊天栏输出恢复摘要：`"restored: 23417 blocks, 83 entities, 2 players, world state"`。

**已实现 API：**

- `world` — 世界控制、事件回调 (16 种)、记分板、Bossbar、队伍、边界、粒子、烟花、射线检测
- `entity` — 实体属性、AI 寻路、装备、药水效果、标签
- `player` — 玩家专属：背包、飞行、游戏模式、二段跳、传送、消息、经验
- `voxels` — 方块读写、区域填充、刷怪笼
- `storage` — JSON 数据持久化
- `console` / `require()` / `sleep()` / `GameVector3` / `GameBounds3` / `GameRGBColor` 等

完整 API 文档见 `docs/api/`。

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
