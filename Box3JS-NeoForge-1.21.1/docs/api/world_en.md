# world — World API

`world` is a global singleton representing the Minecraft server's world state. Controls weather, time, game rules, entity spawning; registers event callbacks; manages scoreboards, bossbars, teams; and fires particles, fireworks, lightning, and other visual effects.

## World Properties

### world.projectName()

⬆ MC Extension | Read-only. The server MOTD string.

```js
console.log(world.projectName()); // "A Minecraft Server"
```

### world.currentTick()

✅ Box3 API | Read-only. Total ticks since server startup.

```js
var uptime = world.currentTick();
world.say(
  "Server has been running for " + Math.floor(uptime / 20 / 60) + " minutes",
);
```

## Weather

### world.rainDensity

✅ Box3 API | Get/set rain intensity, range 0.0–1.0.

```js
world.rainDensity = 1.0; // full rain
console.log(world.rainDensity); // 0.0 ~ 1.0
```

### world.thunderDensity

⬆ MC Extension | Get/set thunderstorm intensity, range 0.0–1.0.

```js
world.thunderDensity = 0.5;
```

### world.clearWeather()

⬆ MC Extension | Clear both rain and thunder.

```js
world.clearWeather();
```

## Time

### world.time

✅ Box3 API | Get/set world time (ticks). One Minecraft day = 24000 ticks.

```js
world.time = 6000; // noon
world.time = 18000; // midnight
console.log(world.time); // current time
```

Also provides `world.setTime(tick)` as a convenience setter.

```js
world.setTime(6000); // equivalent to world.time = 6000
```

Common time values: `0` sunrise, `6000` noon, `12000` sunset, `18000` midnight.

### world.timeScale

✅ Box3 API | Get/set time flow rate. `0` = paused, `1` = normal. Internally modifies the `doDaylightCycle` game rule.

```js
world.timeScale = 0; // freeze time
world.timeScale = 1; // resume normal
```

## Difficulty

### world.difficulty

✅ Box3 API | Get/set game difficulty. Get returns the name string; set accepts a name string or number 0–3.

```js
world.difficulty = "hard";
world.difficulty = 3; // same as hard
console.log(world.difficulty); // "hard"

// Valid values: "peaceful"(0), "easy"(1), "normal"(2), "hard"(3)
```

## Spawn Point

### world.spawnPoint

⬆ MC Extension | Read-only, returns the world spawn point as `GameVector3`.

### world.setWorldSpawn(pos)

⬆ MC Extension | Set the world spawn point.

```js
world.setWorldSpawn(new GameVector3(0, 70, 0));
```

## Game Rules

### world.getGameRule(name)

⬆ MC Extension | Get a game rule boolean value.

### world.setGameRule(name, value)

⬆ MC Extension | Set a game rule. `value` is a boolean.

**Supported rules:**

| Rule Name            | Description             |
| -------------------- | ----------------------- |
| `doDaylightCycle`    | Time progression        |
| `doWeatherCycle`     | Weather changes         |
| `keepInventory`      | Keep inventory on death |
| `doMobSpawning`      | Natural mob spawning    |
| `doFireTick`         | Fire spread             |
| `mobGriefing`        | Mob block griefing      |
| `doImmediateRespawn` | Instant respawn         |

```js
world.setGameRule("keepInventory", true);
world.setGameRule("doFireTick", false);
console.log(world.getGameRule("doMobSpawning")); // true/false
```

## Entity Spawning

### world.spawnEntity(type, pos)

✅ Box3 API | Spawn an entity at the given position. `type` is a namespaced ID, returns `Box3JSEntity`.

```js
var zombie = world.spawnEntity("minecraft:zombie", new GameVector3(0, 100, 0));
zombie.setNameTag("Guard");
zombie.maxHp = 40;
zombie.hp = 40;
zombie.setEquipment("mainhand", "minecraft:iron_sword");
zombie.setAI(true);
```

## Event Callbacks

All event callbacks are registered via `world.onXxx(handler)`. Except for `onTick`, the first callback parameter is usually the triggering `entity` (`Box3JSEntity`).

