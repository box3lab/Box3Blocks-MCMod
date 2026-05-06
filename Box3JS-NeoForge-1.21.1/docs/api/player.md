# player — 玩家 API

`player` 对象通过 `entity.player` 获取，代表登录的玩家。它包含 `entity` 的全部能力，并额外提供玩家专属功能：背包、经验、飞行、消息、传送等。

```js
world.onPlayerJoin((entity) => {
  var p = entity.player; // p 即为 player 对象
  p.directMessage("欢迎回来, " + p.name + "!");
});
```

## 基本信息

### player.name

✅ Box3 API | 只读。玩家名称。

### player.userId

✅ Box3 API | 只读。玩家 UUID 字符串。

### player.getOpLevel()

⬆ MC 扩展 | 获取/设置玩家管理员权限等级 (0-4)。0=普通玩家, 1=可绕过出生点保护, 2=可使用大部分命令, 3=可管理玩家, 4=最高权限。

```js
if (player.getOpLevel() >= 2) {
  // 需要权限等级 2 的操作
}
player.opLevel = 3; // 设置为 3 级权限
```

## 外观

### player.invisible

✅ Box3 API | 获取/设置玩家是否隐形。

### player.scale

✅ Box3 API | 只读。玩家缩放值。

```js
player.invisible = true; // 隐形
```

## 移动

全部 ✅ Box3 API。

### player.walkSpeed

步行速度，对应 `MOVEMENT_SPEED` 属性。默认值约 0.1。

### player.runSpeed

奔跑速度。`walkSpeed × 1.3` 的关系自动保持。

### player.jumpPower

跳跃力度，对应 `JUMP_STRENGTH` 属性。

### player.moveState

只读。当前移动状态：`"FLYING"`、`"SWIM"`、`"JUMP"`、`"FALL"`、`"GROUND"`。

### player.walkState

只读。当前行走状态：`"CROUCH"`、`"RUN"`、`"WALK"`、`"NONE"`。

```js
player.walkSpeed = 0.2; // 加速
player.jumpPower = 0.6; // 跳更高

world.onTick(() => {
  if (player.walkState === "RUN") {
    // 玩家在奔跑
  }
});
```

## 飞行

### player.canFly

✅ Box3 API | 获取/设置飞行权限（`mayfly`）。设为 `true` 后玩家按跳跃键起飞。

### player.flying

✅ Box3 API | 获取/设置是否正在飞行（`flying`）。需要先设置 `canFly = true`。

### player.flySpeed

✅ Box3 API | 飞行速度。

### player.disableFly

✅ Box3 API | 设为 `true` 时立即停止飞行并禁用飞行权限。

### player.spectator

✅ Box3 API | 只读。玩家是否处于旁观模式。

```js
// 允许飞行
player.canFly = true;
player.flySpeed = 0.1;

// 让玩家起飞
player.flying = true;

// 强制落地
player.disableFly = true;
```

### player.collision

⬆ MC 扩展 | 获取/设置团队内碰撞。设为 `false` 可防止多人推搡。底层修改团队的 `CollisionRule`。

```js
player.collision = false; // 禁用碰撞
console.log(player.collision); // false
```

## 生命值

⬆ MC 扩展 | 获取/设置玩家血量。`ServerPlayer` 本身是 `LivingEntity`，直接操作健康值。

### player.hp

获取/设置当前生命值。

### player.maxHp

获取/设置最大生命值。

```js
// 设置职业血量
player.maxHp = 40; // 战士 40 HP
player.hp = 40; // 满血

// 设置后若当前血量超过新最大值会自动截断
player.maxHp = 20;
// player.hp 自动降到 20 封顶
```

## 游戏模式

### player.gameMode

✅ Box3 API | 获取/设置游戏模式。get 返回名称字符串，set 接受字符串或数字。

```js
player.gameMode = "creative"; // 创造模式
player.gameMode = "survival"; // 生存模式
player.gameMode = "adventure"; // 冒险模式
player.gameMode = "spectator"; // 旁观模式
// 或数字: 0=生存, 1=创造, 2=冒险, 3=旁观
```

## 相机

全部 ✅ Box3 API。

### player.cameraMode

获取/设置相机模式：`"FPS"`（第一人称）或 `"FOLLOW"`（跟随实体）。

### player.cameraEntity

设置或获取跟随的实体对象（`Box3JSEntity`）。

### player.cameraPitch / player.cameraYaw

相机的俯仰角和偏航角。

### player.facingDirection

只读 `GameVector3`。玩家视线方向单位向量。

### player.cameraTarget

只读 `GameVector3`。玩家视线前方 5 格的目标点。

### player.lookAt(x, y, z)

⬆ MC 扩展 | 让玩家看向指定坐标。

### player.lookAt(pos)

⬆ GameVector3 重载。

```js
player.lookAt(10, 100, 10);
player.lookAt(target.position);

// 获取视线方向
var dir = player.facingDirection;
var target = player.cameraTarget;
```

## 传送与重生

### player.teleport(pos)

✅ Box3 API | 传送玩家到指定 `GameVector3` 坐标。

### player.setRespawnPoint(pos)

✅ Box3 API | 设置玩家的重生点。

### player.respawn()

✅ Box3 API | 强制玩家重生（仅死亡状态有效）。

### player.dimension

⬆ MC 扩展 | 获取/设置玩家所在维度。set 可跨维度传送。

