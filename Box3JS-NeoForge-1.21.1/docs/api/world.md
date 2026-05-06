# world — 世界 API

`world` 是全局单例，代表 Minecraft 服务端的世界状态。控制天气、时间、游戏规则、实体生成，注册事件回调，管理记分板/Bossbar/队伍，以及发射粒子、烟花、闪电等视觉效果。

## 世界属性

### world.projectName()

⬆ MC 扩展 | 只读。服务端 MOTD 字符串。

```js
console.log(world.projectName()); // "A Minecraft Server"
```

### world.currentTick()

✅ Box3 API | 只读。服务器自启动以来的总 tick 数。

```js
var uptime = world.currentTick();
world.say("服务器已运行 " + Math.floor(uptime / 20 / 60) + " 分钟");
```

## 天气

### world.rainDensity

✅ Box3 API | 获取/设置降雨强度，范围 0.0–1.0。

```js
world.rainDensity = 1.0; // 满强度下雨
console.log(world.rainDensity); // 0.0 ~ 1.0
```

### world.thunderDensity

⬆ MC 扩展 | 获取/设置雷暴强度，范围 0.0–1.0。

```js
world.thunderDensity = 0.5;
```

### world.clearWeather()

⬆ MC 扩展 | 同时清除雨和雷暴。

```js
world.clearWeather();
```

## 时间

### world.time

✅ Box3 API | 获取/设置世界时间（tick）。Minecraft 一天 = 24000 tick。

```js
world.time = 6000; // 正午
world.time = 18000; // 午夜
console.log(world.time); // 当前时间
```

此外提供 `world.setTime(tick)` 方法作为便捷设置接口。

```js
world.setTime(6000); // 等效于 world.time = 6000
```

常用时间值：`0` 日出、`6000` 正午、`12000` 日落、`18000` 午夜。

### world.timeScale

✅ Box3 API | 获取/设置时间流速。`0` = 暂停，`1` = 正常。底层修改 `doDaylightCycle` 游戏规则。

```js
world.timeScale = 0; // 冻结时间
world.timeScale = 1; // 恢复正常
```

## 难度

### world.difficulty

✅ Box3 API | 获取/设置游戏难度。get 返回名称字符串，set 接受名称字符串或数字 0–3。

```js
world.difficulty = "hard";
world.difficulty = 3; // 等同 hard
console.log(world.difficulty); // "hard"

// 有效值: "peaceful"(0), "easy"(1), "normal"(2), "hard"(3)
```

## 出生点

### world.spawnPoint

⬆ MC 扩展 | 只读，返回世界出生点 `GameVector3`。

### world.setWorldSpawn(pos)

⬆ MC 扩展 | 设置世界出生点。

```js
world.setWorldSpawn(new GameVector3(0, 70, 0));
```

## 游戏规则

### world.getGameRule(name)

⬆ MC 扩展 | 获取游戏规则布尔值。

### world.setGameRule(name, value)

⬆ MC 扩展 | 设置游戏规则。`value` 为布尔值。

**支持的规则：**

| 规则名               | 说明         |
| -------------------- | ------------ |
| `doDaylightCycle`    | 时间流动     |
| `doWeatherCycle`     | 天气变化     |
| `keepInventory`      | 死亡不掉落   |
| `doMobSpawning`      | 生物自然生成 |
| `doFireTick`         | 火焰蔓延     |
| `mobGriefing`        | 生物破坏方块 |
| `doImmediateRespawn` | 立即重生     |

```js
world.setGameRule("keepInventory", true);
world.setGameRule("doFireTick", false);
console.log(world.getGameRule("doMobSpawning")); // true/false
```

## 实体生成

### world.spawnEntity(type, pos)

✅ Box3 API | 在指定位置生成实体。`type` 为命名空间 ID，返回 `Box3JSEntity`。

```js
var zombie = world.spawnEntity("minecraft:zombie", new GameVector3(0, 100, 0));
zombie.setNameTag("守卫");
zombie.maxHp = 40;
zombie.hp = 40;
zombie.setEquipment("mainhand", "minecraft:iron_sword");
zombie.setAI(true);
```

