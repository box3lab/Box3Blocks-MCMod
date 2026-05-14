# 教程六：客户端脚本开发

本教程覆盖 Box3JS 客户端脚本的全部 9 个全局对象：生命周期、键盘输入、屏幕 UI、聊天控制、音效/音乐、本地存储、SQLite、HTTP 请求、双向通讯。

## 前置条件

- 玩家必须安装 Box3JS 客户端 Mod
- 服务端已启用该项目，客户端脚本会自动下发
- 客户端代码放在 `src/client/app.ts`

## 6.1 客户端与脚本对比

| 特性 | 服务端脚本 | 客户端脚本 |
|------|-----------|-----------|
| 运行位置 | 服务端 | 玩家本地 |
| 可见范围 | 所有玩家共用 | 每个玩家独立 |
| 世界操作 | ✅ 可修改世界 | ❌ 只读 |
| 键盘输入 | ❌ 无法监听 | ✅ 可检测按键 |
| 屏幕 UI | ❌ 只能标题/ActionBar | ✅ 全屏文字覆盖 |
| 音效 | 全局/定向 | 仅本机听到 |
| 存储 | 服务端统一 | 客户端本地独立 |

## 6.2 完整示例概览

colorzone 项目包含完整的客户端脚本示例（`src/client/app.ts`），覆盖以下功能：

| 系统 | 用途 | 键/命令 |
|------|------|--------|
| storage | 设置存储、笔记、计数器 | `!settings` `!notes` `!note` |
| db | 怪物缓存、收藏夹 | `!mob` `!fav` |
| http | 同步/异步 GET/POST | `!sync` / F8-F10 |
| remoteChannel | 服务端↔客户端通讯 | `!ping` `!broadcast` |
| input | 键盘快捷键 | F6-F12, C, V |
| ui | 屏幕文字 | F6 显示设置 |
| chat | 聊天命令 | `!fav` `!mob` |
| audio | 自定义音效 | V 键 |
| fog | 雾颜色和距离控制 | — |

## 6.3 client — 生命周期

`client.onTick(callback)` 是客户端的"心跳"，每秒执行 20 次。适合做定期检查：

```js
let tickCount = 0;
client.onTick(() => {
  tickCount++;
  // 每 5 秒输出一次日志
  if (tickCount % 100 === 0) {
    console.log(`[client] Running: ${tickCount / 20}s`);
  }
});
```

与其他事件 API 一样，`client.onTick()` 会返回 `GameEventHandlerToken`；不再需要监听时调用 `token.cancel()`。

**性能提示：** 客户端 onTick 也在主线程执行。避免密集循环，用取模运算降低实际执行频率。

## 6.4 input — 键盘输入

### 检测按键

```js
// 按下时触发（单次回调）
input.onKeyPress("f", () => {
  ui.showOverlay("§a你按下了 F 键！");
});

// 检测按键是否按住（放在 onTick 中持续检测）
client.onTick(() => {
  if (input.isKeyDown("space")) {
    // 空格键被按住
  }
});
```

支持的按键名：`a`-`z`, `0`-`9`, `f1`-`f12`, `space`, `shift`, `ctrl`, `alt`, `tab`, `enter`, `backspace`, `escape`, `up`, `down`, `left`, `right`

## 6.5 ui — 屏幕 UI

```js
// ActionBar 覆盖文字（快捷栏上方）
ui.showOverlay("§e这是一条提示");

// 屏幕标题（大字，带淡入淡出）
ui.showTitle("§6§l主标题", "§7副标题");
// 带时间: (主标题, 副标题, 淡入tick, 停留tick, 淡出tick)
ui.showTitle("§c§l警告", "§7距离边界缩圈还有 10 秒", 5, 40, 10);

// 清除标题
ui.clearTitle();
```

**与服端端对比：** `player.title()` 和 `player.actionBar()` 是服务端发送给玩家，`ui.showTitle()` 和 `ui.showOverlay()` 是客户端本地显示。客户端 UI 不受网络延迟影响。

## 6.6 chat — 收发聊天

```js
// 接收聊天消息（包括系统消息）
chat.onMessage((message: string, sender: string, isSystem: boolean) => {
  if (isSystem) return;  // 忽略系统消息

  console.log(`[chat] ${sender}: ${message}`);

  // 客户端本地命令（不影响服务端）
  if (message === "!sync") {
    syncGet();
    return false;  // ★ 返回 false 阻止该消息在聊天栏显示
  }
  return true;
});

// 发送聊天消息
chat.sendMessage("大家好！");

// 发送命令（等同于在聊天栏输入 /command）
chat.sendCommand("box3script");
```

## 6.7 audio — 音效与音乐

```js
// 播放音效: (path, volume, pitch)
audio.playSound("minecraft:block.note_block.pling", 1.0, 1.0);
audio.playSound("minecraft:entity.experience_orb.pickup", 0.5, 1.5);

// 播放音乐
audio.playMusic("minecraft:music.creative", 0.5, 1.0);

// 停止所有声音
audio.stopAll();

// 音量控制
audio.setVolume("music", 0.5);       // 设置音乐音量
audio.setVolume("player", 0.8);      // 设置玩家音效音量
const musicVol = audio.getVolume("music");  // 读取当前音量

// 自定义音效（需 registries 注册）
audio.playSound("colorzone:victory_fanfare", 1.0, 1.0);
```

