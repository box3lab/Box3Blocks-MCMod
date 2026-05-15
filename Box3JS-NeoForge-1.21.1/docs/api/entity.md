# entity — 实体 API

`entity` 代表 Minecraft 世界中的任意实体（怪物、动物、掉落物、玩家）。

通过 `entity.player` 可获取该实体对应的 `player` 对象（仅当是玩家时非 null）。

## 基本身份

### entity.id

只读。实体的 UUID 字符串（如 `"550e8400-e29b-41d4-a716-446655440000"`）。

### entity.isPlayer()

返回 `true` 表示该实体是玩家。当返回 `true` 后，`entity.player` 必定非 null。

### entity.entityType

只读。返回实体的命名空间 ID 字符串（如 `"minecraft:zombie"`）。

```js
var all = world.querySelectorAll("*");
for (var i = 0; i < all.length; i++) {
  var e = all[i];
  console.log(e.id + " -> " + e.entityType + " -> isPlayer: " + e.isPlayer());
}
```

## 位置与移动

### entity.position

只读 `GameVector3`。这是一个 **LiveVec3**：读取时自动同步实体当前坐标，调用 `.set(x,y,z)` 可**直接传送实体**。

```js
var pos = entity.position;
console.log(pos.x, pos.y, pos.z);

// 传送实体
entity.position.set(0, 100, 0);
```

### entity.velocity

只读 `GameVector3`。**LiveVec3**：读取时自动同步当前速度，`.set(x,y,z)` 直接设置速度向量。

```js
entity.velocity.set(0, 1, 0); // 向上弹射
entity.velocity.set(2, 0, 2); // 水平方向速度
```

### entity.bounds

只读 `GameVector3`。实体的包围盒**半尺寸** (half-extents)：

- `x` = 宽度 / 2
- `y` = 高度 / 2
- `z` = 宽度 / 2

### entity.onGround

只读。实体是否站在方块上。

```js
if (entity.onGround) {
  // 在地面上
}
```

### entity.eyePosition

只读 `GameVector3`。实体视线高度位置（射线检测起点）。

```js
var eye = entity.eyePosition;
```

## 生命值

### entity.hp

获取/设置当前生命值。非 LivingEntity 返回/存储自定义属性。

### entity.maxHp

获取/设置最大生命值。设置后若当前生命超过新上限会自动截断。

```js
var zombie = world.spawnEntity("minecraft:zombie", new GameVector3(0, 100, 0));
zombie.maxHp = 100;
zombie.hp = 100;
```

### entity.hurt(amount)

对实体造成 `amount` 点伤害（通用伤害类型，触发伤害事件）。

### entity.heal(amount)

治疗实体 `amount` 点生命值（不超过 maxHp）。

```js
zombie.hurt(10); // 造成 10 点伤害
zombie.heal(5); // 治疗 5 点
```

### entity.invulnerable

获取/设置实体是否无敌（不受伤害）。

```js
entity.invulnerable = true;
console.log(entity.invulnerable);
```

### entity.destroyed

只读。实体是否已被移除/销毁。

## 物理属性

以下属性控制实体的物理行为。

### entity.collides

获取/设置实体是否参与碰撞。默认 `true`。设为 `false` 时对 LivingEntity 禁用物理 (setNoPhysics)。

```js
entity.collides = false; // 无碰撞幽灵
```

### entity.fixed

获取/设置实体是否固定。默认 `false`。设为 `true` 时禁用重力并每 tick 清零移动速度。

```js
entity.fixed = true; // 固定装饰物，不受重力不掉落
```

### entity.gravity

获取/设置实体是否受重力影响。默认 `true`。设为 `false` 时禁用重力 (setNoGravity)。

```js
entity.gravity = false; // 无重力漂浮
```

### entity.friction

获取/设置摩擦系数（自定义属性，默认 `0.0`）。脚本可读取此值自行处理摩擦逻辑。

### entity.mass

获取/设置质量（自定义属性，默认 `1.0`）。脚本可读取此值自行处理物理计算。

### entity.restitution

获取/设置弹性系数 / 反弹力（自定义属性，默认 `0.0`）。脚本可读取此值自行处理碰撞反弹。