## 事件回调

所有事件回调由 `world.onXxx(handler)` 注册。除 `onTick` 外，回调第一个参数通常是触发该事件的 `entity`（`Box3JSEntity`）。

| 事件                         | 类型    | 回调签名                                               | 触发时机                        |
| ---------------------------- | ------- | ------------------------------------------------------ | ------------------------------- |
| `world.onTick(fn)`           | ✅ Box3 | `()`                                                   | 每 tick                         |
| `world.onPlayerJoin(fn)`     | ✅ Box3 | `(entity)`                                             | 玩家登录                        |
| `world.onPlayerLeave(fn)`    | ✅ Box3 | `(entity)`                                             | 玩家退出                        |
| `world.onChat(fn)`           | ✅ Box3 | `(entity, message, tick)`                              | 玩家发送聊天消息                |
| `world.onVoxelDestroy(fn)`   | ✅ Box3 | `(entity, x, y, z, voxel, tick)`                       | 玩家破坏方块                    |
| `world.onBlockPlace(fn)`     | ⬆ MC    | `(entity, x, y, z, voxel, voxelId, tick)`              | 玩家放置方块                    |
| `world.onBlockActivate(fn)`  | ⬆ MC    | `(entity, x, y, z, voxel, tick)`                       | 玩家右键方块                    |
| `world.onInteract(fn)`       | ✅ Box3 | `(entity, target, tick)`                               | 玩家右键实体                    |
| `world.onVoxelContact(fn)`   | ✅ Box3 | `(entity, voxelId, x, y, z, contactType, force, tick)` | 实体接触方块                    |
| `world.onEntityContact(fn)`  | ✅ Box3 | `(entity, other, tick)`                                | 两个实体接触                    |
| `world.onEntitySeparate(fn)` | ✅ Box3 | `(entity, other, tick)`                                | 两个实体分离                    |
| `world.onFluidEnter(fn)`     | ✅ Box3 | `(entity, fluid, x, y, z, tick)`                       | 实体进入液体                    |
| `world.onFluidLeave(fn)`     | ✅ Box3 | `(entity, fluid, x, y, z, tick)`                       | 实体离开液体                    |
| `world.onEntityDeath(fn)`    | ⬆ MC    | `(entity, killer, tick)`                               | 实体死亡；`killer` 可能为 null  |
| `world.onEntityDamage(fn)`   | ⬆ MC    | `(entity, amount, source, attacker, tick)`             | 实体受伤（Pre 阶段）            |
| `world.onPlayerRespawn(fn)`  | ⬆ MC    | `(entity)`                                             | 玩家重生                        |
| `world.onMessage(fn)`        | ⬆ MC    | `(from, data)`                                         | 收到 `world.sendMessage()` 消息 |

```js
world.onTick(() => {
  // 每 tick 执行
});

world.onPlayerJoin((entity) => {
  var p = entity.player;
  world.say(p.name + " 加入了游戏");
  p.teleport(new GameVector3(0, 100, 0));
});

world.onChat((entity, message, tick) => {
  var p = entity.player;
  if (message === "!spawn") {
    p.teleport(new GameVector3(0, 100, 0));
  }
});

world.onEntityDeath((entity, killer) => {
  if (killer && killer.isPlayer()) {
    var kp = killer.player;
    kp.addExperienceLevels(1);
  }
});
```

## 查询

### world.querySelectorAll(selector)

✅ Box3 API | 查询所有匹配实体。返回 `Box3JSEntity[]`。

### world.querySelector(selector)

✅ Box3 API | 查询单个匹配实体。返回 `Box3JSEntity` 或 null。

**选择器语法：**

| 选择器       | 含义             |
| ------------ | ---------------- |
| `"*"`        | 所有在线玩家     |
| `"#uuid"`    | 按 UUID 精确匹配 |
| `".tagName"` | 按标签匹配       |

