# world — World API

`world` is a global singleton representing the Minecraft server's world state. Controls weather, time, game rules, entity spawning; registers event callbacks; manages scoreboards, bossbars, teams; and fires particles, fireworks, lightning, and other visual effects.

## World Properties

### world.projectName

✅ Box3 API | Read-only property. The server MOTD string. Also callable as `world.projectName()` for backward compatibility.

```js
console.log(world.projectName); // "A Minecraft Server"
```

### world.serverId

✅ Box3 API | Read/write property. Server identifier, maps to the server MOTD.

```js
world.serverId = "My Cool Server";
console.log(world.serverId);
```

### world.currentTick

✅ Box3 API | Read-only property. Total ticks since server startup. Also callable as `world.currentTick()` for backward compatibility.

```js
var uptime = world.currentTick;
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

✅ Box3 API | Read-only, returns the world spawn point as `GameVector3`.

### world.setWorldSpawn(pos)

✅ Box3 API | Set the world spawn point.

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

### world.createEntity(config)

✅ Box3 API | Spawn an entity with a full configuration object. Returns `Box3JSEntity`.

Supported config fields: `type`, `position`, `velocity`, `fixed`, `gravity`, `friction`, `mass`, `restitution`, `collides`, `meshInvisible`, `hp`, `maxHp`, `tags` (array).

```js
var entity = world.createEntity({
  type: "minecraft:skeleton",
  position: new GameVector3(0, 100, 0),
  velocity: new GameVector3(0, 0.5, 0),
  fixed: false,
  gravity: true,
  collides: true,
  hp: 30,
  maxHp: 30,
  tags: ["enemy", "undead"],
});
```

## Sound Properties

✅ Box3 API | Sound path strings that auto-play when set to a non-empty value:

| Property           | Trigger                                                    |
| ------------------ | ---------------------------------------------------------- |
| `ambientSound`     | Every 200 ticks (10s) at world spawn with 0.3 volume       |
| `playerJoinSound`  | At player's position with full volume when a player joins  |
| `playerLeaveSound` | At player's position with full volume when a player leaves |
| `placeVoxelSound`  | At block position with full volume when a block is placed  |
| `breakVoxelSound`  | At block position with full volume when a block is broken  |

Set to `null` or empty string to stop auto-play.

```js
world.ambientSound = "minecraft:ambient.cave";
world.playerJoinSound = "minecraft:block.note_block.pling";
world.playerLeaveSound = "minecraft:block.note_block.bass";
world.placeVoxelSound = "minecraft:block.stone.place";
world.breakVoxelSound = "minecraft:block.stone.break";
```

## Sound

### world.sound(config)

✅ Box3 API | Play a sound. `config` can be a path string or `{path, position, volume, pitch}` object.

```js
// String shorthand — plays at origin with default volume/pitch
world.sound("minecraft:block.note_block.pling");

