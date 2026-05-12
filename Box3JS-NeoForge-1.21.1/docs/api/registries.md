# 自定义注册（registries API）

> **仅服务端可用。** 客户端中 `registries` 为 `undefined`。客户端代码请直接使用 ResourceLocation 字符串（如 `audio.playSound("colorzone:victory_fanfare", 1.0, 1.0)`）。
>
> **仅在编译 JAR 模式（`/box3script compile`）下可用。** 解释模式（`/box3script start`）中 `registries` 为 `undefined`。
>
> **需要服务端和客户端都安装编译后的 JAR** 才能正确渲染方块纹理/模型。客户端没有 JAR 的话，方块会显示为紫黑缺失方块。

方块、物品和音效事件在 JSON 配置文件中声明，编译时生成 `DeferredRegister` 代码注入 `@Mod` 类。资源文件从项目 `assets/` 目录打包进 JAR。

## 项目布局

```
mygame/
├── registries/
│   ├── blocks.json          ← 方块定义
│   ├── items.json           ← 物品定义
│   ├── creativeTabs.json    ← 创造标签页定义
│   └── sounds.json          ← 音效事件定义
├── assets/
│   ├── textures/block/      ← 方块纹理（16×16 至 64×64 PNG）
│   ├── textures/item/       ← 物品纹理（16×16 至 64×64 PNG）
│   ├── models/block/        ← 方块模型 JSON
│   ├── models/item/         ← 物品模型 JSON
│   ├── blockstates/         ← blockstate JSON
│   └── sounds/              ← .ogg 音效文件
└── src/server/app.ts        ← 游戏逻辑
```

## blocks.json

每个方块一个条目，属性说明：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `hardness` | float | `1.0` | 破坏硬度 |
| `resistance` | float | `1.0` | 爆炸抗性 |
| `sound` | string | `"stone"` | 音效类型：`wood`/`stone`/`metal`/`glass`/`wool`/`sand`/`snow`/`slime`/`anvil`/`gravel`/`grass`/`bamboo`/`netherite`/`empty` |
| `lightLevel` | int | `0` | 光照等级（0–15） |
| `mapColor` | string | `"stone"` | 小地图颜色 |
| `friction` | float | `0.6` | 摩擦力（溜滑度） |
| `speedFactor` | float | `1.0` | 行走速度倍率 |
| `jumpFactor` | float | `1.0` | 跳跃高度倍率 |
| `noOcclusion` | bool | `false` | 不遮挡面（透明方块） |
| `noCollision` | bool | `false` | 无碰撞（可穿过） |
| `requiresTool` | bool | `false` | 需要正确工具才掉落 |
| `instabreak` | bool | `false` | 瞬间破坏 |
| `creativeTab` | string | `""` | 所属创造标签页 ID（与 creativeTabs.json 中的 key 对应） |

### mapColor 可选值

`none`, `grass`, `sand`, `wool`, `fire`, `ice`, `metal`, `plant`, `snow`, `clay`, `dirt`, `stone`, `water`, `wood`, `quartz`, `gold`, `diamond`, `lapis`, `emerald`, `podzol`, `nether`, `color_orange`, `color_magenta`, `color_light_blue`, `color_yellow`, `color_light_green`, `color_pink`, `color_gray`, `color_light_gray`, `color_cyan`, `color_purple`, `color_blue`, `color_brown`, `color_green`, `color_red`, `color_black`

### 示例

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

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | string | — | 标签页显示名称 |
| `icon` | string | `""` | 标签图标方块 ID（对应 blocks.json 中的 key） |
| `searchBar` | bool | `false` | 添加搜索栏 |
| `rightAligned` | bool | `false` | 放在右侧 |

### 示例

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

每个物品一个条目，属性说明：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `displayName` | string | —（必填） | 物品显示名称 |
| `type` | string | `"item"` | `"item"` 或 `"food"` |
| `rarity` | string | `"common"` | 稀有度：`common`/`uncommon`/`rare`/`epic` |
| `maxStackSize` | int | `64` | 最大堆叠数（1–64） |
| `glint` | bool | `false` | 附魔光效 |
| `creativeTab` | string | `""` | 所属创造标签页 ID |
| `nutrition` | int | `4` | （食物专用）饥饿值恢复 |
| `saturation` | float | `0.6` | （食物专用）饱和度修饰符 |
| `alwaysEdible` | bool | `false` | （食物专用）是否始终可食用 |

