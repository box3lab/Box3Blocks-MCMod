# server — 服务端 API 总览

服务端脚本运行在 Minecraft 服务器线程上，入口文件是 `src/server/app.ts`，构建产物是 `dist/server.js`。服务端 API 负责世界状态、实体和玩家、方块读写、事件回调、持久化数据、网络请求以及服务端到客户端的事件下发。

> 客户端 UI、键盘输入、本地音效和本地 GUI 不在服务端 API 中。相关能力见 [client.md](client.md)。

## 服务端全局对象

| 对象 | 类型 | 作用域 | 说明 |
|------|------|--------|------|
| `world` | `GameWorld` | 服务端 | 世界状态、事件系统、实体生成、计分板、BossBar、队伍、边界、粒子、音效 |
| `voxels` | `GameVoxels` | 服务端 | 方块读写、区域填充、方块 ID/名称转换、刷怪笼控制 |
| `storage` | `GameStorage` | 服务端/客户端 | JSON 持久化存储。服务端按项目写入 `config/box3/storage/<project>/` |
| `db` | `GameDatabase` | 服务端/客户端 | SQLite 查询。服务端数据库在 `config/box3/data/<project>.db` |
| `http` | `GameHttpAPI` | 服务端/客户端 | HTTP 请求，支持同步和回调式异步 |
| `remoteChannel` | `RemoteChannel` | 双端 | 服务端向客户端发事件，或接收客户端发来的事件 |
| `registries` | `GameRegistries \| undefined` | 服务端编译模式 | `/box3script compile` 产物中的自定义方块、物品、音效查询 |
| `console` | `GameConsole` | 双端 | `log` / `debug` / `warn` / `error` 日志 |

实体和玩家不是独立全局对象。它们通常来自事件回调、查询或生成结果：

```ts
world.onPlayerJoin((entity) => {
  const player = entity.player;
  player.directMessage("Welcome, " + player.name);
});
```

## 入口和类型

服务端代码放在 `src/server/`，只会读取服务端类型入口：

```text
src/server/app.ts
types/server/index.d.ts
```

`tsconfig.server.json` 不包含 `types/client/index.d.ts`，所以服务端代码中直接使用 `client`、`audio`、`input`、`ui`、`chat`、`gui` 会被 TypeScript 报错。需要跨端能力时，通过 `remoteChannel` 发送事件给客户端脚本。

```ts
remoteChannel.sendClientEvent(entity, {
  type: "showToast",
  text: "任务完成",
});
```

## 世界与事件

`world` 是服务端脚本的主入口。常用能力：

| 能力 | API |
|------|-----|
| 当前项目名 | `world.projectName()` |
| 当前 tick | `world.currentTick()` |
| 服务端标识/MOTD | `world.serverId` |
| 广播消息 | `world.say(text)` |
| 每 tick 回调 | `world.onTick(handler)` |
| 玩家加入/离开 | `world.onPlayerJoin(handler)` / `world.onPlayerLeave(handler)` |
| 聊天处理 | `world.onChat(handler)` |
| 实体交互 | `world.onInteract(handler)` |
| 方块交互 | `world.onBlockActivate(handler)` |
| 方块破坏/放置 | `world.onVoxelDestroy(handler)` / `world.onBlockPlace(handler)` |
| 定时器 | `world.setTimeout(fn, ticks)` / `world.setInterval(fn, ticks)` |

```ts
world.onChat((entity, message) => {
  if (message === "!tick") {
    entity.player.directMessage("tick = " + world.currentTick());
    return false;
  }
  return true;
});
```

事件注册会返回 `GameEventHandlerToken`。长期运行的脚本应保存 token，在不需要时取消：

```ts
const token = world.onTick(() => {
  // ...
});

token.cancel();
```

## 玩家与实体

`GameEntity` 表示玩家或生物，`GamePlayerEntity` 是玩家实体收窄后的类型。先判断 `entity.isPlayer()`，再访问 `entity.player` 更安全。

```ts
const target = world.querySelector("#some-uuid");
if (target && target.isPlayer()) {
  target.player.actionBar("你被选中了");
}
```

常用实体能力：

| 能力 | API |
|------|-----|
| 位置/速度 | `entity.position` / `entity.velocity` |
| 生命值 | `entity.hp` / `entity.maxHp` |
| 标签 | `entity.addTag()` / `entity.removeTag()` / `entity.hasTag()` |
| 装备 | `entity.setEquipment(slot, itemId)` |
| 药水效果 | `entity.addEffect()` / `entity.clearEffects()` |
| AI 与导航 | `entity.setAI()` / `entity.navigateTo()` |