// Full config
world.sound({
  path: "minecraft:entity.experience_orb.pickup",
  position: new GameVector3(0, 100, 0),
  volume: 0.8,
  pitch: 1.5,
});
```

## Search Box

### world.searchBox(bounds)

✅ Box3 API | Query all entities within a GameBounds3 region.

```js
var bounds = new GameBounds3(
  new GameVector3(-10, 0, -10),
  new GameVector3(10, 50, 10),
);
var entities = world.searchBox(bounds);
```

## Event Callbacks

All event callbacks are registered via `world.onXxx(handler)`, returning a `GameEventHandlerToken`. Call `.cancel()` to unregister, `.active()` to check status. Except for `onTick`, the first callback parameter is usually the triggering `entity` (`Box3JSEntity`). For `world.onChat()`, returning `false` from the handler cancels that chat message.

### GameEventHandlerToken

| Method           | Description                                                |
| ---------------- | ---------------------------------------------------------- |
| `token.cancel()` | Unregister the event handler                               |
| `token.active()` | Returns `true` if the handler is still active              |
| `token.resume()` | Throws UnsupportedOperationException — re-register instead |

```js
var token = world.onTick(function (info) {
  if (info.tick > 6000) {
    token.cancel();
  }
});
```

| Event                        | Type    | Callback Signature                                     | Trigger                                             |
| ---------------------------- | ------- | ------------------------------------------------------ | --------------------------------------------------- |
| `world.onTick(fn)`           | ✅ Box3 | `(info)` → `{tick, prevTick, elapsedTimeMS, skip}`     | Every tick                                          |
| `world.onPlayerJoin(fn)`     | ✅ Box3 | `(entity, tick)`                                       | Player logs in                                      |
| `world.onPlayerLeave(fn)`    | ✅ Box3 | `(entity, tick)`                                       | Player leaves                                       |
| `world.onChat(fn)`           | ✅ Box3 | `(entity, message, tick) => boolean \| void`           | Player sends chat message; return `false` to cancel |
| `world.onVoxelDestroy(fn)`   | ✅ Box3 | `(entity, x, y, z, voxel, tick)`                       | Player breaks a block                               |
| `world.onBlockPlace(fn)`     | ⬆ MC    | `(entity, x, y, z, voxel, voxelId, tick)`              | Player places a block                               |
| `world.onBlockActivate(fn)`  | ⬆ MC    | `(entity, x, y, z, voxel, tick)`                       | Player right-clicks a block                         |
| `world.onInteract(fn)`       | ✅ Box3 | `(entity, target, tick)`                               | Player right-clicks an entity                       |
| `world.onVoxelContact(fn)`   | ✅ Box3 | `(entity, voxelId, x, y, z, contactType, force, tick)` | Entity contacts a block                             |
| `world.onEntityContact(fn)`  | ✅ Box3 | `(entity, other, tick)`                                | Two entities contact                                |
| `world.onEntitySeparate(fn)` | ✅ Box3 | `(entity, other, tick)`                                | Two entities separate                               |
| `world.onFluidEnter(fn)`     | ✅ Box3 | `(entity, fluid, x, y, z, tick)`                       | Entity enters a fluid                               |
| `world.onFluidLeave(fn)`     | ✅ Box3 | `(entity, fluid, x, y, z, tick)`                       | Entity leaves a fluid                               |
| `world.onEntityDeath(fn)`    | ⬆ MC    | `(entity, killer, tick)`                               | Entity dies; `killer` may be null                   |
| `world.onEntityDamage(fn)`   | ⬆ MC    | `(entity, amount, source, attacker, tick)`             | Entity takes damage (Pre phase)                     |
| `world.onPlayerRespawn(fn)`  | ⬆ MC    | `(entity, tick)`                                       | Player respawns                                     |
| `world.onButtonPressed(fn)`  | ⬆ MC    | `(entity, button, tick)`                               | Player presses a button                             |
| `world.onMessage(fn)`        | ⬆ MC    | `(from, data)`                                         | Receives `world.sendMessage()` message              |

### GameButtonType

The `button` parameter in `world.onButtonPressed` callbacks is one of the following string constants:

| Constant    | Description        |
| ----------- | ------------------ |
| `"WALK"`    | Walk (hold)        |
| `"RUN"`     | Run / sprint (hold) |
| `"CROUCH"`  | Crouch / sneak (hold) |
| `"JUMP"`    | Jump               |
| `"FLY"`     | Fly (hold)         |
| `"ACTION0"` | Screen button 0 (tap) |
| `"ACTION1"` | Screen button 1 (tap) |

```js
world.onButtonPressed((entity, button, tick) => {
  if (button === "JUMP") {
    player.directMessage("You pressed jump!");
  }
});
```

All `onXxx()` methods return `GameEventHandlerToken` — call `.cancel()` to unregister.

```js
world.onTick((info) => {
  // info.tick, info.prevTick, info.elapsedTimeMS, info.skip
  if (info.tick % 100 === 0) {
    world.say("Server tick: " + info.tick);
  }
});

