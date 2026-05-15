# player — 玩家 API

`player` 通过 `entity.player` 获取，拥有 `entity` 的全部属性并额外提供背包、经验、飞行、消息、传送等玩家专属功能。

```js
world.onPlayerJoin(function (entity, tick) {
  var p = entity.player; // p 即为 player 对象
  p.directMessage("欢迎回来, " + p.name + "!");
});
```

## 基本信息

### player.name

只读。玩家名称。

### player.userId

只读。玩家 UUID 字符串（与 `entity.id` 相同）。

### player.opLevel

获取/设置玩家管理员权限等级 (0–4)。

| 等级 | 说明                    |
| ---- | ----------------------- |
| 0    | 普通玩家                |
| 1    | 可绕过出生点保护        |
| 2    | 可使用大部分命令        |
| 3    | 可管理玩家              |
| 4    | 最高权限 (等同于 `/op`) |

```js
if (player.opLevel >= 2) {
  // 需要权限等级 2 的操作
}
player.opLevel = 3; // 属性方式设置为 3 级
```

另有 `player.getOpLevel()` 方法返回权限等级数字。

## 外观

### player.invisible

获取/设置玩家是否隐形。

### player.scale

只读。玩家模型缩放比例（MC 原生 scale，非 Box3 scale）。

```js
player.invisible = true; // 隐形
console.log("玩家缩放: " + player.scale);
```

## 移动

### player.position

只读引用。玩家当前世界坐标，可通过 `player.position.set(x, y, z)` 修改向量值；如需传送玩家，优先使用 `player.teleport(pos)`。

### player.velocity

只读引用。玩家当前速度向量，可通过 `.set()` 修改。

### player.bounds

只读。玩家包围盒半尺寸。

### player.onGround

只读。玩家当前是否站在方块上。

### player.walkSpeed

步行速度，对应 `MOVEMENT_SPEED` 属性基值。默认值约 0.1。

### player.runSpeed

奔跑速度。get 返回 `walkSpeed × 1.3`，set 自动反算 `walkSpeed`，保持 1.3 倍比例关系。

### player.jumpPower

跳跃力度，对应 `JUMP_STRENGTH` 属性基值。默认值约 0.42。

### player.enableJump

获取/设置是否允许跳跃。默认 `true`。设为 `false` 时保存当前跳跃力并将 `JUMP_STRENGTH` 设为 0；设回 `true` 时恢复。

```js
player.enableJump = false; // 禁止跳跃
player.enableJump = true; // 恢复跳跃
```

### player.crouchSpeed

获取/设置潜行速度（自定义属性，默认 `0.0`）。MC 无独立潜行速度属性，脚本可读取此值自行实现。

### player.swimSpeed

获取/设置游泳速度。底层映射到 `WATER_MOVEMENT_EFFICIENCY` 属性。

```js
player.swimSpeed = 0.5; // 游泳更快
```

### player.moveState

只读。当前移动状态字符串：

| 值         | 说明     |
| ---------- | -------- |
| `"FLYING"` | 正在飞行 |
| `"SWIM"`   | 在水中   |
| `"JUMP"`   | 向上跳跃 |
| `"FALL"`   | 下落中   |
| `"GROUND"` | 在地面上 |

### player.walkState

只读。当前行走状态字符串：

| 值         | 说明   |
| ---------- | ------ |
| `"CROUCH"` | 潜行中 |
| `"RUN"`    | 奔跑中 |
| `"WALK"`   | 行走中 |
| `"NONE"`   | 静止   |

```js
player.walkSpeed = 0.2; // 加速
player.jumpPower = 0.6; // 跳更高
player.swimSpeed = 0.3; // 游泳速度

world.onTick(function () {
  if (player.walkState === "RUN") {
    // 玩家在奔跑
  }
});
```

## 飞行

### player.canFly

获取/设置飞行权限（`mayfly`）。设为 `true` 后玩家按跳跃键起飞。

### player.flying

获取/设置是否正在飞行（`flying`）。需要先设置 `canFly = true`。

### player.flySpeed

飞行速度。

### player.disableFly

设为 `true` 时立即停止飞行并禁用飞行权限。

### player.spectator

只读。玩家是否处于旁观模式。

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

获取/设置团队内碰撞。设为 `false` 可防止多人推搡。底层修改玩家所在队伍的 `CollisionRule`（ALWAYS / NEVER）。

```js
player.collision = false; // 禁用碰撞
console.log(player.collision);
```

## 生命值

获取/设置玩家血量。`ServerPlayer` 本身是 `LivingEntity`，直接操作健康值。

### player.hp

获取/设置当前生命值。

### player.maxHp

获取/设置最大生命值。

### player.dead

只读。玩家是否已死亡或正在死亡中（`isDeadOrDying()`）。

