# player — Player API

The `player` object is obtained via `entity.player` and represents a logged-in player. It includes all `entity` capabilities (like `hp`, `position`, `tags()`, etc.) plus player-specific features: inventory, XP, flight, messaging, teleport, etc.

```js
world.onPlayerJoin(function(entity, tick) {
  var p = entity.player;  // p is the player object
  p.directMessage("Welcome back, " + p.name + "!");
});
```

## Basic Identity

### player.name

✅ Box3 API | Readonly. Player display name.

### player.userId

✅ Box3 API | Readonly. Player UUID string (same as `entity.id`).

### player.opLevel

✅ Box3 API | Gets/sets the player's operator permission level (0–4).

| Level | Description |
|-------|-------------|
| 0 | Normal player |
| 1 | Can bypass spawn protection |
| 2 | Can use most commands |
| 3 | Can manage players |
| 4 | Full admin (equivalent to `/op`) |

```js
if (player.opLevel >= 2) {
  // Operations requiring permission level 2
}
player.opLevel = 3;  // Set to level 3 via property
```

There is also `player.getOpLevel()` method returning the permission level number.

## Appearance

### player.invisible

✅ Box3 API | Gets/sets whether the player is invisible.

### player.scale

✅ Box3 API | Readonly. Player model scale (Minecraft native scale, not Box3 scale).

```js
player.invisible = true;  // Invisible
console.log("Player scale: " + player.scale);
```

## Movement

All ✅ Box3 API.

### player.walkSpeed

Walk speed, maps to `MOVEMENT_SPEED` attribute base value. Default ~0.1.

### player.runSpeed

Run/sprint speed. Get returns `walkSpeed × 1.3`; set auto-calculates `walkSpeed` to maintain the 1.3× ratio.

### player.jumpPower

Jump strength, maps to `JUMP_STRENGTH` attribute base value. Default ~0.42.

### player.enableJump

Gets/sets whether jumping is enabled. Default `true`. When set to `false`, saves the current jump strength and sets `JUMP_STRENGTH` to 0; set back to `true` to restore.

```js
player.enableJump = false;  // Disable jumping
player.enableJump = true;   // Re-enable jumping
```

### player.crouchSpeed

Gets/sets crouch speed (custom property, default `0.0`). MC has no independent sneak speed attribute; scripts can read this for custom logic.

### player.swimSpeed

Gets/sets swim speed. Backed by the `WATER_MOVEMENT_EFFICIENCY` attribute.

```js
player.swimSpeed = 0.5;  // Swim faster
```

### player.moveState

Readonly. Current movement state string:

| Value | Description |
|-------|-------------|
| `"FLYING"` | Currently flying |
| `"SWIM"` | In water |
| `"JUMP"` | Jumping upward |
| `"FALL"` | Falling |
| `"GROUND"` | On the ground |

### player.walkState

Readonly. Current walk state string:

| Value | Description |
|-------|-------------|
| `"CROUCH"` | Crouching / sneaking |
| `"RUN"` | Sprinting |
| `"WALK"` | Walking |
| `"NONE"` | Standing still |

```js
player.walkSpeed = 0.2;   // Speed up
player.jumpPower = 0.6;   // Jump higher
player.swimSpeed = 0.3;   // Swim speed

world.onTick(function() {
  if (player.walkState === "RUN") {
    // Player is sprinting
  }
});
```

## Flight

### player.canFly

✅ Box3 API | Gets/sets flight permission (`mayfly`). When `true`, player can take off by pressing jump.

### player.flying

✅ Box3 API | Gets/sets whether the player is currently flying (`flying`). Requires `canFly = true` first.

### player.flySpeed

✅ Box3 API | Flying speed.

### player.disableFly

✅ Box3 API | When set to `true`, immediately stops flight and disables flight permission.

### player.spectator

✅ Box3 API | Readonly. Whether the player is in spectator mode.

```js
// Enable flight
player.canFly = true;
player.flySpeed = 0.1;

// Make player take off
player.flying = true;

// Force landing
player.disableFly = true;
```

### player.collision

