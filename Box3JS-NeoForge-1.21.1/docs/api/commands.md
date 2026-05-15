# /box3script 命令参考

所有命令需要 **OP 权限等级 2**（默认管理员权限）。所有 `<project>` 参数均支持 **Tab 自动补全**。

## 命令列表

### `/box3script`

显示项目状态概览。

```js
/box3script
```

输出示例：

```text
══ Box3JS Script Engine ══

  Watch: ● Active    Sandbox: ● 1 project(s)

  Projects: 1/2 enabled  |  1 loaded

  ────────────────────────────
  ● colorzone ▐SANDBOX▌
  ◌ demo
  ────────────────────────────

  Start  /box3script start [name|all]
  Stop   /box3script stop [name|all]
  Reload /box3script reload [name]
  New    /box3script create <name>
```

- `◉` = 已加载运行中，`○` = 已启用但未加载，`◌` = 已禁用
- `▐SANDBOX▌` = 沙盒已开启

### `/box3script create <name>`

创建新的 TypeScript 脚本项目。生成完整的 TS 脚手架，默认**禁用**。

```js
/box3script create mygame
```

创建后需要：

```bash
cd config/box3/script/mygame
npm install && npm run build
```

然后用 `/box3script start mygame` 启用。

### `/box3script start [project|all]`

启用并加载项目。**不带参数** = 启用全部。**带项目名** = 只启用指定项目。**`all`** = 显式启用全部。

```js
/box3script start              # 启用全部
/box3script start all          # 启用全部（同无参数）
/box3script start mygame       # 只启用 mygame
```

### `/box3script stop [project|all]`

禁用并卸载项目。**不带参数** = 禁用全部。**带项目名** = 只禁用指定项目。**`all`** = 显式禁用全部。

```js
/box3script stop               # 禁用全部
/box3script stop all           # 禁用全部（同无参数）
/box3script stop mygame        # 只禁用 mygame
```

### `/box3script reload [project]`

重载脚本。**不带参数** = 停止全部，重新加载所有已启用项目。**带项目名** = 重载指定项目。

```js
/box3script reload            # 重载全部已启用项目
/box3script reload mygame     # 只重载 mygame
```

修改代码并 `npm run build` 后，用 `reload` 刷新。或者开启 `watch` 自动重载。

### `/box3script watch`

开启/关闭文件监听。监听所有项目的 `dist/` 目录，`.js` 文件变化时自动重载对应项目。

```js
/box3script watch             # 切换 开/关
```

### `/box3script sandbox <project>`

切换沙盒模式。开启后自动追踪该项目所有的方块/实体/世界状态变更，关闭时回滚并显示摘要。

```js
/box3script sandbox mygame    # 切换 开/关
```

追踪内容：方块修改、实体状态、玩家状态、世界设置（天气/时间/规则等）。

典型工作流：

```js
/box3script sandbox mygame    # 开启沙盒
/box3script start mygame      # 启用项目
# ... 测试 ...
/box3script reload mygame     # 修改代码后重载（沙盒跟踪保留）
# ... 满意后 ...
/box3script sandbox mygame    # 关闭沙盒 → 回滚全部修改
```

::: warning
沙盒仅追踪通过脚本 API 修改的方块（`setVoxel`/`setVoxelId`/`fillVoxel`），手动挖掘不受影响。
:::

### `/box3script compile <project>`

将脚本项目编译为**轻量独立 JAR 模组**（~50KB），依赖 Box3JS 模组提供 Rhino 运行时和 API 绑定。

```js
/box3script compile mygame
```

::: warning 依赖
脚本 JAR 不包含 Rhino 或 Box3JS API 类，需将 Box3JS 模组（`box3js`）一同放入 `mods/`。
:::

::: info 自定义注册表
如果存在 `registries/blocks.json`、`items.json`、`sounds.json`、`creativeTabs.json` 和 `assets/`，编译时会自动注册方块/物品/音效，并将资源打包进 JAR。客户端也需安装该 JAR 才能正常渲染。详见 [registries.md](registries.md)。
:::

编译时**从 `package.json` 读取以下字段**写入 `neoforge.mods.toml`：

