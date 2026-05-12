/// <reference path="shared.d.ts" />

// ── §0 @zh 类型别名（枚举式限制） @en Type aliases (enum‑like constraints) ──

/**
 * @zh 按键名称。仅允许以下值。
 * @en Key name. Only these values are accepted.
 */
type KeyName =
  | "space" | "enter" | "escape" | "tab" | "backspace" | "delete"
  | "left_shift" | "right_shift" | "left_ctrl" | "right_ctrl"
  | "left_alt" | "right_alt"
  | "up" | "down" | "left" | "right"
  | "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j"
  | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t"
  | "u" | "v" | "w" | "x" | "y" | "z"
  | "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
  | "f1" | "f2" | "f3" | "f4" | "f5" | "f6"
  | "f7" | "f8" | "f9" | "f10" | "f11" | "f12";

/**
 * @zh 音频类别名称。
 * @en Audio category name.
 */
type AudioCategory =
  | "master" | "music" | "record" | "weather" | "block"
  | "hostile" | "neutral" | "player" | "ambient" | "voice";

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

// ── §2 @zh 音频播放 @en Audio playback ──

/** @zh 通过 `audio` 访问：音效、音乐、音量控制 @en Accessed via `audio`: sound, music, volume control */
interface GameAudio {
  /**
   * @zh 播放音效（SoundSource.PLAYERS 类别）。
   * @en Plays a sound effect (SoundSource.PLAYERS category).
   * @param path - @zh 声音 ID（如 "minecraft:block.note_block.pling"） @en sound ID (e.g. "minecraft:block.note_block.pling")
   * @param volume - @zh 音量（0‑1，可选，默认 1） @en volume (0–1, optional, default 1)
   * @param pitch - @zh 音高（0.5‑2，可选，默认 1） @en pitch (0.5–2, optional, default 1)
   */
  playSound(path: string, volume?: number, pitch?: number): void;

  /**
   * @zh 播放音乐（SoundSource.MUSIC 类别）。
   * @en Plays music (SoundSource.MUSIC category).
   * @param path - @zh 声音 ID @en sound ID
   * @param volume - @zh 音量（0‑1，可选，默认 1） @en volume (0–1, optional, default 1)
   * @param pitch - @zh 音高（0.5‑2，可选，默认 1） @en pitch (0.5–2, optional, default 1)
   */
  playMusic(path: string, volume?: number, pitch?: number): void;

  /** @zh 停止所有正在播放的声音和音乐。 @en Stops all currently playing sounds and music. */
  stopAll(): void;

  /**
   * @zh 获取指定音频类别的音量。
   * @en Gets the volume of a specific audio category.
   * @param category - @zh 类别名称 @en category name
   * @returns @zh 音量值（0‑1） @en volume value (0–1)
   */
  getVolume(category: AudioCategory): number;

  /**
   * @zh 设置指定音频类别的音量。
   * @en Sets the volume of a specific audio category.
   * @param category - @zh 类别名称 @en category name
   * @param value - @zh 音量（0‑1） @en volume (0–1)
   */
  setVolume(category: AudioCategory, value: number): void;
}

// ── §3 @zh 客户端生命周期 @en Client lifecycle ──

/** @zh 通过 `client` 访问：生命周期回调 @en Accessed via `client`: lifecycle callbacks */
interface GameClient {
  /** @zh 注册客户端每 tick 回调（每秒 20 次）。 @en Registers a callback invoked every client tick (20/sec). */
  onTick(callback: () => void): void;
}

// ── §4 @zh 键盘输入 @en Keyboard input ──

/** @zh 通过 `input` 访问：键盘输入检测 @en Accessed via `input`: keyboard input detection */
interface GameInput {
  /**
   * @zh 检查指定按键当前是否被按下。
   * @en Checks whether a key is currently held down.
   * @param key - @zh 按键名称 @en key name
   * @returns @zh true 如果按键正在被按住 @en true if the key is held down
   */
  isKeyDown(key: KeyName): boolean;

  /**
   * @zh 注册按键按下回调（按下瞬间触发一次）。
   * @en Registers a callback fired once when the key is pressed.
   * @param key - @zh 按键名称 @en key name
   * @param callback - @zh 回调函数 @en callback function (no arguments)
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onKeyPress(key: KeyName, callback: () => void): GameEventHandlerToken;
}

// ── §5 @zh 屏幕 UI @en Screen UI ──

/** @zh 通过 `ui` 访问：屏幕文字显示 @en Accessed via `ui`: on‑screen text display */
interface GameUI {
  /**
   * @zh 在动作栏（快捷栏上方）显示文字。
   * @en Displays text in the action bar (above the hotbar).
   * @param text - @zh 要显示的文字 @en text to display
   */
  showOverlay(text: string): void;

  /**
   * @zh 显示屏幕标题。
   * @en Displays a screen title.
   * @param title - @zh 主标题 @en main title
   * @param subtitle - @zh 副标题 @en subtitle
   * @param fadeIn - @zh 淡入 tick（可选，默认 10） @en fade‑in ticks (optional, default 10)
   * @param stay - @zh 停留 tick（可选，默认 70） @en stay ticks (optional, default 70)
   * @param fadeOut - @zh 淡出 tick（可选，默认 20） @en fade‑out ticks (optional, default 20)
   */
  showTitle(
    title: string,
    subtitle: string,
    fadeIn?: number,
    stay?: number,
    fadeOut?: number,
  ): void;

  /**
   * @zh 在动作栏显示文字（与 showOverlay 相同）。
   * @en Displays text in the action bar (same as showOverlay).
   * @param text - @zh 要显示的文字 @en text to display
   */
  showActionBar(text: string): void;
}

// ── §6 @zh 聊天消息 @en Chat messages ──

/** @zh 通过 `chat` 访问：收发聊天消息、发送命令 @en Accessed via `chat`: send/receive chat, send commands */
interface GameChat {
  /**
   * @zh 向服务端发送聊天消息。
   * @en Sends a chat message to the server.
   * @param text - @zh 消息内容 @en message content
   */
  sendMessage(text: string): void;

  /**
   * @zh 向服务端发送命令（等同于在聊天框输入 / 前缀的命令）。
   * @en Sends a command to the server (equivalent to typing a /command in chat).
   * @param cmd - @zh 命令字符串（不需要 / 前缀） @en command string (no leading / needed)
   */
  sendCommand(cmd: string): void;

  /**
   * @zh 注册接收聊天消息的处理器。
   * @en Registers a handler for incoming chat messages.
   * @param handler - @zh 回调函数（message: 消息文本, sender: 发送者 UUID, isSystem: 是否系统消息）
   *                  返回 false 可阻止消息显示 @en callback (message, sender, isSystem); return false to suppress display
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onMessage(
    handler: (message: string, sender: string, isSystem: boolean) => boolean | void,
  ): GameEventHandlerToken;
}

// ── §7 @zh 全局声明（客户端） @en Global Declarations (client) ──

declare const audio: GameAudio;
declare const client: GameClient;
declare const input: GameInput;
declare const ui: GameUI;
declare const chat: GameChat;

// storage, console, remoteChannel, db, http — declared in shared.d.ts
