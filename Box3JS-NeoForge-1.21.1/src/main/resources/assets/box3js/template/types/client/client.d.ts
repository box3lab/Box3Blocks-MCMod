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
  /** @zh 注册客户端每 tick 回调（每秒 20 次）。 @en Registers a callback invoked every client tick (20/sec). */
  onTick(callback: () => void): void;

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
    type: string;
    position: { x: number; y: number; z: number };
    entity?: {
      name: string;
      uuid: string;
      type: string;
    };
    blockPos?: { x: number; y: number; z: number };
    direction?: string;
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
    playerCount: number;
    maxPlayers: number;
  };
}

// ── §7 @zh 全局声明（客户端） @en Global Declarations (client) ──

declare const audio: GameAudio;
declare const client: GameClient;
declare const input: GameInput;
declare const ui: GameUI;
declare const chat: GameChat;
declare const gui: GameGUI;

// storage, console, remoteChannel, db, http — declared in shared.d.ts
