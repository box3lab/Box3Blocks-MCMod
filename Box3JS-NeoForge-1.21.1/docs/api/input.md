# input — 输入 API

客户端键盘与鼠标输入检测。

## input.isKeyDown(key)

检查指定按键当前是否被按下。

| 参数 | 类型 | 说明 |
|------|------|------|
| `key` | string | 按键名称（小写），见下方按键列表 |

```js
if (input.isKeyDown("space")) {
  // 空格键正在被按住
}
```

## input.onKeyPress(key, callback)

注册按键按下回调（按下瞬间触发一次）。返回 `GameEventHandlerToken`，调用 `.cancel()` 取消。

```js
var token = input.onKeyPress("f", () => {
  chat.sendCommand("fly");
});

// 取消监听
token.cancel();
```

## input.getMouseX()

获取当前鼠标 X 坐标（屏幕像素）。

```js
var mx = input.getMouseX();
```

## input.getMouseY()

获取当前鼠标 Y 坐标（屏幕像素）。

```js
var my = input.getMouseY();
```

## input.onMouseClick(callback)

注册鼠标按键回调。返回 `GameEventHandlerToken`，调用 `.cancel()` 取消。

回调参数：`(button: number, action: number, x: number, y: number) => void`

| 参数 | 说明 |
|------|------|
| `button` | 0=左键, 1=右键, 2=中键 |
| `action` | 0=释放, 1=按下, 2=重复 |
| `x` | 鼠标 X 坐标（屏幕像素） |
| `y` | 鼠标 Y 坐标（屏幕像素） |

```js
var token = input.onMouseClick((button, action, x, y) => {
  if (action === 1) { // 按下
    console.log(`Clicked button ${button} at (${x}, ${y})`);
  }
});

// 取消监听
token.cancel();
```

## 支持的按键名称

| 类别 | 按键 |
|------|------|
| 字母 | `a`–`z` |
| 数字 | `0`–`9` |
| 功能键 | `f1`–`f12` |
| 方向键 | `up`, `down`, `left`, `right` |
| 特殊键 | `space`, `enter`, `escape`, `tab`, `backspace`, `delete` |
| 修饰键 | `shift`, `left_shift`, `right_shift`, `ctrl`, `left_ctrl`, `right_ctrl`, `alt`, `left_alt`, `right_alt` |