```js
var allPlayers = world.querySelectorAll("*");
for (var i = 0; i < allPlayers.length; i++) {
  var p = allPlayers[i].player;
  p.actionBar("在线人数: " + allPlayers.length);
}

var specific = world.querySelector("#550e8400-e29b-41d4-a716-446655440000");
if (specific) {
  specific.player.directMessage("找到你了");
}
```

### world.say(message)

✅ Box3 API | 向全服广播消息。

```js
world.say("§6[公告] §f比赛即将开始！");
```

## 计时器

### world.setTimeout(handler, ticks)

⬆ MC 扩展 | 延迟 `ticks` 后执行一次，返回 timer ID。

### world.setInterval(handler, ticks)

⬆ MC 扩展 | 每 `ticks` 重复执行，返回 timer ID。

### world.clearTimeout(id)

⬆ MC 扩展 | 取消 timeout。

### world.clearInterval(id)

⬆ MC 扩展 | 取消 interval。

```js
var tid = world.setTimeout(() => {
  world.say("3 秒后执行");
}, 60); // 60 ticks = 3 秒

var iid = world.setInterval(() => {
  world.say("每 10 秒执行一次");
}, 200); // 200 ticks = 10 秒

// 取消
world.clearTimeout(tid);
world.clearInterval(iid);
```

## 记分板

全部 ⬆ MC 扩展。

### world.addScoreboard(name)

创建 dummy 类型记分项。

### world.addScoreboard(name, criteria)

创建指定标准记分项。`criteria` 可选 `"dummy"`（手动修改）、`"deathCount"`（死亡计数）等。

### world.removeScoreboard(name)

删除记分项。

### world.setScore(entityOrName, objectiveName, value)

设置实体或名字的分数。`entityOrName` 可以是 `Box3JSEntity` 或字符串。

### world.getScore(entityOrName, objectiveName)

获取分数。

### world.showScoreboard(slot, objectiveName)

在指定位置显示记分板。`slot`：`"sidebar"`、`"list"`（Tab 列表）、`"belowname"`（名字下方）。

### world.hideScoreboard(slot)

清除槽位。

### world.listScores(objectiveName)

获取记分项所有条目，返回 `[{name, value}]`。

```js
world.addScoreboard("kills");
world.setScore("Steve", "kills", 5);
world.showScoreboard("sidebar", "kills");

var scores = world.listScores("kills");
// [{name: "Steve", value: 5}, ...]

world.hideScoreboard("sidebar");
world.removeScoreboard("kills");
```

## Boss 血条

全部 ⬆ MC 扩展。

### world.showBossbar(name, text, progress, color)

显示或更新 Boss 血条。

| 参数       | 说明                                                                      |
| ---------- | ------------------------------------------------------------------------- |
| `name`     | 血条 ID，用于后续更新或移除                                               |
| `text`     | 显示文本（支持颜色代码）                                                  |
| `progress` | 0.0–1.0，进度条长度                                                       |
| `color`    | `"blue"`、`"green"`、`"pink"`、`"purple"`、`"red"`、`"white"`、`"yellow"` |

### world.removeBossbar(name)

移除血条。

```js
// 创建一个 3 分钟倒计时血条
var totalTicks = 3600;
var iid = world.setInterval(() => {
  totalTicks -= 20;
  var remain = totalTicks / 3600;
  if (remain <= 0) {
    world.removeBossbar("timer");
    world.clearInterval(iid);
  } else {
    world.showBossbar(
      "timer",
      "§e剩余时间: §f" + Math.ceil(totalTicks / 20) + "s",
      remain,
      remain > 0.5 ? "green" : remain > 0.2 ? "yellow" : "red",
    );
  }
}, 20);
```

## 队伍

全部 ⬆ MC 扩展。

### world.createTeam(name, color)

创建队伍。`color`：`"aqua"`、`"black"`、`"blue"`、`"dark_aqua"`、`"dark_blue"`、`"dark_gray"`、`"dark_green"`、`"dark_purple"`、`"dark_red"`、`"gold"`、`"gray"`、`"green"`、`"light_purple"`、`"red"`、`"white"`、`"yellow"`。

