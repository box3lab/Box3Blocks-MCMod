# ui — 界面 API

客户端屏幕 UI 显示，包括标题、动作栏、自定义绘制文字。

## ui.showOverlay(text)

在动作栏（快捷栏上方）显示文字。支持颜色代码（`§a`、`§b` 等）。

```js
ui.showOverlay("§a欢迎来到服务器！");
```

## ui.showTitle(title, subtitle, fadeIn?, stay?, fadeOut?)

显示屏幕中央大标题。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | string | (必需) | 主标题 |
| `subtitle` | string | (必需) | 副标题 |
| `fadeIn` | number | `10` | 淡入 tick 数 |
| `stay` | number | `70` | 停留 tick 数 |
| `fadeOut` | number | `20` | 淡出 tick 数 |

```js
ui.showTitle("Boss 来袭！", "准备战斗", 10, 70, 20);
ui.showTitle("§c游戏结束", "§7再接再厉");
```

## ui.showActionBar(text)

在动作栏显示文字（与 `showOverlay` 相同）。

```js
ui.showActionBar("§e按 F 键使用技能");
```

## ui.getScreenSize()

获取当前游戏窗口和 GUI 缩放尺寸。

```js
var size = ui.getScreenSize();
console.log(size.width, size.height);           // 窗口像素
console.log(size.scaledWidth, size.scaledHeight); // GUI 缩放坐标
```

## ui.drawText(id, x, y, text, color?)

在屏幕上绘制自定义文字（每帧持续绘制，直到调用 `removeDrawText` 移除）。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `id` | number | (必需) | 文字 ID，用于后续移除或更新 |
| `x` | number | (必需) | X 坐标（GUI 缩放坐标系） |
| `y` | number | (必需) | Y 坐标（GUI 缩放坐标系） |
| `text` | string | (必需) | 要显示的文字 |
| `color` | GameRGBColor | `白色` | 文字颜色 |

返回文字 ID（与传入的 `id` 相同）。重复调用相同 ID 会覆盖之前的内容。

```js
var textId = ui.drawText(1, 10, 10, "Hello, Box3JS!");
// 更新位置或内容
ui.drawText(1, 10, 30, "Updated text", new GameRGBColor(1, 0, 0)); // 红色
```

## ui.removeDrawText(id)

移除指定 ID 的绘制文字。

```js
ui.removeDrawText(1);
```

## ui.clearDrawTexts()

清除所有通过 `drawText()` 绘制的文字。

```js
ui.clearDrawTexts();
```
