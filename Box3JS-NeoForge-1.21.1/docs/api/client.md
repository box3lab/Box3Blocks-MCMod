# client — 客户端 API

客户端脚本运行在玩家本地 Minecraft 客户端上，入口文件是 `src/client/app.ts`，构建产物是 `dist/client.js`。客户端 API 只负责本地 UI、输入、音频、聊天辅助、本地存储、本地 HTTP/SQLite 以及接收/发送跨端事件。

客户端脚本通过以下全局对象访问 API：

| 对象 | 类型 | 用途 |
|------|------|------|
| `audio` | `GameAudio` | 音效、音乐播放与音量控制 |
| `client` | `GameClient` | 生命周期回调 |
| `input` | `GameInput` | 键盘输入检测 |
| `ui` | `GameUI` | 屏幕文字显示（ActionBar、标题） |
| `chat` | `GameChat` | 收发聊天消息、发送命令 |
| `storage` | `GameStorage` | 客户端本地持久化存储 |
| `db` | `GameDatabase` | 客户端本地 SQLite 数据库 |
| `http` | `GameHttpAPI` | HTTP 请求（同步/异步） |
| `gui` | `GameGUI` | 自定义容器 GUI 界面 |
| `remoteChannel` | `RemoteChannel` | 客户端 ↔ 服务端事件通信 |

::: info 前置条件
客户端必须安装 Box3JS mod，服务端必须启用该项目的客户端脚本并通过网络自动下发。客户端脚本放在 `src/client/` 目录下，服务端脚本放在 `src/server/` 目录下。客户端类型入口是 `types/client/index.d.ts`，不会包含服务端 `world` / `voxels` API。
:::

客户端脚本不能直接修改服务端世界。需要改变方块、玩家、实体或计分板时，应发送事件给服务端：

```ts
remoteChannel.sendServerEvent({ type: "requestTeleport" });
```

## audio — 音频播放

### audio.playSound(path, volume, pitch)

播放音效（SoundSource.PLAYERS 类别）。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `path` | string | (必需) | 声音 ID，如 `"minecraft:block.note_block.pling"` |
| `volume` | number | `1.0` | 音量 (0–1) |
| `pitch` | number | `1.0` | 音高 (0.5–2) |

```js
audio.playSound("minecraft:block.note_block.pling", 1.0, 1.0);
audio.playSound("minecraft:entity.experience_orb.pickup", 0.5, 1.5);
```

### audio.playMusic(path, volume, pitch)

播放音乐（SoundSource.MUSIC 类别）。参数同 `playSound`。

```js
audio.playMusic("minecraft:music.creative", 0.5, 1.0);
```

### audio.stopAll()

停止所有正在播放的声音和音乐。

```js
audio.stopAll();
```

### audio.getVolume(category)

获取指定音频类别的音量。

| 参数 | 类型 | 说明 |
|------|------|------|
| `category` | string | 类别名称，见下方列表 |

```js
var musicVol = audio.getVolume("music"); // 0.0–1.0
```

### audio.setVolume(category, value)

设置指定音频类别的音量。

| 参数 | 类型 | 说明 |
|------|------|------|
| `category` | string | 类别名称 |
| `value` | number | 音量值 (0–1) |

```js
audio.setVolume("music", 0.5);
audio.setVolume("player", 0.8);
```

### 音频类别

| 类别 | 说明 |
|------|------|
| `master` | 主音量 |
| `music` | 音乐 |
| `record` | 唱片/音符盒 |
| `weather` | 天气（雨） |
| `block` | 方块 |
| `hostile` | 敌对生物 |
| `neutral` | 中立生物 |
| `player` | 玩家 |
| `ambient` | 环境 |
| `voice` | 语音 |

## client — 生命周期

### client.onTick(callback)

注册客户端每 tick 回调（每秒 20 次）。无参数，返回 `GameEventHandlerToken`，可用 `cancel()` 取消监听。

```js
const token = client.onTick(() => {
  // 每帧更新逻辑
});

// token.cancel();
```

::: info 注意
服务端也有 `world.onTick()`，但参数为 `TickInfo` 对象。客户端 `client.onTick()` 无参数。
:::

### client.getFPS()

获取当前游戏帧率 (FPS)。

```js
var fps = client.getFPS();
console.log(`Current FPS: ${fps}`);
```