⬆ MC extension | Gets/sets team collision. Set to `false` to prevent players from pushing each other. Backed by the player's team `CollisionRule` (ALWAYS / NEVER).

```js
player.collision = false;  // Disable collision
console.log(player.collision);
```

## Health

⬆ MC extension | Gets/sets player health. `ServerPlayer` is a `LivingEntity`, so health is operated on directly.

### player.hp

Gets/sets current health.

### player.maxHp

Gets/sets maximum health.

### player.dead

Readonly. Whether the player is dead or dying (`isDeadOrDying()`).

```js
// Set class-specific health
player.maxHp = 40;  // Warrior 40 HP
player.hp = 40;     // Full health

// Current health is clamped when max decreases
player.maxHp = 20;
// player.hp auto-clamped to 20

if (player.dead) {
  console.log("Player is dead");
}
```

## Game Mode

### player.gameMode

✅ Box3 API | Gets/sets game mode. Get returns a name string; set accepts a string or number.

```js
player.gameMode = "creative";   // Creative
player.gameMode = "survival";   // Survival
player.gameMode = "adventure";  // Adventure
player.gameMode = "spectator";  // Spectator
// Or by number: 0=survival, 1=creative, 2=adventure, 3=spectator
```

## Camera

All ✅ Box3 API.

### player.cameraMode

Gets/sets the camera mode: `"FPS"` (first-person) or `"FOLLOW"` (follow entity). Setting to `"FPS"` clears the follow target.

### player.cameraEntity

Gets/sets the entity to follow (`GameEntity`). Setting an entity auto-switches camera mode to `"FOLLOW"`; setting `null` switches back to `"FPS"`.

### player.cameraPitch / player.cameraYaw

Camera pitch (vertical angle) and yaw (horizontal angle). Note: in MC, yaw maps to Y rotation (yRot), pitch maps to X rotation (xRot).

### player.facingDirection

Readonly `GameVector3`. The unit vector of the player's look direction.

### player.cameraTarget

Readonly `GameVector3`. A point 5 blocks ahead of the player's eyes.

### player.lookAt(x, y, z)

⬆ MC extension | Makes the player look at the given coordinates.

### player.lookAt(pos)

⬆ GameVector3 overload.

```js
player.lookAt(10, 100, 10);
player.lookAt(target.position);

// Get view information
var dir = player.facingDirection;
var target = player.cameraTarget;
```

## Teleport & Respawn

### player.teleport(pos)

✅ Box3 API | Teleports the player to the given `GameVector3` coordinates.

### player.spawnPoint

✅ Box3 API | Gets/sets the player's respawn point (`GameVector3`). When reading, returns the world spawn if the player hasn't set a personal respawn point.

```js
// Property-style set
player.spawnPoint = new GameVector3(0, 100, 0);
console.log(player.spawnPoint);
```

### player.setRespawnPoint(pos)

✅ Box3 API | Sets the player's respawn point (method-style, equivalent to `spawnPoint` property).

### player.setSpawnPoint(pos)

✅ Box3 API | Same as `setRespawnPoint`, Box3 standard naming.

### player.respawn()

✅ Box3 API | Forces the player to respawn (only works when dead).

### player.dimension

⬆ MC extension | Gets/sets the player's dimension. Setting it performs a cross-dimensional teleport.

```js
player.teleport(new GameVector3(0, 100, 0));
player.setRespawnPoint(new GameVector3(0, 100, 0));

// Cross-dimension teleport
player.dimension = "minecraft:the_nether";
player.teleport(new GameVector3(0, 70, 0));
```

## Kicking

### player.kick()

✅ Box3 API | Kicks the player with the default reason "Kicked".

### player.kick(reason)

✅ Box3 API | Kicks the player with a custom reason.

```js
player.kick("You have been removed from the game");
```

## Messaging

### player.directMessage(msg)

✅ Box3 API | Sends a chat message visible only to this player (system message).

### player.actionBar(msg)

✅ Box3 API | Sends a message displayed on the action bar (above the hotbar).

### player.title(title, subtitle)

✅ Box3 API | Displays a screen title with default animation: fade-in 10 ticks, stay 70 ticks, fade-out 20 ticks.