```js
// 创建弹跳球
var ball = world.createEntity({
  type: "minecraft:slime",
  position: new GameVector3(0, 100, 0),
  gravity: true,
  collides: true,
  restitution: 0.8,
  mass: 0.5,
});
```

## 外观

### entity.meshInvisible

控制实体是否不可见（隐身）。

```js
entity.meshInvisible = true; // 隐身
console.log(entity.meshInvisible);
```

### entity.glowing

获取/设置发光效果（类似光灵箭轮廓高亮）。

```js
entity.glowing = true;
console.log(entity.glowing);
```

### entity.setGlowColor(color)

设置发光轮廓颜色。通过队伍颜色实现，映射 RGB 到最接近的 `ChatFormatting`（16 色）。

```js
entity.glowing = true;
entity.setGlowColor(new GameRGBColor(1, 0, 0)); // 红色发光
entity.setGlowColor(new GameRGBColor(0, 0, 1)); // 蓝色发光
```

### entity.setText(text)

设置文字展示实体的文本内容（仅 `minecraft:text_display` 实体有效）。

### entity.setTextColor(color)

设置文字展示实体的文本颜色。

### entity.setTextBackgroundColor(color)

设置文字展示实体的背景颜色，`GameRGBAColor` 可用于半透明背景。

```js
// 创建文字展示实体
var textEntity = world.createEntity("minecraft:text_display", pos);
textEntity.setText("Hello, World!");
textEntity.setTextColor(new GameRGBColor(1, 1, 1)); // 白色文字
textEntity.setTextBackgroundColor(new GameRGBAColor(0, 0, 0, 0.5)); // 半透明黑色背景
```

### entity.nameTag

获取/设置实体的自定义名称（头上显示的名字，支持颜色代码）。空字符串 = 无名称。

```js
entity.nameTag = "§cBoss 怪物";
console.log(entity.nameTag); // 属性方式读取
entity.setNameTag("§e守卫"); // 方法方式设置
```

## 标签系统

标签是附加在实体上的字符串标记（实质是 Minecraft 的 scoreboard tags），用于分类和查询。

### entity.addTag(tag)

添加标签。

### entity.hasTag(tag)

检查是否有指定标签。

### entity.removeTag(tag)

移除标签。

### entity.tags()

返回所有标签的字符串数组。

```js
entity.addTag("boss");
entity.addTag("red_team");

if (entity.hasTag("boss")) {
  entity.maxHp = 200;
}

// 获取全部标签
var allTags = entity.tags();
for (var i = 0; i < allTags.length; i++) {
  console.log(allTags[i]);
}

// 通过标签查询
var bosses = world.querySelectorAll(".boss");
```

## 火焰

### entity.setFire(ticks)

点燃实体指定 tick 数。20 ticks = 1 秒。

### entity.clearFire()

扑灭实体火焰。

```js
entity.setFire(100); // 点燃 5 秒
entity.clearFire(); // 立即扑灭
```

## AI 与导航

### entity.setAI(enabled)

启用/禁用实体 AI（仅 Mob 有效）。禁用后实体不会移动或攻击。

```js
entity.setAI(false); // 冻结实体
```

### entity.setTarget(target)

设置怪物的攻击目标（仅 Mob 有效）。怪物会自动寻路并攻击该目标。

### entity.getTarget()

获取当前攻击目标，返回 `GameEntity` 或 `null`。

### entity.clearTarget()

清除攻击目标，停止追击。

```js
var boss = world.spawnEntity("minecraft:skeleton", new GameVector3(0, 100, 0));
var target = world.querySelectorAll("*")[0];
boss.setTarget(target);
// ...
boss.clearTarget();
```

### entity.navigateTo(x, y, z, speed)

让实体寻路到目标坐标（仅 PathfinderMob 有效）。返回 `true` 表示路径计算成功。

### entity.navigateTo(pos, speed)

⬆ GameVector3 重载。

```js
entity.navigateTo(10, 100, 10, 1.0);
entity.navigateTo(target.position, 1.0);
```

### entity.lookAt(x, y, z)

实体面朝目标坐标。

### entity.lookAt(pos)

⬆ GameVector3 重载。

```js
entity.lookAt(0, 100, -10);
entity.lookAt(target.position);
```

## 药水效果

全部 ⬆ MC 扩展。

