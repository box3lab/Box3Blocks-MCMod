# chat — Chat API

Client-side chat message sending/receiving and command sending.

## chat.sendMessage(text)

Sends a chat message to the server.

```js
chat.sendMessage("Hello everyone!");
```

## chat.sendCommand(cmd)

Sends a command to the server (equivalent to typing a `/` command in chat).

```js
chat.sendCommand("spawn");
chat.sendCommand("home");
```

## chat.onMessage(handler)

Registers a handler for incoming chat messages. Returns `GameEventHandlerToken`; call `.cancel()` to unregister.

Callback: `(message: string, sender: string, isSystem: boolean) => boolean | void`

Return `false` to suppress the message from appearing in chat.

```js
var token = chat.onMessage((message, sender, isSystem) => {
  console.log(`[chat] ${sender}: ${message}`);

  if (message.includes("filtered_word")) {
    return false; // Suppress this message
  }
});

// Unregister
token.cancel();
```