world.onPlayerJoin((entity, tick) => {
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

### world.launchFirework(x, y, z, colors, shape)

⬆ MC Extension | Launches a firework with an array of `GameRGBColor` values for arbitrary RGB colors.

### world.launchFirework(pos, colors, shape)

⬆ GameVector3 + `GameRGBColor[]` overload.

```js
world.launchFirework(0, 100, 0, [new GameRGBColor(1, 0, 0), new GameRGBColor(1, 0.5, 0)], "large_ball");
```

### world.spawnParticle(type, x, y, z, count, dx, dy, dz, speed)

Spawn particles at coordinates. Particle type uses namespaced ID.

### world.spawnParticle(type, pos, count, dx, dy, dz, speed)

⬆ GameVector3 overload.

### world.spawnParticle(x, y, z, color, count, dx, dy, dz, speed)

⬆ MC Extension | Spawns colored particles (dust type) using `GameRGBColor` to specify the color.

### world.spawnParticle(pos, color, count, dx, dy, dz, speed)

⬆ GameVector3 + `GameRGBColor` overload.

```js
// Spawn red particles
world.spawnParticle(0, 100, 0, new GameRGBColor(1, 0, 0), 20, 0.5, 0.5, 0.5, 0.1);

// Spawn cyan particles
world.spawnParticle(entity.position, new GameRGBColor(0, 1, 1), 10, 0.2, 0.2, 0.2, 0);
```

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

## Custom Items

### world.loadCustomItems(packName)

⬆ MC Extension | Loads custom item definitions from a resource pack's `items.json`. Reads `resourcepacks/<packName>/items.json`, parses item definitions using Minecraft's native data component IDs as JSON keys. All items use `minecraft:paper` as the base, with `DataComponents` providing name, lore, texture, food, etc.

JSON format uses MC component ID prefixes:

| JSON Key                               | DataComponent                | Description                                            |
| -------------------------------------- | ---------------------------- | ------------------------------------------------------ |
| `minecraft:custom_model_data`          | `CUSTOM_MODEL_DATA`          | Model predicate value, matched by paper.json overrides |
| `minecraft:custom_name`                | `CUSTOM_NAME`                | Display name                                           |
| `minecraft:lore`                       | `LORE`                       | Lore text array                                        |
| `minecraft:max_stack_size`             | `MAX_STACK_SIZE`             | Max stack size (1–64), default 64                      |
| `minecraft:enchantment_glint_override` | `ENCHANTMENT_GLINT_OVERRIDE` | Enchantment foil effect                                |
| `minecraft:rarity`                     | `RARITY`                     | Rarity: `common`/`uncommon`/`rare`/`epic`              |
| `minecraft:food`                       | `FOOD`                       | Food properties (see sub-fields below)                 |

**`minecraft:food` sub-fields:**

| Sub-field        | Type  | Description                      |
| ---------------- | ----- | -------------------------------- |
| `nutrition`      | int   | Nutrition value (1–20)           |
| `saturation`     | float | Saturation modifier              |
| `can_always_eat` | bool  | Always edible                    |
| `eat_seconds`    | float | Eat time in seconds, ≤0.8 = fast |

```js
world.loadCustomItems("box3js-items");
// Loads all items defined in resourcepacks/box3js-items/items.json
// Items can then be given via player.giveCustomItem("arena_trophy", 1)
```

**Resource pack structure reference:**

```
resourcepacks/box3js-items/
├── pack.mcmeta
├── items.json                          # Item definitions
└── assets/
    ├── minecraft/models/item/
    │   └── paper.json                  # custom_model_data overrides
    └── box3js/
        ├── models/item/                # Model JSONs
        │   ├── arena_trophy.json
        │   ├── arena_stew.json
        │   └── arena_medal.json
        └── textures/item/              # PNG textures
            ├── arena_trophy.png
            ├── arena_stew.png
            └── arena_medal.png
```

**Note:** Textures require the client to load the resource pack. Without it, items still function (name/lore/food), but display the default paper texture.

## Cross-script Messaging

### world.sendMessage(target, data)

⬆ MC Extension | Send a message to another script project. `target` is `"*"` (broadcast) or a project name. Receivers listen via `world.onMessage()`.

### world.runCommand(cmd)

⬆ MC Extension | Execute a command as the server console.

```js
world.runCommand("time set day");
world.runCommand("weather clear");
```

## Structures & Advancements

### world.placeStructure(x, y, z, structureId)

⬆ MC Extension | Places a datapack structure template (NBT) at the given position.

### world.placeStructure(pos, structureId)

⬆ GameVector3 overload.

```js
world.placeStructure(
  0,
  100,
  0,
  "minecraft:village/plains/houses/plains_small_house_1",
);
world.placeStructure(pos, "box3js:arena");
```

### world.grantAdvancement(playerName, advancementId)

⬆ MC Extension | Grants an advancement to a player by name.

```js
world.grantAdvancement("Steve", "minecraft:story/mine_stone");
```

## Recipe Management

### world.listRecipes(filter)

⬆ MC Extension | Searches recipe IDs matching a keyword.

```js
var recipes = world.listRecipes("diamond");
console.log(recipes); // ["minecraft:diamond_sword", "minecraft:diamond_block", ...]
```

### world.removeRecipe(recipeId)

⬆ MC Extension | Blacklists a recipe so it's no longer craftable. Returns whether successful.

```js
world.removeRecipe("minecraft:iron_pickaxe");
```

### world.clearRecipes()

⬆ MC Extension | Clears the recipe blacklist, restoring all original recipes.

```js
world.clearRecipes();
```
