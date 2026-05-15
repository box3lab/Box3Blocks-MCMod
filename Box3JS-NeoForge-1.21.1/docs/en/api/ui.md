# ui — UI API

Client-side screen UI display: titles, action bar, custom drawn text.

## ui.showOverlay(text)

Displays text in the action bar (above the hotbar). Supports color codes (`§a`, `§b`, etc.).

```js
ui.showOverlay("§aWelcome to the server!");
```

## ui.showTitle(title, subtitle, fadeIn?, stay?, fadeOut?)

Displays a large centered screen title.

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `title` | string | (required) | Main title |
| `subtitle` | string | (required) | Subtitle |
| `fadeIn` | number | `10` | Fade-in ticks |
| `stay` | number | `70` | Stay ticks |
| `fadeOut` | number | `20` | Fade-out ticks |

```js
ui.showTitle("Boss Incoming!", "Get ready", 10, 70, 20);
ui.showTitle("§cGame Over", "§7Try again");
```

## ui.showActionBar(text)

Displays text in the action bar (same as `showOverlay`).

```js
ui.showActionBar("§ePress F to use ability");
```

## ui.getScreenSize()

Gets the current game window and GUI-scaled dimensions.

```js
var size = ui.getScreenSize();
console.log(size.width, size.height);           // window pixels
console.log(size.scaledWidth, size.scaledHeight); // GUI-scaled
```

## ui.drawText(id, x, y, text, color?)

Draws custom text on screen (persists every frame until removed via `removeDrawText`).

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `id` | number | (required) | Text ID for later removal or update |
| `x` | number | (required) | X position (GUI-scaled coordinates) |
| `y` | number | (required) | Y position (GUI-scaled coordinates) |
| `text` | string | (required) | Text to display |
| `color` | GameRGBColor | white | Text colour |

Returns the text ID (same as the passed `id`). Reusing the same ID overwrites the previous entry.

```js
var textId = ui.drawText(1, 10, 10, "Hello, Box3JS!");
// Update position or content
ui.drawText(1, 10, 30, "Updated text", new GameRGBColor(1, 0, 0)); // red
```

## ui.removeDrawText(id)

Removes the drawn text with the given ID.

```js
ui.removeDrawText(1);
```

## ui.clearDrawTexts()

Clears all texts drawn via `drawText()`.

```js
ui.clearDrawTexts();
```
