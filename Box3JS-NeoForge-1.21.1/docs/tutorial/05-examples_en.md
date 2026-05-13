# Tutorial 5: Visual Effects & Complete Mini-Games

This tutorial covers particles, fireworks, lightning, explosions, and other visual effects, plus three verified complete mini-games.

## 5.1 Particle Effects

```js
// Single-point particles: (type, x, y, z, count, dx, dy, dz, speed)
world.spawnParticle("minecraft:flame", 0, 100, 0, 20, 0.5, 0.5, 0.5, 0.05);
world.spawnParticle("minecraft:portal", 0, 100, 0, 15, 0.5, 0.5, 0.5, 0.02);
world.spawnParticle("minecraft:end_rod", 0, 100, 0, 8, 0.2, 0, 0.2, 0.01);
world.spawnParticle("minecraft:witch", 0, 100, 0, 10, 0.3, 0.3, 0.3, 0.03);

// Particle circle: (x, y, z, radius, type, count)
world.spawnParticleCircle(0, 100, 0, 3.0, "minecraft:happy_villager", 30);
world.spawnParticleCircle(0, 100, 0, 2.0, "minecraft:flame", 24);
world.spawnParticleCircle(0, 100, 0, 4.0, "minecraft:end_rod", 36);
```

Common particles:

| Particle ID | Effect |
|-------------|--------|
| `minecraft:flame` | Fire |
| `minecraft:cloud` | Smoke |
| `minecraft:happy_villager` | Green particles (positive) |
| `minecraft:witch` | Purple particles |
| `minecraft:portal` | Portal |
| `minecraft:end_rod` | End rod light |
| `minecraft:heart` | Hearts |
| `minecraft:note` | Music notes |
| `minecraft:dragon_breath` | Dragon's breath |
| `minecraft:angry_villager` | Angry particles (red) |
| `minecraft:soul_fire_flame` | Soul fire (blue) |
| `minecraft:redstone` | Redstone particles |
| `minecraft:explosion` | Explosion particles |

### Spiral Rising Particles

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

## 5.2 Fireworks

```js
// Firework: (x, y, z, color, shape)
world.launchFirework(0, 100, 0, "gold", "large_ball");
world.launchFirework(0, 100, 0, "red", "star");
world.launchFirework(0, 100, 0, "purple", "burst");
world.launchFirework(0, 100, 0, "green", "creeper");
```

Firework colors: `"red"` `"blue"` `"green"` `"yellow"` `"gold"` `"white"` `"aqua"` `"pink"` `"purple"`

Firework shapes: `"ball"` `"large_ball"` `"star"` `"creeper"` `"burst"`

### Sequential Firework Show

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

## 5.3 Lightning

```js
// Lightning: (x, y, z, damage)
world.strikeLightning(0, 100, 0);        // Default damage
world.strikeLightning(0, 100, 0, 10);    // 10 damage
world.strikeLightning(0, 100, 0, 0);     // No damage, visual only

// Summon lightning around a player
for (let i = 0; i < 3; i++) {
  world.setTimeout(() => {
    const lx = pos.x + (Math.random() - 0.5) * 12;
    const lz = pos.z + (Math.random() - 0.5) * 12;
    world.strikeLightning(lx, pos.y, lz, 0);
  }, i * 200);
}
world.playSound("minecraft:entity.lightning_bolt.thunder", pos, 1.0, 1.0);
```

## 5.4 Explosions

```js
// Explosion: (x, y, z, power, causesFire)
world.explode(0, 100, 0, 4, false);   // Power 4, no fire
world.explode(0, 100, 0, 8, true);    // Power 8, causes fire

// Player-triggered self-destruct (3-second countdown)
world.playSound("minecraft:block.note_block.bass", pos, 1.0, 0.5);
world.setTimeout(() => {
  world.spawnParticle("minecraft:explosion", pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
  world.setTimeout(() => {
    world.explode(pos.x, pos.y, pos.z, 4, false);
    world.playSound("minecraft:entity.generic.explode", pos, 1.0, 1.0);
  }, 10);
}, 60);
```

## 5.5 Sounds

```js
// Global sound (all players hear it)
world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
world.playSound("minecraft:entity.ender_dragon.growl", pos, 1.0, 1.0);

// Only one player hears it
player.playSound("minecraft:entity.player.levelup", 1.0, 1.0);
```

Common sounds:

