# /box3script 命令参考

所有命令需要 **OP 权限等级 2**（默认管理员权限）。

---

## 命令列表

### `/box3script eval <code>`

直接执行一段 JS 代码。

```
/box3script eval world.say("hello")
/box3script eval var p = world.querySelectorAll("*")[0].player; p.teleport(new GameVector3(0,100,0))
```

### `/box3script file <path>`

加载并执行服务器上的 JS 文件。支持相对路径（相对于 `config/box3/script/`）和绝对路径。

```
/box3script file my_script.js
/box3script file /home/server/scripts/test.js
```

### `/box3script run <project>`

运行一次指定项目的 `app.js`（不改变启用状态）。

```
/box3script run skyrun
```

### `/box3script create <name>`

创建新的脚本项目。在 `config/box3/script/<name>/` 下创建目录和 `app.js` 模板文件。创建后默认**禁用**。

```
/box3script create mygame
```

生成的文件结构：
```
config/box3/script/
  └── mygame/
      └── app.js     ← 模板脚本
```

### `/box3script list`

列出所有已发现的脚本项目及其启用/禁用状态。

```
/box3script list
```

输出示例：
```
项目列表:
  [开] skyrun
  [关] siege
  [关] mygame
```

### `/box3script on <project>`

启用指定项目。下次服务端重启时自动执行该项目的 `app.js`。

```
/box3script on skyrun
```

### `/box3script on all`

一键启用所有项目。

```
/box3script on all
```

### `/box3script off <project>`

禁用指定项目。下次服务端重启时不再自动执行。

```
/box3script off siege
```

### `/box3script off all`

一键禁用所有项目。

```
/box3script off all
```

### `/box3script reload`

停止所有脚本，重新加载所有已启用项目的 `app.js`。等价于 `stop` + 重新 `autoLoad`。

```
/box3script reload
```

### `/box3script stop`

立即停止所有正在运行的脚本。清除所有回调、定时器和作用域。

```
/box3script stop
```

---

## 配置文件

启用/禁用状态保存在 `config/box3/scripts.json`：

```json
{
  "skyrun": true,
  "siege": false,
  "mygame": true
}
```

---

## 脚本目录结构

```
config/box3/
  ├── scripts.json        ← 项目开关配置
  ├── script/              ← 脚本目录
  │   ├── skyrun/
  │   │   └── app.js       ← 天空跑酷
  │   ├── siege/
  │   │   └── app.js       ← 围攻游戏
  │   └── mygame/
  │       └── app.js       ← 自定义项目
  └── data/                ← 存储数据目录 (storage API)
      ├── skyrun_times/
      └── ...
```
