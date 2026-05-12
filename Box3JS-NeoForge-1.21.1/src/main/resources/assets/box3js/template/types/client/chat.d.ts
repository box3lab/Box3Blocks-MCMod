/// <reference path="../shared.d.ts" />

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
