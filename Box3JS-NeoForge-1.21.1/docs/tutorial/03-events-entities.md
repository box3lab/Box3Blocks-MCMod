# 教程三：事件系统与实体操控

本教程深入讲解事件回调机制、实体生成与控制、以及计分板/队伍等游戏系统。

## 3.1 事件回调基础

所有事件通过 `world.onXxx(handler)` 注册，返回 `GameEventHandlerToken`。

```js
// 注册事件，拿到 token
const token = world.onTick((info) => {
  console.log("Tick: " + info.tick);
});

// 取消监听
token.cancel();

// 检查是否活跃
if (token.active()) {
  console.log("回调仍在运行");
}
```

### 事件一览

| 注册方法 | 回调参数 | 触发时机 |
|----------|---------|------|
| `world.onTick(fn)` | `(info)` | 每 tick (20次/秒) |
| `world.onPlayerJoin(fn)` | `(entity, tick)` | 玩家加入 |
| `world.onPlayerLeave(fn)` | `(entity, tick)` | 玩家离开 |
| `world.onChat(fn)` | `(entity, message, tick)` | 聊天消息 |
| `world.onBlockActivate(fn)` | `(entity, x, y, z, voxel, tick)` | 右键方块 |
| `world.onVoxelDestroy(fn)` | `(entity, x, y, z, voxel, tick)` | 破坏方块 |
| `world.onBlockPlace(fn)` | `(entity, x, y, z, voxel, voxelId, tick)` | 放置方块 |
| `world.onInteract(fn)` | `(entity, target, tick)` | 右键实体 |
| `world.onEntityDeath(fn)` | `(entity, killer, tick)` | 实体死亡 |
| `world.onEntityDamage(fn)` | `(entity, amount, source, attacker, tick)` | 实体受伤 |
| `world.onPlayerRespawn(fn)` | `(entity, tick)` | 玩家重生 |
| `world.onButtonPressed(fn)` | `(entity, button, tick)` | 按钮按下 |
| `world.onMessage(fn)` | `(from, data)` | 跨脚本消息 |

## 3.2 方块交互

```js
// 右键方块保护
world.onBlockActivate((entity, x, y, z, voxel, tick) => {
  const p = entity.player;
  if (voxel === "minecraft:chest" && p.opLevel < 2) {
    p.directMessage("§c你没有权限打开这个箱子！");
    // 注意: 右键方块事件无法阻止交互, 仅能检测
  }
});

// 记录破坏日志
world.onVoxelDestroy((entity, x, y, z, voxel, tick) => {
  console.log(entity.player.name + " 破坏了 " + voxel + " 在 (" + x + "," + y + "," + z + ")");
});

// 禁止放置特定方块
world.onBlockPlace((entity, x, y, z, voxel, voxelId, tick) => {
  if (voxel === "minecraft:tnt" && entity.player.opLevel < 2) {
    // 放置后用 voxels 替换为空气
    voxels.setVoxel(x, y, z, "minecraft:air");
    entity.player.directMessage("§c禁止放置TNT!");
  }
});
```

## 3.3 实体交互与战斗

```js
// 死亡奖励
world.onEntityDeath((entity, killer, tick) => {
  if (killer && killer.isPlayer()) {
    const p = killer.player;
    p.addExperienceLevels(1);
    p.actionBar("§e击杀 " + entity.entityType + "! +1 经验等级");

    // 掉落额外物品
    const pos = entity.position;
    world.dropItem(pos, "minecraft:diamond", 1);
  }
});

// 受伤日志
world.onEntityDamage((entity, amount, source, attacker, tick) => {
  if (attacker && attacker.isPlayer()) {
    const p = attacker.player;
    p.actionBar("§c造成 " + amount + " 点伤害");
  }
});

// 右键实体
world.onInteract((entity, target, tick) => {
  const p = entity.player;
  p.directMessage("§e你点击了: §f" + target.entityType);

  // 如果是村民，显示信息
  if (target.entityType === "minecraft:villager") {
    p.directMessage("§7这个村民看起来不想说话...");
  }
});
```

## 3.4 实体生成与属性