| package.json | mods.toml 字段 | 说明 |
|-------------|---------------|------|
| `name` | `modId` | 模组 ID |
| `displayName` | `displayName` | 模组显示名称（默认同 `name`） |
| `version` | `version` | 版本号 |
| `description` | `description` | 模组简介 |
| `author` | `credits` | 作者/致谢 |
| `license` | `license` | 许可证（默认 `All Rights Reserved`） |
| `homepage` | `displayURL` | 项目主页链接 |
| `bugs.url` | `issueTrackerURL` | 问题反馈链接 |
| `logoFile` | `logoFile` | 模组图标（项目中的 PNG 路径，打包为 `logo.png`） |

::: tip logoFile 使用说明
填写项目根目录下的 PNG 文件相对路径（如 `"logoFile": "logo.png"`），编译时自动打包为 JAR 根目录的 `logo.png`，无需在 `neoforge.mods.toml` 中手动配置。NeoForge 建议尺寸 128×128 或 256×256，仅支持 PNG 格式。不填则使用默认模组图标。
:::

输出文件名格式：`dist/<name>-<version>.jar`。编译在后台线程运行，不阻塞服务器 tick，完成后聊天栏通知输出路径。

**前提条件：**

- 已完成 `npm run build`（`dist/server.js` 存在）
- 服务器运行在 **JDK**（不是 JRE），因为需要调用 `javac` 编译生成的 `@Mod` 入口类

**输出 JAR 内容：**

```text
mygame-1.0.0.jar
├── META-INF/neoforge.mods.toml      ← 模组元数据（依赖 box3js）
├── logo.png                         ← 模组图标（如有指定）
├── assets/mygame/                   ← 方块模型、纹理、blockstate（如有）
│   ├── lang/en_us.json              ← 语言文件（自动生成）
│   ├── blockstates/*.json
│   ├── models/block/*.json
│   ├── models/item/*.json
│   └── textures/block/*.png
├── box3script/mygame/MygameMod.class ← @Mod 入口（含 DeferredRegister）
├── box3script/mygame/server.js       ← 打包的服务端脚本
└── box3script/mygame/client.js       ← 打包的客户端脚本（如有）
```

**部署：** 将脚本 JAR 与 Box3JS 模组一起放入 `mods/`：

```text
mods/
├── box3js-1.0.0.jar       ← Box3JS 主模组
└── mygame-1.0.0.jar       ← 编译的脚本模组
```

**与解释模式的区别：**

| | 解释模式 | 编译模式 |
|---|---|---|
| 加载方式 | `/box3script start` | 放入 `mods/` 启动服务器 |
| 命令管理 | `/box3script start/stop/reload` | 不受 `/box3script` 管理 |
| 启用/禁用 | `/box3script start/stop` | 增删 `mods/` 下的 JAR，重启服务器 |
| 需要 Box3JS | 是 | 是 |
| 热重载 | 支持 | 不支持（JAR 重启才生效） |
| 适用场景 | 开发调试 | 分发部署 |

::: warning
编译后的 JAR 是标准 NeoForge mod，由 NeoForge mod loader 管理，**不受** `/box3script start/stop/reload` 控制。多个编译 JAR 可同时放入 `mods/`，各自独立运行，互不干扰。
:::

## 配置文件

启用/禁用状态保存在 `config/box3/scripts.json`：

```json
{
  "colorzone": true,
  "demo": false
}
```

## 脚本目录结构

```text
config/box3/
  ├── scripts.json             ← 项目开关配置
  ├── script/                   ← 脚本目录
  │   └── mygame/
  │       ├── build.mjs
  │       ├── package.json
  │       ├── eslint.config.mjs
  │       ├── tsconfig.json
  │       ├── types/
  │       ├── src/
  │       │   ├── server/app.ts
  │       │   └── client/app.ts
  │       ├── registries/         ← 方块/物品/音效注册（编译模式）
  │       │   ├── blocks.json
  │       │   └── creativeTabs.json
  │       ├── assets/             ← 模型/纹理/音效/语言（编译模式）
  │       │   └── textures/block/
  │       └── dist/
  │           ├── server.js       ← 编译产物
  │           ├── client.js       ← 客户端编译产物
  │           └── <name>-<ver>.jar ← 独立 JAR（compile 命令生成）
  ├── data/                      ← SQLite 数据库 (db API)
  └── storage/                  ← storage API 持久化
```
