# player — Player API

The `player` object is obtained via `entity.player` and represents a logged-in player. It includes all `entity` capabilities plus player-specific features: inventory, XP, flight, messaging, teleport, etc.

```js
world.onPlayerJoin((entity) => {
  var p = entity.player; // p is the player object
  p.directMessage("Welcome back, " + p.name + "!");
});
```

## Basic Info

### player.name

✅ Box3 API | Read-only. Player name.

### player.userId

✅ Box3 API | Read-only. Player UUID string.

### player.getOpLevel()

⬆ MC Extension | Get/set the player's operator permission level (0–4). 0 = normal player, 1 = bypass spawn protection, 2 = most commands, 3 = manage players, 4 = full access.

```js
if (player.getOpLevel() >= 2) {
  // operations requiring permission level 2
}
player.opLevel = 3; // set to level 3
```

## Appearance

### player.invisible

✅ Box3 API | Get/set whether the player is invisible.

### player.scale

✅ Box3 API | Read-only. Player scale value.

```js
player.invisible = true; // invisible
```

## Movement

All ✅ Box3 API.

### player.walkSpeed

Walking speed, corresponds to `MOVEMENT_SPEED` attribute. Default ~0.1.

### player.runSpeed

Running speed. Automatically maintained as `walkSpeed × 1.3`.

### player.jumpPower

Jump strength, corresponds to `JUMP_STRENGTH` attribute.

### player.moveState

Read-only. Current movement state: `"FLYING"`, `"SWIM"`, `"JUMP"`, `"FALL"`, `"GROUND"`.

### player.walkState

Read-only. Current walking state: `"CROUCH"`, `"RUN"`, `"WALK"`, `"NONE"`.

```js
player.walkSpeed = 0.2; // speed up
player.jumpPower = 0.6; // jump higher

world.onTick(() => {
  if (player.walkState === "RUN") {
    // player is sprinting
  }
});
```

## Flight

### player.canFly

✅ Box3 API | Get/set flight permission (`mayfly`). When `true`, player can take off by pressing jump.

### player.flying

✅ Box3 API | Get/set whether currently flying (`flying`). Requires `canFly = true` first.

### player.flySpeed

✅ Box3 API | Flight speed.

### player.disableFly

✅ Box3 API | When set to `true`, immediately stops flight and disables flight permission.

### player.spectator

✅ Box3 API | Read-only. Whether the player is in spectator mode.

```js
// Allow flight
player.canFly = true;
player.flySpeed = 0.1;

// Make player take off
player.flying = true;

// Force landing
player.disableFly = true;
```

### player.collision

⬆ MC Extension | Get/set team collision. Set to `false` to prevent players pushing each other. Modifies the team's `CollisionRule` internally.

```js
player.collision = false; // disable collision
console.log(player.collision); // false
```

## Health

⬆ MC Extension | Get/set player health. `ServerPlayer` is a `LivingEntity`, so health is accessed directly.

### player.hp

Get/set current health.

### player.maxHp

Get/set maximum health.

```js
// Set class-based health
player.maxHp = 40; // Warrior 40 HP
player.hp = 40; // full health

// If current HP exceeds new max, it's auto-capped
player.maxHp = 20;
// player.hp is auto-clamped to 20
```

> Typically set during `!join` or `!ready` phase for class-based HP. Use `player.addEffect("minecraft:instant_health", ...)` for healing afterward.

## Gamemode

### player.gameMode

✅ Box3 API | Get/set the player's gamemode. Get returns the name string; set accepts a string or number.

```js
player.gameMode = "creative"; // creative mode
player.gameMode = "survival"; // survival mode
player.gameMode = "adventure"; // adventure mode
player.gameMode = "spectator"; // spectator mode
// or numbers: 0=survival, 1=creative, 2=adventure, 3=spectator
```

## Camera

All ✅ Box3 API.

### player.cameraMode

Get/set camera mode: `"FPS"` (first person) or `"FOLLOW"` (follow entity).

### player.cameraEntity

Set or get the followed entity object (`Box3JSEntity`).

