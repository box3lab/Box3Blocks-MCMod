# gui — 自定义 GUI

客户端自定义容器界面，类似箱子界面，由脚本完全控制。

## gui.openGUI(config)

打开一个脚本控制的自定义容器 GUI，返回 `GuiController` 对象。
客户端会自动向服务端请求创建容器。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `config.title` | string | `"Container"` | 标题 |
| `config.rows` | number | `3` | 行数 (1–6) |
| `config.slots` | object | `{}` | 初始物品，格式 `{ [槽位]: "物品ID" }` |

```js
var ctrl = gui.openGUI({
  title: "§6商店",
  rows: 3,
  slots: { 0: "minecraft:diamond", 4: "minecraft:emerald" },
});

// 设置物品
ctrl.setItem(1, "minecraft:gold_ingot", 5);

// 获取物品
var item = ctrl.getItem(0);
console.log(item.id, item.count); // minecraft:diamond, 1

// 监听槽位点击
var clickToken = ctrl.onSlotClick((slot) => {
  console.log("Clicked slot:", slot);
});

// 监听关闭
var closeToken = ctrl.onClose(() => {
  console.log("GUI closed");
});

// 关闭 GUI
ctrl.close();
```

## GuiController 方法

| 方法 | 说明 |
|------|------|
| `setItem(slot, itemId, count?)` | 设置指定槽位的物品 |
| `getItem(slot)` | 获取指定槽位的物品，返回 `{ id, count }` |
| `onSlotClick(callback)` | 注册槽位点击回调，返回 `GameEventHandlerToken`，`callback(slot: number)` |
| `onClose(callback)` | 注册关闭回调，返回 `GameEventHandlerToken`，`callback()` |
| `close()` | 关闭 GUI |
