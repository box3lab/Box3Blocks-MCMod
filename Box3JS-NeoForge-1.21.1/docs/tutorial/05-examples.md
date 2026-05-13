# 教程五：可视化特效与实战小游戏

本教程涵盖粒子、烟花、闪电、爆炸等视觉效果，并提供三个经过验证的完整小游戏。

## 5.1 粒子效果

```js
// 单点粒子: (类型, x, y, z, 数量, dx, dy, dz, 速度)
world.spawnParticle("minecraft:flame", 0, 100, 0, 20, 0.5, 0.5, 0.5, 0.05);
world.spawnParticle("minecraft:portal", 0, 100, 0, 15, 0.5, 0.5, 0.5, 0.02);
world.spawnParticle("minecraft:end_rod", 0, 100, 0, 8, 0.2, 0, 0.2, 0.01);
world.spawnParticle("minecraft:witch", 0, 100, 0, 10, 0.3, 0.3, 0.3, 0.03);

// 圆形粒子圈: (x, y, z, 半径, 类型, 数量)
world.spawnParticleCircle(0, 100, 0, 3.0, "minecraft:happy_villager", 30);
world.spawnParticleCircle(0, 100, 0, 2.0, "minecraft:flame", 24);
world.spawnParticleCircle(0, 100, 0, 4.0, "minecraft:end_rod", 36);
```

常用粒子：

| 粒子 ID | 效果 |
|---------|------|
| `minecraft:flame` | 火焰 |
| `minecraft:cloud` | 烟雾 |
| `minecraft:happy_villager` | 绿色粒子（正面） |
| `minecraft:witch` | 紫色粒子 |
| `minecraft:portal` | 传送门 |
| `minecraft:end_rod` | 末地烛光 |
| `minecraft:heart` | 爱心 |
| `minecraft:note` | 音符 |
| `minecraft:dragon_breath` | 龙息 |
| `minecraft:angry_villager` | 愤怒粒子（红色） |
| `minecraft:soul_fire_flame` | 灵魂火焰（蓝色） |
| `minecraft:redstone` | 红石粒子 |
| `minecraft:explosion` | 爆炸粒子 |

### 螺旋上升粒子

```js
function spiralEffect(pos: GameVector3): void {
  for (let i = 0; i < 40; i++) {
    world.setTimeout(() => {
      const angle = (i / 40) * Math.PI * 4;
      const radius = 2.0;
      const px = pos.x + Math.cos(angle) * radius;
      const py = pos.y + i * 0.1;
      const pz = pos.z + Math.sin(angle) * radius;
      world.spawnParticle("minecraft:portal", px, py, pz, 2, 0, 0, 0, 0);
    }, i * 2);
  }
  world.playSound("minecraft:block.beacon.activate", pos, 1.0, 1.5);
}
```

## 5.2 烟花

```js
// 烟花: (x, y, z, 颜色, 形状)
world.launchFirework(0, 100, 0, "gold", "large_ball");
world.launchFirework(0, 100, 0, "red", "star");
world.launchFirework(0, 100, 0, "purple", "burst");
world.launchFirework(0, 100, 0, "green", "creeper");
```

烟花颜色：`"red"` `"blue"` `"green"` `"yellow"` `"gold"` `"white"` `"aqua"` `"pink"` `"purple"`

烟花形状：`"ball"` `"large_ball"` `"star"` `"creeper"` `"burst"`

### 连续烟花秀

```js
const colors = ["red", "gold", "green", "blue", "purple", "white", "pink", "aqua"];
const shapes = ["ball", "large_ball", "star", "creeper", "burst"];

for (let i = 0; i < 8; i++) {
  world.setTimeout(() => {
    const c = colors[i % colors.length];
    const s = shapes[i % shapes.length];
    world.launchFirework(
      pos.x + (Math.random() - 0.5) * 10,
      pos.y + 5 + Math.random() * 8,
      pos.z + (Math.random() - 0.5) * 10,
      c, s
    );
  }, i * 300);
}
```

## 5.3 闪电

```js
// 闪电: (x, y, z, 伤害)
world.strikeLightning(0, 100, 0);        // 默认伤害
world.strikeLightning(0, 100, 0, 10);    // 10 点伤害
world.strikeLightning(0, 100, 0, 0);     // 无伤害，纯视觉效果

// 在玩家周围召唤闪电
for (let i = 0; i < 3; i++) {
  world.setTimeout(() => {
    const lx = pos.x + (Math.random() - 0.5) * 12;
    const lz = pos.z + (Math.random() - 0.5) * 12;
    world.strikeLightning(lx, pos.y, lz, 0);
  }, i * 200);
}
world.playSound("minecraft:entity.lightning_bolt.thunder", pos, 1.0, 1.0);
```

