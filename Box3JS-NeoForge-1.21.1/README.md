# Box3JS

> **测试版（Beta）** — 本项目处于早期测试阶段，API 可能变动，可能存在未发现的缺陷。欢迎反馈问题。

**Box3JS** 是一个 Minecraft NeoForge 1.21.1 服务端模组，将 JavaScript 运行时（Mozilla Rhino 1.9.1）嵌入服务端。无需编写 Java 代码，用 JS/TS 即可编写服务端脚本——小游戏、机制扩展、自动化管理。

---

## 特性

- **JS 运行时** — Rhino 1.9.1 引擎，通过 Babel 向下兼容 ES5
- **TypeScript 支持** — 项目模板内置 TS 类型声明，esbuild 打包，完整类型检查
- **Box3 API 兼容** — 实现了 Box3 平台核心 API（World / Entity / Player / Voxels / Storage）
- **MC 扩展** — 90+ Minecraft 独有功能：记分板、Bossbar、队伍、世界边界、粒子、烟花、药水等
- **CommonJS 模块** — `require()` 多文件组织，支持大型脚本项目
- **热重载** — `/box3script reload` 重新加载，无需重启
- **项目管理** — 多项目隔离，独立启用/禁用，重启自动执行

---

## 安装

1. 将 JAR 放入服务端 `mods/` 目录
2. 启动服务端
3. 脚本目录自动创建在 `config/box3/script/`

**需求：** NeoForge 1.21.1，Java 21

---

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
    └── app.ts            ← 入口
```

然后构建：

```bash
cd config/box3/script/mygame
npm install
npm run build          # 输出 dist/app.js
```

回到游戏启用：

```
/box3script on mygame
/box3script reload
```

---

## 可用 API

| 对象 | 说明 | 文档 |
|---|---|---|
| `world` | 世界状态、事件、记分板、Bossbar、队伍、粒子、烟花 | [world.md](docs/api/world.md) |
| `entity` | 实体属性、AI、装备、药水、标签 | [entity.md](docs/api/entity.md) |
| `player` | 玩家背包、消息、飞行、游戏模式、传送 | [player.md](docs/api/player.md) |
| `voxels` | 方块读写、区域填充 | [voxels.md](docs/api/voxels.md) |
| `storage` | JSON 数据持久化 | [storage.md](docs/api/storage.md) |
| 数学类型 | Vector3、Bounds3、Color、Quaternion | [math.md](docs/api/math.md) |

[API 总览 →](docs/api/README.md)

---

## 命令

| 命令 | 说明 |
|---|---|
| `/box3script create <name>` | 创建 TS 脚手架项目 |
| `/box3script run <project>` | 运行一次项目 |
| `/box3script list` | 列出所有项目及启用状态 |
| `/box3script on <project\|all>` | 启用项目 |
| `/box3script off <project\|all>` | 禁用项目 |
| `/box3script reload` | 重载所有已启用脚本 |
| `/box3script stop` | 停止所有脚本 |
| `/box3script file <path>` | 加载 JS 文件 |

[命令详细参考 →](docs/api/commands.md)

---

## 事件

```js
world.onTick(function () { ... });
world.onPlayerJoin(function (entity) { ... });
world.onPlayerLeave(function (entity) { ... });
world.onChat(function (entity, message, tick) { ... });
world.onEntityDeath(function (entity, killer, tick) { ... });
world.onEntityDamage(function (entity, amount, source, attacker, tick) { ... });
world.onPlayerRespawn(function (entity) { ... });
world.onVoxelDestroy(function (entity, x, y, z, voxel, tick) { ... });
world.onBlockPlace(function (entity, x, y, z, voxel, voxelId, tick) { ... });
world.onBlockActivate(function (entity, x, y, z, voxel, tick) { ... });
// 共 17 种事件，完整列表见 docs/api/world.md
```

---

## 已知限制（测试版）

- 仅支持 NeoForge 1.21.1（Fabric / 其他 MC 版本暂未适配）
- Rhino 1.9.1 仅支持到 ES5 语法（class / 箭头函数 / 模板字符串由 Babel 转译）
- `player.dialog()` 为简化实现，仅发送系统消息
- 部分 Box3 API（如 UI 相关）在服务端环境下不适用
- 暂无自动化测试覆盖

---

## 构建

```bash
cd Box3JS-NeoForge-1.21.1
./gradlew build
```

输出：`build/libs/box3js-<version>.jar`

---

## 许可证

Apache License 2.0
