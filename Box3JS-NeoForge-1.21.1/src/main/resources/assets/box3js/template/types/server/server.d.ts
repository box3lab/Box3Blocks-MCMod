/// <reference path="../shared.d.ts" />
/// <reference path="entity.d.ts" />
/// <reference path="player.d.ts" />
/// <reference path="world.d.ts" />
/// <reference path="voxels.d.ts" />

// ── §0 @zh RemoteChannel 服务端方法（接口合并） @en RemoteChannel server‑side methods (interface merging) ──

interface RemoteChannel {
  /**
   * @zh 向指定玩家发送客户端事件。
   * @en Sends a client‑side event to the specified player(s).
   * @param entities - @zh 单个玩家实体或实体数组 @en A single player entity or an array of them
   * @param clientEvent - @zh 事件数据（任意 JSON 可序列化的值） @en Event data (any JSON‑serializable value)
   */
  sendClientEvent<T = any>(entities: any | any[], clientEvent: T): void;

  /**
   * @zh 向所有玩家广播客户端事件。
   * @en Broadcasts a client‑side event to every connected player.
   * @param clientEvent - @zh 事件数据（任意 JSON 可序列化的值） @en Event data (any JSON‑serializable value)
   */
  broadcastClientEvent<T = any>(clientEvent: T): void;

  /**
   * @zh 注册来自客户端的远程事件处理器。
   * @en Registers a handler for remote events sent from clients.
   * @param handler - @zh 回调函数，接收包含 tick / entity / args 的事件对象 @en Callback receiving an event object with tick, entity, and args
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onServerEvent<T = any>(
    handler: (event: {
      /** @zh 事件到达时的服务端 tick @en Server tick when the event arrived */
      tick: number;
      /** @zh 发送事件的玩家实体 @en The player entity that sent the event */
      entity: any;
      /** @zh 事件数据（已反序列化） @en Event data (deserialised) */
      args: T;
    }) => void,
  ): GameEventHandlerToken;
}

// ── §1 @zh 服务端回调参数 @en Server Callback Parameters ──

/**
 * @zh `onTick` 回调的参数类型。
 * @en The info object passed to `onTick` handlers.
 */
interface TickInfo {
  /** @zh 当前 tick 数 @en Current tick count. */
  tick: number;
  /** @zh 上一 tick 数 @en Previous tick count. */
  prevTick: number;
  /** @zh 自启动以来的毫秒数 @en Milliseconds elapsed since server start. */
  elapsedTimeMS: number;
  /** @zh 跳过的 tick 数 (MC 下始终为 0) @en Number of skipped ticks (always 0 in MC). */
  skip: number;
}

// ── §2 @zh 持久化存储扩展（服务端专用方法） @en Storage Extensions (server‑only methods) ──

// Declaration merging: augment GameStorage with server‑only method
interface GameStorage {
  /**
   * @zh 获取跨项目共享存储 — 所有项目通过同一 name 读写同一份数据（服务端专用）。
   * @en Shared cross‑project storage — all projects read/write the same data by name (server‑only).
   * @param name - @zh 命名空间 @en namespace
   * @remarks 底层使用 `__shared__/` 前缀, 适合全服排行榜、全局配置等场景。
   *          Uses `__shared__/` prefix internally; suitable for global leaderboards, shared config, etc.
   */
  getGroupStorage<T = JSONValue>(name: string): GameDataStorage<T>;
}

// ── §8 @zh 运行时枚举常量（服务端专用） @en Runtime Enum Constants (server‑only) ──

/**
 * @zh 按钮类型常量 — 用于 `world.onButtonPressed()` 的 `button` 参数。
 * @en Button type constants for the `button` parameter of `world.onButtonPressed()`.
 */
declare const GameButtonType: {
  readonly WALK: "WALK";
  readonly RUN: "RUN";
  readonly CROUCH: "CROUCH";
  readonly JUMP: "JUMP";
  readonly FLY: "FLY";
  readonly ACTION0: "ACTION0";
  readonly ACTION1: "ACTION1";
};

/**
 * @zh 相机模式常量 — `player.cameraMode` 的取值。
 * @en Camera mode constants for the `player.cameraMode` property.
 */
declare const GameCameraMode: {
  readonly FOLLOW: "FOLLOW";
  readonly FPS: "FPS";
};

/**
 * @zh 玩家移动状态常量 — `player.moveState` 的可能返回值。
 * @en Player movement state constants — possible return values of `player.moveState`.
 */
declare const GamePlayerMoveState: {
  readonly FLYING: "FLYING";
  readonly GROUND: "GROUND";
  readonly SWIM: "SWIM";
  readonly FALL: "FALL";
  readonly JUMP: "JUMP";
};

/**
 * @zh 玩家行走状态常量 — `player.walkState` 的可能返回值。
 * @en Player walk state constants — possible return values of `player.walkState`.
 */
declare const GamePlayerWalkState: {
  readonly NONE: "NONE";
  readonly CROUCH: "CROUCH";
  readonly WALK: "WALK";
  readonly RUN: "RUN";
};

// ── §9 @zh 注册表（仅编译 JAR 模式） @en Registries (compiled JAR mode only) ──