### world.removeTeam(name)

删除队伍。

### world.joinTeam(entity, teamName)

将实体加入队伍。

### world.leaveTeam(entity)

将实体移出当前队伍。

### world.getTeamOf(entity)

获取实体所在队伍名称。

```js
world.createTeam("red_team", "red");
world.createTeam("blue_team", "blue");

world.onPlayerJoin((entity) => {
  // 交替分边
  var online = world.querySelectorAll("*").length;
  world.joinTeam(entity, online % 2 === 0 ? "red_team" : "blue_team");
});
```

## 世界边界

全部 ⬆ MC 扩展。

### world.borderSize

获取/设置当前边界大小。

### world.setBorderCenter(x, z)

设置边界中心。

### world.shrinkBorder(targetSize, seconds)

边界平滑缩小到目标大小，耗时 `seconds` 秒。

### world.setBorderDamage(damagePerBlock)

边界外每秒伤害值。

### world.setBorderWarning(blocks)

边界警告距离（屏幕变红的提前量）。

```js
// 缩圈玩法
world.setBorderCenter(0, 0);
world.borderSize = 500;
world.setBorderDamage(2);
world.setBorderWarning(10);

world.setTimeout(() => {
  world.shrinkBorder(100, 120); // 2 分钟缩到 100
}, 600); // 30 秒后开始
```

## 视觉效果

全部 ⬆ MC 扩展。

### world.strikeLightning(x, y, z)

在坐标召唤闪电（造成默认伤害）。

### world.strikeLightning(pos)

⬆ GameVector3 重载。

### world.strikeLightning(x, y, z, damage)

召唤闪电并指定伤害值。

### world.strikeLightning(pos, damage)

⬆ GameVector3 重载。

```js
world.strikeLightning(0, 100, 0);
world.strikeLightning(new GameVector3(0, 100, 0));
world.strikeLightning(new GameVector3(0, 100, 0), 10); // 10 点伤害
```

### world.launchFirework(x, y, z, color, shape)

在坐标发射烟花火箭。

### world.launchFirework(pos, color, shape)

⬆ GameVector3 重载。

**颜色：** `"red"`、`"blue"`、`"green"`、`"yellow"`、`"gold"`、`"white"`、`"aqua"`、`"pink"`、`"purple"`

**形状：** `"ball"`（小球，默认）、`"large_ball"`（大球）、`"star"`（星形）、`"creeper"`（苦力怕脸）、`"burst"`（爆裂）

```js
world.launchFirework(0, 100, 0, "gold", "large_ball");
world.launchFirework(new GameVector3(0, 100, 0), "red", "star");
```

### world.spawnParticle(type, x, y, z, count, dx, dy, dz, speed)

在坐标生成粒子。粒子类型使用命名空间 ID。

### world.spawnParticle(type, pos, count, dx, dy, dz, speed)

⬆ GameVector3 重载。

### world.spawnParticleCircle(x, y, z, radius, type, count)

在水平圆形上均匀生成粒子。

### world.spawnParticleCircle(pos, radius, type, count)

⬆ GameVector3 重载。

```js
// 单点粒子
world.spawnParticle("minecraft:flame", 0, 100, 0, 10, 0.5, 0.5, 0.5, 0.1);
world.spawnParticle("minecraft:cloud", entity.position, 1, 0, 0, 0, 0);

// 圆形粒子圈
world.spawnParticleCircle(0, 100, 0, 2.0, "minecraft:happy_villager", 20);
world.spawnParticleCircle(
  new GameVector3(0, 100, 0),
  2.0,
  "minecraft:happy_villager",
  20,
);

// 常用粒子:
// minecraft:flame, minecraft:cloud, minecraft:happy_villager
// minecraft:witch, minecraft:portal, minecraft:end_rod
// minecraft:heart, minecraft:note, minecraft:dragon_breath
```

## 物品 / 抛射物

全部 ⬆ MC 扩展。

### world.dropItem(x, y, z, itemId, count)

在坐标掉落物品实体。

