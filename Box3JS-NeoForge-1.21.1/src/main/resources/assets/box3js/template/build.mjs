import * as esbuild from "esbuild";
import { resolve, dirname } from "path";
import { fileURLToPath } from "url";
import { writeFileSync, mkdirSync } from "fs";
import babel from "@babel/core";

const __dirname = dirname(fileURLToPath(import.meta.url));
const srcDir = resolve(__dirname, "src");
const distDir = resolve(__dirname, "dist");

const result = await esbuild.build({
  entryPoints: [resolve(srcDir, "app.ts")],
  outfile: resolve(distDir, "app.js"),
  bundle: true,
  format: "cjs",
  target: "rhino1.9.1",
  platform: "neutral",
  minify: false,
  write: false,
  supported: {
    class: true,
  },
});

for (const out of result.outputFiles) {
  let code = out.text;
  const transformed = babel.transformSync(code, {
    presets: [["@babel/preset-env", { targets: { ie: "11" }, modules: false }]],
    configFile: false,
  });
  code = transformed.code;
  mkdirSync(dirname(out.path), { recursive: true });
  writeFileSync(out.path, code, "utf-8");
}
