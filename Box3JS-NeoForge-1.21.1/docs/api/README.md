# Box3JS API 参考

Box3JS 是一个 Minecraft 模组，允许用 JavaScript/TypeScript 编写服务端脚本。所有脚本运行在 `config/box3/script/<项目名>` 下。

## 5 分钟快速开始

```bash
# 1. 游戏内创建项目
/box3script create mygame

# 2. 安装依赖并构建
cd config/box3/script/mygame
npm install && npm run build

# 3. 启动脚本
/box3script start mygame
```

打开 `src/app.ts`，写入：

```js
world.onChat((entity, message) => {
  if (message === "!hello") {
    entity.player.directMessage("Hello, " + entity.player.name + "!");
    return false; // 阻止消息显示在聊天栏
  }
  return true;
});

console.log("脚本已加载");
```

每次修改后重新 `npm run build`，然后用 `/box3script reload mygame` 热重载。

## 功能速查 — 我想...

按你想做的事情查找对应 API，而非按全局对象记。

### 消息与聊天

| 我想... | 用这个 |
|---------|--------|
| 全服广播 | `world.say("消息")` |
| 私密消息（只一人看到） | `player.directMessage("消息")` |
| 快捷栏上方文字 | `player.actionBar("消息")` |
| 屏幕中央大标题 | `player.title("标题", "副标题")` |
| 拦截/处理聊天 | `world.onChat((entity, msg) => { ... })` |

### 玩家属性

| 我想... | 用这个 |
|---------|--------|
| 获取/设置玩家位置 | `player.position` → `GameVector3` |
| 传送玩家 | `player.teleport(new GameVector3(x, y, z))` |
| 修改生命值 | `player.hp = 20` / `player.maxHp = 40` |
| 修改饱食度 | `player.food = 20` / `player.saturation = 10` |
| 切换游戏模式 | `player.gameMode = "creative"` |
| 切换飞行 | `player.canFly = true` / `player.flying = true` |
| 踢出玩家 | `player.kick("原因")` |
| 以玩家身份执行命令 | `player.runCommand("say hi")` |

### 物品与装备

| 我想... | 用这个 |
|---------|--------|
| 给玩家普通物品 | `player.giveItem("minecraft:diamond", 1)` |
| 给带附魔的物品 | `player.giveEnchantedItem(...)` |
| 给带自定义名称的物品 | `player.giveNamedItem(...)` |
| 给自定义模组物品 | `player.giveCustomItem("my_item", 1)` |
| 获取手持物品 | `player.getHeldItem()` |
| 清空背包 | `player.clearInventory()` |
| 设置实体装备 | `entity.setEquipment("head", "iron_helmet")` |
| 加载自定义物品包 | `world.loadCustomItems("mypack")` |

### 方块操作

| 我想... | 用这个 |
|---------|--------|
| 读取某个位置的方块 | `voxels.getVoxel(x, y, z)` |
| 放置/替换方块 | `voxels.setVoxel(x, y, z, "minecraft:stone")` |
| 填充区域 | `voxels.fillVoxel(x1,y1,z1, x2,y2,z2, "stone")` |
| 监听方块破坏 | `world.onVoxelDestroy((entity, x, y, z, voxel) => { ... })` |
| 监听方块放置 | `world.onBlockPlace((entity, x, y, z, voxel) => { ... })` |

### 实体操控

| 我想... | 用这个 |
|---------|--------|
| 生成实体 | `world.spawnEntity("minecraft:zombie", pos)` |
| 带配置创建实体 | `world.createEntity({ type, position, ... })` |
| 设置实体名称 | `entity.setNameTag("§cBoss")` |
| 开关 AI | `entity.setAI(true)` |
| 实体导航 | `entity.navigateTo(x, y, z, speed)` |
| 设置攻击目标 | `entity.setTarget(otherEntity)` |
| 判断是否是玩家 | `entity.isPlayer()` |
| 获取实体类型 | `entity.entityType` |
| 获取实体标签 | `entity.tags()` / `entity.hasTag("boss")` |
| 查询附近实体 | `world.entitiesInRadius(pos, radius)` |
| 查询所有实体 | `world.querySelectorAll("*")` |