```js
// 设置职业血量
player.maxHp = 40; // 战士 40 HP
player.hp = 40; // 满血

// 设置后若当前血量超过新最大值会自动截断
player.maxHp = 20;
// player.hp 自动降到 20 封顶

if (player.dead) {
  console.log("玩家已死亡");
}
```

## 游戏模式

### player.gameMode

获取/设置游戏模式。get 返回名称字符串，set 接受字符串或数字。

```js
player.gameMode = "creative"; // 创造模式
player.gameMode = "survival"; // 生存模式
player.gameMode = "adventure"; // 冒险模式
player.gameMode = "spectator"; // 旁观模式
// 或数字: 0=生存, 1=创造, 2=冒险, 3=旁观
```

## 相机

### player.cameraMode

获取/设置相机模式：`"FPS"`（第一人称）或 `"FOLLOW"`（跟随实体）。设为 `"FPS"` 时自动清除跟随目标。

### player.cameraEntity

设置或获取跟随的实体对象（`GameEntity`）。设为实体时相机模式自动切换为 `"FOLLOW"`，设为 `null` 时切换回 `"FPS"`。

### player.cameraPitch / player.cameraYaw

相机的俯仰角（pitch）和偏航角（yaw）。注意：MC 中 yaw 对应 Y 旋转角 (xRot)，pitch 对应 X 旋转角 (yRot)。

### player.facingDirection

只读 `GameVector3`。玩家视线方向的单位向量。

### player.cameraTarget

只读 `GameVector3`。玩家视线前方 5 格的目标点。

### player.lookAt(x, y, z)

让玩家看向指定坐标。

### player.lookAt(pos)

⬆ GameVector3 重载。

```js
player.lookAt(10, 100, 10);
player.lookAt(target.position);

// 获取视线信息
var dir = player.facingDirection;
var target = player.cameraTarget;
```

## 传送与重生

### player.teleport(pos)

传送玩家到指定 `GameVector3` 坐标。

### player.spawnPoint

获取/设置玩家的重生点坐标 (`GameVector3`)。读取时若玩家未设置重生点，返回世界出生点。

```js
// 属性方式设置
player.spawnPoint = new GameVector3(0, 100, 0);
console.log(player.spawnPoint);
```

### player.setRespawnPoint(pos)

设置玩家重生点（方法方式，与 `spawnPoint` 属性等价）。

### player.setSpawnPoint(pos)

同 `setRespawnPoint`，Box3 标准命名。

### player.respawn()

强制玩家重生（仅死亡状态有效）。

### player.dimension

获取/设置玩家所在维度。set 可跨维度传送。

```js
player.teleport(new GameVector3(0, 100, 0));
player.setRespawnPoint(new GameVector3(0, 100, 0));

// 跨维度传送
player.dimension = "minecraft:the_nether";
player.teleport(new GameVector3(0, 70, 0));
```

## 踢出

### player.kick()

踢出玩家，默认提示 "Kicked"。

### player.kick(reason)

踢出玩家，自定义踢出原因。

```js
player.kick("你已被移出游戏");
```

## 消息

### player.directMessage(msg)

向玩家发送聊天栏消息（仅该玩家可见的系统消息）。

### player.directMessage(msg, color)

发送带颜色的聊天消息。

```js
player.directMessage("操作成功!", new GameRGBColor(0, 1, 0)); // 绿色
player.directMessage("警告!", new GameRGBColor(1, 0.5, 0)); // 橙色
```

### player.actionBar(msg)

向玩家发送快捷栏上方消息（Action Bar）。

### player.title(title, subtitle)

向玩家发送屏幕标题。使用默认动画参数：淡入 10 tick、停留 70 tick、淡出 20 tick。

### player.title(title, subtitle, fadeIn, stay, fadeOut)

完全参数的标题。`fadeIn`/`stay`/`fadeOut` 单位均为 tick (20 tick = 1秒)。

### player.dialog(config)

弹出对话框。传入 `{content, options}` 配置，返回 `{index, value}`。目前 MC 中发送系统消息作为简化实现。

```js
var result = player.dialog({
  content: "选择你的道路",
  options: ["战士", "法师", "弓箭手"],
});
player.directMessage("你选择了: " + result.value);
```

### player.link(href)

向玩家发送可点击的 URL 链接（蓝色下划线）。

### player.onChat(handler)

为单个玩家注册聊天回调（比全局 `world.onChat` 更精细的控制，常用于对话树）。

```js
player.directMessage("你好！");
player.actionBar("§e按 !help 查看帮助");
player.title("§6§lBOSS战", "§7击败所有敌人", 10, 60, 10);
player.link("https://example.com");

// 对话树
player.directMessage("输入你的选择: A 或 B");
player.onChat(function (entity, msg, tick) {
  if (msg === "A") {
    player.directMessage("你选择了 A");
  }
});
```