### entity.addEffect(effectId, duration, amplifier)

添加药水效果。`duration` 单位为 tick (20 tick = 1秒)，`amplifier` 从 0 开始（0 = 一级效果）。

### entity.addEffect(effectId, duration, amplifier, hideParticles)

添加效果并可选择隐藏粒子。

```js
entity.addEffect("minecraft:speed", 600, 2); // 速度 III，30 秒
entity.addEffect("minecraft:strength", 99999, 1, true); // 永久力量 II，无粒子
entity.addEffect("minecraft:glowing", 200, 0); // 发光 10 秒

// 常用效果:
// minecraft:speed, minecraft:slowness, minecraft:strength
// minecraft:weakness, minecraft:regeneration, minecraft:poison
// minecraft:jump_boost, minecraft:slow_falling, minecraft:invisibility
// minecraft:glowing, minecraft:levitation, minecraft:fire_resistance
```

## 装备

全部 ⬆ MC 扩展。

### entity.setEquipment(slot, itemId)

给生物穿戴装备。**slot 值：**

| slot                           | 说明 |
| ------------------------------ | ---- |
| `"mainhand"`                   | 主手 |
| `"offhand"`                    | 副手 |
| `"head"`, `"helmet"`, `"helm"` | 头盔 |
| `"chest"`, `"chestplate"`      | 胸甲 |
| `"legs"`, `"leggings"`         | 护腿 |
| `"feet"`, `"boots"`            | 靴子 |

```js
entity.setEquipment("mainhand", "minecraft:diamond_sword");
entity.setEquipment("head", "minecraft:iron_helmet");
entity.setEquipment("chest", "minecraft:iron_chestplate");
entity.setEquipment("feet", "minecraft:leather_boots");
```

### entity.setDropChance(slot, chance)

设置装备槽物品的掉落概率，范围 0.0–1.0。`slot` 设为 `"all"` 可一次性设置所有槽位（包括主副手和四个护甲槽）。

```js
entity.setDropChance("mainhand", 0.5); // 50% 概率掉落主手物品
entity.setDropChance("all", 0); // 不掉落任何装备
```

## 属性

全部 ⬆ MC 扩展。

### entity.getAttribute(attributeId)

获取实体属性当前值。非 LivingEntity 返回 0。

### entity.setAttribute(attributeId, value)

设置实体属性基值。仅 LivingEntity 有效。

```js
var attack = entity.getAttribute("minecraft:generic.attack_damage");
entity.setAttribute("minecraft:generic.attack_damage", 10);
entity.setAttribute("minecraft:generic.max_health", 100);
entity.setAttribute("minecraft:generic.movement_speed", 0.5);
entity.setAttribute("minecraft:generic.knockback_resistance", 1.0);
entity.setAttribute("minecraft:generic.armor", 10);
```

::: tip
`maxHp` / `hp` / `walkSpeed` / `jumpPower` 等 Box3 便捷属性内部也使用这些 attribute，推荐优先使用便捷属性。仅当需要访问未封装的属性时才用 `setAttribute`。
:::

## 生命周期

### entity.destroy()

销毁实体。如果通过 `setOnDestroy()` 设置了回调，会触发它。

### entity.setOnDestroy(handler)

设置销毁回调。`handler` 接收一个参数 `(entity)`。

```js
entity.setOnDestroy(function (e) {
  console.log("实体 " + e.id + " 被销毁");
});
```

### entity.setPersistent(v)

设为 `true` 时生物不会因远离玩家而自然消失（仅 Mob 有效）。仅写方法，无 getter。

```js
var boss = world.spawnEntity(
  "minecraft:wither_skeleton",
  new GameVector3(0, 100, 0),
);
boss.setPersistent(true); // 不会自然消失
boss.setNameTag("§c§l凋零守卫");
boss.setOnDestroy(function (e) {
  world.say("Boss 被击败了！");
});
```

## 自定义属性

可以直接在 entity 上存储任意 JS 数据，存活期等于实体生命周期。

自定义属性存储在 entity 的 UUID 下，通过 `ConcurrentHashMap` 持久化直到实体被移除。

```js
entity.myCustomField = "hello";
entity.spawnTick = world.currentTick();
entity.killCount = 0;

console.log(entity.myCustomField);
```