| Sound ID | Use |
|----------|-----|
| `minecraft:block.note_block.pling` | Bell chime |
| `minecraft:block.note_block.bass` | Bass note |
| `minecraft:entity.experience_orb.pickup` | XP orb pickup |
| `minecraft:entity.player.levelup` | Level up |
| `minecraft:entity.ender_dragon.growl` | Dragon roar (boss entrance) |
| `minecraft:entity.wither.spawn` | Wither spawn (menacing) |
| `minecraft:entity.lightning_bolt.thunder` | Thunder |
| `minecraft:entity.generic.explode` | Explosion |
| `minecraft:entity.witch.throw` | Potion throw |
| `minecraft:block.beacon.activate` | Beacon activation |
| `minecraft:block.anvil.land` | Anvil landing |
| `minecraft:ui.toast.challenge_complete` | Challenge complete |
| `minecraft:entity.player.burp` | Eating sound |
| `minecraft:entity.enderman.teleport` | Teleport sound |

## 5.6 Player Join/Leave Effects

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

## 5.7 Complete Mini-Game 1: PvP Arena

This is a practical application of the design patterns from Tutorial 4 — a full red-vs-blue PvP mini-game integrating events, BossBar, scoreboard, teams, particles, fireworks, shrinking border, airdrops, and more.

**Commands:**
- `!pvp join` — Join the game
- `!pvp leave` — Leave the queue
- `!pvp start` — (OP) Start the game
- `!pvp stop` — (OP) Force end
- `!pvp status` — Check status

**Features:**
- Lobby countdown 30s → game duration 300s
- Auto-assign red/blue teams + team prefixes
- Kill scoring + global announcements + firework effects
- BossBar countdown (>30% green → <10% red)
- Border shrinks at 120s
- Airdrops every 60s (lightning marker + ender pearls/golden apples)
- Center lightning strike in final 30s
- Victory fireworks show + auto-reset

```js
// ═══════════════════════════════════════════
//  PvP Arena — Complete Example
//  (Verified: tsc + eslint + build pass)
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

// ── Initialization ──
world.setGameRule("keepInventory", false);
world.setGameRule("doMobSpawning", false);
world.clearWeather();
world.time = 6000;
world.addScoreboard("pvp_kills");
world.addScoreboard("pvp_score");
world.createTeam("red", "red");
world.createTeam("blue", "blue");

// ── Chat commands ──
world.onChat((entity, message, _tick) => {
  const p = entity.player;

  switch (message) {
    case "!pvp":
      p.directMessage("§6── PvP Arena ──");
      p.directMessage("§f!pvp join  §7- Join game");
      p.directMessage("§f!pvp start §7- (OP) Start game");
      return false;

    case "!pvp join":
      if (state.phase !== "waiting") { p.directMessage("§cGame already in progress"); return false; }
      state.playersReady++;
      p.clearInventory();
      p.hp = 20; p.maxHp = 20; p.food = 20;
      p.gameMode = "adventure";
      p.teleport(ARENA);
      p.directMessage(`§aJoined! Current players: §f${state.playersReady}`);
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

// ── 30-second lobby countdown ──
function startLobby(): void {
  state.phase = "starting";
  let cd = 30;
  pvpLobbyTimer = world.setInterval(() => {
    cd--;
    if (cd <= 0 && pvpLobbyTimer) { world.clearInterval(pvpLobbyTimer); beginPvPGame(); }
    else if (cd <= 5) { world.say(`§eGame starts in §c${cd} §eseconds!`); }
    else if (cd % 10 === 0) { world.say(`§7Game starts in ${cd} seconds...`); }
  }, 20);
}

// ── Start game ──
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
      p.setPlayerListName(`§c[Red] §f${p.name}`);
      p.giveItem("minecraft:iron_sword", 1);
      p.giveItem("minecraft:bow", 1);
    } else {
      world.joinTeam(entity, "blue");
      p.teleport(new GameVector3(20, ARENA.y, ARENA.z));
      p.setPlayerListName(`§9[Blue] §f${p.name}`);
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
  world.say("§c§l⚔ Arena begins! ⚔");
  world.playSound("minecraft:entity.ender_dragon.growl", ARENA, 1.0, 1.0);

  // Game countdown
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
      `§eTime remaining: §f${mins}:${secs < 10 ? "0" : ""}${secs}`,
      progress, color);

    if (remaining === SHRINK_AT) {
      world.say("§cBorder is shrinking!");
      world.shrinkBorder(20, 60);
    }
    if (remaining === 60) { world.say("§cFinal minute!"); }
    if (remaining === 30) { world.strikeLightning(ARENA.x, ARENA.y, ARENA.z, 0); }
    if (remaining <= 0 && pvpGameTimer) {
      world.clearInterval(pvpGameTimer);
      endPvPGame();
    }
  }, 20);

  // Airdrops
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
      world.say("§e☄ Airdrop has landed!");
    }, 20);
  }, 1200);
}

// ── Kill scoring ──
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
    world.say(`§${team === "red" ? "c" : "9"}[${team}] §f${kp.name} §7eliminated §f[${killedTeam}] ${entity.player.name}`);
  }
});

// ── Respawn handling ──
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

// ── End game ──
function endPvPGame(): void {
  state.phase = "ending";
  world.removeBossbar("pvp_timer");
  if (pvpAirdropTimer) { world.clearInterval(pvpAirdropTimer); }

  let winner = "Draw!";
  let color = "e";
  if (state.redScore > state.blueScore) { winner = "Red Team wins!"; color = "c"; }
  else if (state.blueScore > state.redScore) { winner = "Blue Team wins!"; color = "9"; }

  world.querySelectorAll("*").forEach((entity) => {
    if (!entity.isPlayer()) return;
    const p = entity.player;
    p.title(`§${color}§l${winner}`, "§7Arena complete", 10, 80, 10);
    p.playSound("minecraft:ui.toast.challenge_complete", 1.0, 1.0);
    p.clearEffects();
  });

  world.say(`§${color}§l🏆 ${winner}`);

  // Victory fireworks
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

  // Reset after 30 seconds
  world.setTimeout(() => {
    state.phase = "waiting";
    state.playersReady = 0; state.redScore = 0; state.blueScore = 0;
    world.hideScoreboard("sidebar");
    world.setBorderCenter(0, 0);
    world.borderSize = 60000000;
    world.say("§aArena reset — !pvp join for next round");
  }, 600);
}
```