```js
player.teleport(new GameVector3(0, 100, 0));
player.setRespawnPoint(new GameVector3(0, 100, 0));

// 跨维度传送
player.dimension = "minecraft:the_nether";
player.teleport(new GameVector3(0, 70, 0));
```

## 踢出

### player.kick()

✅ Box3 API | 踢出玩家，默认提示 "Kicked"。

### player.kick(reason)

✅ Box3 API | 踢出玩家，自定义原因。

```js
player.kick("你已被移出游戏");
```

## 消息

### player.directMessage(msg)

✅ Box3 API | 向玩家发送聊天栏消息。

### player.actionBar(msg)

✅ Box3 API | 向玩家发送快捷栏上方消息（Action Bar）。

### player.title(title, subtitle)

✅ Box3 API | 向玩家发送屏幕标题。使用默认动画参数。

### player.title(title, subtitle, fadeIn, stay, fadeOut)

⬆ MC 扩展 | 完全参数的标题。`fadeIn`/`stay`/`fadeOut` 单位均为 tick。

### player.dialog(config)

✅ Box3 API | 弹出对话框。传入 `{content, options}` 配置，返回 `{index, value}`。目前 MC 中发送系统消息作为简化实现。

```js
var result = player.dialog({
  content: "选择你的道路",
  options: ["战士", "法师", "弓箭手"],
});
player.directMessage("你选择了: " + result.value);
```

### player.link(href)

✅ Box3 API | 向玩家发送可点击链接。

### player.onChat(handler)

✅ Box3 API | 为单个玩家注册聊天回调（更精细的控制，常用于对话树）。

```js
player.directMessage("你好！");
player.actionBar("§e按 !help 查看帮助");
player.title("§6§lBOSS战", "§7击败所有敌人", 10, 60, 10);
player.link("https://example.com");

// 对话树
player.directMessage("输入你的选择: A 或 B");
player.onChat((entity, msg, tick) => {
  if (msg === "A") {
    player.directMessage("你选择了 A");
  }
});
```

## 经验与饱食度

### player.xp

⬆ MC 扩展 | 获取/设置经验等级。

### player.addExperienceLevels(levels)

⬆ MC 扩展 | 增加 `levels` 级经验。

### player.food

⬆ MC 扩展 | 获取/设置饱食度（0–20）。

### player.saturation

⬆ MC 扩展 | 获取/设置饱和度（0–20，浮点数）。

```js
player.xp = 10; // 设置 10 级
player.addExperienceLevels(3); // 加 3 级
player.food = 20;
player.saturation = 10;
```

## 背包

全部 ⬆ MC 扩展。

### player.giveItem(itemId, count)

给予物品。

### player.clearInventory()

清空背包。

### player.getHeldItem()

获取主手物品，返回 `{id, count}`。空手返回 `{id: "minecraft:air", count: 0}`。

```js
player.giveItem("minecraft:diamond_sword", 1);
player.giveItem("minecraft:golden_apple", 5);
player.giveItem("minecraft:arrow", 64);

var held = player.getHeldItem();
console.log(held.id, held.count); // "minecraft:diamond_sword" 1

player.clearInventory();
```

### player.giveEnchantedItem(itemId, count, enchants)

给予附魔物品。`enchants` 是 `{附魔ID: 等级}` 对象。

```js
player.giveEnchantedItem("minecraft:diamond_sword", 1, {
  "minecraft:sharpness": 5,
  "minecraft:fire_aspect": 2,
  "minecraft:unbreaking": 3,
});

player.giveEnchantedItem("minecraft:bow", 1, {
  "minecraft:power": 5,
  "minecraft:punch": 2,
  "minecraft:infinity": 1,
});
```

### player.giveNamedItem(itemId, count, name, lore)

给予带自定义名称和描述的物品。`lore` 为字符串数组。

```js
player.giveNamedItem("minecraft:gold_ingot", 1, "§6§l跑酷金牌", [
  "§7天空跑酷锦标赛",
  "§e完赛时间: 1:23.450",
]);

player.giveNamedItem("minecraft:diamond_sword", 1, "§c§l烈焰之刃", [
  "§7绑定: 火焰",
  "§e右键: 发射火球",
]);
```

## 药水效果

### player.addEffect(effectId, duration, amplifier)

⬆ MC 扩展 | 添加药水效果。`duration` 为 tick，`amplifier` 从 0 开始。

### player.addEffect(effectId, duration, amplifier, hideParticles)

⬆ MC 扩展 | 添加效果并可选隐藏粒子。

### player.clearEffects()

⬆ MC 扩展 | 移除所有药水效果。

```js
player.addEffect("minecraft:speed", 600, 2);
player.addEffect("minecraft:jump_boost", 99999, 1, true); // 永久，无粒子
player.clearEffects();
```

## 音效与指令

### player.playSound(path, volume, pitch)

⬆ MC 扩展 | 向该玩家播放音效。`path` 为命名空间 ID。

### player.runCommand(cmd)

⬆ MC 扩展 | 以玩家身份执行命令。

```js
player.playSound("minecraft:block.note_block.pling", 0.8, 1.5);
player.runCommand("say hello");
```

## Tab 列表

### player.setPlayerListName(name)

⬆ MC 扩展 | 修改该玩家在 Tab 列表中显示的名字。

```js
player.setPlayerListName("§e[CP3] §f" + player.name);
player.setPlayerListName("§6★ §f" + player.name);

// 重置为原名
player.setPlayerListName(player.name);
```