### 客户端本地功能（需 Box3JS 客户端 Mod）

| 我想... | 用这个 |
|---------|--------|
| 客户端每帧执行 | `client.onTick(() => { ... })` |
| 检测按键按下 | `input.isKeyDown("space")` |
| 监听按键事件 | `input.onKeyPress("f", () => { ... })` |
| 播放客户端音效 | `audio.playSound("pling", 1.0, 1.0)` |
| 播放客户端音乐 | `audio.playMusic("minecraft:music.game", 0.5, 1.0)` |
| 停止所有声音 | `audio.stopAll()` |
| 获取/设置音量 | `audio.getVolume("music")` / `audio.setVolume("player", 0.8)` |
| 快捷栏上方显示文字 | `ui.showOverlay("文字")` |
| 显示屏幕大标题 | `ui.showTitle("标题", "副标题")` |
| 发送聊天消息 | `chat.sendMessage("消息")` |
| 接收聊天消息 | `chat.onMessage((msg, sender, isSystem) => { ... })` |
| 发送服务端事件 | `remoteChannel.sendServerEvent({ ... })` |
| 接收服务端事件 | `remoteChannel.onClientEvent((event) => { ... })` |
| 客户端本地存储 | `storage.getDataStorage("key")` |

### 视觉效果

| 我想... | 用这个 |
|---------|--------|
| 粒子效果 | `world.spawnParticle("flame", x, y, z, ...)` |
| 圆形粒子圈 | `world.spawnParticleCircle(x, y, z, radius, "heart", 20)` |
| 烟花 | `world.launchFirework(x, y, z, "red", "large_ball")` |
| 闪电 | `world.strikeLightning(x, y, z)` |
| 爆炸 | `world.explode(x, y, z, 4)` |
| 播放音效（全局） | `world.playSound("pling", pos, 1.0, 1.0)` |
| 播放音效（单人） | `player.playSound("pling", 1.0, 1.0)` |

### 药水效果

| 我想... | 用这个 |
|---------|--------|
| 施加药水效果 | `entity.addEffect("minecraft:speed", duration, level, hideParticles)` |
| 清除所有效果 | `entity.clearEffects()` |

### 事件系统

| 我想... | 用这个 |
|---------|--------|
| 每 tick 执行 | `world.onTick((info) => { ... })` |
| 玩家加入时 | `world.onPlayerJoin((entity, tick) => { ... })` |
| 玩家离开时 | `world.onPlayerLeave((entity, tick) => { ... })` |
| 实体死亡时 | `world.onEntityDeath((entity, killer, tick) => { ... })` |
| 实体受伤时 | `world.onEntityDamage((entity, amount, source, attacker, tick) => { ... })` |
| 右键实体时 | `world.onInteract((entity, target, tick) => { ... })` |
| 右键方块时 | `world.onBlockActivate((entity, x, y, z, voxel, tick) => { ... })` |
| 按钮按下时 | `world.onButtonPressed((entity, button, tick) => { ... })` |
| 玩家重生时 | `world.onPlayerRespawn((entity, tick) => { ... })` |
| 定时执行一次 | `world.setTimeout(() => { ... }, ticks)` |
| 定时循环执行 | `world.setInterval(() => { ... }, ticks)` |
| 取消事件监听 | `token.cancel()` |
| 检查事件是否活跃 | `token.active()` |

### 数据持久化

| 我想... | 用这个 |
|---------|--------|
| 读写 JSON 数据 | `storage.getDataStorage("key")` |
| SQL 查询 | `db.sql("SELECT ...")` |
| SQL 写入 | `db.sql("INSERT INTO ...")` |

### 网络请求

