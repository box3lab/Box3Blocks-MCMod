# remoteChannel — Client ↔ Server Communication

The client uses `remoteChannel` for bidirectional event communication with the server. Event data is JSON-serialized.

## remoteChannel.sendServerEvent(event)

Sends an event to the server. `event` is any JSON-serializable value.

```js
remoteChannel.sendServerEvent({
  type: "clientReady",
  timestamp: Date.now(),
});
```

## remoteChannel.onClientEvent(handler)

Registers a handler for remote events sent from the server. Returns `GameEventHandlerToken`.

Callback: `(event: { tick: number, args: T }) => void`

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
Server-side equivalents: `remoteChannel.sendClientEvent()` / `broadcastClientEvent()` / `onServerEvent()`. See type declarations in `server.d.ts`.
:::
