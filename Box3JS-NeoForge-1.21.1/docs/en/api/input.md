# input — Input API

Client-side keyboard and mouse input detection.

## input.isKeyDown(key)

Checks whether a key is currently held down.

| Parameter | Type | Description |
|-----------|------|-------------|
| `key` | string | Key name (lowercase), see key list below |

```js
if (input.isKeyDown("space")) {
  // Space key is held
}
```

## input.onKeyPress(key, callback)

Registers a callback fired once when the key is pressed. Returns `GameEventHandlerToken`; call `.cancel()` to unregister.

```js
var token = input.onKeyPress("f", () => {
  chat.sendCommand("fly");
});

// Unregister
token.cancel();
```

## input.getMouseX()

Gets the current mouse X position in screen pixels.

```js
var mx = input.getMouseX();
```

## input.getMouseY()

Gets the current mouse Y position in screen pixels.

```js
var my = input.getMouseY();
```

## input.onMouseClick(callback)

Registers a mouse button callback. Returns `GameEventHandlerToken`; call `.cancel()` to unregister.

Callback: `(button: number, action: number, x: number, y: number) => void`

| Parameter | Description |
|-----------|-------------|
| `button` | 0=left, 1=right, 2=middle |
| `action` | 0=release, 1=press, 2=repeat |
| `x` | Mouse X in screen pixels |
| `y` | Mouse Y in screen pixels |

```js
var token = input.onMouseClick((button, action, x, y) => {
  if (action === 1) { // pressed
    console.log(`Clicked button ${button} at (${x}, ${y})`);
  }
});

// Unregister
token.cancel();
```

## Supported Key Names

| Category | Keys |
|----------|------|
| Letters | `a`–`z` |
| Digits | `0`–`9` |
| Function keys | `f1`–`f12` |
| Arrow keys | `up`, `down`, `left`, `right` |
| Special keys | `space`, `enter`, `escape`, `tab`, `backspace`, `delete` |
| Modifiers | `shift`, `left_shift`, `right_shift`, `ctrl`, `left_ctrl`, `right_ctrl`, `alt`, `left_alt`, `right_alt` |