| 我想... | 用这个 |
|---------|--------|
| GET 请求 | `http.fetch("https://...")` |
| POST JSON | `http.fetch(url, { method: "POST", headers, body })` |
| 解析 JSON | `resp.json()` 或 `{ responseType: "json" }` |
| 读取文本 | `resp.text()` |
| 设置超时 | `http.fetch(url, { timeout: 5000 })` |

### 游戏系统

| 我想... | 用这个 |
|---------|--------|
| 创建计分板 | `world.addScoreboard("name")` |
| 设置分数 | `world.setScore("玩家", "计分板", 10)` |
| 显示计分板 | `world.showScoreboard("sidebar", "name")` |
| 显示 BossBar | `world.showBossbar("id", "标题", 0.5, "red")` |
| 创建队伍 | `world.createTeam("teamName", "color")` |
| 加入队伍 | `world.joinTeam(entity, "teamName")` |
| 设置世界边界 | `world.borderSize = 500` |
| 缩圈 | `world.shrinkBorder(100, 60)` |
| 修改世界时间 | `world.time = 6000` |
| 设置天气 | `world.rainDensity = 0` / `world.clearWeather()` |
| 修改游戏规则 | `world.setGameRule("keepInventory", true)` |

### 数学工具

| 我想... | 用这个 |
|---------|--------|
| 三维坐标 | `new GameVector3(x, y, z)` |
| 向量运算 | `v.add(other)`, `v.scale(n)`, `v.length()` |
| 包围盒 | `new GameBounds3(min, max)` |
| 颜色 | `new GameRGBColor(r, g, b)` / `new GameRGBAColor(r, g, b, a)` |

### 跨脚本通信

| 我想... | 用这个 |
|---------|--------|
| 发送消息给其他脚本 | `world.sendMessage("projectName", data)` |
| 接收其他脚本的消息 | `world.onMessage((from, data) => { ... })` |

---

## 全局对象一览

| 对象 | 类型 | 说明 |
|------|------|------|
| `world` | ✅ Box3 | 世界控制，见 [world.md](world.md) |
| `entity` | ✅ Box3 | 实体包装（回调参数或 `world.spawnEntity` 创建），见 [entity.md](entity.md) |
| `player` | ✅ Box3 | 玩家包装（通过 `entity.player` 获取），见 [player.md](player.md) |
| `voxels` | ✅ Box3 | 方块操作，见 [voxels.md](voxels.md) |
| `storage` | ✅ Box3 | 数据持久化，见 [storage.md](storage.md) |
| `db` | ✅ Box3 | SQLite 数据库，见 [database.md](database.md) |
| `http` | 🆕 MC 扩展 | HTTP 请求，见 [http.md](http.md) |
| `audio` | 🆕 MC 扩展 | 客户端音效、音乐、音量控制，见 [client.md](client.md) |
| `client` | 🆕 MC 扩展 | 客户端生命周期，见 [client.md](client.md) |
| `input` | 🆕 MC 扩展 | 客户端键盘输入，见 [client.md](client.md) |
| `ui` | 🆕 MC 扩展 | 客户端屏幕 UI，见 [client.md](client.md) |
| `chat` | 🆕 MC 扩展 | 客户端聊天收发，见 [client.md](client.md) |
| `remoteChannel` | 🆕 MC 扩展 | 服务端↔客户端事件通信，见 [client.md](client.md) |
| `console` | ✅ Box3 | 控制台日志输出（`log`/`warn`/`error`/`debug`） |
| `GameVector3` | ✅ Box3 | 三维向量，见 [math.md](math.md) |
| `GameBounds3` | ✅ Box3 | 包围盒，见 [math.md](math.md) |
| `GameRGBColor` | ✅ Box3 | RGB 颜色，见 [math.md](math.md) |
| `GameRGBAColor` | ✅ Box3 | RGBA 颜色，见 [math.md](math.md) |
| `GameQuaternion` | ✅ Box3 | 四元数，见 [math.md](math.md) |

## API 标注说明