### player.cameraPitch / player.cameraYaw

Camera pitch and yaw angles.

### player.facingDirection

Read-only `GameVector3`. The player's look direction unit vector.

### player.cameraTarget

Read-only `GameVector3`. The point 5 blocks ahead of the player's eyes.

### player.lookAt(x, y, z)

⬆ MC Extension | Make the player look at the given coordinates.

### player.lookAt(pos)

⬆ GameVector3 overload.

```js
player.lookAt(10, 100, 10);
player.lookAt(target.position);

// Get look direction
var dir = player.facingDirection;
var target = player.cameraTarget;
```

## Teleport & Respawn

### player.teleport(pos)

✅ Box3 API | Teleport the player to the given `GameVector3` coordinates.

### player.setRespawnPoint(pos)

✅ Box3 API | Set the player's respawn point.

### player.respawn()

✅ Box3 API | Force the player to respawn (only effective when dead).

### player.dimension

⬆ MC Extension | Get/set the player's dimension. Set can teleport cross-dimension.

```js
player.teleport(new GameVector3(0, 100, 0));
player.setRespawnPoint(new GameVector3(0, 100, 0));

// Cross-dimension teleport
player.dimension = "minecraft:the_nether";
player.teleport(new GameVector3(0, 70, 0));
```

## Kick

### player.kick()

✅ Box3 API | Kick the player, default reason "Kicked".

### player.kick(reason)

✅ Box3 API | Kick the player with a custom reason.

```js
player.kick("You have been removed from the game");
```

## Messaging

### player.directMessage(msg)

✅ Box3 API | Send a chat message to the player.

### player.actionBar(msg)

✅ Box3 API | Send an action bar message (above the hotbar).

### player.title(title, subtitle)

✅ Box3 API | Send a screen title to the player. Uses default animation parameters.

### player.title(title, subtitle, fadeIn, stay, fadeOut)

⬆ MC Extension | Full-parameter title. `fadeIn`/`stay`/`fadeOut` are in ticks.

### player.dialog(config)

✅ Box3 API | Show a dialog. Pass `{content, options}` config, returns `{index, value}`. Currently implemented as a simplified system message in MC.

```js
var result = player.dialog({
  content: "Choose your path",
  options: ["Warrior", "Mage", "Archer"],
});
player.directMessage("You chose: " + result.value);
```

### player.link(href)

✅ Box3 API | Send a clickable link to the player.

### player.onChat(handler)

✅ Box3 API | Register a per-player chat callback (for finer control, commonly used in dialogue trees).

```js
player.directMessage("Hello!");
player.actionBar("§eType !help for help");
player.title("§6§lBOSS FIGHT", "§7Defeat all enemies", 10, 60, 10);
player.link("https://example.com");

// Dialogue tree
player.directMessage("Enter your choice: A or B");
player.onChat((entity, msg, tick) => {
  if (msg === "A") {
    player.directMessage("You chose A");
  }
});
```

## XP & Food

### player.xp

⬆ MC Extension | Get/set experience level.

### player.addExperienceLevels(levels)

⬆ MC Extension | Add `levels` experience levels.

### player.food

⬆ MC Extension | Get/set food level (0–20).

### player.saturation

⬆ MC Extension | Get/set saturation (0–20, float).

```js
player.xp = 10; // set to level 10
player.addExperienceLevels(3); // add 3 levels
player.food = 20;
player.saturation = 10;
```

## Inventory

All ⬆ MC Extension.

### player.giveItem(itemId, count)

Give an item.

### player.clearInventory()

Clear the inventory.

### player.getHeldItem()

Get the main hand item, returns `{id, count}`. Empty hand returns `{id: "minecraft:air", count: 0}`.

```js
player.giveItem("minecraft:diamond_sword", 1);
player.giveItem("minecraft:golden_apple", 5);
player.giveItem("minecraft:arrow", 64);

var held = player.getHeldItem();
console.log(held.id, held.count); // "minecraft:diamond_sword" 1

player.clearInventory();
```

### player.giveEnchantedItem(itemId, count, enchants)

