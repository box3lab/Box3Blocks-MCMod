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

Readonly. Player display name.

### player.userId

Readonly. Player UUID string (same as `entity.id`).

### player.opLevel

Gets/sets the player's operator permission level (0–4).

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

Gets/sets whether the player is invisible.

### player.scale

Readonly. Player model scale (Minecraft native scale, not Box3 scale).

```js
player.invisible = true;  // Invisible
console.log("Player scale: " + player.scale);
```

## Movement



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

Gets/sets flight permission (`mayfly`). When `true`, player can take off by pressing jump.

### player.flying

Gets/sets whether the player is currently flying (`flying`). Requires `canFly = true` first.

### player.flySpeed

Flying speed.

### player.disableFly

When set to `true`, immediately stops flight and disables flight permission.

### player.spectator

Readonly. Whether the player is in spectator mode.

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

⬆ MC Extension | Gets/sets team collision. Set to `false` to prevent players from pushing each other. Backed by the player's team `CollisionRule` (ALWAYS / NEVER).

```js
player.collision = false;  // Disable collision
console.log(player.collision);
```

## Health

⬆ MC Extension | Gets/sets player health. `ServerPlayer` is a `LivingEntity`, so health is operated on directly.

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

Gets/sets game mode. Get returns a name string; set accepts a string or number.

```js
player.gameMode = "creative";   // Creative
player.gameMode = "survival";   // Survival
player.gameMode = "adventure";  // Adventure
player.gameMode = "spectator";  // Spectator
// Or by number: 0=survival, 1=creative, 2=adventure, 3=spectator
```

## Camera



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

⬆ MC Extension | Makes the player look at the given coordinates.

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

Teleports the player to the given `GameVector3` coordinates.

### player.spawnPoint

Gets/sets the player's respawn point (`GameVector3`). When reading, returns the world spawn if the player hasn't set a personal respawn point.

```js
// Property-style set
player.spawnPoint = new GameVector3(0, 100, 0);
console.log(player.spawnPoint);
```

### player.setRespawnPoint(pos)

Sets the player's respawn point (method-style, equivalent to `spawnPoint` property).

### player.setSpawnPoint(pos)

Same as `setRespawnPoint`, Box3 standard naming.

### player.respawn()

Forces the player to respawn (only works when dead).

### player.dimension

⬆ MC Extension | Gets/sets the player's dimension. Setting it performs a cross-dimensional teleport.

```js
player.teleport(new GameVector3(0, 100, 0));
player.setRespawnPoint(new GameVector3(0, 100, 0));

// Cross-dimension teleport
player.dimension = "minecraft:the_nether";
player.teleport(new GameVector3(0, 70, 0));
```

## Kicking

### player.kick()

Kicks the player with the default reason "Kicked".

### player.kick(reason)

Kicks the player with a custom reason.

```js
player.kick("You have been removed from the game");
```

## Messaging

### player.directMessage(msg)

Sends a chat message visible only to this player (system message).

### player.directMessage(msg, color)

⬆ MC Extension | Sends a colored chat message.

```js
player.directMessage("Success!", new GameRGBColor(0, 1, 0));   // Green
player.directMessage("Warning!", new GameRGBColor(1, 0.5, 0)); // Orange
```

### player.actionBar(msg)

Sends a message displayed on the action bar (above the hotbar).

### player.title(title, subtitle)

Displays a screen title with default animation: fade-in 10 ticks, stay 70 ticks, fade-out 20 ticks.

### player.title(title, subtitle, fadeIn, stay, fadeOut)

⬆ MC Extension | Title with full animation parameters. `fadeIn`/`stay`/`fadeOut` are all in ticks (20 ticks = 1 second).

### player.dialog(config)

Shows a dialog panel. Pass `{content, options}`, returns `{index, value}`. Currently simplified in MC — sends system messages.

```js
var result = player.dialog({
  content: "Choose your path",
  options: ["Warrior", "Mage", "Archer"],
});
player.directMessage("You chose: " + result.value);
```

### player.link(href)