## 6.8 fog — 雾效控制

覆盖 Minecraft 的雾颜色和渲染距离：

```js
// 设置雾颜色（RGB 0-255）
client.setFogColor(255, 100, 50);

// 设置雾距离（单位：方块）
client.setFogStartDistance(10);     // 雾从 10 格外开始
client.setFogEndDistance(50);       // 50 格外完全被雾遮挡

// 读取当前雾颜色
const color = client.getFogColor(); // 返回 GameRGBColor 或 null

// 恢复 Minecraft 默认雾效果
client.resetFog();
```

> **注意**: 雾效修改在客户端本地生效。可通过 `remoteChannel` 让服务端指令触发客户端雾效变化，实现服务端控制的天气效果。

## 6.9 storage — 客户端本地存储

客户端的 `storage` 与服务端用法相同，但数据存储在玩家本地：

```js
// 方式一：整个对象作为一个 key 存储（推荐，强类型）
type Settings = {
  theme: string;
  overlayEnabled: boolean;
  fontSize: number;
};

const settings = storage.getDataStorage<Settings>("client-settings");

// 初始化默认值
if (settings.get("main") === null) {
  settings.set("main", {
    theme: "dark",
    overlayEnabled: true,
    fontSize: 14,
  });
}

// 读取
const cfg = settings.get("main") as Settings;
console.log(cfg.theme);

// 原子更新（读取→修改→写回）
settings.update("main", (prev: Settings) => {
  prev.overlayEnabled = !prev.overlayEnabled;
  return prev;
});

// 方式二：单独字段存储（适合简单键值对）
const prefs = storage.getDataStorage("prefs");
prefs.set("soundVolume", 0.8);
prefs.set("showTips", true);

// 方式三：计数器（自动递增）
const visitCount = storage.getDataStorage<number>("visit-counter");
const count = visitCount.increment("total", 1);

// 方式四：笔记系统（结构化数据 + 分页查询）
type Note = { title: string; content: string; createdAt: number };
const notes = storage.getDataStorage<Note>("notes");

notes.set("welcome", {
  title: "Welcome",
  content: "Box3JS client demo is ready!",
  createdAt: Date.now(),
});

// 分页列表
const page = notes.list({ pageSize: 10, ascending: false });
const entries = page.getCurrentPage();
```

## 6.10 db — 客户端 SQLite

客户端也支持 SQLite（需要 `minecraft-sqlite-jdbc` 模组）：

```js
// 检查数据库是否可用
if (!db.isAvailable()) {
  console.warn("SQLite driver not installed");
  return;
}

// 建表
db.sql(
  "CREATE TABLE IF NOT EXISTS mob_cache (name TEXT PRIMARY KEY, health REAL, type TEXT)"
);

// 插入数据
db.sql(
  "INSERT OR REPLACE INTO mob_cache (name, health, type) VALUES (?, ?, ?)",
  "Zombie", 20, "undead"
);

// 查询
const allMobs = db.sql("SELECT * FROM mob_cache ORDER BY name");
console.log(`Found ${allMobs.rowCount} mobs`);

// 遍历结果
for (let i = 0; i < allMobs.rowCount; i++) {
  const row = allMobs.rows[i];
  console.log(`${row.name} (HP: ${row.health})`);
}

// tagged template 风格（防 SQL 注入）
function searchMobs(keyword: string): void {
  const result = db.sql(
    ["SELECT * FROM mob_cache WHERE name LIKE '%", "%'"],
    keyword,
  );
  if (result.rowCount > 0) {
    const names: string[] = [];
    for (let i = 0; i < result.rowCount; i++) {
      names.push(result.rows[i].name);
    }
    ui.showOverlay(`§aMobs: §f${names.join(", ")}`);
  }
}
```

> 未安装 `minecraft-sqlite-jdbc` 时，`db.isAvailable()` 返回 `false`，所有 SQL 调用静默返回空结果。

## 6.11 http — 客户端 HTTP 请求

```js
// 同步 GET
const resp = http.fetch("https://httpbin.org/get", {
  method: "GET",
  timeout: 5000,
  responseType: "json",
});

if (resp.ok) {
  console.log(JSON.stringify(resp.data));
  ui.showOverlay(`§aOK — status=${resp.status}`);
} else {
  ui.showOverlay(`§cHTTP ${resp.status} — ${resp.errorMessage}`);
}

// 异步 GET（不阻塞游戏）
http.fetch("https://httpbin.org/delay/2", {
  method: "GET",
  timeout: 8000,
  responseType: "json",
  async: true,
  onResponse: (resp) => {
    console.log(`Async OK — status=${resp.status}`);
    ui.showOverlay(`§aAsync response received`);
  },
  onError: (err) => {
    console.error(`Async error: ${err}`);
  },
});

// POST JSON
http.fetch("https://httpbin.org/post", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ source: "box3js-client", timestamp: Date.now() }),
  timeout: 5000,
  responseType: "json",
});
```