| Event                        | Type    | Callback Signature                                     | Trigger                                |
| ---------------------------- | ------- | ------------------------------------------------------ | -------------------------------------- |
| `world.onTick(fn)`           | ✅ Box3 | `()`                                                   | Every tick                             |
| `world.onPlayerJoin(fn)`     | ✅ Box3 | `(entity)`                                             | Player logs in                         |
| `world.onPlayerLeave(fn)`    | ✅ Box3 | `(entity)`                                             | Player leaves                          |
| `world.onChat(fn)`           | ✅ Box3 | `(entity, message, tick)`                              | Player sends chat message              |
| `world.onVoxelDestroy(fn)`   | ✅ Box3 | `(entity, x, y, z, voxel, tick)`                       | Player breaks a block                  |
| `world.onBlockPlace(fn)`     | ⬆ MC    | `(entity, x, y, z, voxel, voxelId, tick)`              | Player places a block                  |
| `world.onBlockActivate(fn)`  | ⬆ MC    | `(entity, x, y, z, voxel, tick)`                       | Player right-clicks a block            |
| `world.onInteract(fn)`       | ✅ Box3 | `(entity, target, tick)`                               | Player right-clicks an entity          |
| `world.onVoxelContact(fn)`   | ✅ Box3 | `(entity, voxelId, x, y, z, contactType, force, tick)` | Entity contacts a block                |
| `world.onEntityContact(fn)`  | ✅ Box3 | `(entity, other, tick)`                                | Two entities contact                   |
| `world.onEntitySeparate(fn)` | ✅ Box3 | `(entity, other, tick)`                                | Two entities separate                  |
| `world.onFluidEnter(fn)`     | ✅ Box3 | `(entity, fluid, x, y, z, tick)`                       | Entity enters a fluid                  |
| `world.onFluidLeave(fn)`     | ✅ Box3 | `(entity, fluid, x, y, z, tick)`                       | Entity leaves a fluid                  |
| `world.onEntityDeath(fn)`    | ⬆ MC    | `(entity, killer, tick)`                               | Entity dies; `killer` may be null      |
| `world.onEntityDamage(fn)`   | ⬆ MC    | `(entity, amount, source, attacker, tick)`             | Entity takes damage (Pre phase)        |
| `world.onPlayerRespawn(fn)`  | ⬆ MC    | `(entity)`                                             | Player respawns                        |
| `world.onMessage(fn)`        | ⬆ MC    | `(from, data)`                                         | Receives `world.sendMessage()` message |

```js
world.onTick(() => {
  // runs every tick
});

world.onPlayerJoin((entity) => {
  var p = entity.player;
  world.say(p.name + " joined the game");
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

## Query

### world.querySelectorAll(selector)

✅ Box3 API | Query all matching entities. Returns `Box3JSEntity[]`.

### world.querySelector(selector)

✅ Box3 API | Query a single matching entity. Returns `Box3JSEntity` or null.

**Selector syntax:**

| Selector     | Meaning             |
| ------------ | ------------------- |
| `"*"`        | All online players  |
| `"#uuid"`    | Exact match by UUID |
| `".tagName"` | Match by tag        |

```js
var allPlayers = world.querySelectorAll("*");
for (var i = 0; i < allPlayers.length; i++) {
  var p = allPlayers[i].player;
  p.actionBar("Online: " + allPlayers.length);
}

var specific = world.querySelector("#550e8400-e29b-41d4-a716-446655440000");
if (specific) {
  specific.player.directMessage("Found you");
}
```

### world.say(message)

✅ Box3 API | Broadcast a message to the entire server.

```js
world.say("§6[Announcement] §fThe match is about to begin!");
```

## Timers

### world.setTimeout(handler, ticks)

⬆ MC Extension | Execute once after `ticks` delay, returns timer ID.

### world.setInterval(handler, ticks)

⬆ MC Extension | Execute repeatedly every `ticks`, returns timer ID.

### world.clearTimeout(id)

⬆ MC Extension | Cancel a timeout.

### world.clearInterval(id)

⬆ MC Extension | Cancel an interval.

```js
var tid = world.setTimeout(() => {
  world.say("Executed after 3 seconds");
}, 60); // 60 ticks = 3 seconds

var iid = world.setInterval(() => {
  world.say("Executed every 10 seconds");
}, 200); // 200 ticks = 10 seconds

// Cancel
world.clearTimeout(tid);
world.clearInterval(iid);
```

## Scoreboard

All ⬆ MC Extension.

### world.addScoreboard(name)

Create a dummy-type objective.

### world.addScoreboard(name, criteria)

Create an objective with a specific criteria. `criteria` can be `"dummy"` (manual), `"deathCount"`, etc.

### world.removeScoreboard(name)

Delete an objective.

### world.setScore(entityOrName, objectiveName, value)

Set the score for an entity or name. `entityOrName` can be a `Box3JSEntity` or a string.

### world.getScore(entityOrName, objectiveName)

Get a score.

### world.showScoreboard(slot, objectiveName)

Display a scoreboard at the given slot. `slot`: `"sidebar"`, `"list"` (tab list), `"belowname"`.

### world.hideScoreboard(slot)

Clear a slot.

### world.listScores(objectiveName)

Get all entries for an objective, returns `[{name, value}]`.

```js
world.addScoreboard("kills");
world.setScore("Steve", "kills", 5);
world.showScoreboard("sidebar", "kills");

