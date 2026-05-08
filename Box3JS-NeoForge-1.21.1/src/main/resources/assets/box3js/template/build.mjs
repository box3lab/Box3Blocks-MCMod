import * as esbuild from "esbuild";
import { resolve, dirname } from "path";
import { fileURLToPath } from "url";
import { writeFileSync, readFileSync, mkdirSync } from "fs";
import babel from "@babel/core";

// Get current directory path / 获取当前脚本所在的目录路径
const __dirname = dirname(fileURLToPath(import.meta.url));
const entryFile = resolve(__dirname, "src/app.ts");
const distDir = resolve(__dirname, "dist");
const outFile = resolve(distDir, "app.js");

/**
 * Custom Babel plugin: converts tagged and regular template literals into
 * Rhino 1.9.1-compatible code.
 *
 * Tagged: db.sql`SELECT * FROM t WHERE id = ${id}`
 *   → db.sql(["SELECT * FROM t WHERE id = ", ""], id)
 * Regular: `hello ${name}!`
 *   → "hello ".concat(name, "!")
 */
function rhinoTemplatePlugin({ types: t }) {
  return {
    visitor: {
      TaggedTemplateExpression(path) {
        const { tag, quasi } = path.node;
        const strings = quasi.quasis.map((q) =>
          t.stringLiteral(q.value.cooked ?? q.value.raw),
        );
        const args = [t.arrayExpression(strings), ...quasi.expressions];
        path.replaceWith(t.callExpression(tag, args));
      },

      TemplateLiteral(path) {
        const { quasis, expressions } = path.node;
        if (expressions.length === 0) {
          path.replaceWith(t.stringLiteral(quasis[0].value.cooked ?? quasis[0].value.raw));
          return;
        }
        const base = t.stringLiteral(quasis[0].value.cooked ?? quasis[0].value.raw);
        const args = [];
        for (let i = 0; i < expressions.length; i++) {
          args.push(expressions[i]);
          args.push(t.stringLiteral(quasis[i + 1].value.cooked ?? quasis[i + 1].value.raw));
        }
        path.replaceWith(
          t.callExpression(
            t.memberExpression(base, t.identifier("concat")),
            args,
          ),
        );
      },
    },
  };
}

/**
 * Rhino Compatibility Plugin: Invokes Babel during the esbuild process.
 * This avoids the need for manual temporary directory management.
 * Rhino 兼容性插件：在 esbuild 构建过程中调用 Babel。
 * 避免了手动创建和管理临时目录的需求。
 */
const babelRhinoPlugin = {
  name: "babel-rhino",
  setup(build) {
    build.onLoad({ filter: /\.ts$/ }, async (args) => {
      const source = readFileSync(args.path, "utf8");

      // Use Babel for precise downleveling targeted at Rhino
      // 使用 Babel 进行针对 Rhino 环境的精准降级转译
      const result = await babel.transformAsync(source, {
        filename: args.path,
        presets: [
          [
            "@babel/preset-env",
            {
              targets: { rhino: "1.9.1" },
              modules: false,
              loose: true,
              bugfixes: true,
            },
          ],
          "@babel/preset-typescript",
        ],
        plugins: [rhinoTemplatePlugin],
        configFile: false,
        babelrc: false,
        sourceMaps: false,
        compact: false,
      });

      return { contents: result.code, loader: "js" };
    });
  },
};

/**
 * Post-processing function: Fixes regex literals unsupported by Rhino.
 * This is the final defense against specific patterns in Babel's injected helpers.
 * 后处理函数：修复 Rhino 不支持的正则字面量。
 * 这是处理 Babel 注入的辅助函数中特定问题的最后一道防线。
 */
function sanitizeForRhino(code) {
  // Regex pattern for TypedArray checks that Rhino cannot parse
  // Rhino 无法解析的 TypedArray 检查正则模式
  const BAD_REGEX =
    /\/\^\(\?:Ui\|I\)nt\(\?:8\|16\|32\)\(\?:Clamped\)\?Array\$\/\.test\((\w+)\)/g;
  return code.replace(BAD_REGEX, (_, varName) => {
    return `(${varName} === "Int8Array" || ${varName} === "Uint8Array" || ${varName} === "Uint8ClampedArray" || ${varName} === "Int16Array" || ${varName} === "Uint16Array" || ${varName} === "Int32Array" || ${varName} === "Uint32Array")`;
  });
}

// esbuild configuration options / esbuild 构建配置选项
const buildOptions = {
  entryPoints: [entryFile],
  outfile: outFile,
  bundle: true,
  format: "cjs",
  platform: "neutral",
  target: ["rhino1.9.1"],
  plugins: [babelRhinoPlugin],
  logLevel: "info",
};

/**
 * Executes a single build pipeline
 * 执行单次构建流程
 */
async function runBuild() {
  try {
    mkdirSync(distDir, { recursive: true });

    // 1. Run the bundler / 执行打包
    await esbuild.build({
      ...buildOptions,
      metafile: true,
    });

    // 2. Read output and apply Rhino-specific sanitization / 读取产物并进行针对 Rhino 的正则修复
    const code = readFileSync(outFile, "utf8");
    const sanitizedCode = sanitizeForRhino(code);
    writeFileSync(outFile, sanitizedCode, "utf-8");
  } catch (err) {
    console.error("❌ Build failed: / 构建失败：", err);
    process.exit(1);
  }
}

// Main logic: Watch mode or Single build / 主逻辑：监听模式或单次构建
if (process.argv.includes("--watch")) {
  console.log("👀 Watch mode enabled... / 监听模式已启用...");

  const ctx = await esbuild.context({
    ...buildOptions,
    plugins: [
      babelRhinoPlugin,
      {
        name: "post-process-plugin",
        setup(build) {
          // Triggered after every rebuild in watch mode / 在监听模式的每次重建后触发
          build.onEnd(() => {
            const code = readFileSync(outFile, "utf8");
            writeFileSync(outFile, sanitizeForRhino(code), "utf-8");
          });
        },
      },
    ],
  });

  await ctx.watch();
} else {
  runBuild();
}
