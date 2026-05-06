import * as esbuild from "esbuild";
import { resolve, dirname, relative } from "path";
import { fileURLToPath } from "url";
import {
  writeFileSync,
  mkdirSync,
  readdirSync,
  statSync,
  rmSync,
  existsSync,
  readFileSync,
  watch as fsWatch,
} from "fs";
import babel from "@babel/core";

// Build script entry paths / 构建脚本入口路径
const __dirname = dirname(fileURLToPath(import.meta.url));
const srcDir = resolve(__dirname, "src");
const tempDir = resolve(__dirname, ".temp");
const distDir = resolve(__dirname, "dist");
const distFile = resolve(distDir, "app.js");

// Watch mode flag: `node build.mjs --watch` / 监听模式开关
const isWatchMode = process.argv.includes("--watch");

// Rhino parser cannot handle regex literal in bundled helper,
// so we replace that specific generated pattern after bundling.
// Rhino 解析器不接受该 helper 正则字面量，因此打包后做定向替换。
const BAD_REGEX =
  /\/\^\(\?:Ui\|I\)nt\(\?:8\|16\|32\)\(\?:Clamped\)\?Array\$\/\.test\((\w+)\)/g;
function typedArrayCheck(_, varName) {
  return `(${varName} === "Int8Array" || ${varName} === "Uint8Array" || ${varName} === "Uint8ClampedArray" || ${varName} === "Int16Array" || ${varName} === "Uint16Array" || ${varName} === "Int32Array" || ${varName} === "Uint32Array")`;
}

// Remove temp directory safely / 安全清理临时目录
function cleanupTempDir() {
  if (existsSync(tempDir)) rmSync(tempDir, { recursive: true, force: true });
}

// Recursively collect all .ts files under src / 递归收集 src 下所有 .ts 文件
function collectTsFiles(dir, out = []) {
  for (const name of readdirSync(dir)) {
    const full = resolve(dir, name);
    const st = statSync(full);
    if (st.isDirectory()) collectTsFiles(full, out);
    else if (name.endsWith(".ts")) out.push(full);
  }
  return out;
}

// Step 1: Babel transpile TS -> temp JS /  第一步：使用 Babel 将 TS 转为临时 JS
async function transpileTsToTemp() {
  cleanupTempDir();
  mkdirSync(tempDir, { recursive: true });
  mkdirSync(distDir, { recursive: true });

  for (const inputPath of collectTsFiles(srcDir)) {
    const rel = relative(srcDir, inputPath);
    const out = resolve(tempDir, rel.replace(/\.ts$/, ".js"));

    const result = await babel.transformFileAsync(inputPath, {
      presets: [
        [
          "@babel/preset-env",
          {
            targets: { rhino: "1.9.1" },
            modules: false,
            bugfixes: true,
            loose: true,
          },
        ],
        "@babel/preset-typescript",
      ],
      configFile: false,
      babelrc: false,
      comments: false,
    });

    if (!result?.code) throw new Error(`Babel transform failed: ${rel}`);
    mkdirSync(dirname(out), { recursive: true });
    writeFileSync(out, result.code, "utf-8");
  }
}

// Step 2: Bundle and sanitize regex literal for Rhino parser / 第二步：打包并修正 Rhino 不支持的 helper 正则字面量
async function bundleAndSanitize() {
  await esbuild.build({
    entryPoints: [resolve(tempDir, "app.js")],
    outfile: distFile,
    bundle: true,
    format: "cjs",
    platform: "neutral",
    target: ["rhino1.9.1"],
    minify: true,
    write: true,
    logLevel: "info",
  });

  const code = readFileSync(distFile, "utf-8");
  const sanitized = code.replace(BAD_REGEX, typedArrayCheck);
  writeFileSync(distFile, sanitized, "utf-8");
}

// Single build pipeline / 单次构建流程
async function buildOnce() {
  await transpileTsToTemp();
  await bundleAndSanitize();
}

if (!isWatchMode) {
  // One-shot build mode / 单次构建模式
  try {
    await buildOnce();
  } finally {
    cleanupTempDir();
  }
} else {
  // Watch mode with debounce + serial rebuild / 监听模式
  let timer = null;
  let building = false;
  let pending = false;
  let closing = false;

  // Graceful shutdown: clean temp then exit / 先清理临时目录再退出
  const closeWatch = () => {
    if (closing) return;
    closing = true;
    cleanupTempDir();
    process.exit(0);
  };

  process.on("SIGINT", closeWatch);
  process.on("SIGTERM", closeWatch);

  const runBuild = async () => {
    if (building) {
      pending = true;
      return;
    }

    building = true;
    try {
      await buildOnce();
    } catch (err) {
      console.error("❌ Build failed:", err);
    } finally {
      building = false;
      if (pending) {
        pending = false;
        void runBuild();
      }
    }
  };

  await runBuild();
  console.log("👀 Watching src/**/*.ts for changes...");

  fsWatch(srcDir, { recursive: true }, (_eventType, filename) => {
    if (!filename || !filename.endsWith(".ts")) return;
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      console.log(`♻️ Rebuilding: ${filename}`);
      void runBuild();
    }, 120);
  });

  await new Promise(() => {});
}
