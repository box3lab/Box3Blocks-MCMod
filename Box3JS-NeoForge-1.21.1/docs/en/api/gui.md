# gui — Custom GUI

Client-side custom container GUI (chest-like screen), fully controlled by scripts.

## gui.openGUI(config)

Opens a script-controlled custom container GUI, returning a `GuiController` object.
The client automatically requests the server to create the container.

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `config.title` | string | `"Container"` | Title |
| `config.rows` | number | `3` | Number of rows (1–6) |
| `config.slots` | object | `{}` | Initial items, format `{ [slot]: "itemId" }` |

```js
var ctrl = gui.openGUI({
  title: "§6Shop",
  rows: 3,
  slots: { 0: "minecraft:diamond", 4: "minecraft:emerald" },
});

// Set item
ctrl.setItem(1, "minecraft:gold_ingot", 5);

// Get item
var item = ctrl.getItem(0);
console.log(item.id, item.count); // minecraft:diamond, 1

// Slot click listener
var clickToken = ctrl.onSlotClick((slot) => {
  console.log("Clicked slot:", slot);
});

// Close listener
var closeToken = ctrl.onClose(() => {
  console.log("GUI closed");
});

// Close the GUI
ctrl.close();
```

## GuiController Methods

| Method | Description |
|--------|-------------|
| `setItem(slot, itemId, count?)` | Sets the item in the given slot |
| `getItem(slot)` | Gets the item in the given slot, returns `{ id, count }` |
| `onSlotClick(callback)` | Registers a slot click callback and returns `GameEventHandlerToken`, `callback(slot: number)` |
| `onClose(callback)` | Registers a close callback and returns `GameEventHandlerToken`, `callback()` |
| `close()` | Closes the GUI |