## 6.12 remoteChannel — 两端通讯

这是客户端脚本最强大的功能：服务端和客户端可以互相发送事件。

### 服务端 → 客户端

```js
// === 服务端 ===
// 发给单个玩家
remoteChannel.sendClientEvent(entity, {
  type: "ping",
  message: "Hello from server!",
  serverTick: world.currentTick(),
});

// 广播给所有玩家
remoteChannel.broadcastClientEvent({
  type: "broadcast",
  message: "Server announcement!",
});
```

```js
// === 客户端 ===
remoteChannel.onClientEvent((event) => {
  const { tick, args } = event;

  switch (args.type) {
    case "ping": {
      console.log(`Ping (tick ${tick}): ${args.message}`);
      // 回复给服务端
      remoteChannel.sendServerEvent({
        type: "pong",
        clientTick: tick,
        timestamp: Date.now(),
      });
      break;
    }

    case "broadcast":
      ui.showOverlay(`§e📢 ${args.message}`);
      break;
  }
});
```

### 客户端 → 服务端

```js
// === 客户端 ===
remoteChannel.sendServerEvent({
  type: "clientReady",
  clientVersion: "1.0.0",
});
```

```js
// === 服务端 ===
remoteChannel.onServerEvent((event) => {
  const { entity, tick, args } = event;
  const name = entity.player.name;

  console.log(`[server] Received from ${name}: ${JSON.stringify(args)}`);

  if (args.type === "clientReady") {
    console.log(`[server] ${name}'s client has Box3JS installed!`);
    // 返回欢迎消息
    remoteChannel.sendClientEvent(entity, {
      type: "welcome",
      message: `Welcome, ${name}!`,
    });
  }
});
```

### 检测客户端兼容性

无需手动检测。`remoteChannel.sendClientEvent()` 使用可选数据包，未安装 Box3JS 客户端的玩家会自动忽略，不会报错或断线。可以放心向所有玩家发送。

### 通讯数据格式

> **重要：** 跨网络传输的数据必须是 JSON 可序列化的类型（string、number、boolean、null、普通对象、数组）。不能传函数、Java 对象或 `GameVector3`。

## 6.13 完整实战：客户端 HUD 状态栏

综合运用 input、ui、remoteChannel 和 storage 创建一个自定义 HUD：

```js
// ── 设置管理 ──
type HUDConfig = {
  showFPS: boolean;
  showCoords: boolean;
  showPing: boolean;
};

const hudConfig = storage.getDataStorage<HUDConfig>("hud-config");
if (hudConfig.get("main") === null) {
  hudConfig.set("main", { showFPS: true, showCoords: true, showPing: true });
}

// ── 切换开关 ──
input.onKeyPress("f6", () => {
  hudConfig.update("main", (prev: HUDConfig) => {
    prev.showCoords = !prev.showCoords;
    return prev;
  });
  const cfg = hudConfig.get("main") as HUDConfig;
  ui.showOverlay(`坐标显示: ${cfg.showCoords ? "§aON" : "§cOFF"}`);
});

input.onKeyPress("f7", () => {
  hudConfig.update("main", (prev: HUDConfig) => {
    prev.showFPS = !prev.showFPS;
    return prev;
  });
});

// ── 每 2 秒刷新 HUD ──
let lastPing = 0;
let frameCount = 0;

client.onTick(() => {
  frameCount++;
  if (frameCount % 40 !== 0) return;  // 每 2 秒更新

  const cfg = hudConfig.get("main") as HUDConfig;
  const lines: string[] = [];

  if (cfg.showFPS) lines.push(`§fFPS: §a${/* 估算 FPS */ Math.round(frameCount / 2)}`);
  if (cfg.showCoords) {
    // 通过 remoteChannel 从服务端获取位置
    remoteChannel.sendServerEvent({ type: "requestPosition" });
  }
  if (cfg.showPing && lastPing > 0) lines.push(`§fPing: §e${lastPing}ms`);

  if (lines.length > 0) ui.showOverlay(lines.join(" §7| "));
});

// ── 接收服务端响应 ──
remoteChannel.onClientEvent((event) => {
  if (event.args.type === "position") {
    const pos = event.args.data;
    // 位置信息由服务端回传
  }
});

// ── 启动 ──
ui.showTitle("§6自定义 HUD 已启动", "§7F6=坐标 F7=FPS", 10, 40, 10);
console.log("[HUD] Client HUD demo loaded");
```

## 6.14 客户端脚本调试

客户端脚本的 `console.log` 输出到**客户端日志**（不是服务端）。在 Minecraft 启动器或日志目录中查看。

排查顺序：
1. 确认客户端已安装 Box3JS mod
2. 检查 `dist/client.js` 是否已生成（`npm run build`）
3. 服务端 `/box3script status` 确认客户端脚本已启用
4. 查看客户端日志文件

## 下一步

[API 参考 →](../api/client.md) 完整客户端 API 文档 · [教程一](01-basics.md) 回到基础