物品纹理遵循与方块相同的模式：`assets/textures/item/<id>.png` + `assets/models/item/<id>.json`。

### 示例

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

> **注意：** `creativeTab` 图标会自动从物品中查找（优先物品，其次方块）。如果 `creativeTabs.json` 的 `icon` 匹配某个物品 key，会使用该物品作为图标。

### 装备类型（工具 & 盔甲）

`type` 字段支持以下装备类型，编译时生成对应的 Java 类：

| `type` | Java 类 | 说明 |
|--------|---------|------|
| `"sword"` | `SwordItem` | 剑 |
| `"pickaxe"` | `PickaxeItem` | 镐 |
| `"axe"` | `AxeItem` | 斧 |
| `"shovel"` | `ShovelItem` | 锹 |
| `"hoe"` | `HoeItem` | 锄 |
| `"helmet"` | `ArmorItem` | 头盔 |
| `"chestplate"` | `ArmorItem` | 胸甲 |
| `"leggings"` | `ArmorItem` | 护腿 |
| `"boots"` | `ArmorItem` | 靴子 |

装备专用属性：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `tier` | string | `"iron"` | 材料等级（工具和盔甲通用） |
| `armorTexture` | string | `""` | 自定义护甲纹理名。设置后护甲使用 `assets/<modid>/textures/models/armor/<值>_layer_1.png` 和 `_layer_2.png`，留空则使用 `tier` 对应的原版材质 |

`tier` 可选值及效果：

| tier | 工具 (Tiers) | 盔甲 (ArmorMaterials) |
|------|-------------|----------------------|
| `wood` | 木质 | — |
| `stone` | 石质 | — |
| `leather` | — | 皮革 |
| `chain` | — | 锁链 |
| `iron` | 铁质 | 铁质 |
| `gold` | 金质 | 金质 |
| `diamond` | 钻石 | 钻石 |
| `netherite` | 下界合金 | 下界合金 |
| `turtle` | — | 海龟壳 |

装备示例：

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

> **注意：** 装备的 `maxStackSize` 固定为 1（不可堆叠），无需手动设置。`nutrition`/`saturation`/`alwaysEdible` 仅用于 `"food"` 类型。

## sounds.json

每个音效事件一个条目。每个 key 在编译时注册为一个 `SoundEvent` 到 `minecraft:sound_event` 注册表中。

### 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `subtitle` | string | `""` | 播放时在字幕中显示的文字 |
| `stream` | bool | `false` | 从磁盘流式读取而非预加载到内存。适用于长背景音乐或环境音 |

### 音效文件

将 `.ogg` 文件放在 `assets/sounds/<id>.ogg`，一个 key 对应一个文件。

编译器会自动生成 `assets/<modId>/sounds.json`（Minecraft 标准资源包格式），**无需**手动创建该文件。

### 示例

```json
{
  "victory_fanfare": {
    "subtitle": "Victory!"
  },
  "skill_cast": {},
  "background_music": {
    "subtitle": "诡异环境音",
    "stream": true
  }
}
```

对应文件：

```
assets/sounds/victory_fanfare.ogg
assets/sounds/skill_cast.ogg
assets/sounds/background_music.ogg
```

### 服务端用法

```ts
const s = registries.getSound("victory_fanfare");
if (s) {
  // 在指定位置向全服播放
  world.playSound(s.soundId, x, y, z, 1.0, 1.0);

  // 仅对指定玩家播放
  player.playSound(s.soundId, 1.0, 1.0);
}
```

### 客户端用法

客户端脚本不需要 `registries`，直接用 ResourceLocation 字符串：

```ts
// 在客户端播放自定义音效
audio.playSound("colorzone:victory_fanfare", 1.0, 1.0);
```

## assets/ 目录

与 Minecraft 资源包结构一致：

