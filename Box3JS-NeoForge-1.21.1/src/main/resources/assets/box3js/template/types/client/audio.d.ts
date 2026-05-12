/// <reference path="../shared.d.ts" />

// ── @zh 音频类别名称 @en Audio category name ──

/**
 * @zh 音频类别名称。
 * @en Audio category name.
 */
type AudioCategory =
  | "master" | "music" | "record" | "weather" | "block"
  | "hostile" | "neutral" | "player" | "ambient" | "voice";

// ── §2 @zh 音频播放 @en Audio playback ──

/** @zh 通过 `audio` 访问：音效、音乐、音量控制 @en Accessed via `audio`: sound, music, volume control */
interface GameAudio {
  /**
   * @zh 播放音效（SoundSource.PLAYERS 类别）。
   * @en Plays a sound effect (SoundSource.PLAYERS category).
   * @param path - @zh 声音 ID（如 "minecraft:block.note_block.pling"） @en sound ID (e.g. "minecraft:block.note_block.pling")
   * @param volume - @zh 音量（0‑1，可选，默认 1） @en volume (0–1, optional, default 1)
   * @param pitch - @zh 音高（0.5‑2，可选，默认 1） @en pitch (0.5–2, optional, default 1)
   */
  playSound(path: string, volume?: number, pitch?: number): void;

  /**
   * @zh 播放音乐（SoundSource.MUSIC 类别）。
   * @en Plays music (SoundSource.MUSIC category).
   * @param path - @zh 声音 ID @en sound ID
   * @param volume - @zh 音量（0‑1，可选，默认 1） @en volume (0–1, optional, default 1)
   * @param pitch - @zh 音高（0.5‑2，可选，默认 1） @en pitch (0.5–2, optional, default 1)
   */
  playMusic(path: string, volume?: number, pitch?: number): void;

  /** @zh 停止所有正在播放的声音和音乐。 @en Stops all currently playing sounds and music. */
  stopAll(): void;

  /**
   * @zh 获取指定音频类别的音量。
   * @en Gets the volume of a specific audio category.
   * @param category - @zh 类别名称 @en category name
   * @returns @zh 音量值（0‑1） @en volume value (0–1)
   */
  getVolume(category: AudioCategory): number;

  /**
   * @zh 设置指定音频类别的音量。
   * @en Sets the volume of a specific audio category.
   * @param category - @zh 类别名称 @en category name
   * @param value - @zh 音量（0‑1） @en volume (0–1)
   */
  setVolume(category: AudioCategory, value: number): void;
}