常用玩家能力：

| 能力 | API |
|------|-----|
| 私聊/ActionBar/标题 | `player.directMessage()` / `player.actionBar()` / `player.title()` |
| 传送 | `player.teleport(pos)` |
| 背包物品 | `player.giveItem()` / `player.clearInventory()` / `player.getHeldItem()` |
| 游戏模式 | `player.gameMode` |
| 飞行 | `player.canFly` / `player.flying` |
| 经验/饥饿值 | `player.xp` / `player.food` / `player.saturation` |
| 命令 | `player.runCommand(cmd)` |

## 方块与体素

`voxels` 负责方块读写。字符串优先使用命名空间 ID，例如 `"minecraft:stone"`；简写 `"stone"` 也会尝试解析。

```ts
const pos = new GameVector3(0, 80, 0);
voxels.setVoxel(pos, "minecraft:diamond_block");

const name = voxels.getVoxelName(pos);
world.say("block = " + name);
```

区域操作可能一次修改大量方块，应控制范围并优先在服务端空闲时执行：

```ts
voxels.fillVoxel(0, 70, 0, 10, 75, 10, "minecraft:glass");
```

## 服务端数据

`storage` 适合 JSON 型小数据、配置、排行榜缓存；`db` 适合复杂查询和表结构。

```ts
const scores = storage.getDataStorage<number>("scores");
scores.increment("alice", 1);

const rows = db.sql<{ name: string; score: number }>(
  "SELECT name, score FROM scores ORDER BY score DESC LIMIT ?",
  10,
);
```

服务端还额外提供共享存储：

```ts
const globalConfig = storage.getGroupStorage("config");
globalConfig.set("season", "spring");
```

## 跨端通信

服务端使用 `remoteChannel` 与客户端脚本通信。发送前建议检查玩家是否安装了 Box3JS 客户端：

```ts
world.onPlayerJoin((entity) => {
  if (!entity.hasBox3JSClient()) {
    return;
  }

  remoteChannel.sendClientEvent(entity, {
    type: "welcome",
    text: "欢迎来到服务器",
  });
});
```

接收客户端事件：

```ts
remoteChannel.onServerEvent<{ type: string; key?: string }>((event) => {
  if (event.args.type === "hotkey") {
    event.entity.player.actionBar("按键: " + event.args.key);
  }
});
```

事件数据必须能 JSON 序列化。不要传 Java 对象、函数或循环引用对象。

## 自定义注册表

`registries` 只在 `/box3script compile` 打包后的独立 JAR 中可用，解释模式为 `undefined`。

```ts
if (registries) {
  const block = registries.getBlock("rainbow_cube");
  if (block) {
    player.giveItem(block.itemId, 1);
  }
}
```

自定义内容文件：

| 文件 | 内容 |
|------|------|
| `registries/blocks.json` | 方块定义 |
| `registries/items.json` | 物品、食物、工具、护甲定义 |
| `registries/sounds.json` | 音效定义 |
| `registries/creativeTabs.json` | 创造模式标签页 |

## 服务端 API 文档索引

| 文档 | 内容 |
|------|------|
| [world.md](world.md) | 世界状态、事件、计分板、BossBar、队伍、边界、粒子、音效 |
| [entity.md](entity.md) | 实体属性、AI、装备、效果、寻路、标签 |
| [player.md](player.md) | 玩家背包、消息、传送、飞行、游戏模式、经验 |
| [voxels.md](voxels.md) | 方块读写、区域填充、刷怪笼 |
| [storage.md](storage.md) | JSON 持久化存储 |
| [database.md](database.md) | SQLite 数据库 |
| [http.md](http.md) | HTTP 请求 |
| [registries.md](registries.md) | 自定义注册表与编译 JAR 模式 |
| [math.md](math.md) | 向量、包围盒、颜色、四元数 |

## 推荐风格

- 服务端文件只引用服务端 API：世界、实体、玩家、方块、数据、网络。
- 客户端 UI 和输入逻辑放到 `src/client/app.ts`，通过 `remoteChannel` 接收服务端状态。
- 对所有长期事件监听保存 token，需要关闭玩法或重载模块时调用 `cancel()`。
- 大范围 `voxels.fillVoxel()`、大量实体生成、同步 HTTP 请求都应控制频率，避免卡住服务器 tick。
- 共享数据优先明确命名空间，例如 `storage.getDataStorage("arena/scores")` 或 `storage.getGroupStorage("global/season")`。