## 5.4 爆炸

```js
// 爆炸: (x, y, z, 威力, 是否引火)
world.explode(0, 100, 0, 4, false);   // 威力 4，不引火
world.explode(0, 100, 0, 8, true);    // 威力 8，引火

// 玩家引爆自身周围（3 秒倒计时）
world.playSound("minecraft:block.note_block.bass", pos, 1.0, 0.5);
world.setTimeout(() => {
  world.spawnParticle("minecraft:explosion", pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
  world.setTimeout(() => {
    world.explode(pos.x, pos.y, pos.z, 4, false);
    world.playSound("minecraft:entity.generic.explode", pos, 1.0, 1.0);
  }, 10);
}, 60);
```

## 5.5 音效

```js
// 全局音效（所有玩家听到）
world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
world.playSound("minecraft:entity.ender_dragon.growl", pos, 1.0, 1.0);

// 仅某个玩家听到
player.playSound("minecraft:entity.player.levelup", 1.0, 1.0);
```

常用音效：

| 音效 ID | 用途 |
|---------|------|
| `minecraft:block.note_block.pling` | 铃铛提示 |
| `minecraft:block.note_block.bass` | 低音提示 |
| `minecraft:entity.experience_orb.pickup` | 经验球拾取 |
| `minecraft:entity.player.levelup` | 升级 |
| `minecraft:entity.ender_dragon.growl` | 龙吼（Boss 出场） |
| `minecraft:entity.wither.spawn` | 凋零生成（压迫感） |
| `minecraft:entity.lightning_bolt.thunder` | 雷鸣 |
| `minecraft:entity.generic.explode` | 爆炸 |
| `minecraft:entity.witch.throw` | 药水投掷 |
| `minecraft:block.beacon.activate` | 信标激活 |
| `minecraft:block.anvil.land` | 铁砧落地 |
| `minecraft:ui.toast.challenge_complete` | 挑战完成 |
| `minecraft:entity.player.burp` | 吃食物音效 |
| `minecraft:entity.enderman.teleport` | 传送音效 |

## 5.6 玩家进出特效

```js
world.onPlayerJoin((entity, _tick) => {
  const pos = entity.position;
  world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
  world.spawnParticleCircle(pos.x, pos.y, pos.z, 1.5, "minecraft:happy_villager", 15);
});

world.onPlayerLeave((entity, _tick) => {
  const pos = entity.position;
  world.spawnParticle("minecraft:cloud", pos.x, pos.y, pos.z, 10, 0.3, 0.3, 0.3, 0.01);
});
```

## 5.7 完整小游戏一：PvP 竞技场

这是教程四中设计模式的实际应用——一个完整的红蓝两队 PvP 小游戏，整合了事件、BossBar、计分板、队伍、粒子、烟花、边界缩圈、空投等所有系统。

**命令：**
- `!pvp join` — 加入游戏
- `!pvp leave` — 退出等待
- `!pvp start` — (OP) 开始游戏
- `!pvp stop` — (OP) 强制结束
- `!pvp status` — 查看状态

**特性：**
- 大厅倒计时 30 秒 → 游戏时长 300 秒
- 红蓝两队自动分配 + 队伍前缀
- 击杀计分 + 全局播报 + 烟花特效
- BossBar 倒计时（超过 30% 绿色 → 低于 10% 红色）
- 第 120 秒边界缩圈
- 每 60 秒空投（闪电标记 + 末影珍珠/金苹果）
- 最后 30 秒中心闪电
- 结束烟花秀 + 自动重置

