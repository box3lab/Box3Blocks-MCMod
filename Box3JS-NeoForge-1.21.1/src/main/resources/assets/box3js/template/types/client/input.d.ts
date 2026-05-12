/// <reference path="../shared.d.ts" />

// ── @zh 按键名称类型 @en KeyName type ──

/**
 * @zh 按键名称。仅允许以下值。
 * @en Key name. Only these values are accepted.
 */
type KeyName =
  | "space" | "enter" | "escape" | "tab" | "backspace" | "delete"
  | "left_shift" | "right_shift" | "left_ctrl" | "right_ctrl"
  | "left_alt" | "right_alt"
  | "up" | "down" | "left" | "right"
  | "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j"
  | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t"
  | "u" | "v" | "w" | "x" | "y" | "z"
  | "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
  | "f1" | "f2" | "f3" | "f4" | "f5" | "f6"
  | "f7" | "f8" | "f9" | "f10" | "f11" | "f12";

// ── §4 @zh 键盘输入 @en Keyboard input ──

/** @zh 通过 `input` 访问：键盘输入检测 @en Accessed via `input`: keyboard input detection */
interface GameInput {
  /**
   * @zh 检查指定按键当前是否被按下。
   * @en Checks whether a key is currently held down.
   * @param key - @zh 按键名称 @en key name
   * @returns @zh true 如果按键正在被按住 @en true if the key is held down
   */
  isKeyDown(key: KeyName): boolean;

  /**
   * @zh 注册按键按下回调（按下瞬间触发一次）。
   * @en Registers a callback fired once when the key is pressed.
   * @param key - @zh 按键名称 @en key name
   * @param callback - @zh 回调函数 @en callback function (no arguments)
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onKeyPress(key: KeyName, callback: () => void): GameEventHandlerToken;
}
