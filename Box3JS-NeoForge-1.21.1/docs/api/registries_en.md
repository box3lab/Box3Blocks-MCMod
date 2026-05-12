# Custom Registries (registries API)

> **Server-side only.** `registries` is `undefined` on the client. For client code, use the ResourceLocation string directly (e.g. `audio.playSound("colorzone:victory_fanfare", 1.0, 1.0)`).
>
> **Only available in compiled JAR mode (`/box3script compile`).** In interpreted mode (`/box3script start`), `registries` is `undefined`.
>
> **The compiled JAR must be installed on both server and client** for block textures/models to render. Without it on the client, blocks appear as purple/black missing textures.

Blocks, items, and sound events are declared in JSON config files. At compile time, `DeferredRegister` code is generated and injected into the `@Mod` class. Assets are bundled from the project's `assets/` directory into the JAR.

## Project Layout

```
mygame/
├── registries/
│   ├── blocks.json          ← block definitions
│   ├── items.json           ← item definitions
│   ├── creativeTabs.json    ← creative tab definitions
│   └── sounds.json          ← sound event definitions
├── assets/
│   ├── textures/block/      ← block textures (16×16 to 64×64 PNG)
│   ├── textures/item/       ← item textures (16×16 to 64×64 PNG)
│   ├── models/block/        ← block model JSON
│   ├── models/item/         ← item model JSON
│   ├── blockstates/         ← blockstate JSON
│   └── sounds/              ← .ogg sound files
└── src/server/app.ts        ← game logic
```

## blocks.json

One entry per block. Supported properties:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `hardness` | float | `1.0` | Destroy time |
| `resistance` | float | `1.0` | Explosion resistance |
| `sound` | string | `"stone"` | Sound type: `wood`/`stone`/`metal`/`glass`/`wool`/`sand`/`snow`/`slime`/`anvil`/`gravel`/`grass`/`bamboo`/`netherite`/`empty` |
| `lightLevel` | int | `0` | Light emission (0–15) |
| `mapColor` | string | `"stone"` | Minimap color |
| `friction` | float | `0.6` | Slipperiness |
| `speedFactor` | float | `1.0` | Walk speed multiplier |
| `jumpFactor` | float | `1.0` | Jump height multiplier |
| `noOcclusion` | bool | `false` | No face culling (transparent) |
| `noCollision` | bool | `false` | No collision (pass-through) |
| `requiresTool` | bool | `false` | Requires correct tool to drop |
| `instabreak` | bool | `false` | Breaks instantly |
| `creativeTab` | string | `""` | Creative tab ID (matches a key in creativeTabs.json) |

### mapColor values

`none`, `grass`, `sand`, `wool`, `fire`, `ice`, `metal`, `plant`, `snow`, `clay`, `dirt`, `stone`, `water`, `wood`, `quartz`, `gold`, `diamond`, `lapis`, `emerald`, `podzol`, `nether`, `color_orange`, `color_magenta`, `color_light_blue`, `color_yellow`, `color_light_green`, `color_pink`, `color_gray`, `color_light_gray`, `color_cyan`, `color_purple`, `color_blue`, `color_brown`, `color_green`, `color_red`, `color_black`

### Example

```json
{
  "ruby_block": {
    "hardness": 5.0,
    "resistance": 6.0,
    "sound": "metal",
    "lightLevel": 7,
    "mapColor": "color_red",
    "requiresTool": true,
    "creativeTab": "my_blocks"
  },
  "glass_block": {
    "hardness": 0.3,
    "resistance": 0.3,
    "sound": "glass",
    "noOcclusion": true,
    "creativeTab": "my_blocks"
  }
}
```

## creativeTabs.json

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `title` | string | — | Tab display name |
| `icon` | string | `""` | Icon block ID (matches a key in blocks.json) |
| `searchBar` | bool | `false` | Add a search bar |
| `rightAligned` | bool | `false` | Place on the right |

### Example

```json
{
  "my_blocks": {
    "title": "My Custom Blocks",
    "icon": "ruby_block",
    "searchBar": true
  }
}
```

## items.json

One entry per item. Supported properties:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `displayName` | string | — (required) | Item display name |
| `type` | string | `"item"` | `"item"` or `"food"` |
| `rarity` | string | `"common"` | Rarity: `common`/`uncommon`/`rare`/`epic` |
| `maxStackSize` | int | `64` | Max stack size (1–64) |
| `glint` | bool | `false` | Enchantment glint effect |
| `creativeTab` | string | `""` | Creative tab ID |
| `nutrition` | int | `4` | (food only) Hunger points restored |
| `saturation` | float | `0.6` | (food only) Saturation modifier |
| `alwaysEdible` | bool | `false` | (food only) Can eat when full |

