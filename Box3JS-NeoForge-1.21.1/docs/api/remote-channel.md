# remoteChannel — 跨端通信

客户端通过 `remoteChannel` 与服务端进行双向事件通信。事件数据通过 JSON 序列化传输。

## remoteChannel.sendServerEvent(event)

向服务端发送事件。`event` 为任意 JSON 可序列化的值。

```js
remoteChannel.sendServerEvent({
  type: "clientReady",
  timestamp: Date.now(),
});
```

## remoteChannel.onClientEvent(handler)

注册来自服务端的远程事件处理器。返回 `GameEventHandlerToken`。

回调参数：`(event: { tick: number, args: T }) => void`

```js
remoteChannel.onClientEvent((event) => {
  const { tick, args } = event;

  switch (args.type) {
    case "ping":
      console.log(`[client] Ping: ${args.message}`);
      remoteChannel.sendServerEvent({ type: "pong" });
      break;
    case "notify":
      ui.showOverlay(`§b${args.message}`);
      break;
  }
});
```

::: info
服务端对应 API 为 `remoteChannel.sendClientEvent()` / `broadcastClientEvent()` / `onServerEvent()`。详见类型声明中的 `server.d.ts`。
:::