var scores = world.listScores("kills");
// [{name: "Steve", value: 5}, ...]

world.hideScoreboard("sidebar");
world.removeScoreboard("kills");
```

## Boss Bar

All ⬆ MC Extension.

### world.showBossbar(name, text, progress, color)

Show or update a boss bar.

| Parameter  | Description                                                               |
| ---------- | ------------------------------------------------------------------------- |
| `name`     | Bar ID, used for subsequent updates or removal                            |
| `text`     | Display text (supports color codes)                                       |
| `progress` | 0.0–1.0, bar fill length                                                  |
| `color`    | `"blue"`, `"green"`, `"pink"`, `"purple"`, `"red"`, `"white"`, `"yellow"` |

### world.removeBossbar(name)

Remove a boss bar.

```js
// Create a 3-minute countdown boss bar
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
      "§eTime remaining: §f" + Math.ceil(totalTicks / 20) + "s",
      remain,
      remain > 0.5 ? "green" : remain > 0.2 ? "yellow" : "red",
    );
  }
}, 20);
```

## Teams

All ⬆ MC Extension.

### world.createTeam(name, color)

Create a team. `color`: `"aqua"`, `"black"`, `"blue"`, `"dark_aqua"`, `"dark_blue"`, `"dark_gray"`, `"dark_green"`, `"dark_purple"`, `"dark_red"`, `"gold"`, `"gray"`, `"green"`, `"light_purple"`, `"red"`, `"white"`, `"yellow"`.

### world.removeTeam(name)

Delete a team.

### world.joinTeam(entity, teamName)

Add an entity to a team.

### world.leaveTeam(entity)

Remove an entity from its current team.

### world.getTeamOf(entity)

Get the team name an entity belongs to.

```js
world.createTeam("red_team", "red");
world.createTeam("blue_team", "blue");

world.onPlayerJoin((entity) => {
  // Alternate team assignment
  var online = world.querySelectorAll("*").length;
  world.joinTeam(entity, online % 2 === 0 ? "red_team" : "blue_team");
});
```

## World Border

All ⬆ MC Extension.

### world.borderSize

Get/set the current border size.

### world.setBorderCenter(x, z)

Set the border center.

### world.shrinkBorder(targetSize, seconds)

Smoothly shrink the border to the target size over `seconds` seconds.

### world.setBorderDamage(damagePerBlock)

Damage per second outside the border.

### world.setBorderWarning(blocks)

Border warning distance (screen reddening advance notice).

```js
// Shrinking zone gameplay
world.setBorderCenter(0, 0);
world.borderSize = 500;
world.setBorderDamage(2);
world.setBorderWarning(10);

world.setTimeout(() => {
  world.shrinkBorder(100, 120); // shrink to 100 over 2 minutes
}, 600); // start after 30 seconds
```

## Visual Effects

All ⬆ MC Extension.

### world.strikeLightning(x, y, z)

Summon lightning at coordinates (default damage).

### world.strikeLightning(pos)

⬆ GameVector3 overload.

### world.strikeLightning(x, y, z, damage)

Summon lightning with custom damage.

### world.strikeLightning(pos, damage)

⬆ GameVector3 overload.

```js
world.strikeLightning(0, 100, 0);
world.strikeLightning(new GameVector3(0, 100, 0));
world.strikeLightning(new GameVector3(0, 100, 0), 10); // 10 damage
```

### world.launchFirework(x, y, z, color, shape)

Launch a firework rocket at coordinates.

### world.launchFirework(pos, color, shape)

⬆ GameVector3 overload.

**Colors:** `"red"`, `"blue"`, `"green"`, `"yellow"`, `"gold"`, `"white"`, `"aqua"`, `"pink"`, `"purple"`

**Shapes:** `"ball"` (small ball, default), `"large_ball"`, `"star"`, `"creeper"`, `"burst"`

```js
world.launchFirework(0, 100, 0, "gold", "large_ball");
world.launchFirework(new GameVector3(0, 100, 0), "red", "star");
```

### world.spawnParticle(type, x, y, z, count, dx, dy, dz, speed)

Spawn particles at coordinates. Particle type uses namespaced ID.

### world.spawnParticle(type, pos, count, dx, dy, dz, speed)

⬆ GameVector3 overload.

### world.spawnParticleCircle(x, y, z, radius, type, count)

Spawn particles evenly on a horizontal circle.

### world.spawnParticleCircle(pos, radius, type, count)

⬆ GameVector3 overload.

```js
// Point particles
world.spawnParticle("minecraft:flame", 0, 100, 0, 10, 0.5, 0.5, 0.5, 0.1);
world.spawnParticle("minecraft:cloud", entity.position, 1, 0, 0, 0, 0);

