# Box3JS

**Box3JS** 是一个 Minecraft NeoForge 1.21.1 模组，将 JavaScript 脚本引擎（Mozilla Rhino）嵌入到服务端中。允许用 JavaScript 编写游戏脚本——小游戏、机制扩展、自动化管理——而无需编写 Java 代码。

---

## 特性

- **JavaScript 运行时** — 使用 Rhino 1.7.14 引擎，编写 ES5/部分 ES6 代码
- **Box3 API 兼容** — 实现了 Box3 平台核心 API（`world`、`entity`、`player`、`voxels`、`storage`）
- **MC 扩展 API** — 90+ Minecraft 独有功能：记分板、Bossbar、队伍、世界边界、粒子、烟花、闪电、药水效果、装备、属性、存储等
- **热重载** — 使用 `/box3script reload` 重新加载脚本，无需重启服务端
- **项目管理** — 多项目隔离，每个项目独立启用/禁用，下次启动自动执行
- **多语言** — 所有命令提示支持英文和中文

---

## 安装

1. 下载 `.jar` 文件放入服务端 `mods/` 目录
2. 启动服务端
3. 脚本目录自动创建在 `config/box3/script/`

**需求：** NeoForge 1.21.1+，Java 21+

---

## 快速开始

### 创建第一个脚本

在游戏中（需要 OP 权限）：

```
/box3script create hello
/box3script on hello
/box3script run hello
```

这会创建 `config/box3/script/hello/app.js`：

```js
// hello — Box3JS 项目
world.onTick(() => {
    // 每 tick 执行
});

world.onChat((entity, message) => {
    var p = entity.player;
    if (message === "!hello") {
        p.directMessage("你好，" + p.name + "！");
    }
});

console.log("hello 已加载");
```

### 目录结构

```
config/box3/
  ├── scripts.json         ← 项目启用/禁用配置
  ├── script/
  │   ├── hello/
  │   │   └── app.js
  │   └── mygame/
  │       └── app.js
  └── data/                ← storage API 数据文件
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
| `GameVector3` 等 | 向量、包围盒、颜色、四元数 | [math.md](docs/api/math.md) |

[API 总览 →](docs/api/README.md)

---

## 命令

| 命令 | 说明 |
|---|---|
| `/box3script eval <code>` | 直接执行 JS 代码 |
| `/box3script file <path>` | 加载 JS 文件 |
| `/box3script create <name>` | 创建新项目 |
| `/box3script run <project>` | 运行一次项目 |
| `/box3script list` | 列出所有项目及状态 |
| `/box3script on <project>` | 启用项目 |
| `/box3script on all` | 启用所有 |
| `/box3script off <project>` | 禁用项目 |
| `/box3script off all` | 禁用所有 |
| `/box3script reload` | 重载所有已启用脚本 |
| `/box3script stop` | 停止所有脚本 |

[命令详细参考 →](docs/api/commands.md)

---

## 事件

脚本通过事件回调响应游戏行为：

```js
world.onTick(() => { ... });
world.onPlayerJoin((entity) => { ... });
world.onPlayerLeave((entity) => { ... });
world.onChat((entity, message, tick) => { ... });
world.onEntityDeath((entity, killer, tick) => { ... });
world.onEntityDamage((entity, amount, source, attacker, tick) => { ... });
world.onPlayerRespawn((entity) => { ... });
world.onVoxelDestroy((entity, x, y, z, voxel, tick) => { ... });
world.onBlockPlace((entity, x, y, z, voxel, voxelId, tick) => { ... });
world.onBlockActivate((entity, x, y, z, voxel, tick) => { ... });
// 完整 17 种事件见 docs/api/world.md
```

---

## 构建

```bash
cd Box3JS-NeoForge-1.21.1
./gradlew build
```

输出 JAR 在 `build/libs/box3js-<version>.jar`。

---

## 许可证

Apache License 2.0
