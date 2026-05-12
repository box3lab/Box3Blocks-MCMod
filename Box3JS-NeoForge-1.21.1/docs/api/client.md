# client — 客户端 API

客户端脚本运行在玩家本地 Minecraft 客户端上，通过以下五个全局对象访问：

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
| `remoteChannel` | `RemoteChannel` | 客户端 ↔ 服务端事件通信 |

> **前置条件：** 客户端必须安装 Box3JS mod，服务端必须启用该项目的客户端脚本并通过网络自动下发。
> 客户端脚本放在 `src/client/` 目录下，服务端脚本放在 `src/server/` 目录下。

## audio — 音频播放

### audio.playSound(path, volume, pitch)

🆕 MC 扩展 | 播放音效（SoundSource.PLAYERS 类别）。

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

🆕 MC 扩展 | 播放音乐（SoundSource.MUSIC 类别）。参数同 `playSound`。

```js
audio.playMusic("minecraft:music.creative", 0.5, 1.0);
```

### audio.stopAll()

🆕 MC 扩展 | 停止所有正在播放的声音和音乐。

```js
audio.stopAll();
```

### audio.getVolume(category)

🆕 MC 扩展 | 获取指定音频类别的音量。

| 参数 | 类型 | 说明 |
|------|------|------|
| `category` | string | 类别名称，见下方列表 |

```js
var musicVol = audio.getVolume("music"); // 0.0–1.0
```

### audio.setVolume(category, value)

🆕 MC 扩展 | 设置指定音频类别的音量。

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

🆕 MC 扩展 | 注册客户端每 tick 回调（每秒 20 次）。无参数，无返回值。

```js
client.onTick(() => {
  // 每帧更新逻辑
});
```

> **注意：** 服务端也有 `world.onTick()`，但参数为 `TickInfo` 对象。客户端 `client.onTick()` 无参数。

## input — 键盘输入

### input.isKeyDown(key)

🆕 MC 扩展 | 检查指定按键当前是否被按下。

| 参数 | 类型 | 说明 |
|------|------|------|
| `key` | string | 按键名称（小写），见下方按键列表 |

```js
if (input.isKeyDown("space")) {
  // 空格键正在被按住
}
```

### input.onKeyPress(key, callback)

🆕 MC 扩展 | 注册按键按下回调（按下瞬间触发一次）。返回 `GameEventHandlerToken`，调用 `.cancel()` 取消。

```js
var token = input.onKeyPress("f", () => {
  chat.sendCommand("fly");
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
| 修饰键 | `left_shift`, `right_shift`, `left_ctrl`, `right_ctrl`, `left_alt`, `right_alt` |

## ui — 屏幕 UI

### ui.showOverlay(text)

🆕 MC 扩展 | 在动作栏（快捷栏上方）显示文字。支持颜色代码（`§a`、`§b` 等）。

```js
ui.showOverlay("§a欢迎来到服务器！");
```

### ui.showTitle(title, subtitle, fadeIn?, stay?, fadeOut?)

🆕 MC 扩展 | 显示屏幕中央大标题。

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

🆕 MC 扩展 | 在动作栏显示文字（与 `showOverlay` 相同）。

```js
ui.showActionBar("§e按 F 键使用技能");
```

## chat — 聊天消息与命令

### chat.sendMessage(text)

🆕 MC 扩展 | 向服务端发送聊天消息。

```js
chat.sendMessage("大家好！");
```

### chat.sendCommand(cmd)

🆕 MC 扩展 | 向服务端发送命令（等同于在聊天框输入 `/` 前缀的命令）。

```js
chat.sendCommand("spawn");
chat.sendCommand("home");
```

### chat.onMessage(handler)

🆕 MC 扩展 | 注册接收聊天消息的处理器。返回 `GameEventHandlerToken`，调用 `.cancel()` 取消。

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

## remoteChannel — 客户端 ↔ 服务端通信

客户端通过 `remoteChannel` 与服务端进行双向事件通信。事件数据通过 JSON 序列化传输。

### remoteChannel.sendServerEvent(event)

🆕 MC 扩展 | 向服务端发送事件。`event` 为任意 JSON 可序列化的值。

```js
remoteChannel.sendServerEvent({
  type: "clientReady",
  timestamp: Date.now(),
});
```

### remoteChannel.onClientEvent(handler)

🆕 MC 扩展 | 注册来自服务端的远程事件处理器。返回 `GameEventHandlerToken`。

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

> 服务端对应 API 为 `remoteChannel.sendClientEvent()` / `broadcastClientEvent()` / `onServerEvent()`。
> 详见 `server.d.ts` 中的类型声明。

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

全部 🆕 MC 扩展（客户端 API 为 Box3JS 专属，非 Box3 平台原有）。