Sends a clickable URL link to the player (blue underlined text).

### player.onChat(handler)

Registers a per-player chat handler (more granular than global `world.onChat`, useful for dialog trees).

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

⬆ MC Extension | Gets/sets experience level.

### player.addExperienceLevels(levels)

⬆ MC Extension | Adds `levels` experience levels.

### player.food

⬆ MC Extension | Gets/sets food level (0–20).

### player.saturation

⬆ MC Extension | Gets/sets saturation level (0–20, floating-point).

```js
player.xp = 10;                 // Set to level 10
player.addExperienceLevels(3);  // Add 3 levels
player.food = 20;
player.saturation = 10;
```

## Inventory

All ⬆ MC Extension.

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

## Custom Container GUI

⬆ MC Extension | Opens a script-controlled container GUI (chest-like screen) for the player, with custom slot contents, click behavior, and close callbacks.

### player.openGUI(config?)

Opens a container GUI and returns a `GUIController` handle.

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `title` | `string` | `"Container"` | Container title |
| `rows` | `number` | `3` | Number of rows (1–6), 9 slots each |
| `slots` | `{ [slot: number]: string }` | `{}` | Pre-fill slots, key = slot index, value = item ID |

**Returned `GUIController` methods:**

| Method | Description |
|--------|-------------|
| `setItem(slot, itemId, count?)` | Place an item in the given slot |
| `getItem(slot)` | Get item info as `{ id, count }` |
| `onSlotClick(callback)` | Register click callback; `return false` to cancel |
| `onClose(callback)` | Register close callback (ESC or `close()` fires it) |
| `close()` | Close the container |

```js
world.onChat(function(entity, msg, tick) {
  if (msg === "!shop") {
    var gui = entity.player.openGUI({
      title: "§6§lShop",
      rows: 3,
      slots: {
        0: "minecraft:diamond",
        4: "minecraft:emerald",
        8: "minecraft:gold_ingot",
      },
    });

    gui.setItem(1, "minecraft:netherite_ingot", 5);

    gui.onSlotClick(function(slot, player) {
      console.log("Clicked slot: " + slot);
      if (slot === 0) return false;  // prevent taking the diamond
    });

    gui.onClose(function(player) {
      player.directMessage("Shop closed");
    });
  }
});
```

## Status Effects

### player.addEffect(effectId, duration, amplifier)

⬆ MC Extension | Applies a status effect. `duration` in ticks, `amplifier` starts at 0.

### player.addEffect(effectId, duration, amplifier, hideParticles)

⬆ MC Extension | Applies an effect, optionally hiding particles.

### player.clearEffects()

⬆ MC Extension | Removes all status effects.

```js
player.addEffect("minecraft:speed", 600, 2);
player.addEffect("minecraft:jump_boost", 99999, 1, true);  // Permanent, no particles
player.clearEffects();
```

## Sound & Commands

### player.playSound(path, volume, pitch)

⬆ MC Extension | Plays a sound to this player only. `path` is a namespace ID (e.g. `"minecraft:block.note_block.pling"`), `volume` 0–1, `pitch` 0.5–2.

### player.runCommand(cmd)

⬆ MC Extension | Executes a Minecraft command as this player.

```js
player.playSound("minecraft:block.note_block.pling", 0.8, 1.5);
player.runCommand("say hello");
```

## Advancements

### player.grantAdvancement(advancementId)

⬆ MC Extension | Grants an advancement to this player.

### player.revokeAdvancement(advancementId)

⬆ MC Extension | Revokes an advancement from this player.

```js
player.grantAdvancement("minecraft:story/mine_stone");
player.grantAdvancement("minecraft:adventure/kill_a_mob");
player.revokeAdvancement("minecraft:story/mine_stone");
```

## Tab List

### player.setPlayerListName(name)

⬆ MC Extension | Changes the player's display name in the tab list (supports color codes).

```js
player.setPlayerListName("§e[CP3] §f" + player.name);
player.setPlayerListName("§6★ §f" + player.name);

// Reset to original name
player.setPlayerListName(player.name);
```
