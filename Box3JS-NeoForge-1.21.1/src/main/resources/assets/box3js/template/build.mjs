import * as esbuild from "esbuild";
import { resolve, dirname } from "path";
import { fileURLToPath } from "url";
import { writeFileSync, readFileSync, mkdirSync } from "fs";
import babel from "@babel/core";

// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
const __dirname = dirname(fileURLToPath(import.meta.url));
const entryFile = resolve(__dirname, "src/app.ts");
const distDir = resolve(__dirname, "dist");
const outFile = resolve(distDir, "app.js");

// ═══════════════════════════════════════════════════════════════
//  Babel plugins — Rhino 1.9.1 compatibility transforms
// ═══════════════════════════════════════════════════════════════

/**
 * Template literal → .concat() calls.
 *
 * `hello ${name}!`  →  "hello ".concat(name, "!")
 * db.sql`...`       →  db.sql(["a", "b"], expr1, expr2)
 *
 * Babel's default transform emits Object.defineProperties / Object.freeze
 * helpers that crash in Rhino 1.9.1.
 */
function rhinoTemplatePlugin({ types: t }) {
  return {
    visitor: {
      TaggedTemplateExpression(path) {
        const { tag, quasi } = path.node;
        const strings = quasi.quasis.map((q) =>
          t.stringLiteral(q.value.cooked ?? q.value.raw),
        );
        path.replaceWith(
          t.callExpression(tag, [
            t.arrayExpression(strings),
            ...quasi.expressions,
          ]),
        );
      },

      TemplateLiteral(path) {
        const { quasis, expressions } = path.node;
        if (expressions.length === 0) {
          path.replaceWith(
            t.stringLiteral(quasis[0].value.cooked ?? quasis[0].value.raw),
          );
          return;
        }
        const base = t.stringLiteral(
          quasis[0].value.cooked ?? quasis[0].value.raw,
        );
        const args = [];
        for (let i = 0; i < expressions.length; i++) {
          args.push(expressions[i]);
          args.push(
            t.stringLiteral(
              quasis[i + 1].value.cooked ?? quasis[i + 1].value.raw,
            ),
          );
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
 * for...of → indexed for loop with Java ArrayList detection.
 *
 * for (const x of arr) { ... }
 *   ↓
 * var _coll = arr;
 * if (_coll.toArray) _coll = _coll.toArray();
 * for (var _i = 0; _i < _coll.length; _i++) {
 *   var x = _coll[_i];
 *   ...
 * }
 *
 * Babel's _createForEachIteratorHelperLoose calls .call() on the iterable,
 * which crashes on Java ArrayList. Java lists expose .toArray() → Object[]
 * (wrapped as NativeJavaArray), which supports .length and [i].
 */
function rhinoForOfPlugin({ types: t }) {
  return {
    visitor: {
      ForOfStatement(path) {
        const { left, right, body } = path.node;

        const collId = path.scope.generateUidIdentifier("coll");
        const iId = path.scope.generateUidIdentifier("i");

        // var _coll = <right>
        const collDecl = t.variableDeclaration("var", [
          t.variableDeclarator(collId, right),
        ]);

        // if (_coll.toArray) _coll = _coll.toArray();
        const toArrayCheck = t.memberExpression(
          collId,
          t.identifier("toArray"),
        );
        const toArrayCall = t.assignmentExpression(
          "=",
          collId,
          t.callExpression(toArrayCheck, []),
        );
        const ifToArray = t.ifStatement(
          toArrayCheck,
          t.expressionStatement(toArrayCall),
        );

        // _coll[_i]
        const elementAccess = t.memberExpression(collId, iId, true);

        // var x = _coll[_i];  (or assignment for non-declaration left)
        let elementAssign;
        if (t.isVariableDeclaration(left)) {
          const decl = left.declarations[0];
          elementAssign = t.variableDeclaration("var", [
            t.variableDeclarator(decl.id, elementAccess),
          ]);
        } else if (t.isIdentifier(left) || t.isMemberExpression(left)) {
          elementAssign = t.expressionStatement(
            t.assignmentExpression("=", left, elementAccess),
          );
        } else {
          elementAssign = t.variableDeclaration("var", [
            t.variableDeclarator(t.identifier("_v"), elementAccess),
          ]);
        }

        const newBody = t.isBlockStatement(body)
          ? t.blockStatement([elementAssign, ...body.body])
          : t.blockStatement([elementAssign, t.expressionStatement(body)]);

        // for (var _i = 0; _i < _coll.length; _i++) { ... }
        const forLoop = t.forStatement(
          t.variableDeclaration("var", [
            t.variableDeclarator(iId, t.numericLiteral(0)),
          ]),
          t.binaryExpression(
            "<",
            iId,
            t.memberExpression(collId, t.identifier("length")),
          ),
          t.updateExpression("++", iId, false),
          newBody,
        );

        path.replaceWithMultiple([collDecl, ifToArray, forLoop]);
      },
    },
  };
}

/**
 * ES5 array methods → indexed for loops via IIFE.
 *
 * arr.map(fn)     → (function(){var _a=arr;var _r=[];for(var _i=0;_i<_a.length;_i++)_r.push(fn.call(null,_a[_i],_i,_a));return _r})()
 * arr.filter(fn)  → (function(){var _a=arr;var _r=[];for(var _i=0;_i<_a.length;_i++){if(fn.call(null,_a[_i],_i,_a))_r.push(_a[_i])}return _r})()
 * arr.forEach(fn) → (function(){var _a=arr;for(var _i=0;_i<_a.length;_i++)fn.call(null,_a[_i],_i,_a)})()
 * arr.find(fn)    → (function(){var _a=arr;for(var _i=0;_i<_a.length;_i++){if(fn.call(null,_a[_i],_i,_a))return _a[_i]}return undefined})()
 * arr.some(fn)    → (function(){var _a=arr;for(var _i=0;_i<_a.length;_i++){if(fn.call(null,_a[_i],_i,_a))return true}return false})()
 * arr.every(fn)   → (function(){var _a=arr;for(var _i=0;_i<_a.length;_i++){if(!fn.call(null,_a[_i],_i,_a))return false}return true})()
 *
 * Rhino's NativeArray (from Java interop, e.g. db.sql().rows) lacks ES5
 * Array.prototype methods. The IIFE form preserves chaining (arr.map(f).filter(g)).
 */
function rhinoArrayMethodsPlugin({ types: t }) {
  const METHODS = ["map", "filter", "forEach", "find", "some", "every"];

  function buildIIFE(bodyNodes, returnExpr) {
    const fnBody = returnExpr
      ? t.blockStatement([...bodyNodes, t.returnStatement(returnExpr)])
      : t.blockStatement(bodyNodes);
    return t.callExpression(t.functionExpression(null, [], fnBody), []);
  }

  return {
    visitor: {
      CallExpression(path) {
        const { callee } = path.node;
        if (!t.isMemberExpression(callee)) {
          return;
        }
        if (!t.isIdentifier(callee.property)) {
          return;
        }
        const method = callee.property.name;
        if (!METHODS.includes(method)) {
          return;
        }

        const obj = callee.object;
        const args = path.node.arguments;
        const callback = args[0];
        if (!callback) {
          return;
        }
        const thisArg = args[1] || t.nullLiteral();

        const arrId = path.scope.generateUidIdentifier("arr");
        const iId = path.scope.generateUidIdentifier("i");
        const rId = path.scope.generateUidIdentifier("r");

        const arrDecl = t.variableDeclaration("var", [
          t.variableDeclarator(arrId, obj),
        ]);

        const element = t.memberExpression(arrId, iId, true);

        // callback.call(thisArg, element, i, arr)
        const cbCall = t.callExpression(
          t.memberExpression(callback, t.identifier("call")),
          [thisArg, element, iId, arrId],
        );

        const forInit = t.variableDeclaration("var", [
          t.variableDeclarator(iId, t.numericLiteral(0)),
        ]);
        const forTest = t.binaryExpression(
          "<",
          iId,
          t.memberExpression(arrId, t.identifier("length")),
        );
        const forUpdate = t.updateExpression("++", iId, false);

        let bodyNodes, returnExpr;

        switch (method) {
          case "map": {
            const rDecl = t.variableDeclaration("var", [
              t.variableDeclarator(rId, t.arrayExpression([])),
            ]);
            const pushCall = t.callExpression(
              t.memberExpression(rId, t.identifier("push")),
              [cbCall],
            );
            bodyNodes = [
              arrDecl,
              rDecl,
              t.forStatement(
                forInit,
                forTest,
                forUpdate,
                t.blockStatement([t.expressionStatement(pushCall)]),
              ),
            ];
            returnExpr = rId;
            break;
          }
          case "filter": {
            const rDecl = t.variableDeclaration("var", [
              t.variableDeclarator(rId, t.arrayExpression([])),
            ]);
            const pushCall = t.callExpression(
              t.memberExpression(rId, t.identifier("push")),
              [element],
            );
            bodyNodes = [
              arrDecl,
              rDecl,
              t.forStatement(
                forInit,
                forTest,
                forUpdate,
                t.blockStatement([
                  t.ifStatement(cbCall, t.expressionStatement(pushCall)),
                ]),
              ),
            ];
            returnExpr = rId;
            break;
          }
          case "forEach": {
            bodyNodes = [
              arrDecl,
              t.forStatement(
                forInit,
                forTest,
                forUpdate,
                t.blockStatement([t.expressionStatement(cbCall)]),
              ),
            ];
            returnExpr = null;
            break;
          }
          case "find": {
            bodyNodes = [
              arrDecl,
              t.forStatement(
                forInit,
                forTest,
                forUpdate,
                t.blockStatement([
                  t.ifStatement(cbCall, t.returnStatement(element)),
                ]),
              ),
            ];
            returnExpr = t.identifier("undefined");
            break;
          }
          case "some": {
            bodyNodes = [
              arrDecl,
              t.forStatement(
                forInit,
                forTest,
                forUpdate,
                t.blockStatement([
                  t.ifStatement(
                    cbCall,
                    t.returnStatement(t.booleanLiteral(true)),
                  ),
                ]),
              ),
            ];
            returnExpr = t.booleanLiteral(false);
            break;
          }
          case "every": {
            bodyNodes = [
              arrDecl,
              t.forStatement(
                forInit,
                forTest,
                forUpdate,
                t.blockStatement([
                  t.ifStatement(
                    t.unaryExpression("!", cbCall),
                    t.returnStatement(t.booleanLiteral(false)),
                  ),
                ]),
              ),
            ];
            returnExpr = t.booleanLiteral(true);
            break;
          }
          default:
            return;
        }

        path.replaceWith(buildIIFE(bodyNodes, returnExpr));
      },
    },
  };
}

// ═══════════════════════════════════════════════════════════════
//  esbuild plugin
// ═══════════════════════════════════════════════════════════════

/**
 * Routes .ts files through Babel for Rhino downleveling.
 * esbuild handles bundling; Babel handles precise syntax transforms.
 */
const babelRhinoPlugin = {
  name: "babel-rhino",
  setup(build) {
    build.onLoad({ filter: /\.ts$/ }, async (args) => {
      const source = readFileSync(args.path, "utf8");
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
        plugins: [
          rhinoArrayMethodsPlugin,
          rhinoForOfPlugin,
          rhinoTemplatePlugin,
        ],
        configFile: false,
        babelrc: false,
        sourceMaps: false,
        compact: false,
      });

      return { contents: result.code, loader: "js" };
    });
  },
};

// ═══════════════════════════════════════════════════════════════
//  Post-processing
// ═══════════════════════════════════════════════════════════════

/**
 * Fixes regex literals in Babel's injected helpers that Rhino cannot parse.
 * Specific target: TypedArray detection via /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/
 */
function sanitizeForRhino(code) {
  const BAD_REGEX =
    /\/\^\(\?:Ui\|I\)nt\(\?:8\|16\|32\)\(\?:Clamped\)\?Array\$\/\.test\((\w+)\)/g;
  return code.replace(BAD_REGEX, (_, varName) => {
    return `(${varName} === "Int8Array" || ${varName} === "Uint8Array" || ${varName} === "Uint8ClampedArray" || ${varName} === "Int16Array" || ${varName} === "Uint16Array" || ${varName} === "Int32Array" || ${varName} === "Uint32Array")`;
  });
}

// ═══════════════════════════════════════════════════════════════
//  Build
// ═══════════════════════════════════════════════════════════════

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

async function runBuild() {
  try {
    mkdirSync(distDir, { recursive: true });

    await esbuild.build({ ...buildOptions, metafile: true });

    const code = readFileSync(outFile, "utf8");
    writeFileSync(outFile, sanitizeForRhino(code), "utf-8");
  } catch (err) {
    console.error("Build failed:", err);
    process.exit(1);
  }
}

// ── Entry ──

if (process.argv.includes("--watch")) {
  console.log("Watch mode enabled...");

  const ctx = await esbuild.context({
    ...buildOptions,
    plugins: [
      babelRhinoPlugin,
      {
        name: "post-process-plugin",
        setup(build) {
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