### player.title(title, subtitle, fadeIn, stay, fadeOut)

⬆ MC extension | Title with full animation parameters. `fadeIn`/`stay`/`fadeOut` are all in ticks (20 ticks = 1 second).

### player.dialog(config)

✅ Box3 API | Shows a dialog panel. Pass `{content, options}`, returns `{index, value}`. Currently simplified in MC — sends system messages.

```js
var result = player.dialog({
  content: "Choose your path",
  options: ["Warrior", "Mage", "Archer"],
});
player.directMessage("You chose: " + result.value);
```

### player.link(href)

✅ Box3 API | Sends a clickable URL link to the player (blue underlined text).

### player.onChat(handler)

✅ Box3 API | Registers a per-player chat handler (more granular than global `world.onChat`, useful for dialog trees).

```js
player.directMessage("Hello!");
player.actionBar("§eType !help for help");
player.title("§6§lBOSS FIGHT", "§7Defeat all enemies", 10, 60, 10);
player.link("https://example.com");

// Dialog tree
player.directMessage("Enter your choice: A or B");
player.onChat(function(entity, msg, tick) {
  if (msg === "A") {
    player.directMessage("You chose A");
  }
});
```

## Experience & Food

### player.xp

⬆ MC extension | Gets/sets experience level.

### player.addExperienceLevels(levels)

⬆ MC extension | Adds `levels` experience levels.

### player.food

⬆ MC extension | Gets/sets food level (0–20).

### player.saturation

⬆ MC extension | Gets/sets saturation level (0–20, floating-point).

```js
player.xp = 10;                 // Set to level 10
player.addExperienceLevels(3);  // Add 3 levels
player.food = 20;
player.saturation = 10;
```

## Inventory

All ⬆ MC extension.

### player.giveItem(itemId, count)

Gives items to the player.

### player.giveEnchantedItem(itemId, count, enchants)

Gives an enchanted item. `enchants` is an `{enchantmentId: level}` object.

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

Gives an item with a custom name and lore. `lore` is a string array, one line per entry.

```js
player.giveNamedItem("minecraft:gold_ingot", 1, "§6§lParkour Medal", [
  "§7Sky Parkour Championship",
  "§eFinishing time: 1:23.450",
]);

player.giveNamedItem("minecraft:diamond_sword", 1, "§c§lFlameblade", [
  "§7Bound: Fire",
  "§eRight-click: Launch fireball",
]);
```

### player.getHeldItem()

Returns the currently held main-hand item as `{id, count}`. Empty hand returns `{id: "minecraft:air", count: 0}`.

```js
var held = player.getHeldItem();
console.log(held.id, held.count);  // "minecraft:diamond_sword" 1
```

### player.clearInventory()

Clears the entire inventory (including armor slots and offhand).

```js
player.clearInventory();
```

## Status Effects

### player.addEffect(effectId, duration, amplifier)

⬆ MC extension | Applies a status effect. `duration` in ticks, `amplifier` starts at 0.

### player.addEffect(effectId, duration, amplifier, hideParticles)

⬆ MC extension | Applies an effect, optionally hiding particles.

### player.clearEffects()

⬆ MC extension | Removes all status effects.

```js
player.addEffect("minecraft:speed", 600, 2);
player.addEffect("minecraft:jump_boost", 99999, 1, true);  // Permanent, no particles
player.clearEffects();
```

## Sound & Commands

### player.playSound(path, volume, pitch)

⬆ MC extension | Plays a sound to this player only. `path` is a namespace ID (e.g. `"minecraft:block.note_block.pling"`), `volume` 0–1, `pitch` 0.5–2.

### player.runCommand(cmd)

⬆ MC extension | Executes a Minecraft command as this player.

```js
player.playSound("minecraft:block.note_block.pling", 0.8, 1.5);
player.runCommand("say hello");
```

## Tab List

### player.setPlayerListName(name)

⬆ MC extension | Changes the player's display name in the tab list (supports color codes).

```js
player.setPlayerListName("§e[CP3] §f" + player.name);
player.setPlayerListName("§6★ §f" + player.name);

// Reset to original name
player.setPlayerListName(player.name);
```