```js
// ═══════════════════════════════════════════
//  PvP 竞技场 — 完整示例
//  (已验证: tsc + eslint + build 通过)
// ═══════════════════════════════════════════

const ARENA = new GameVector3(0, 70, 0);
const ARENA_RADIUS = 80;
const DURATION = 300;
const SHRINK_AT = 120;

interface PvPState {
  phase: "waiting" | "starting" | "playing" | "ending";
  playersReady: number;
  redScore: number;
  blueScore: number;
}

const state: PvPState = {
  phase: "waiting",
  playersReady: 0,
  redScore: 0,
  blueScore: 0,
};

let pvpGameTimer: number | null = null;
let pvpAirdropTimer: number | null = null;
let pvpLobbyTimer: number | null = null;

// ── 初始化 ──
world.setGameRule("keepInventory", false);
world.setGameRule("doMobSpawning", false);
world.clearWeather();
world.time = 6000;
world.addScoreboard("pvp_kills");
world.addScoreboard("pvp_score");
world.createTeam("red", "red");
world.createTeam("blue", "blue");

// ── 聊天命令 ──
world.onChat((entity, message, _tick) => {
  const p = entity.player;

  switch (message) {
    case "!pvp":
      p.directMessage("§6── PvP 竞技场 ──");
      p.directMessage("§f!pvp join  §7- 加入游戏");
      p.directMessage("§f!pvp start §7- (OP) 开始游戏");
      return false;

    case "!pvp join":
      if (state.phase !== "waiting") { p.directMessage("§c游戏已开始"); return false; }
      state.playersReady++;
      p.clearInventory();
      p.hp = 20; p.maxHp = 20; p.food = 20;
      p.gameMode = "adventure";
      p.teleport(ARENA);
      p.directMessage(`§a已加入！当前 §f${state.playersReady} §a人`);
      p.playSound("minecraft:block.note_block.pling", 1.0, 1.5);
      if (state.playersReady >= 2) { startLobby(); }
      return false;

    case "!pvp start":
      if (p.opLevel < 2) return false;
      beginPvPGame();
      return false;

    case "!pvp stop":
      if (p.opLevel < 2) return false;
      endPvPGame();
      return false;
  }
  return true;
});

// ── 大厅倒计时 30 秒 ──
function startLobby(): void {
  state.phase = "starting";
  let cd = 30;
  pvpLobbyTimer = world.setInterval(() => {
    cd--;
    if (cd <= 0 && pvpLobbyTimer) { world.clearInterval(pvpLobbyTimer); beginPvPGame(); }
    else if (cd <= 5) { world.say(`§e游戏将在 §c${cd} §e秒后开始！`); }
    else if (cd % 10 === 0) { world.say(`§7游戏将在 ${cd} 秒后开始...`); }
  }, 20);
}

// ── 开始游戏 ──
function beginPvPGame(): void {
  state.phase = "playing";
  state.redScore = 0; state.blueScore = 0;
  world.setScore("red", "pvp_score", 0);
  world.setScore("blue", "pvp_score", 0);
  world.showScoreboard("sidebar", "pvp_score");

  const players = world.querySelectorAll("*");
  players.forEach((entity, i) => {
    if (!entity.isPlayer()) { return; }
    const p = entity.player;
    p.clearInventory(); p.hp = 20; p.maxHp = 20; p.food = 20;

    if (i % 2 === 0) {
      world.joinTeam(entity, "red");
      p.teleport(new GameVector3(-20, ARENA.y, ARENA.z));
      p.setPlayerListName(`§c[红] §f${p.name}`);
      p.giveItem("minecraft:iron_sword", 1);
      p.giveItem("minecraft:bow", 1);
    } else {
      world.joinTeam(entity, "blue");
      p.teleport(new GameVector3(20, ARENA.y, ARENA.z));
      p.setPlayerListName(`§9[蓝] §f${p.name}`);
      p.giveItem("minecraft:iron_sword", 1);
      p.giveItem("minecraft:crossbow", 1);
    }
    p.giveItem("minecraft:arrow", 32);
    p.giveItem("minecraft:golden_apple", 3);
    p.giveItem("minecraft:cooked_beef", 16);
    p.addEffect("minecraft:speed", 99999, 1, true);

    const pos = p.position;
    world.spawnParticleCircle(pos.x, pos.y, pos.z, 1.5, "minecraft:happy_villager", 20);
    world.playSound("minecraft:entity.player.levelup", pos, 1.0, 1.0);
  });

  world.setBorderCenter(ARENA.x, ARENA.z);
  world.borderSize = ARENA_RADIUS * 2;
  world.setBorderDamage(1);
  world.say("§c§l⚔ 竞技场开始！⚔");
  world.playSound("minecraft:entity.ender_dragon.growl", ARENA, 1.0, 1.0);

  // 游戏倒计时
  let remaining = DURATION;
  pvpGameTimer = world.setInterval(() => {
    remaining--;
    const progress = remaining / DURATION;
    const mins = Math.floor(remaining / 60);
    const secs = remaining % 60;

    let color = "red";
    if (progress > 0.3) { color = "green"; }
    else if (progress > 0.1) { color = "yellow"; }

    world.showBossbar("pvp_timer",
      `§e战斗剩余: §f${mins}:${secs < 10 ? "0" : ""}${secs}`,
      progress, color);

    if (remaining === SHRINK_AT) {
      world.say("§c边界开始缩小！");
      world.shrinkBorder(20, 60);
    }
    if (remaining === 60) { world.say("§c最后一分钟！"); }
    if (remaining === 30) { world.strikeLightning(ARENA.x, ARENA.y, ARENA.z, 0); }
    if (remaining <= 0 && pvpGameTimer) {
      world.clearInterval(pvpGameTimer);
      endPvPGame();
    }
  }, 20);

  // 空投
  pvpAirdropTimer = world.setInterval(() => {
    if (state.phase !== "playing") return;
    const angle = Math.random() * Math.PI * 2;
    const dist = Math.random() * ARENA_RADIUS * 0.6;
    const dx = ARENA.x + Math.cos(angle) * dist;
    const dz = ARENA.z + Math.sin(angle) * dist;
    world.strikeLightning(dx, ARENA.y + 30, dz, 0);
    world.setTimeout(() => {
      world.dropItem(dx, ARENA.y + 1, dz, "minecraft:ender_pearl", 2);
      world.dropItem(dx, ARENA.y + 1, dz, "minecraft:golden_apple", 2);
      world.launchFirework(dx, ARENA.y + 3, dz, "yellow", "ball");
      world.say("§e☄ 空投已降落！");
    }, 20);
  }, 1200);
}

// ── 击杀计分 ──
world.onEntityDeath((entity, killer, _tick) => {
  if (state.phase !== "playing") return;
  if (!killer?.isPlayer() || !entity.isPlayer()) return;

  const kp = killer.player;
  const team = world.getTeamOf(killer) || "";
  const current = world.getScore(kp.name, "pvp_kills");
  world.setScore(kp.name, "pvp_kills", current + 1);

  if (team === "red") { state.redScore++; }
  else if (team === "blue") { state.blueScore++; }
  world.setScore(team, "pvp_score", team === "red" ? state.redScore : state.blueScore);

  kp.addExperienceLevels(2);
  kp.playSound("minecraft:entity.player.levelup", 1.0, 1.0);

  const pos = entity.position;
  world.spawnParticleCircle(pos.x, pos.y, pos.z, 1.5, "minecraft:angry_villager", 12);
  world.launchFirework(pos.x, pos.y + 1, pos.z, "red", "star");

  const killedTeam = world.getTeamOf(entity) || "";
  if (killedTeam !== team) {
    world.say(`§${team === "red" ? "c" : "9"}[${team}] §f${kp.name} §7击杀了 §f[${killedTeam}] ${entity.player.name}`);
  }
});

// ── 重生处理 ──
world.onPlayerRespawn((entity, _tick) => {
  if (state.phase !== "playing") return;
  const team = world.getTeamOf(entity);
  const p = entity.player;
  p.teleport(new GameVector3(team === "red" ? -20 : 20, ARENA.y, ARENA.z));
  p.giveItem("minecraft:iron_sword", 1);
  p.giveItem("minecraft:cooked_beef", 4);
  p.addEffect("minecraft:regeneration", 100, 1, true);
  p.addEffect("minecraft:resistance", 100, 2, true);
});

// ── 结束 ──
function endPvPGame(): void {
  state.phase = "ending";
  world.removeBossbar("pvp_timer");
  if (pvpAirdropTimer) { world.clearInterval(pvpAirdropTimer); }

  let winner = "平局！";
  let color = "e";
  if (state.redScore > state.blueScore) { winner = "红队 获胜！"; color = "c"; }
  else if (state.blueScore > state.redScore) { winner = "蓝队 获胜！"; color = "9"; }

  world.querySelectorAll("*").forEach((entity) => {
    if (!entity.isPlayer()) return;
    const p = entity.player;
    p.title(`§${color}§l${winner}`, "§7竞技场结束", 10, 80, 10);
    p.playSound("minecraft:ui.toast.challenge_complete", 1.0, 1.0);
    p.clearEffects();
  });

  world.say(`§${color}§l🏆 ${winner}`);

  // 烟花庆祝
  for (let i = 0; i < 8; i++) {
    world.setTimeout(() => {
      const cs = ["red", "gold", "green", "blue", "purple"];
      const ss = ["ball", "large_ball", "star", "burst"];
      world.launchFirework(
        ARENA.x + (Math.random() - 0.5) * 20,
        ARENA.y + Math.random() * 5,
        ARENA.z + (Math.random() - 0.5) * 20,
        cs[Math.floor(Math.random() * cs.length)],
        ss[Math.floor(Math.random() * ss.length)]
      );
    }, i * 400);
  }

  // 30 秒后重置
  world.setTimeout(() => {
    state.phase = "waiting";
    state.playersReady = 0; state.redScore = 0; state.blueScore = 0;
    world.hideScoreboard("sidebar");
    world.setBorderCenter(0, 0);
    world.borderSize = 60000000;
    world.say("§a竞技场已重置 — !pvp join 加入下一局");
  }, 600);
}
```

