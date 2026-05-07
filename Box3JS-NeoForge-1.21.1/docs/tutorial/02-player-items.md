# 教程二：玩家与物品

本教程涵盖玩家属性操作、物品给予和背包管理。

## 2.1 玩家基本信息

```js
world.onPlayerJoin((entity, tick) => {
  const p = entity.player;

  // 只读属性
  console.log("玩家名: " + p.name);
  console.log("UUID: " + p.userId);

  // entity 上的属性也可以通过 p 访问
  console.log("血量: " + p.hp + "/" + p.maxHp);
  console.log("位置: " + p.position);

  // 生命值
  p.maxHp = 40; // 设置最大血量 (20 = 默认)
  p.hp = 40;    // 回满血

  // 饱食度
  p.food = 20;
  p.saturation = 10;
});
```

## 2.2 移动控制

```js
// 速度 (默认 walkSpeed ≈ 0.1)
p.walkSpeed = 0.2;   // 走路翻倍
p.runSpeed = 0.26;   // 跑步翻倍 (自动维持 walkSpeed × 1.3)
p.jumpPower = 0.6;   // 跳得更高 (默认约 0.42)
p.swimSpeed = 0.3;   // 游泳更快

// 禁止跳跃
p.enableJump = false;
// p.enableJump = true; // 恢复

// 飞行
p.canFly = true;
p.flying = true;
p.flySpeed = 0.15;

// 传送
p.teleport(new GameVector3(0, 100, 0));
```

## 2.3 游戏模式与维度

```js
p.gameMode = "creative";  // survival / creative / adventure / spectator
p.gameMode = 1;           // 也可以用数字: 0=生存, 1=创造, 2=冒险, 3=旁观

// 跨维度传送
p.dimension = "minecraft:the_nether";    // 地狱
p.teleport(new GameVector3(0, 70, 0));
p.dimension = "minecraft:overworld";     // 主世界
p.dimension = "minecraft:the_end";       // 末地
```

## 2.4 经验与音效

```js
p.xp = 10;                   // 设为 10 级
p.addExperienceLevels(3);    // 加 3 级

// 播放音效 (仅该玩家听到)
p.playSound("minecraft:block.note_block.pling", 1.0, 1.5);
// 参数: (音效ID, 音量 0-1, 音高 0.5-2)

// 常用音效:
// minecraft:block.note_block.pling      铃铛
// minecraft:entity.experience_orb.pickup 经验球
// minecraft:entity.player.levelup       升级
// minecraft:block.anvil.land            铁砧落地
// minecraft:entity.ender_dragon.growl   末影龙吼
```

## 2.5 给予物品

```js
// 基础物品
p.giveItem("minecraft:diamond_sword", 1);
p.giveItem("minecraft:golden_apple", 5);
p.giveItem("minecraft:arrow", 64);

// 带附魔的物品
p.giveEnchantedItem("minecraft:diamond_sword", 1, {
  "minecraft:sharpness": 5,
  "minecraft:fire_aspect": 2,
  "minecraft:unbreaking": 3,
});

// 带自定义名称和描述的物品
p.giveNamedItem("minecraft:diamond_sword", 1, "§c§l烈焰之刃", [
  "§7绑定: 火焰",
  "§e右键: 发射火球",
]);

p.giveNamedItem("minecraft:gold_ingot", 1, "§6§l通关金牌", [
  "§7天空跑酷锦标赛",
  "§e完赛时间: 1:23.450",
]);
```

## 2.6 物品栏操作

```js
// 获取手持物品
const held = p.getHeldItem();
if (held.id !== "minecraft:air") {
  p.directMessage("你手持: " + held.id + " x" + held.count);
}

// 清空背包 (包括盔甲和副手)
p.clearInventory();
```

## 2.7 自定义物品

自定义物品通过资源包 + JSON 配置实现，无需修改 Java 代码。

**第一步：** 在 `resourcepacks/mypack/items.json` 定义物品：

```json
{
  "base_item": "minecraft:paper",
  "items": {
    "magic_wand": {
      "minecraft:custom_model_data": 12001,
      "minecraft:custom_name": "§d§l魔法杖 §r§5★",
      "minecraft:lore": [
        "§7蕴藏着神秘力量的魔法杖",
        "",
        "§6稀有度: §5史诗"
      ],
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
// 之后就可以:
p.giveCustomItem("magic_wand", 1);
p.giveCustomItem("energy_drink", 8);
```

## 2.8 踢出与管理员

```js
// 踢出玩家
p.kick("你已被移出游戏");

// 权限等级
p.opLevel = 4;          // 最高权限 (等同 /op)
console.log(p.opLevel); // 0=普通, 1-4=管理员级别

// 以玩家身份执行命令
p.runCommand("say 大家好");
```

## 2.9 完整示例：新手礼包

```js
world.onPlayerJoin((entity, tick) => {
  const p = entity.player;

  // 欢迎标题
  p.title("§6§l欢迎来到服务器！", "§7准备开始冒险", 10, 60, 10);

  // 新手礼包
  p.giveItem("minecraft:stone_sword", 1);
  p.giveItem("minecraft:stone_pickaxe", 1);
  p.giveItem("minecraft:stone_axe", 1);
  p.giveItem("minecraft:stone_shovel", 1);
  p.giveItem("minecraft:bread", 16);
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

教程三将介绍事件系统：方块交互、实体交互、伤害/死亡事件等。
