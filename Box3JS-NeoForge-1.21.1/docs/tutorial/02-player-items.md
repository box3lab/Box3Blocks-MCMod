# 教程二：玩家操控与物品

本教程涵盖玩家属性操作、传送、物品给予、药水效果、游戏模式等。

## 2.1 传送与飞行

```js
world.onChat((entity, message, _tick) => {
  const p = entity.player;

  // ── 随机传送 ──
  if (message === "!tp") {
    p.teleport(new GameVector3(
      (Math.random() - 0.5) * 100, 80, (Math.random() - 0.5) * 100
    ));
    p.directMessage("§a已随机传送！");
    p.playSound("minecraft:entity.enderman.teleport", 1.0, 1.0);
    return false;
  }

  // ── 飞行切换 ──
  if (message === "!fly") {
    p.canFly = !p.canFly;
    p.flying = p.canFly;
    p.directMessage(p.canFly ? "§a飞行模式: 开启" : "§7飞行模式: 关闭");
    p.playSound("minecraft:entity.experience_orb.pickup", 1.0, 1.0);
    return false;
  }
  return true;
});
```

### 移动属性

```js
p.walkSpeed = 0.25;   // 走路速度（默认 ~0.1）
p.runSpeed = 0.26;    // 疾跑速度
p.jumpPower = 0.6;    // 跳跃力度（默认 ~0.42）
p.swimSpeed = 0.3;    // 游泳速度
p.flySpeed = 0.15;    // 飞行速度
p.enableJump = false; // 禁止跳跃

// 传送
p.teleport(new GameVector3(0, 100, 0));
```

## 2.2 游戏模式

```js
p.gameMode = "creative";  // 创造
p.gameMode = "survival";   // 生存
p.gameMode = "adventure";  // 冒险
p.gameMode = "spectator";  // 旁观
p.gameMode = 1;            // 也可用数字: 0=生存, 1=创造, 2=冒险, 3=旁观

// 跨维度传送
p.dimension = "minecraft:the_nether";  // 地狱
p.teleport(new GameVector3(0, 70, 0));
p.dimension = "minecraft:overworld";   // 主世界
p.dimension = "minecraft:the_end";     // 末地
```

## 2.3 药水效果

```js
// 给玩家施加效果: (效果ID, 持续tick, 等级, 是否隐藏粒子)
p.addEffect("minecraft:speed", 600, 1, true);          // 30秒 速度II
p.addEffect("minecraft:jump_boost", 600, 1, true);     // 30秒 跳跃II
p.addEffect("minecraft:regeneration", 200, 1, true);   // 10秒 回复II
p.addEffect("minecraft:resistance", 200, 0, true);     // 10秒 抗性I
p.addEffect("minecraft:strength", 100, 1, true);       // 5秒 力量II
p.addEffect("minecraft:glowing", 200, 0, false);       // 10秒 发光（粒子可见）
p.addEffect("minecraft:invisibility", 200, 0, true);   // 10秒 隐身

// 清除所有效果
p.clearEffects();
```

实战：输入 `!buffs` 获得全套增益：

```js
world.onChat((entity, message, _tick) => {
  const p = entity.player;

  if (message === "!buffs") {
    p.addEffect("minecraft:speed", 600, 1, true);
    p.addEffect("minecraft:jump_boost", 600, 1, true);
    p.addEffect("minecraft:regeneration", 200, 1, true);
    p.addEffect("minecraft:resistance", 200, 0, true);
    p.directMessage("§d已施加增益效果！30秒速度+跳跃，10秒回复+抗性");
    p.playSound("minecraft:entity.witch.throw", 1.0, 1.2);
    return false;
  }
  return true;
});
```

## 2.4 生命值与饱食度

```js
p.hp = 20;           // 当前血量（10心）
p.maxHp = 40;        // 最大血量（20心）
p.food = 20;         // 饱食度
p.saturation = 10;   // 饱和度

// 一键恢复
p.hp = p.maxHp;
p.food = 20;
p.saturation = 10;
```

## 2.5 给予物品

```js
// 基础物品
p.giveItem("minecraft:diamond_sword", 1);
p.giveItem("minecraft:golden_apple", 8);
p.giveItem("minecraft:arrow", 64);

// 带附魔的物品
p.giveEnchantedItem("minecraft:diamond_sword", 1, {
  "minecraft:sharpness": 5,
  "minecraft:fire_aspect": 2,
  "minecraft:unbreaking": 3,
});

// 带自定义名称和描述的物品
p.giveNamedItem("minecraft:netherite_sword", 1, "§c§l烈焰之刃", [
  "§7绑定: 火焰之力",
  "§e被动: 攻击附带燃烧",
]);

p.giveNamedItem("minecraft:gold_ingot", 1, "§6§l通关金牌", [
  "§7挑战通关证明",
  "§7§o唯有强者才配拥有",
]);

// 获取手持物品
const held = p.getHeldItem();
if (held.id !== "minecraft:air") {
  p.directMessage(`你手持: ${held.id} x${held.count}`);
}

// 清空背包（包括盔甲和副手）
p.clearInventory();
```