Item textures follow the same pattern as block textures: `assets/textures/item/<id>.png` + `assets/models/item/<id>.json`.

### Example

```json
{
  "chocolate": {
    "displayName": "Chocolate Bar",
    "type": "food",
    "nutrition": 4,
    "saturation": 0.6,
    "alwaysEdible": true,
    "maxStackSize": 64,
    "creativeTab": "my_items"
  },
  "trophy": {
    "displayName": "Golden Trophy",
    "type": "item",
    "rarity": "uncommon",
    "maxStackSize": 1,
    "glint": true,
    "creativeTab": "my_items"
  }
}
```

> **Note:** Creative tab icon lookup searches items first, then blocks. If `creativeTabs.json`'s `icon` matches an item key, that item will be used as the icon.

### Equipment Types (Tools & Armor)

The `type` field supports these equipment types, which generate the corresponding Java class at compile time:

| `type` | Java Class | Description |
|--------|-----------|-------------|
| `"sword"` | `SwordItem` | Sword |
| `"pickaxe"` | `PickaxeItem` | Pickaxe |
| `"axe"` | `AxeItem` | Axe |
| `"shovel"` | `ShovelItem` | Shovel |
| `"hoe"` | `HoeItem` | Hoe |
| `"helmet"` | `ArmorItem` | Helmet |
| `"chestplate"` | `ArmorItem` | Chestplate |
| `"leggings"` | `ArmorItem` | Leggings |
| `"boots"` | `ArmorItem` | Boots |

Equipment-specific property:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `tier` | string | `"iron"` | Material tier (applies to both tools and armor) |
| `armorTexture` | string | `""` | Custom armor texture name. When set, armor uses `assets/<modid>/textures/models/armor/<value>_layer_1.png` and `_layer_2.png`. Leave empty to use the vanilla tier texture |

`tier` values and their effects:

| tier | Tools (Tiers) | Armor (ArmorMaterials) |
|------|--------------|----------------------|
| `wood` | Wood (59 durability) | — |
| `stone` | Stone (131 durability) | — |
| `leather` | — | Leather |
| `chain` | — | Chain |
| `iron` | Iron (250 durability) | Iron |
| `gold` | Gold (32 durability) | Gold |
| `diamond` | Diamond (1561 durability) | Diamond |
| `netherite` | Netherite (2031 durability) | Netherite |
| `turtle` | — | Turtle |

Equipment examples:

```json
{
  "ruby_sword": {
    "displayName": "Ruby Sword",
    "type": "sword",
    "tier": "diamond",
    "rarity": "epic",
    "glint": true,
    "creativeTab": "my_equipment"
  },
  "ruby_chestplate": {
    "displayName": "Ruby Chestplate",
    "type": "chestplate",
    "tier": "iron",
    "rarity": "rare",
    "creativeTab": "my_equipment"
  }
}
```

> **Note:** Equipment items always have `maxStackSize` fixed to 1 (unstackable). `nutrition`/`saturation`/`alwaysEdible` only apply to `"food"` type.

## sounds.json

One entry per sound event. Each key becomes a `SoundEvent` registered to the `minecraft:sound_event` registry at compile time.

### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `subtitle` | string | `""` | Shown in the subtitles overlay when the sound plays |
| `stream` | bool | `false` | Stream from disk instead of preloading into memory. Use for long background music or ambient tracks |

### Sound Files

Place `.ogg` files in `assets/sounds/<id>.ogg` — one file per sound event key.

The compiler auto-generates `assets/<modId>/sounds.json` in the standard Minecraft resource pack format. You do **not** need to create this file yourself.

### Example

```json
{
  "victory_fanfare": {
    "subtitle": "Victory!"
  },
  "skill_cast": {},
  "background_music": {
    "subtitle": "Eerie ambience",
    "stream": true
  }
}
```

Corresponding files:

```
assets/sounds/victory_fanfare.ogg
assets/sounds/skill_cast.ogg
assets/sounds/background_music.ogg
```

### Server-side usage

```ts
const s = registries.getSound("victory_fanfare");
if (s) {
  // Play for all players at a location
  world.playSound(s.soundId, x, y, z, 1.0, 1.0);

  // Play for a specific player only
  player.playSound(s.soundId, 1.0, 1.0);
}
```

