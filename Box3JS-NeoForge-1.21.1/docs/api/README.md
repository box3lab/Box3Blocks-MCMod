# Box3JS API 参考

Box3JS 是一个 Minecraft 模组，允许用 JavaScript 编写服务端脚本。所有脚本运行在 `config/box3/script/<项目名>` 下。

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

| 对象             | 类型    | 说明                                                                             |
| ---------------- | ------- | -------------------------------------------------------------------------------- |
| `world`          | ✅ Box3 | 世界控制，见 [world.md](world.md)                                                |
| `entity`         | ✅ Box3 | 实体包装（回调参数，或通过 `world.spawnEntity` 创建），见 [entity.md](entity.md) |
| `player`         | ✅ Box3 | 玩家包装（通过 `entity.player` 获取），见 [player.md](player.md)                 |
| `voxels`         | ✅ Box3 | 方块操作，见 [voxels.md](voxels.md)                                              |
| `storage`        | ✅ Box3 | 数据持久化，见 [storage.md](storage.md)                                          |
| `db`             | ⬆ MC    | SQLite 数据库，见 [database.md](database.md)                                      |
| `console`        | ⬆ MC    | `console.log/debug/warn/error/assert/clear`                                      |
| `require(id)`    | ⬆ MC    | CommonJS 模块导入，见下方模块说明                                                |
| `sleep(ms)`      | ⬆ MC    | 阻塞线程指定毫秒（运行时会将值限制为最多 10ms）                                  |
| `GameVector3`    | ✅ Box3 | 三维向量，见 [math.md](math.md)                                                  |
| `GameBounds3`    | ✅ Box3 | 包围盒，见 [math.md](math.md)                                                    |
| `GameRGBColor`   | ✅ Box3 | RGB 颜色，见 [math.md](math.md)                                                  |
| `GameRGBAColor`  | ✅ Box3 | RGBA 颜色，见 [math.md](math.md)                                                 |
| `GameQuaternion` | ✅ Box3 | 四元数，见 [math.md](math.md)                                                    |

## API 标注说明

| 标注            | 含义                                        |
| --------------- | ------------------------------------------- |
| ✅ **Box3 API** | 源自 Box3 平台，命名和语义与 Box3 保持一致  |
| ⬆ **MC 扩展**   | 非 Box3 原有，利用 Minecraft 特性新增的 API |

## 文档索引

| 文档                       | 内容                                                    |
| -------------------------- | ------------------------------------------------------- |
| [world.md](world.md)       | 世界状态、事件、记分板、Bossbar、队伍、边界、粒子、烟花 |
| [entity.md](entity.md)     | 实体属性、AI、装备、药水、寻路、标签                    |
| [player.md](player.md)     | 背包、消息、飞行、游戏模式、传送、命令                  |
| [voxels.md](voxels.md)     | 方块读写、区域填充、刷怪笼                              |
| [storage.md](storage.md)   | 数据持久化存储                                          |
| [database.md](database.md) | SQLite 数据库                                            |
| [math.md](math.md)         | Vector3、Bounds3、Color、Quaternion                     |
| [commands.md](commands.md) | `/box3script` 命令参考                                  |

## 文件模块

**TypeScript 构建管线：**

`/box3script create` 创建的项目自带完整的 TS 构建环境。写入 `src/*.ts`，构建输出到 `dist/app.js`：

```
config/box3/script/mygame/
├── package.json          ← esbuild + Babel + @babel/preset-typescript
├── tsconfig.json
├── build.mjs             ← Babel TS→JS → esbuild bundle → dist/
├── types/
│   └── globals.d.ts      ← 完整 API 类型声明 (IDE 自动补全)
├── src/
│   ├── app.ts            ← 入口，require() 其他模块
│   ├── state.ts          ← 共享游戏状态
│   ├── course.ts         ← 赛道数据与建筑
│   └── ...
└── dist/
    └── app.js            ← 编译产物（模组实际加载此文件）
```

`npm run build` 或 `node build.mjs` 执行构建。`/box3script watch` 可开启文件监控自动热重载。