Give an enchanted item. `enchants` is a `{enchantmentId: level}` object.

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

Give an item with a custom name and lore. `lore` is a string array.

```js
player.giveNamedItem("minecraft:gold_ingot", 1, "§6§lParkour Gold Medal", [
  "§7Sky Parkour Championship",
  "§eFinish time: 1:23.450",
]);

player.giveNamedItem("minecraft:diamond_sword", 1, "§c§lBlade of Flame", [
  "§7Bound: Fire",
  "§eRight-click: Launch fireball",
]);
```

## Potion Effects

### player.addEffect(effectId, duration, amplifier)

⬆ MC Extension | Add a potion effect. `duration` in ticks, `amplifier` starts at 0.

### player.addEffect(effectId, duration, amplifier, hideParticles)

⬆ MC Extension | Add effect with optional particle hiding.

### player.clearEffects()

⬆ MC Extension | Remove all potion effects.

```js
player.addEffect("minecraft:speed", 600, 2);
player.addEffect("minecraft:jump_boost", 99999, 1, true); // permanent, no particles
player.clearEffects();
```

## Sound & Commands

### player.playSound(path, volume, pitch)

⬆ MC Extension | Play a sound to this player. `path` is a namespaced ID.

### player.runCommand(cmd)

⬆ MC Extension | Execute a command as this player.

```js
player.playSound("minecraft:block.note_block.pling", 0.8, 1.5);
player.runCommand("say hello");
```

## Tab List

### player.setPlayerListName(name)

⬆ MC Extension | Modify this player's displayed name in the tab list.

```js
player.setPlayerListName("§e[CP3] §f" + player.name);
player.setPlayerListName("§6★ §f" + player.name);

// Reset to original name
player.setPlayerListName(player.name);
```

## Box3 API List

| API                                                         | Type    |
| ----------------------------------------------------------- | ------- |
| `name`                                                      | ✅ Box3 |
| `userId`                                                    | ✅ Box3 |
| `invisible`                                                 | ✅ Box3 |
| `scale`                                                     | ✅ Box3 |
| `walkSpeed` / `runSpeed` / `jumpPower`                      | ✅ Box3 |
| `moveState` / `walkState`                                   | ✅ Box3 |
| `canFly` / `flying` / `flySpeed` / `disableFly`             | ✅ Box3 |
| `spectator`                                                 | ✅ Box3 |
| `gameMode`                                                  | ✅ Box3 |
| `cameraMode` / `cameraEntity` / `cameraPitch` / `cameraYaw` | ✅ Box3 |
| `facingDirection` / `cameraTarget`                          | ✅ Box3 |
| `setRespawnPoint()` / `respawn()`                           | ✅ Box3 |
| `kick()`                                                    | ✅ Box3 |
| `teleport()`                                                | ✅ Box3 |
| `directMessage()` / `actionBar()`                           | ✅ Box3 |
| `title()` (2-param)                                         | ✅ Box3 |
| `dialog()`                                                  | ✅ Box3 |
| `link()`                                                    | ✅ Box3 |
| `onChat()` (player-level)                                   | ✅ Box3 |

## MC Extension List

| API                                                  | Type |
| ---------------------------------------------------- | ---- |
| `collision`                                          | ⬆ MC |
| `title()` (5-param)                                  | ⬆ MC |
| `hp` / `maxHp`                                       | ⬆ MC |
| `xp` / `addExperienceLevels()`                       | ⬆ MC |
| `food` / `saturation`                                | ⬆ MC |
| `giveItem()` / `clearInventory()` / `getHeldItem()`  | ⬆ MC |
| `giveEnchantedItem()` / `giveNamedItem()`            | ⬆ MC |
| `addEffect()` (3/4-param) / `clearEffects()`         | ⬆ MC |
| `playSound()`                                        | ⬆ MC |
| `dimension`                                          | ⬆ MC |
| `lookAt()`                                           | ⬆ MC |
| `runCommand()`                                       | ⬆ MC |
| `setPlayerListName()`                                | ⬆ MC |
| `getOpLevel()` / `opLevel`                           | ⬆ MC |