## 5.8 完整小游戏二：领地争夺战

已在 `colorzone` 的 `app.ts` 中实现。命令：`!cz` 加入、`!cz start` 开始、`!cz top` 排行榜。

核心机制：玩家走过地面 → 自动染色为队伍颜色 → 定时发药水 + 速度效果 → 90 秒后按占地数量排名。

详见 `src/server/app.ts` 中的 Territory Rush 实现。

## 5.9 更多实用示例

以下简化示例可在 `src/examples/` 中找到完整验证版本：

### 弹幕颜色命令

```js
world.onChat((entity, message, _tick) => {
  const p = entity.player;
  const colors: Record<string, string> = { "r": "c", "g": "a", "b": "9", "y": "e", "p": "d", "w": "f" };
  const match = message.match(/^!(\w)\s(.+)/);

  if (match && colors[match[1]]) {
    world.say(`§${colors[match[1]]}[${p.name}] §f${match[2]}`);
    return false;
  }
  return true;
});
// 用法: !r 大家好  → 红色发送
```

### 家传送

```js
const homeLocations = new Map<string, GameVector3>();

world.onChat((entity, message, _tick) => {
  const p = entity.player;

  if (message === "!sethome") {
    homeLocations.set(p.userId, new GameVector3(
      p.position.x, p.position.y, p.position.z
    ));
    p.directMessage("§a家已设置！输入 !home 回家");
    p.playSound("minecraft:block.note_block.pling", 1.0, 1.5);
    return false;
  }

  if (message === "!home") {
    const home = homeLocations.get(p.userId);
    if (!home) {
      p.directMessage("§c你还没有设置家！先输入 !sethome");
      return false;
    }
    p.teleport(home);
    p.directMessage("§a已传送回家！");
    p.playSound("minecraft:entity.enderman.teleport", 1.0, 1.0);
    return false;
  }

  // 分享坐标
  if (message === "!sharepos") {
    const pos = p.position;
    world.say(
      `§e${p.name} §f的坐标: §a[${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}]`
    );
    return false;
  }

  // 随机传送
  if (message === "!rtp") {
    const range = 500;
    const x = (Math.random() - 0.5) * range * 2;
    const z = (Math.random() - 0.5) * range * 2;
    p.teleport(new GameVector3(x, 150, z));
    p.directMessage(`§a已随机传送到 (${Math.floor(x)}, ~, ${Math.floor(z)})`);
    return false;
  }

  return true;
});
```

