# audio — 音频 API

客户端音频播放与音量控制。

## audio.playSound(path, volume, pitch)

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

## audio.playMusic(path, volume, pitch)

播放音乐（SoundSource.MUSIC 类别）。参数同 `playSound`。

```js
audio.playMusic("minecraft:music.creative", 0.5, 1.0);
```

## audio.stopAll()

停止所有正在播放的声音和音乐。

```js
audio.stopAll();
```

## audio.getVolume(category)

获取指定音频类别的音量。

| 参数 | 类型 | 说明 |
|------|------|------|
| `category` | string | 类别名称，见下方列表 |

```js
var musicVol = audio.getVolume("music"); // 0.0–1.0
```

## audio.setVolume(category, value)

设置指定音频类别的音量。

| 参数 | 类型 | 说明 |
|------|------|------|
| `category` | string | 类别名称 |
| `value` | number | 音量值 (0–1) |

```js
audio.setVolume("music", 0.5);
audio.setVolume("player", 0.8);
```

## 音频类别

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