// Circular particle ring
world.spawnParticleCircle(0, 100, 0, 2.0, "minecraft:happy_villager", 20);
world.spawnParticleCircle(
  new GameVector3(0, 100, 0),
  2.0,
  "minecraft:happy_villager",
  20,
);

// Common particles:
// minecraft:flame, minecraft:cloud, minecraft:happy_villager
// minecraft:witch, minecraft:portal, minecraft:end_rod
// minecraft:heart, minecraft:note, minecraft:dragon_breath
```

## Items / Projectiles

All ⬆ MC Extension.

### world.dropItem(x, y, z, itemId, count)

Drop an item entity at coordinates.

### world.dropItem(pos, itemId, count)

⬆ GameVector3 overload.

```js
world.dropItem(0, 100, 0, "minecraft:diamond", 3);
world.dropItem(entity.position, "minecraft:diamond", 3);
```

### world.launchProjectile(type, x, y, z, tx, ty, tz, speed)

Launch a projectile from start to target, returns `Box3JSEntity`.

### world.launchProjectile(type, pos, target, speed)

⬆ GameVector3 overload — both start and target accept `GameVector3`.

```js
// Launch a fireball from (0, 100, 0) toward (10, 100, 10)
world.launchProjectile("minecraft:fireball", 0, 100, 0, 10, 100, 10, 2);
world.launchProjectile(
  "minecraft:fireball",
  new GameVector3(0, 100, 0),
  new GameVector3(10, 100, 10),
  2,
);

// Launch an arrow
world.launchProjectile("minecraft:arrow", 0, 100, 0, 5, 105, 0, 3);
```

## Explosion / Sound / Query

All ⬆ MC Extension.

### world.explode(x, y, z, power)

Create an explosion.

### world.explode(pos, power)

⬆ GameVector3 overload.

### world.explode(x, y, z, power, fire)

Create an explosion (`fire=true` can ignite blocks).

### world.explode(pos, power, fire)

⬆ GameVector3 overload.

```js
world.explode(0, 100, 0, 4); // power 4, no fire
world.explode(new GameVector3(0, 100, 0), 8, true); // power 8, with fire
```

### world.playSound(path, x, y, z, volume, pitch)

Play a sound at coordinates to all online players. `path` is a sound namespaced ID, `volume` 0–1, `pitch` 0.5–2.0.

### world.playSound(path, pos, volume, pitch)

⬆ GameVector3 overload.

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

Cast a ray from `origin` in `direction`, default max distance 5 blocks.

### world.raycast(origin, direction, maxDistance)

Raycast with custom max distance.

**Returns:** `{hit, x, y, z, normalX, normalY, normalZ, distance, entity, voxel}`

```js
var dir = new GameVector3(0, -1, 0);
var result = world.raycast(playerEntity.position, dir, 50);
if (result.hit) {
  console.log("Hit block:", result.voxel, "distance:", result.distance);
  if (result.entity) {
    console.log("Hit entity:", result.entity.entityType);
  }
}
```

### world.entitiesInArea(pos1, pos2)

Returns all entities within the AABB defined by two corner positions.

### world.entitiesInRadius(x, y, z, radius)

⬆ MC Extension | Returns all entities within a spherical radius. Convenience wrapper around `entitiesInArea`.

### world.entitiesInRadius(pos, radius)

⬆ GameVector3 overload.

```js
// Find all entities within 10-block radius
var nearby = world.entitiesInRadius(0, 100, 0, 10);
var nearby = world.entitiesInRadius(entity.position, 10);
for (var i = 0; i < nearby.length; i++) {
  console.log(nearby[i].entityType);
}
```

### world.getBiome(x, y, z)

⬆ MC Extension | Returns the biome namespaced ID string.

### world.getBiome(pos)

⬆ GameVector3 overload.

```js
var biome = world.getBiome(0, 70, 0);
console.log(biome); // "minecraft:plains"
var biome = world.getBiome(entity.position);
```

## Cross-script Messaging

### world.sendMessage(target, data)

⬆ MC Extension | Send a message to another script project. `target` is `"*"` (broadcast) or a project name. Receivers listen via `world.onMessage()`.

### world.runCommand(cmd)

⬆ MC Extension | Execute a command as the server console.

```js
world.runCommand("time set day");
world.runCommand("weather clear");
```