```
assets/<modId>/
├── blockstates/<blockId>.json       ← 自动生成，可自定义覆盖
├── models/block/<blockId>.json      ← 自动生成，可自定义覆盖
├── models/item/<blockId>.json       ← 方块自动生成；物品必须提供
├── lang/
│   ├── en_us.json                   ← 需手动创建
│   ├── zh_cn.json                   ← 需手动创建
│   └── ja_jp.json                   ← 可选：自行添加
├── sounds.json                      ← 由 registries/sounds.json 自动生成
└── textures/
    ├── block/<blockId>_<face>.png
    └── item/<itemId>.png
```

> **注意：** `<modId>` 来自 `package.json` 的 `name` 字段（从第二个 `/` 后取，如 `@scope/mygame` → `mygame`）。

编译时自动将 `assets/` 打包为 `assets/<modId>/`。

### 多语言

语言文件需要在 `assets/lang/` 目录下**手动创建**，不会被自动生成。至少应提供 `en_us.json` 和 `zh_cn.json`，也可添加更多语言：

```
mygame/
└── assets/
    └── lang/
        ├── en_us.json      ← 英文翻译
        ├── zh_cn.json      ← 中文翻译
        ├── ja_jp.json      ← 你的日文翻译
        └── ko_kr.json      ← 你的韩文翻译
```

格式与 Minecraft 标准 lang 文件一致：

```json
{
  "block.mygame.ruby_block": "Ruby Block",
  "item.mygame.ruby_sword": "Ruby Sword",
  "item.mygame.chocolate": "Chocolate Bar",
  "itemGroup.mygame.my_blocks": "My Blocks"
}
```

MC 客户端会根据语言设置自动加载对应的文件，无需任何额外配置。

## registries 运行时 API

### `registries.getBlock(id)`

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `string` | 方块 ID（blocks.json 的 key） |

返回值：`{ block: any, itemId: string }` 或 `null`（未找到时）。

- `block.block` — `Block` 实例，用于 `voxels.setVoxel()`
- `block.itemId` — `"modId:blockId"` 字符串，用于 `player.giveItem()`

### `registries.hasBlock(id)`

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `string` | 方块 ID |

返回 `boolean`，检查方块是否已注册。

### `registries.listBlocks()`

返回 `string[]`，所有已注册方块的 ID 列表。

```ts
// 遍历所有方块
registries.listBlocks().forEach(id => {
  const block = registries.getBlock(id);
  player.giveItem(block.itemId, 1);
});
```

### `registries.getItem(id)`

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `string` | 物品 ID（items.json 的 key） |

返回值：`{ item: any, itemId: string }` 或 `null`（未找到时）。

- `item.item` — `Item` 实例，可用于比较
- `item.itemId` — `"modId:itemId"` 字符串，用于 `player.giveItem()`

```ts
const chocolate = registries.getItem("chocolate");
if (chocolate) {
  player.giveItem(chocolate.itemId, 8);
}
```

### `registries.hasItem(id)`

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `string` | 物品 ID |

返回 `boolean`，检查物品是否已注册。

### `registries.listItems()`

返回 `string[]`，所有已注册物品的 ID 列表。

```ts
// 遍历所有物品
registries.listItems().forEach(id => {
  const item = registries.getItem(id);
  player.giveItem(item.itemId, 1);
});
```

### `registries.getSound(id)`

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `string` | 音效 ID（sounds.json 的 key） |

返回值：`{ soundId: string }` 或 `null`（未找到时）。

```ts
const s = registries.getSound("victory_fanfare");
if (s) {
  world.playSound(s.soundId, x, y, z, 1.0, 1.0);
}
```

### `registries.hasSound(id)`

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `string` | 音效 ID |

返回 `boolean`，检查音效是否已注册。

### `registries.listSounds()`

返回 `string[]`，所有已注册音效的 ID 列表。

## 客户端

自定义方块**需要客户端也安装**编译出的 JAR。JAR 包含模型、纹理和 blockstate，客户端加载后即可正常渲染。

## 示例参考

`colorzone/registries/` 包含完整示例，含方块（动画纹理）、物品、创造标签页和音效：

- `rainbow_cube` — 彩虹立方（动画纹理）
- `star_lamp` — 星形灯（动画纹理，发光）
- `snowflake_lamp` — 雪花灯（动画纹理，发光）
- `candy` — 糖果方块
- `treasure_chest` — 宝物箱
- `chocolate_bar` — 巧克力棒（食物物品）
- `victory_fanfare` — 自定义音效事件
