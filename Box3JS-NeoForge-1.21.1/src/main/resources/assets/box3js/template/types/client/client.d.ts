/// <reference path="../shared.d.ts" />
/// <reference path="audio.d.ts" />
/// <reference path="input.d.ts" />
/// <reference path="ui.d.ts" />
/// <reference path="chat.d.ts" />
/// <reference path="gui.d.ts" />

// ── §1 @zh RemoteChannel 客户端方法（接口合并） @en RemoteChannel client‑side methods (interface merging) ──

interface RemoteChannel {
  /**
   * @zh 向服务端发送事件。
   * @en Sends an event to the server.
   * @param event - @zh 事件数据（任意 JSON 可序列化的值） @en Event data (any JSON‑serializable value)
   */
  sendServerEvent<T = any>(event: T): void;

  /**
   * @zh 注册来自服务端的远程事件处理器。
   * @en Registers a handler for remote events sent from the server.
   * @param handler - @zh 回调函数，接收包含 tick / args 的事件对象 @en Callback receiving an event object with tick and args
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onClientEvent<T = any>(
    handler: (event: {
      /** @zh 事件到达时的服务端 tick @en Server tick when the event was sent */
      tick: number;
      /** @zh 事件数据（已反序列化） @en Event data (deserialised) */
      args: T;
    }) => void,
  ): GameEventHandlerToken;
}

// ── §3 @zh 客户端生命周期 @en Client lifecycle ──

/** @zh 通过 `client` 访问：生命周期回调 @en Accessed via `client`: lifecycle callbacks */
interface GameClient {
  /**
   * @zh 注册客户端每 tick 回调（每秒 20 次）。
   * @en Registers a callback invoked every client tick (20/sec).
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onTick(callback: () => void): GameEventHandlerToken;

  /**
   * @zh 获取当前帧率 (FPS)。
   * @en Gets the current frames per second (FPS).
   * @returns @zh FPS 数值 @en FPS value
   */
  getFPS(): number;

  /**
   * @zh 获取本地玩家信息。
   * @en Gets local player information.
   * @returns @zh `{ name, uuid, health, maxHealth, food, saturation, xp, dimension, position }` 或 null @en `{ name, uuid, health, maxHealth, food, saturation, xp, dimension, position }` or null
   */
  getPlayer(): {
    name: string;
    uuid: string;
    health: number;
    maxHealth: number;
    food: number;
    saturation: number;
    xp: number;
    dimension: string;
    position: { x: number; y: number; z: number };
  } | null;

  /**
   * @zh 获取玩家准星正在看向的目标。
   * @en Gets what the player's crosshair is currently pointing at.
   * @returns @zh `{ type: "block"|"entity", position, entity?, blockPos?, direction? }` 或 null @en `{ type: "block"|"entity", position, entity?, blockPos?, direction? }` or null
   */
  getLookingAt(): {
    type: "entity";
    position: { x: number; y: number; z: number };
    entity: { name: string; uuid: string; type: string };
  } | {
    type: "block";
    position: { x: number; y: number; z: number };
    blockPos: { x: number; y: number; z: number };
    direction: string;
  } | null;

  /**
   * @zh 获取当前连接的服务器信息。
   * @en Gets current server connection information.
   * @returns @zh `{ ip, name, isLocal, playerCount?, maxPlayers? }` — 单人游戏返回 localhost @en `{ ip, name, isLocal, playerCount?, maxPlayers? }` — returns localhost for singleplayer
   */
  getServerInfo(): {
    ip: string;
    name: string;
    isLocal: boolean;
    playerCount?: number;
    maxPlayers?: number;
  };

  // ── Fog control ──

  /**
   * @zh 获取当前自定义雾颜色。未设置时返回 null。
   * @en Gets the current custom fog colour. Returns null if not set.
   * @returns @zh GameRGBColor 或 null @en GameRGBColor or null
   */
  getFogColor(): GameRGBColor | null;

  /**
   * @zh 设置雾颜色（RGB 0-255）。
   * @en Sets the fog colour (RGB 0-255).
   * @param r - @zh 红色 (0-255) @en Red (0-255)
   * @param g - @zh 绿色 (0-255) @en Green (0-255)
   * @param b - @zh 蓝色 (0-255) @en Blue (0-255)
   */
  setFogColor(r: number, g: number, b: number): void;

  /**
   * @zh 设置雾起始距离（方块）。低于此距离完全透明。
   * @en Sets the distance (in blocks) where fog begins. Fully transparent below this distance.
   * @param distance - @zh 雾起始距离（方块） @en Fog start distance (blocks)
   */
  setFogStartDistance(distance: number): void;

  /**
   * @zh 设置雾结束距离（方块），对应 Box3 的 maxFog。超过此距离完全被雾遮挡。
   * @en Sets the distance (in blocks) where fog is fully opaque, equivalent to Box3's maxFog.
   * @param distance - @zh 雾结束距离（方块） @en Fog end distance (blocks)
   */
  setFogEndDistance(distance: number): void;

  /**
   * @zh 重置雾效果为 Minecraft 默认值。
   * @en Resets fog to Minecraft's default behaviour.
   */
  resetFog(): void;
}

// ── §7 @zh 全局声明（客户端） @en Global Declarations (client) ──

declare const audio: GameAudio;
declare const client: GameClient;
declare const input: GameInput;
declare const ui: GameUI;
declare const chat: GameChat;
declare const gui: GameGUI;

// storage, console, remoteChannel, db, http — declared in shared.d.ts