实战：输入 `!kit` 获得全套装备：

```js
world.onChat((entity, message, _tick) => {
  const p = entity.player;

  if (message === "!kit") {
    p.clearInventory();
    p.giveItem("minecraft:diamond_sword", 1);
    p.giveItem("minecraft:diamond_pickaxe", 1);
    p.giveItem("minecraft:golden_apple", 8);
    p.giveItem("minecraft:arrow", 64);
    p.giveItem("minecraft:bow", 1);
    p.giveEnchantedItem("minecraft:diamond_sword", 1, {
      "minecraft:sharpness": 5,
      "minecraft:fire_aspect": 2,
      "minecraft:unbreaking": 3,
    });
    p.directMessage("§a装备已发放！");
    p.playSound("minecraft:entity.player.levelup", 1.0, 1.0);
    return false;
  }
  return true;
});
```

## 2.6 自定义物品

自定义物品通过资源包 + JSON 配置实现，无需修改 Java 代码。

**第一步：** 在 `resourcepacks/mypack/items.json` 定义物品：

```json
{
  "base_item": "minecraft:paper",
  "items": {
    "magic_wand": {
      "minecraft:custom_model_data": 12001,
      "minecraft:custom_name": "§d§l魔法杖 §r§5★",
      "minecraft:lore": ["§7蕴藏着神秘力量的魔法杖", "", "§6稀有度: §5史诗"],
      "minecraft:max_stack_size": 1,
      "minecraft:enchantment_glint_override": true,
      "minecraft:rarity": "epic"
    },
    "energy_drink": {
      "minecraft:custom_model_data": 12002,
      "minecraft:custom_name": "§b能量饮料",
      "minecraft:lore": ["§7恢复少量生命值", "§7§o咕噜咕噜..."],
      "minecraft:food": {
        "nutrition": 4,
        "saturation": 0.6,
        "can_always_eat": true,
        "eat_seconds": 0.8
      }
    }
  }
}
```

**第二步：** 准备资源包结构（贴图 + 模型 JSON）。

**第三步：** 在脚本中加载并给予：

```js
world.loadCustomItems("mypack");
p.giveCustomItem("magic_wand", 1);
p.giveCustomItem("energy_drink", 8);
```

## 2.7 经验、音效、标题

```js
// 经验值
p.xp = 10;                    // 设为 10 级
p.addExperienceLevels(5);     // 加 5 级

// 播放音效（仅该玩家听到）
p.playSound("minecraft:block.note_block.pling", 1.0, 1.5);
p.playSound("minecraft:entity.player.levelup", 1.0, 1.0);
p.playSound("minecraft:entity.ender_dragon.growl", 1.0, 0.8);

// 屏幕标题
p.title("§c§lBOSS 来袭", "§7远古巨龙 · 生命值 200/200", 10, 60, 10);
```

常用音效：
- `minecraft:block.note_block.pling` — 铃铛
- `minecraft:entity.experience_orb.pickup` — 经验球
- `minecraft:entity.player.levelup` — 升级
- `minecraft:entity.ender_dragon.growl` — 末影龙吼
- `minecraft:entity.witch.throw` — 药水投掷

## 2.8 踢出与管理

```js
p.kick("你已被移出游戏");

p.opLevel = 4;             // 最高权限（等同 /op）
console.log(p.opLevel);    // 0=普通, 1-4=管理员级别

p.runCommand("say 大家好"); // 以玩家身份执行命令
```

## 2.9 完整示例：新手大礼包

```js
world.onPlayerJoin((entity, _tick) => {
  const p = entity.player;

  // 欢迎标题 + 粒子
  p.title("§6§l欢迎来到服务器！", "§7准备开始冒险", 10, 60, 10);
  const pos = p.position;
  world.spawnParticleCircle(pos.x, pos.y, pos.z, 1.5, "minecraft:happy_villager", 20);
  world.playSound("minecraft:entity.player.levelup", pos, 1.0, 1.0);

  // 新手礼包
  p.giveItem("minecraft:stone_sword", 1);
  p.giveItem("minecraft:stone_pickaxe", 1);
  p.giveItem("minecraft:stone_axe", 1);
  p.giveItem("minecraft:stone_shovel", 1);
  p.giveItem("minecraft:bread", 32);
  p.giveItem("minecraft:torch", 16);

  // 命名特殊物品
  p.giveNamedItem("minecraft:shield", 1, "§b§l初心者之盾", [
    "§7只有真正的新手才能拥有的盾牌",
    "§7§o它看起来不太结实...",
  ]);

  p.directMessage("§a你已收到新手礼包！输入 !help 查看帮助");
});
```

## 下一步

教程三将介绍事件系统与实体操控：方块交互、实体生成、死亡事件、装备与 AI。
