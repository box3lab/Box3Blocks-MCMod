# audio — Audio API

Client-side audio playback and volume control.

## audio.playSound(path, volume, pitch)

Plays a sound effect (SoundSource.PLAYERS category).

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `path` | string | (required) | Sound ID, e.g. `"minecraft:block.note_block.pling"` |
| `volume` | number | `1.0` | Volume (0–1) |
| `pitch` | number | `1.0` | Pitch (0.5–2) |

```js
audio.playSound("minecraft:block.note_block.pling", 1.0, 1.0);
audio.playSound("minecraft:entity.experience_orb.pickup", 0.5, 1.5);
```

## audio.playMusic(path, volume, pitch)

Plays music (SoundSource.MUSIC category). Same parameters as `playSound`.

```js
audio.playMusic("minecraft:music.creative", 0.5, 1.0);
```

## audio.stopAll()

Stops all currently playing sounds and music.

```js
audio.stopAll();
```

## audio.getVolume(category)

Gets the volume of a specific audio category.

| Parameter | Type | Description |
|-----------|------|-------------|
| `category` | string | Category name, see list below |

```js
var musicVol = audio.getVolume("music"); // 0.0–1.0
```

## audio.setVolume(category, value)

Sets the volume of a specific audio category.

| Parameter | Type | Description |
|-----------|------|-------------|
| `category` | string | Category name |
| `value` | number | Volume (0–1) |

```js
audio.setVolume("music", 0.5);
audio.setVolume("player", 0.8);
```

## Audio Categories

| Category | Description |
|----------|-------------|
| `master` | Master volume |
| `music` | Music |
| `record` | Records/note blocks |
| `weather` | Weather (rain) |
| `block` | Blocks |
| `hostile` | Hostile mobs |
| `neutral` | Neutral mobs |
| `player` | Players |
| `ambient` | Ambient |
| `voice` | Voice |