### client.getPlayer()

获取本地玩家信息。如果玩家未加载则返回 `null`。

```js
var player = client.getPlayer();
if (player) {
  console.log(`Player: ${player.name}, HP: ${player.health}/${player.maxHealth}`);
  console.log(`Position: ${player.position.x}, ${player.position.y}, ${player.position.z}`);
}
```

### client.getLookingAt()

获取玩家准星正在看向的目标。未指向任何目标时返回 `null`。

```js
var target = client.getLookingAt();
if (target) {
  if (target.type === "entity") {
    console.log(`Looking at entity: ${target.entity.name}`);
  } else if (target.type === "block") {
    console.log(`Looking at block: ${target.blockPos.x}, ${target.blockPos.y}, ${target.blockPos.z}`);
  }
}
```

### client.getServerInfo()

获取当前连接的服务器信息。单人游戏返回 `{ ip: "localhost", name: "Singleplayer", isLocal: true }`。

```js
var info = client.getServerInfo();
console.log(`Server: ${info.name} (${info.ip})`);
if (!info.isLocal) {
  console.log(`Players: ${info.playerCount}/${info.maxPlayers}`);
}
```

### 雾效控制 (Fog Control)

Box3JS 客户端可以覆盖 Minecraft 的雾颜色和距离，实现类似 Box3 `world.fogColor` / `world.maxFog` 的效果。

### client.getFogColor()

获取当前自定义雾颜色。未设置时返回 `null`。

```js
var color = client.getFogColor();
if (color) {
  console.log("Fog color: " + color.r + ", " + color.g + ", " + color.b);
}
```

### client.setFogColor(r, g, b)

设置雾颜色（RGB 0-255）。

| 参数 | 类型   | 说明       |
|------|--------|------------|
| `r`  | number | 红色 (0-255) |
| `g`  | number | 绿色 (0-255) |
| `b`  | number | 蓝色 (0-255) |

```js
// 红色迷雾效果
client.setFogColor(255, 50, 50);
```

### client.setFogStartDistance(distance)

设置雾起始距离（方块）。低于此距离完全透明。

| 参数       | 类型   | 说明               |
|------------|--------|--------------------|
| `distance` | number | 雾起始距离（方块） |

```js
// 雾从 10 个方块距离外开始
client.setFogStartDistance(10);
```

### client.setFogEndDistance(distance)

设置雾结束距离（方块），对应 Box3 的 `maxFog`。超过此距离完全被雾遮挡。

| 参数       | 类型   | 说明               |
|------------|--------|--------------------|
| `distance` | number | 雾结束距离（方块） |

```js
// 50 格以外完全被雾遮挡
client.setFogEndDistance(50);
```

### client.resetFog()

重置雾效果为 Minecraft 默认值。

```js
client.resetFog();
```

## input — 键盘输入

### input.isKeyDown(key)

检查指定按键当前是否被按下。

| 参数 | 类型 | 说明 |
|------|------|------|
| `key` | string | 按键名称（小写），见下方按键列表 |

```js
if (input.isKeyDown("space")) {
  // 空格键正在被按住
}
```

### input.onKeyPress(key, callback)

注册按键按下回调（按下瞬间触发一次）。返回 `GameEventHandlerToken`，调用 `.cancel()` 取消。

```js
var token = input.onKeyPress("f", () => {
  chat.sendCommand("fly");
});

// 取消监听
token.cancel();
```

### input.getMouseX()

获取当前鼠标 X 坐标（屏幕像素）。

```js
var mx = input.getMouseX();
```

### input.getMouseY()

获取当前鼠标 Y 坐标（屏幕像素）。

```js
var my = input.getMouseY();
```

### input.onMouseClick(callback)

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

### 支持的按键名称

| 类别 | 按键 |
|------|------|
| 字母 | `a`–`z` |
| 数字 | `0`–`9` |
| 功能键 | `f1`–`f12` |
| 方向键 | `up`, `down`, `left`, `right` |
| 特殊键 | `space`, `enter`, `escape`, `tab`, `backspace`, `delete` |
| 修饰键 | `shift`, `left_shift`, `right_shift`, `ctrl`, `left_ctrl`, `right_ctrl`, `alt`, `left_alt`, `right_alt` |

## ui — 屏幕 UI