### Client-side usage

On the client, skip `registries` and use the ResourceLocation string directly:

```ts
// Play custom sound on the client
audio.playSound("colorzone:victory_fanfare", 1.0, 1.0);
```

## assets/ Directory

Follows the standard Minecraft resource pack structure:

```
assets/<modId>/
├── blockstates/<blockId>.json       ← auto-generated unless you provide custom
├── models/block/<blockId>.json      ← auto-generated; can override
├── models/item/<blockId>.json       ← auto-generated for blocks; required for items
├── lang/
│   ├── en_us.json                   ← created manually
│   ├── zh_cn.json                   ← created manually
│   └── ja_jp.json                   ← optional: add any language
├── sounds.json                      ← auto-generated from registries/sounds.json
└── textures/
    ├── block/<blockId>_<face>.png
    └── item/<itemId>.png
```

> **Note:** `<modId>` is taken from the `name` field in `package.json` (text after the last `/`, e.g. `@scope/mygame` → `mygame`).

At compile time, `assets/` is automatically bundled as `assets/<modId>/` in the JAR.

### Languages

Language files must be **created manually** in `assets/lang/`. They are not auto-generated. At minimum, provide `en_us.json` and `zh_cn.json`. You can also add more languages:

```
mygame/
└── assets/
    └── lang/
        ├── en_us.json      ← English translations
        ├── zh_cn.json      ← Chinese translations
        ├── ja_jp.json      ← your Japanese translations
        └── ko_kr.json      ← your Korean translations
```

Format follows the standard Minecraft lang file:

```json
{
  "block.mygame.ruby_block": "Ruby Block",
  "item.mygame.ruby_sword": "Ruby Sword",
  "item.mygame.chocolate": "Chocolate Bar",
  "itemGroup.mygame.my_blocks": "My Blocks"
}
```

The MC client automatically loads the correct file based on its language setting — no extra configuration needed.

## registries Runtime API

### `registries.getBlock(id)`

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `string` | Block ID (key from blocks.json) |

Returns: `{ block: any, itemId: string }` or `null` if not found.

- `block.block` — `Block` instance, usable with `voxels.setVoxel()`
- `block.itemId` — `"modId:blockId"` string, usable with `player.giveItem()`

### `registries.hasBlock(id)`

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `string` | Block ID |

Returns `boolean` — whether the block is registered.

### `registries.listBlocks()`

Returns `string[]` — all registered block IDs.

```ts
// Iterate all blocks
registries.listBlocks().forEach(id => {
  const block = registries.getBlock(id);
  player.giveItem(block.itemId, 1);
});
```

### `registries.getItem(id)`

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `string` | Item ID (key from items.json) |

Returns: `{ item: any, itemId: string }` or `null` if not found.

- `item.item` — `Item` instance
- `item.itemId` — `"modId:itemId"` string, usable with `player.giveItem()`

```ts
const chocolate = registries.getItem("chocolate");
if (chocolate) {
  player.giveItem(chocolate.itemId, 8);
}
```

### `registries.hasItem(id)`

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `string` | Item ID |

Returns `boolean` — whether the item is registered.

### `registries.listItems()`

Returns `string[]` — all registered item IDs.

```ts
// Iterate all items
registries.listItems().forEach(id => {
  const item = registries.getItem(id);
  player.giveItem(item.itemId, 1);
});
```

### `registries.getSound(id)`

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `string` | Sound ID (key from sounds.json) |

Returns: `{ soundId: string }` or `null` if not found.

```ts
const s = registries.getSound("victory_fanfare");
if (s) {
  world.playSound(s.soundId, x, y, z, 1.0, 1.0);
}
```

### `registries.hasSound(id)`

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `string` | Sound ID |

Returns `boolean` — whether the sound is registered.

### `registries.listSounds()`

Returns `string[]` — all registered sound IDs.

## Client

Custom blocks **require the compiled JAR on the client** as well. The JAR includes models, textures, and blockstates — the client loads them to render correctly.

## Example Reference

The `colorzone/registries/` example includes blocks (with animated textures), items, a creative tab, and sounds:

- `rainbow_cube` — Rainbow cube (animated texture)
- `star_lamp` — Star lamp (animated texture, glowing)
- `snowflake_lamp` — Snowflake lamp (animated texture, glowing)
- `candy` — Candy block
- `treasure_chest` — Treasure chest
- `chocolate_bar` — Chocolate Bar (food item)
- `victory_fanfare` — Custom sound event
