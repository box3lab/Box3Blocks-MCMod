# /box3script 命令参考

所有命令需要 **OP 权限等级 2**（默认管理员权限）。所有 `<project>` 参数均支持 **Tab 自动补全**。

## 命令列表

### `/box3script`

显示项目状态概览。

```
/box3script
```

输出示例：

```
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

```
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

```
/box3script start              # 启用全部
/box3script start all          # 启用全部（同无参数）
/box3script start mygame       # 只启用 mygame
```

### `/box3script stop [project|all]`

禁用并卸载项目。**不带参数** = 禁用全部。**带项目名** = 只禁用指定项目。**`all`** = 显式禁用全部。

```
/box3script stop               # 禁用全部
/box3script stop all           # 禁用全部（同无参数）
/box3script stop mygame        # 只禁用 mygame
```

### `/box3script reload [project]`

重载脚本。**不带参数** = 停止全部，重新加载所有已启用项目。**带项目名** = 重载指定项目。

```
/box3script reload            # 重载全部已启用项目
/box3script reload mygame     # 只重载 mygame
```

修改代码并 `npm run build` 后，用 `reload` 刷新。或者开启 `watch` 自动重载。

### `/box3script watch`

开启/关闭文件监听。监听所有项目的 `dist/` 目录，`.js` 文件变化时自动重载对应项目。

```
/box3script watch             # 切换 开/关
```

### `/box3script sandbox <project>`

切换沙盒模式。开启后自动追踪该项目所有的方块/实体/世界状态变更，关闭时回滚并显示摘要。

```
/box3script sandbox mygame    # 切换 开/关
```

追踪内容：方块修改、实体状态、玩家状态、世界设置（天气/时间/规则等）。

典型工作流：

```
/box3script sandbox mygame    # 开启沙盒
/box3script start mygame      # 启用项目
# ... 测试 ...
/box3script reload mygame     # 修改代码后重载（沙盒跟踪保留）
# ... 满意后 ...
/box3script sandbox mygame    # 关闭沙盒 → 回滚全部修改
```

> **注意：** 沙盒仅追踪通过脚本 API 修改的方块（`setVoxel`/`setVoxelId`/`fillVoxel`），手动挖掘不受影响。

## 配置文件

启用/禁用状态保存在 `config/box3/scripts.json`：

```json
{
  "colorzone": true,
  "demo": false
}
```

## 脚本目录结构

```
config/box3/
  ├── scripts.json             ← 项目开关配置
  ├── script/                   ← 脚本目录
  │   └── mygame/
  │       ├── build.mjs
  │       ├── package.json
  │       ├── eslint.config.mjs
  │       ├── tsconfig.json
  │       ├── types/globals.d.ts
  │       ├── src/app.ts
  │       └── dist/app.js       ← 编译产物
  ├── data/                      ← SQLite 数据库 (db API)
  └── storage/                  ← storage API 持久化
```