### ui.showOverlay(text)

在动作栏（快捷栏上方）显示文字。支持颜色代码（`§a`、`§b` 等）。

```js
ui.showOverlay("§a欢迎来到服务器！");
```

### ui.showTitle(title, subtitle, fadeIn?, stay?, fadeOut?)

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

### ui.showActionBar(text)

在动作栏显示文字（与 `showOverlay` 相同）。

```js
ui.showActionBar("§e按 F 键使用技能");
```

### ui.getScreenSize()

获取当前游戏窗口和 GUI 缩放尺寸。

```js
var size = ui.getScreenSize();
console.log(size.width, size.height);           // 窗口像素
console.log(size.scaledWidth, size.scaledHeight); // GUI 缩放坐标
```

### ui.drawText(id, x, y, text, color?)

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

### ui.removeDrawText(id)

移除指定 ID 的绘制文字。

```js
ui.removeDrawText(1);
```

### ui.clearDrawTexts()

清除所有通过 `drawText()` 绘制的文字。

```js
ui.clearDrawTexts();
```

## chat — 聊天消息与命令

### chat.sendMessage(text)

向服务端发送聊天消息。

```js
chat.sendMessage("大家好！");
```

### chat.sendCommand(cmd)

向服务端发送命令（等同于在聊天框输入 `/` 前缀的命令）。

```js
chat.sendCommand("spawn");
chat.sendCommand("home");
```

### chat.onMessage(handler)

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

## gui — 自定义 GUI

### gui.openGUI(config)

打开一个脚本控制的自定义容器 GUI（类似箱子界面），返回控制器对象。
客户端会自动向服务端请求创建容器，并返回 `GuiController` 用于操作界面和监听事件。

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

### GuiController 方法

| 方法 | 说明 |
|------|------|
| `setItem(slot, itemId, count?)` | 设置指定槽位的物品 |
| `getItem(slot)` | 获取指定槽位的物品，返回 `{ id, count }` |
| `onSlotClick(callback)` | 注册槽位点击回调，返回 `GameEventHandlerToken`，`callback(slot: number)` |
| `onClose(callback)` | 注册关闭回调，返回 `GameEventHandlerToken`，`callback()` |
| `close()` | 关闭 GUI |

## remoteChannel — 客户端 ↔ 服务端通信

客户端通过 `remoteChannel` 与服务端进行双向事件通信。事件数据通过 JSON 序列化传输。

### remoteChannel.sendServerEvent(event)

向服务端发送事件。`event` 为任意 JSON 可序列化的值。

```js
remoteChannel.sendServerEvent({
  type: "clientReady",
  timestamp: Date.now(),
});
```

### remoteChannel.onClientEvent(handler)

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
服务端对应 API 为 `remoteChannel.sendClientEvent()` / `broadcastClientEvent()` / `onServerEvent()`。详见 `server.d.ts` 中的类型声明。
:::

## storage — 客户端存储

客户端也有独立的 `storage`，数据保存在客户端本地 `.minecraft/config/box3/data/<项目名>/` 目录下。API 与服务端 `storage` 完全一致：

```js
var store = storage.getDataStorage("settings");
store.set("volume", 0.8);
var volume = store.get("volume"); // 0.8
```

详细 API 参考 [storage.md](storage.md)。

## 客户端完整示例

```js
// src/client/app.ts

// 每帧更新
client.onTick(() => {
  if (input.isKeyDown("space")) {
    // 空格键被按住
  }
});

// 按键触发命令
input.onKeyPress("g", () => {
  chat.sendCommand("gamemode creative");
});

// 显示欢迎标题
ui.showTitle("§a欢迎回来", "§7祝你游戏愉快", 10, 70, 20);

// 接收聊天
chat.onMessage((message, sender, isSystem) => {
  if (message === "!info") {
    ui.showOverlay("§e当前服务器: §f" + sender);
    return false;
  }
});

// 与服务端通信
remoteChannel.sendServerEvent({ type: "clientLoaded" });

remoteChannel.onClientEvent((event) => {
  if (event.args.type === "alert") {
    audio.playSound("minecraft:block.note_block.pling", 1.0, 1.0);
    ui.showOverlay("§c" + event.args.message);
  }
});

console.log("[client] loaded!");
```

客户端 API 为 Box3JS 专属。