### world.dropItem(pos, itemId, count)

⬆ GameVector3 重载。

```js
world.dropItem(0, 100, 0, "minecraft:diamond", 3);
world.dropItem(entity.position, "minecraft:diamond", 3);
```

### world.launchProjectile(type, x, y, z, tx, ty, tz, speed)

从起点向目标发射抛射物，返回 `Box3JSEntity`。

### world.launchProjectile(type, pos, target, speed)

⬆ GameVector3 重载，起点和目标均接受 `GameVector3`。

```js
// 从 (0, 100, 0) 向 (10, 100, 10) 发射火球
world.launchProjectile("minecraft:fireball", 0, 100, 0, 10, 100, 10, 2);
world.launchProjectile(
  "minecraft:fireball",
  new GameVector3(0, 100, 0),
  new GameVector3(10, 100, 10),
  2,
);

// 发射箭
world.launchProjectile("minecraft:arrow", 0, 100, 0, 5, 105, 0, 3);
```

## 爆炸 / 音效 / 查询

全部 ⬆ MC 扩展。

### world.explode(x, y, z, power)

创建爆炸。

### world.explode(pos, power)

⬆ GameVector3 重载。

### world.explode(x, y, z, power, fire)

创建爆炸（`fire=true` 可引燃方块）。

### world.explode(pos, power, fire)

⬆ GameVector3 重载。

```js
world.explode(0, 100, 0, 4); // 威力 4，不引火
world.explode(new GameVector3(0, 100, 0), 8, true); // 威力 8，引火
```

### world.playSound(path, x, y, z, volume, pitch)

在坐标播放音效给所有在线玩家。`path` 为音效命名空间 ID，`volume` 0–1，`pitch` 0.5–2.0。

### world.playSound(path, pos, volume, pitch)

⬆ GameVector3 重载。

```js
world.playSound("minecraft:block.note_block.pling", 0, 100, 0, 1.0, 1.5);
world.playSound(
  "minecraft:block.note_block.pling",
  new GameVector3(0, 100, 0),
  1.0,
  1.5,
);
```

### world.raycast(origin, direction)

从 `origin` 向 `direction` 发射射线，默认最大距离 5 格。

### world.raycast(origin, direction, maxDistance)

自定义最大距离的射线检测。

**返回值：** `{hit, x, y, z, normalX, normalY, normalZ, distance, entity, voxel}`

```js
var dir = new GameVector3(0, -1, 0);
var result = world.raycast(playerEntity.position, dir, 50);
if (result.hit) {
  console.log("命中方块:", result.voxel, "距离:", result.distance);
  if (result.entity) {
    console.log("命中实体:", result.entity.entityType);
  }
}
```

### world.entitiesInArea(pos1, pos2)

返回 AABB 包围盒内所有实体。

### world.entitiesInRadius(x, y, z, radius)

⬆ MC 扩展 | 返回球体范围内所有实体。`entitiesInArea` 的便捷封装。

### world.entitiesInRadius(pos, radius)

⬆ GameVector3 重载。

```js
// 查找 10 格半径内的所有实体
var nearby = world.entitiesInRadius(0, 100, 0, 10);
var nearby = world.entitiesInRadius(entity.position, 10);
for (var i = 0; i < nearby.length; i++) {
  console.log(nearby[i].entityType);
}
```

### world.getBiome(x, y, z)

⬆ MC 扩展 | 返回生物群系的命名空间 ID 字符串。

### world.getBiome(pos)

⬆ GameVector3 重载。

```js
var biome = world.getBiome(0, 70, 0);
console.log(biome); // "minecraft:plains"
var biome = world.getBiome(entity.position);
```

## 跨脚本消息

### world.sendMessage(target, data)

⬆ MC 扩展 | 发送消息给其他脚本项目。`target` 为 `"*"`（广播）或项目名。接收方用 `world.onMessage()` 监听。

### world.runCommand(cmd)

⬆ MC 扩展 | 以服务器控制台身份执行命令。

```js
world.runCommand("time set day");
world.runCommand("weather clear");
```
