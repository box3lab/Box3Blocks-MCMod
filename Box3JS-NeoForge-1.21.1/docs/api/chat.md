# chat — 聊天 API

客户端聊天消息收发与命令发送。

## chat.sendMessage(text)

向服务端发送聊天消息。

```js
chat.sendMessage("大家好！");
```

## chat.sendCommand(cmd)

向服务端发送命令（等同于在聊天框输入 `/` 前缀的命令）。

```js
chat.sendCommand("spawn");
chat.sendCommand("home");
```

## chat.onMessage(handler)

注册接收聊天消息的处理器。返回 `GameEventHandlerToken`，调用 `.cancel()` 取消。

回调参数：`(message: string, sender: string, isSystem: boolean) => boolean | void`

返回 `false` 可阻止消息显示在聊天栏。

```js
var token = chat.onMessage((message, sender, isSystem) => {
  console.log(`[chat] ${sender}: ${message}`);

  if (message.includes("filtered_word")) {
    return false; // 阻止该消息显示
  }
});

// 取消监听
token.cancel();
```