/**
 * @zh 物品类型 — `items.json` 中 `type` 字段的有效值。
 * @en Item type — valid values for the `type` field in `items.json`.
 *
 * | value | class |
 * |-------|-------|
 * | `"item"` | `Item` |
 * | `"food"` | `Item` + food properties |
 * | `"sword"` | `SwordItem` |
 * | `"pickaxe"` | `PickaxeItem` |
 * | `"axe"` | `AxeItem` |
 * | `"shovel"` | `ShovelItem` |
 * | `"hoe"` | `HoeItem` |
 * | `"helmet"` | `ArmorItem` |
 * | `"chestplate"` | `ArmorItem` |
 * | `"leggings"` | `ArmorItem` |
 * | `"boots"` | `ArmorItem` |
 */
type GameItemType =
  | "item"
  | "food"
  | "sword"
  | "pickaxe"
  | "axe"
  | "shovel"
  | "hoe"
  | "helmet"
  | "chestplate"
  | "leggings"
  | "boots";

/**
 * @zh 装备等级 — `items.json` 中 `tier` 字段的有效值。
 * @en Equipment tier — valid values for the `tier` field in `items.json`.
 *
 * | tier | tools (Tiers) | armor (ArmorMaterials) |
 * |------|--------------|----------------------|
 * | `"wood"` | Wood (59 durability) | — |
 * | `"stone"` | Stone (131 durability) | — |
 * | `"leather"` | — | Leather |
 * | `"chain"` | — | Chain |
 * | `"iron"` | Iron (250 durability) | Iron |
 * | `"gold"` | Gold (32 durability) | Gold |
 * | `"diamond"` | Diamond (1561 durability) | Diamond |
 * | `"netherite"` | Netherite (2031 durability) | Netherite |
 * | `"turtle"` | — | Turtle |
 */
type GameTier =
  | "wood"
  | "stone"
  | "leather"
  | "chain"
  | "iron"
  | "gold"
  | "diamond"
  | "netherite"
  | "turtle";

/**
 * @zh 护甲自定义纹理 — `items.json` 中 armor 类型物品的 `armorTexture` 字段。
 * 设置后，护甲将使用 `assets/<modid>/textures/models/armor/<value>_layer_1.png` 和 `_layer_2.png` 作为纹理，
 * 而非原版材质（如钻石）的默认纹理。不设置或为空则使用 `tier` 对应的原版材质。
 * @en Custom armor texture — the `armorTexture` field for armor-type items in `items.json`.
 * When set, armor uses `assets/<modid>/textures/models/armor/<value>_layer_1.png` and `_layer_2.png`
 * instead of the vanilla tier's default texture. Leave empty or unset to use the vanilla tier texture.
 *
 * @example
 * ```json
 * { "star_chestplate": { "type": "chestplate", "tier": "diamond",
 *   "armorTexture": "star" } }
 * ```
 */
type GameArmorTexture = string;

/**
 * @zh 方块音效 — `blocks.json` 中 `sound` 字段的有效值。
 * @en Block sound type — valid values for the `sound` field in `blocks.json`.
 */
type GameBlockSound =
  | "wood"
  | "stone"
  | "metal"
  | "glass"
  | "wool"
  | "sand"
  | "snow"
  | "slime"
  | "anvil"
  | "gravel"
  | "grass"
  | "bamboo"
  | "netherite"
  | "empty";

/**
 * @zh 方块地图颜色 — `blocks.json` 中 `mapColor` 字段的有效值。
 * @en Block map color — valid values for the `mapColor` field in `blocks.json`.
 */
type GameMapColor =
  | "none"
  | "grass"
  | "sand"
  | "wool"
  | "fire"
  | "ice"
  | "metal"
  | "plant"
  | "snow"
  | "clay"
  | "dirt"
  | "stone"
  | "water"
  | "wood"
  | "quartz"
  | "gold"
  | "diamond"
  | "lapis"
  | "emerald"
  | "podzol"
  | "nether"
  | "color_orange"
  | "color_magenta"
  | "color_light_blue"
  | "color_yellow"
  | "color_light_green"
  | "color_pink"
  | "color_gray"
  | "color_light_gray"
  | "color_cyan"
  | "color_purple"
  | "color_blue"
  | "color_brown"
  | "color_green"
  | "color_red"
  | "color_black";

/**
 * @zh 已注册内容的查询接口 — 仅在 `/box3script compile` 打包的 JAR 中可用。
 *
 * @en Query interface for registered content — only available in JARs built via `/box3script compile`.
 */
interface GameRegistries {
  getBlock(id: string): {
    block: any;
    itemId: string;
  } | null;

  hasBlock(id: string): boolean;

  listBlocks(): string[];

  getItem(id: string): {
    item: any;
    itemId: string;
  } | null;

  hasItem(id: string): boolean;

  listItems(): string[];

  getSound(id: string): {
    soundId: string;
  } | null;

  hasSound(id: string): boolean;

  listSounds(): string[];
}

// ── §10 @zh 全局声明（服务端） @en Global Declarations (server) ──

/** @zh 世界控制与事件 API @en World control & events */
declare const world: GameWorld;

/** @zh 方块读写 API @en Block read & write */
declare const voxels: GameVoxels;

/**
 * @zh 注册表（方块/物品/音效） — 仅在编译 JAR 模式下存在，解释模式下为 `undefined`。
 * @en Registries (blocks/items/sounds) — only exists in compiled JAR mode; `undefined` in interpreted mode.
 */
declare const registries: GameRegistries | undefined;