| 标注 | 含义 |
|------|------|
| ✅ **Box3 API** | 源自 Box3 平台，命名和语义与 Box3 保持一致 |
| ⬆ **MC 扩展** | 非 Box3 原有，利用 Minecraft 特性新增的 API |

## 详细文档索引

| 文档 | 内容 |
|------|------|
| [world.md](world.md) | 世界状态、事件回调、记分板、BossBar、队伍、边界、粒子、烟花、闪电、音效 |
| [entity.md](entity.md) | 实体属性、AI、装备、药水效果、寻路、标签、碰撞 |
| [player.md](player.md) | 背包、消息、飞行、游戏模式、传送、命令、经验值 |
| [voxels.md](voxels.md) | 方块读写、区域填充、刷怪笼 |
| [storage.md](storage.md) | 数据持久化存储 |
| [database.md](database.md) | SQLite 数据库 |
| [http.md](http.md) | HTTP 网络请求 |
| [client.md](client.md) | 客户端脚本：生命周期、键盘输入、屏幕 UI、聊天、remoteChannel、客户端本地存储 |
| [math.md](math.md) | GameVector3、GameBounds3、GameRGBColor、GameRGBAColor、GameQuaternion |
| [commands.md](commands.md) | `/box3script` 命令参考 |

## 文件模块 — TypeScript 构建管线

`/box3script create` 创建的项目自带完整的 TS 构建环境：

```
config/box3/script/mygame/
├── package.json          ← esbuild + Babel + @babel/preset-typescript
├── tsconfig.base.json    ← 公共 TS 编译选项
├── tsconfig.server.json  ← 服务端 TS 配置
├── tsconfig.client.json  ← 客户端 TS 配置
├── build.mjs             ← Babel TS→JS → esbuild bundle → dist/
├── types/
│   ├── shared.d.ts       ← 服务端&客户端共享类型
│   ├── server.d.ts       ← 服务端专属类型
│   └── client.d.ts       ← 客户端专属类型
├── src/
│   ├── server/
│   │   ├── app.ts        ← 服务端入口
│   │   └── ...
│   └── client/
│       ├── app.ts        ← 客户端入口
│       └── ...
└── dist/
    ├── server.js            ← 服务端编译产物
    ├── client.js          ← 客户端编译产物
    └── <name>-<ver>.jar  ← 独立 JAR（/box3script compile）
```

`npm run build` 执行构建。`/box3script watch` 开启文件监控自动热重载。

## 发布部署

开发调试完成后，将脚本编译为**独立 JAR 模组**，需与 Box3JS 一同部署在 NeoForge 服务器：

```
/box3script compile <项目名>
```

生成 `<项目名>-<版本号>.jar`（从 `package.json` 读取 name/displayName/version/description/author/license/homepage/logoFile 等元数据），放入 `mods/` 目录启动即可。

详见 [完整命令参考 →](commands.md#box3script-compile-project)

## Tick 换算

| 时长 | Ticks |
|------|-------|
| 1 秒 | 20 |
| 5 秒 | 100 |
| 30 秒 | 600 |
| 1 分钟 | 1200 |
| 5 分钟 | 6000 |

## 教程

从零开始学习 Box3JS 脚本开发，请阅读 `docs/tutorial/` 系列教程：

| 教程 | 内容 |
|------|------|
| [01-basics.md](../tutorial/01-basics.md) | 从零开始：第一个脚本、聊天命令、定时任务 |
| [02-player-items.md](../tutorial/02-player-items.md) | 玩家操控：传送、物品给予、药水效果、游戏模式 |
| [03-events-entities.md](../tutorial/03-events-entities.md) | 事件系统与实体操控：AI、战斗、巡逻 |
| [04-advanced-systems.md](../tutorial/04-advanced-systems.md) | 高级系统：计分板、BossBar、队伍、世界边界 |
| [05-examples.md](../tutorial/05-examples.md) | 实战示例：PvP 竞技场、特效、烟花、波次刷怪 |
