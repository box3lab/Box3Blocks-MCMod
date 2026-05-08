# 教程三：事件系统与实体操控

本教程深入讲解事件回调、方块交互、实体生成与 AI、战斗事件等。

## 3.1 事件回调一览

所有事件通过 `world.onXxx(handler)` 注册，返回 `GameEventHandlerToken`。

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

### Token 操作

```js
const token = world.onTick((info) => {
  console.log("Tick: " + info.tick);
});

token.cancel();       // 取消监听
token.active();       // 检查是否活跃
```

## 3.2 方块交互事件

```js
// ── 右键方块检测 ──
world.onBlockActivate((entity, x, y, z, voxel, _tick) => {
  if (voxel === "minecraft:chest") {
    const p = entity.player;
    p.actionBar(`§e打开了箱子 @ (${x}, ${y}, ${z})`);
  }
  if (voxel === "minecraft:crafting_table") {
    entity.player.playSound("minecraft:block.wood.place", 0.5, 1.0);
  }
});

// ── 破坏记录 ──
world.onVoxelDestroy((entity, x, y, z, voxel, _tick) => {
  if (voxel !== "minecraft:air" && voxel !== "minecraft:grass_block") {
    console.log(`[Demo] ${entity.player.name} 破坏了 ${voxel} @ (${x},${y},${z})`);
  }
});

// ── 禁止放置 TNT ──
world.onBlockPlace((entity, x, y, z, voxel, _voxelId, _tick) => {
  if (voxel === "minecraft:tnt" && entity.player.opLevel < 2) {
    voxels.setVoxel(x, y, z, "minecraft:air");  // 替换为空气
    entity.player.directMessage("§c禁止放置 TNT！");
    entity.player.playSound("minecraft:block.note_block.bass", 1.0, 0.5);
  }
});
```

## 3.3 实体受伤与死亡

```js
// ── 死亡奖励 + Boss 特效 ──
world.onEntityDeath((entity, killer, _tick) => {
  if (killer?.isPlayer()) {
    const p = killer.player;
    const pos = entity.position;

    // 击杀粒子
    world.spawnParticle(
      "minecraft:angry_villager",
      pos.x, pos.y + 1, pos.z,
      10, 0.3, 0.3, 0.3, 0.05
    );

    // Boss 击杀特殊奖励
    if (entity.hasTag("boss")) {
      p.addExperienceLevels(5);
      world.dropItem(pos, "minecraft:diamond", 3);
      world.dropItem(pos, "minecraft:emerald", 5);
      world.say(
        `§6${p.name} §f击败了 §c${
          entity.nameTag || entity.entityType}§f！`
      );
      world.launchFirework(pos.x, pos.y + 2, pos.z, "gold", "large_ball");
    }
  }
});

// ── 受伤提示 ──
world.onEntityDamage((entity, amount, _source, attacker, _tick) => {
  if (attacker?.isPlayer()) {
    attacker.player.actionBar(
      `§c造成 ${amount} 点伤害 → ${entity.nameTag || entity.entityType}`
    );
  }
});
```

## 3.4 右键实体

```js
world.onInteract((entity, target, _tick) => {
  const p = entity.player;

  if (target.entityType === "minecraft:villager") {
    p.directMessage("§e这个村民正在忙，不想说话...");
    // 愤怒粒子
    world.spawnParticle(
      "minecraft:angry_villager",
      target.position.x, target.position.y + 2, target.position.z,
      3, 0.2, 0.2, 0.2, 0
    );
  }
});
```

## 3.5 实体生成与配置

```js
// ── 生成精英僵尸 ──
const boss = world.spawnEntity(
  "minecraft:zombie",
  new GameVector3(x, y, z)
);
if (!boss) return;  // spawnEntity 可能返回 null

boss.setNameTag("§c§l精英僵尸");
boss.maxHp = 100;
boss.hp = 100;
boss.addTag("boss");
boss.setAI(true);
boss.addEffect("minecraft:resistance", 99999, 0, true);
boss.addEffect("minecraft:speed", 99999, 1, true);

// 装备
boss.setEquipment("mainhand", "minecraft:iron_sword");
boss.setEquipment("head", "minecraft:iron_helmet");
// 槽位: mainhand / offhand / head / chest / legs / feet

boss.setDropChance("mainhand", 0.3);  // 30% 掉落手持物品
boss.setDropChance("all", 0);          // 全部不掉落
```