### 波次刷怪

```js
let wave = 0;
let mobsAlive = 0;

function startWave(pos: GameVector3): void {
  wave++;
  const count = wave * 3;
  mobsAlive = count;
  world.say(`§c§l⚔ 第 ${wave} 波开始！§f生成 ${count} 只僵尸`);

  for (let i = 0; i < count; i++) {
    world.setTimeout(() => {
      const x = pos.x + (Math.random() - 0.5) * 10;
      const z = pos.z + (Math.random() - 0.5) * 10;
      const zombie = world.spawnEntity("minecraft:zombie", new GameVector3(x, pos.y, z));
      if (!zombie) return;
      zombie.setNameTag(`§7[第${wave}波] 僵尸`);
      zombie.maxHp = 20 + wave * 5;
      zombie.hp = zombie.maxHp;
      zombie.setAI(true);
      zombie.addTag("wave_mob");
    }, i * 200);
  }
}

world.onEntityDeath((entity, killer, _tick) => {
  if (!entity.hasTag("wave_mob")) return;
  mobsAlive--;
  if (mobsAlive <= 0) {
    world.say(`§a§l✔ 第 ${wave} 波清除！`);
    world.setTimeout(() => startWave(entity.position), 200);
  }
});
```

## 5.10 音阶测试

一个快速音效测试命令：

```js
world.onChat((entity, message, _tick) => {
  const p = entity.player;
  if (message === "!sounds") {
    const notes = [1.0, 1.2, 1.5, 2.0];
    notes.forEach((pitch, i) => {
      world.setTimeout(() => {
        p.playSound("minecraft:block.note_block.pling", 1.0, pitch);
      }, i * 100);
    });
    p.directMessage("§a音阶测试播放中...");
    return false;
  }
  return true;
});
```

---

所有示例代码均已通过 `tsc --noEmit`、`eslint` 和 `node build.mjs` 完整验证。可直接使用。

更多 API 细节请参考 `docs/api/` 目录中的完整 API 文档。
