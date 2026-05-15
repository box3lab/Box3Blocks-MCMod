# client — 客户端 API

客户端脚本运行在玩家本地 Minecraft 客户端上，入口文件是 `src/client/app.ts`，构建产物是 `dist/client.js`。

## 客户端全局对象

| 对象 | 类型 | 用途 | 文档 |
|------|------|------|------|
| `client` | `GameClient` | 生命周期回调、玩家信息、雾效 | 本页 |
| `audio` | `GameAudio` | 音效、音乐播放与音量控制 | [audio.md](audio.md) |
| `input` | `GameInput` | 键盘输入检测、鼠标事件 | [input.md](input.md) |
| `ui` | `GameUI` | 屏幕文字显示（ActionBar、标题、自定义绘制） | [ui.md](ui.md) |
| `chat` | `GameChat` | 收发聊天消息、发送命令 | [chat.md](chat.md) |
| `gui` | `GameGUI` | 自定义容器 GUI 界面 | [gui.md](gui.md) |
| `remoteChannel` | `RemoteChannel` | 客户端 ↔ 服务端事件通信 | [remote-channel.md](remote-channel.md) |
| `storage` | `GameStorage` | 客户端本地持久化存储 | [storage.md](storage.md) |
| `db` | `GameDatabase` | 客户端本地 SQLite 数据库 | [database.md](database.md) |
| `http` | `GameHttpAPI` | HTTP 请求（同步/异步） | [http.md](http.md) |

::: info 前置条件
客户端必须安装 Box3JS mod，服务端必须启用该项目的客户端脚本并通过网络自动下发。客户端类型入口是 `types/client/index.d.ts`，不会包含服务端 `world` / `voxels` API。
:::

客户端脚本不能直接修改服务端世界。需要改变方块、玩家、实体或计分板时，应发送事件给服务端：

```ts
remoteChannel.sendServerEvent({ type: "requestTeleport" });
```

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

## 雾效控制 (Fog Control)

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