## 经验与饱食度

### player.xp

获取/设置经验等级。

### player.addExperienceLevels(levels)

增加 `levels` 级经验。

### player.food

获取/设置饱食度（0–20）。

### player.saturation

获取/设置饱和度（0–20，浮点数）。

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

### player.giveEnchantedItem(itemId, count, enchants)

给予附魔物品。`enchants` 是 `{附魔ID: 等级}` 对象。

```js
player.giveItem("minecraft:diamond_sword", 1);
player.giveItem("minecraft:golden_apple", 5);
player.giveItem("minecraft:arrow", 64);

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

给予带自定义名称和描述的物品。`lore` 为字符串数组，每项一行描述文字。

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

### player.getHeldItem()

获取主手物品，返回 `{id, count}`。空手返回 `{id: "minecraft:air", count: 0}`。

```js
var held = player.getHeldItem();
console.log(held.id, held.count); // "minecraft:diamond_sword" 1
```

### player.clearInventory()

清空背包（包括盔甲槽和副手）。

```js
player.clearInventory();
```

## 自定义容器 GUI

为玩家打开脚本控制的容器 GUI（类似箱子界面），可自定义格子内容、点击行为和关闭回调。

### player.openGUI(config?)

打开一个容器 GUI 并返回 `GUIController` 控制器对象。

| 参数    | 类型                         | 默认值        | 说明                                        |
| ------- | ---------------------------- | ------------- | ------------------------------------------- |
| `title` | `string`                     | `"Container"` | 容器标题                                    |
| `rows`  | `number`                     | `3`           | 行数 (1–6)，每行 9 格                       |
| `slots` | `{ [slot: number]: string }` | `{}`          | 预填充物品，key 为格子索引，value 为物品 ID |

**返回值 `GUIController` 方法：**

| 方法                            | 说明                                    |
| ------------------------------- | --------------------------------------- |
| `setItem(slot, itemId, count?)` | 在指定格子放置物品                      |
| `getItem(slot)`                 | 获取格子物品，返回 `{ id, count }`      |
| `onSlotClick(callback)`         | 注册点击回调，`return false` 可取消点击 |
| `onClose(callback)`             | 注册关闭回调（ESC 或 `close()` 触发）   |
| `close()`                       | 关闭容器                                |

```js
world.onChat(function (entity, msg, tick) {
  if (msg === "!shop") {
    var gui = entity.player.openGUI({
      title: "§6§l商店",
      rows: 3,
      slots: {
        0: "minecraft:diamond",
        4: "minecraft:emerald",
        8: "minecraft:gold_ingot",
      },
    });

    gui.setItem(1, "minecraft:netherite_ingot", 5);

    gui.onSlotClick(function (slot, player) {
      console.log("点击格子: " + slot);
      if (slot === 0) return false; // 禁止拿走钻石
    });

    gui.onClose(function (player) {
      player.directMessage("商店已关闭");
    });
  }

  if (msg === "!closegui") {
    // 也可通过 controller 编程关闭
    // (需要将 controller 保存在外部作用域)
  }
});
```

## 药水效果

### player.addEffect(effectId, duration, amplifier)

添加药水效果。`duration` 为 tick，`amplifier` 从 0 开始。

### player.addEffect(effectId, duration, amplifier, hideParticles)

添加效果并可选择隐藏粒子。

### player.clearEffects()

移除所有药水效果。

```js
player.addEffect("minecraft:speed", 600, 2);
player.addEffect("minecraft:jump_boost", 99999, 1, true); // 永久，无粒子
player.clearEffects();
```

## 音效与指令

### player.playSound(path, volume, pitch)

向该玩家单独播放音效。`path` 为命名空间 ID（如 `"minecraft:block.note_block.pling"`），`volume` 0–1，`pitch` 0.5–2。

### player.runCommand(cmd)

以该玩家身份执行 Minecraft 命令。

```js
player.playSound("minecraft:block.note_block.pling", 0.8, 1.5);
player.runCommand("say hello");
```

## 成就

### player.grantAdvancement(advancementId)

为该玩家授予成就/进度。

### player.revokeAdvancement(advancementId)

撤销该玩家的成就/进度。

```js
player.grantAdvancement("minecraft:story/mine_stone");
player.grantAdvancement("minecraft:adventure/kill_a_mob");
player.revokeAdvancement("minecraft:story/mine_stone");
```

## Tab 列表

### player.setPlayerListName(name)

修改该玩家在 Tab 列表中显示的名字（支持颜色代码）。

```js
player.setPlayerListName("§e[CP3] §f" + player.name);
player.setPlayerListName("§6★ §f" + player.name);

// 重置为原名
player.setPlayerListName(player.name);
```