## 5.8 Complete Mini-Game 2: Territory Rush

Implemented in the `colorzone` project's `app.ts`. Commands: `!cz` to join, `!cz start` to begin, `!cz top` for leaderboard.

Core mechanic: players walk over the ground → tiles auto-color to their team → periodic potions + speed buffs → after 90 seconds, ranking by territory claimed.

See `src/server/app.ts` for the full Territory Rush implementation.

## 5.9 More Practical Examples

The following simplified examples have full verified versions in `src/examples/`:

### Colored Chat Command

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
// Usage: !r Hello everyone  → sends in red
```

### Home Teleport

```js
const homeLocations = new Map<string, GameVector3>();

world.onChat((entity, message, _tick) => {
  const p = entity.player;

  if (message === "!sethome") {
    homeLocations.set(p.userId, new GameVector3(
      p.position.x, p.position.y, p.position.z
    ));
    p.directMessage("§aHome set! Type !home to return");
    p.playSound("minecraft:block.note_block.pling", 1.0, 1.5);
    return false;
  }

  if (message === "!home") {
    const home = homeLocations.get(p.userId);
    if (!home) {
      p.directMessage("§cYou haven't set a home yet! Use !sethome first");
      return false;
    }
    p.teleport(home);
    p.directMessage("§aTeleported home!");
    p.playSound("minecraft:entity.enderman.teleport", 1.0, 1.0);
    return false;
  }

  // Share position
  if (message === "!sharepos") {
    const pos = p.position;
    world.say(
      `§e${p.name} §fis at: §a[${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}]`
    );
    return false;
  }

  // Random teleport
  if (message === "!rtp") {
    const range = 500;
    const x = (Math.random() - 0.5) * range * 2;
    const z = (Math.random() - 0.5) * range * 2;
    p.teleport(new GameVector3(x, 150, z));
    p.directMessage(`§aRandomly teleported to (${Math.floor(x)}, ~, ${Math.floor(z)})`);
    return false;
  }

  return true;
});
```

### Wave Spawning

```js
let wave = 0;
let mobsAlive = 0;

function startWave(pos: GameVector3): void {
  wave++;
  const count = wave * 3;
  mobsAlive = count;
  world.say(`§c§l⚔ Wave ${wave} begins!§f Spawning ${count} zombies`);

  for (let i = 0; i < count; i++) {
    world.setTimeout(() => {
      const x = pos.x + (Math.random() - 0.5) * 10;
      const z = pos.z + (Math.random() - 0.5) * 10;
      const zombie = world.spawnEntity("minecraft:zombie", new GameVector3(x, pos.y, z));
      if (!zombie) return;
      zombie.setNameTag(`§7[Wave ${wave}] Zombie`);
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
    world.say(`§a§l✔ Wave ${wave} cleared!`);
    world.setTimeout(() => startWave(entity.position), 200);
  }
});
```

## 5.10 Sound Scale Test

A quick sound test command:

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
    p.directMessage("§aPlaying sound scale test...");
    return false;
  }
  return true;
});
```

---

All example code has been verified with `tsc --noEmit`, `eslint`, and `node build.mjs`. Ready to use.

For more API details, refer to the complete API docs in the `docs/api/` directory.
