# Box3JS API 参考

Box3JS 是一个 Minecraft NeoForge 1.21.1 模组，允许用 JavaScript (Rhino 引擎) 编写服务端脚本。所有脚本运行在 `config/box3/script/<项目名>/app.js`。

## 快速开始

```js
// app.js — 最简示例
world.onTick(() => {
    // 每 tick 执行 (20 tick = 1 秒)
});

world.onChat((entity, message, tick) => {
    var p = entity.player;
    if (message === "!hello") {
        p.directMessage("Hello, " + p.name + "!");
    }
});

console.log("脚本已加载");
```

## 全局对象

| 对象 | 类型 | 说明 |
|---|---|---|
| `world` | ✅ Box3 | 世界控制，见 [world.md](world.md) |
| `entity` | ✅ Box3 | 实体包装（回调参数，或通过 `world.spawnEntity` 创建），见 [entity.md](entity.md) |
| `player` | ✅ Box3 | 玩家包装（通过 `entity.player` 获取），见 [player.md](player.md) |
| `voxels` | ✅ Box3 | 方块操作，见 [voxels.md](voxels.md) |
| `storage` | ✅ Box3 | 数据持久化，见 [storage.md](storage.md) |
| `console` | ⬆ MC | `console.log/debug/warn/error/assert/clear` |
| `require(id)` | ⬆ MC | CommonJS 模块导入，见下方模块说明 |
| `sleep(ms)` | ⬆ MC | 阻塞线程指定毫秒 |
| `GameVector3` | ✅ Box3 | 三维向量，见 [math.md](math.md) |
| `GameBounds3` | ✅ Box3 | 包围盒，见 [math.md](math.md) |
| `GameRGBColor` | ✅ Box3 | RGB 颜色，见 [math.md](math.md) |
| `GameRGBAColor` | ✅ Box3 | RGBA 颜色，见 [math.md](math.md) |
| `GameQuaternion` | ✅ Box3 | 四元数，见 [math.md](math.md) |

## API 标注说明

| 标注 | 含义 |
|---|---|
| ✅ **Box3 API** | 源自 Box3 平台，命名和语义与 Box3 保持一致 |
| ⬆ **MC 扩展** | 非 Box3 原有，利用 Minecraft 特性新增的 API |

## 文档索引

| 文档 | 内容 |
|---|---|
| [world.md](world.md) | 世界状态、事件、记分板、Bossbar、队伍、边界、粒子、烟花 |
| [entity.md](entity.md) | 实体属性、AI、装备、药水、寻路、标签 |
| [player.md](player.md) | 背包、消息、飞行、游戏模式、传送、命令 |
| [voxels.md](voxels.md) | 方块读写、区域填充、刷怪笼 |
| [storage.md](storage.md) | 数据持久化存储 |
| [math.md](math.md) | Vector3、Bounds3、Color、Quaternion |
| [commands.md](commands.md) | `/box3script` 命令参考 |

## 多文件模块

使用 CommonJS 的 `require()` / `module.exports` 来组织和导入多文件项目。每个文件是一个独立模块，通过 `require("./模块名")` 导入（自动追加 `.js` 后缀）：

```
config/box3/script/skyrun/
├── app.js           ← 入口，require() 其他模块
├── state.js         ← 共享游戏状态
├── course.js        ← 赛道数据与建筑
├── game.js          ← 游戏流程控制
├── checkpoints.js   ← 检查点检测
└── leaderboard.js   ← 排行榜
```

```js
// state.js — 导出共享状态
var G = { phase: "lobby", checkpoints: [], ... };
module.exports = { G: G, SB: "skyrun_scores" };

// app.js — 导入模块
var G = require("./state").G;
var buildCourse = require("./course").buildCourse;
var startRace = require("./game").startRace;
```

> 注意：`require()` 使用 Rhino 内置的 CommonJS 模块系统，模块会被缓存供后续导入。仅在脚本加载执行时可用（需要项目上下文）。

## Tick 与性能

- 服务端每秒 20 tick，`world.onTick()` 每 tick 触发
- `world.setInterval(handler, ticks)` 可以降低频率，例如 `setInterval(fn, 20)` = 每秒 1 次
- 避免在 tick 中执行大量方块操作或实体遍历
- 使用 `world.setTimeout(handler, ticks)` 做延时操作