### 使用完整配置生成

```js
const entity = world.createEntity({
  type: "minecraft:skeleton",
  position: new GameVector3(0, 100, 0),
  velocity: new GameVector3(0, 0.5, 0),
  fixed: false,
  gravity: true,
  friction: 0.5,
  collides: true,
  hp: 40,
  maxHp: 40,
  tags: ["elite", "undead"],
});

entity.setEquipment("mainhand", "minecraft:bow");
entity.setTarget(somePlayerEntity);   // 设置攻击目标
entity.clearTarget();                 // 清除目标
entity.navigateTo(10, 100, 10, 0.5); // 导航到指定位置
entity.setPersistent(true);           // 持久化（不会被卸载）

// 死亡回调
entity.setOnDestroy(() => {
  console.log("精英骷髅已被击败");
});
```

## 3.6 巡逻守卫（完整实战）

以下代码生成一个在四个路点之间巡逻、遇到玩家自动攻击的骷髅守卫：

```js
function createPatrol(
  name: string,
  startPos: GameVector3,
  waypoints: GameVector3[],
  speed: number
): GameEntity | null {
  const guard = world.spawnEntity("minecraft:skeleton", startPos);
  if (!guard) { return null; }

  guard.setNameTag(name);
  guard.maxHp = 50;
  guard.hp = 50;
  guard.setEquipment("mainhand", "minecraft:bow");
  guard.setEquipment("head", "minecraft:iron_helmet");
  guard.setAI(true);

  let wpIndex = 0;
  const tid = world.setInterval(() => {
    if (guard.destroyed) {
      world.clearInterval(tid);
      return;
    }
    // 到达当前路点 → 下一个
    const wp = waypoints[wpIndex];
    const pos = guard.position;
    const dx = pos.x - wp.x;
    const dy = pos.y - wp.y;
    const dz = pos.z - wp.z;
    const dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
    if (dist < 2) {
      wpIndex = (wpIndex + 1) % waypoints.length;
    }
    guard.navigateTo(
      waypoints[wpIndex].x, waypoints[wpIndex].y, waypoints[wpIndex].z,
      speed
    );
    // 附近有玩家就攻击
    const nearby = world.entitiesInRadius(pos, 8);
    nearby.forEach((e) => {
      if (e.isPlayer() && !guard.getTarget()) {
        guard.setTarget(e);
      }
    });
  }, 40);  // 每 2 秒更新一次导航

  return guard;
}

// 用法：
const route = [
  new GameVector3(0, 70, 0),
  new GameVector3(10, 70, 0),
  new GameVector3(10, 70, 10),
  new GameVector3(0, 70, 10),
];
void createPatrol("§e巡逻守卫", route[0], route, 0.8);
```

## 3.7 实体标签与碰撞

```js
entity.addTag("boss");
entity.removeTag("elite");
if (entity.hasTag("boss")) {
  // 特殊处理 Boss
}
const tags = entity.tags();  // ["boss", "undead"]

// 实体碰撞
world.onEntityContact((entityA, entityB, tick) => {
  if (entityA.isPlayer() && entityB.hasTag("boss")) {
    entityA.player.actionBar("§c小心 Boss！");
  }
});

world.onEntitySeparate((entityA, entityB, tick) => {
  // 两个实体分离
});
```

## 3.8 常用实体类型

```
minecraft:zombie      僵尸
minecraft:skeleton    骷髅
minecraft:creeper     苦力怕
minecraft:spider      蜘蛛
minecraft:witch       女巫
minecraft:villager    村民
minecraft:iron_golem  铁傀儡
minecraft:slime       史莱姆
minecraft:wither      凋零
minecraft:ender_dragon 末影龙
minecraft:area_effect_cloud  效果云（常用于固定位置标记）
```

## 下一步

教程四将介绍高级游戏系统：计分板、BossBar、队伍、世界边界、跨脚本通信。