```js
// 生成僵尸
const zombie = world.spawnEntity("minecraft:zombie", new GameVector3(0, 100, 0));

// 自定义属性
zombie.setNameTag("§c§l精英僵尸");
zombie.maxHp = 60;
zombie.hp = 60;
zombie.walkSpeed = 0.3;

// 装备
zombie.setEquipment("mainhand", "minecraft:iron_sword");
zombie.setEquipment("head", "minecraft:iron_helmet");
// 槽位: mainhand / offhand / head(helmet/helm) / chest(chestplate) / legs(leggings) / feet(boots)

// 掉落概率
zombie.setDropChance("mainhand", 0.3);
zombie.setDropChance("all", 0); // 全部不掉落

// 效果
zombie.addEffect("minecraft:speed", 99999, 1); // 永久速度 II

// AI
zombie.setAI(true); // 启用寻路
```

### 使用完整配置生成

```js
const entity = world.createEntity({
  type: "minecraft:skeleton",
  position: new GameVector3(0, 100, 0),
  velocity: new GameVector3(0, 0.5, 0), // 向上弹射
  fixed: false,
  gravity: true,
  friction: 0.5,
  collides: true,
  hp: 40,
  maxHp: 40,
  tags: ["elite", "undead"],
});

entity.setEquipment("mainhand", "minecraft:bow");

// 设置攻击目标
entity.setTarget(somePlayerEntity);
entity.clearTarget();

// 让生物导航到指定位置
entity.navigateTo(10, 100, 10, 0.5);

// 设置死亡回调
entity.setOnDestroy((e) => {
  console.log("精英骷髅已被击败");
});
```

## 3.5 计分板

```js
// 创建计分板
world.addScoreboard("kills");
world.addScoreboard("deaths", "deathCount"); // 死亡计数 (自动统计)

// 设置分数
world.setScore("Steve", "kills", 5);
world.setScore(entity, "kills", 10); // 也可以用实体对象

// 读取
const kills = world.getScore("Steve", "kills");

// 显示在屏幕右侧
world.showScoreboard("sidebar", "kills");

// 显示在 Tab 列表
world.showScoreboard("list", "deaths");

// 列出所有分数
const scores = world.listScores("kills");
// [{name: "Steve", value: 5}, {name: "Alex", value: 3}, ...]

// 清除显示
world.hideScoreboard("sidebar");
world.removeScoreboard("kills");
```

### 实战：击杀计数

```js
world.addScoreboard("kills");
world.showScoreboard("sidebar", "kills");

world.onEntityDeath((entity, killer, tick) => {
  if (killer && killer.isPlayer()) {
    const p = killer.player;
    const current = world.getScore(p.name, "kills");
    world.setScore(p.name, "kills", current + 1);
    p.actionBar("§e击杀: §f" + (current + 1));
  }
});
```

## 3.6 队伍系统

```js
// 创建队伍
world.createTeam("red", "red");
world.createTeam("blue", "blue");

// 划分队伍
world.onPlayerJoin((entity, tick) => {
  const online = world.querySelectorAll("*").length;
  const team = online % 2 === 0 ? "red" : "blue";
  world.joinTeam(entity, team);
  entity.player.directMessage("§7你加入了 " + team + " 队");
});

// 获取队伍
const team = world.getTeamOf(entity);
console.log(team); // "red" 或 null

// 移出队伍
world.leaveTeam(entity);

// 删除队伍
world.removeTeam("red");
```

## 3.7 碰撞与标签

```js
// 实体碰撞
world.onEntityContact((entityA, entityB, tick) => {
  // 两个实体开始接触
  if (entityA.isPlayer() && entityB.entityType === "minecraft:zombie") {
    entityA.player.actionBar("§c小心僵尸！");
  }
});

world.onEntitySeparate((entityA, entityB, tick) => {
  // 两个实体分离
});

// 实体标签
entity.addTag("boss");
entity.removeTag("elite");
if (entity.hasTag("boss")) {
  // 特殊处理 Boss
}
const tags = entity.tags(); // ["boss", "undead"]
```

## 下一步

教程四将介绍高级游戏系统：BossBar、计时器、粒子/烟花/闪电、定时任务，以及一个完整的 PvP 小游戏示例。
