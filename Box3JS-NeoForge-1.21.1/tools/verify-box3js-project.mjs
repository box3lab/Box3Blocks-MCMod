import { readFileSync, readdirSync, statSync } from "node:fs";
import { join, relative } from "node:path";

const root = new URL("..", import.meta.url).pathname.replace(/\/$/, "");
const failures = [];

function read(path) {
  return readFileSync(join(root, path), "utf8");
}

function readJson(path) {
  return JSON.parse(read(path));
}

function fail(message) {
  failures.push(message);
}

const apiManifest = readJson("tools/box3js-api-manifest.json");

function listFiles(dir) {
  const base = join(root, dir);
  const out = [];
  function walk(abs) {
    for (const name of readdirSync(abs)) {
      const file = join(abs, name);
      if (statSync(file).isDirectory()) {
        walk(file);
      } else {
        out.push(relative(base, file).replaceAll("\\", "/"));
      }
    }
  }
  walk(base);
  return out.sort();
}

function verifyTemplateRecursive() {
  const source = read("src/main/java/com/box3lab/box3js/script/Box3ScriptTemplate.java");
  if (!source.includes("JarFile") || !source.includes("Files.walk")) {
    fail("Box3ScriptTemplate must use recursive enumeration (JarFile + Files.walk) to copy all template files");
  }
}

function verifyRuntimeTypeSplit() {
  const serverIndex = read("src/main/resources/assets/box3js/template/types/server/index.d.ts");
  const clientIndex = read("src/main/resources/assets/box3js/template/types/client/index.d.ts");
  const serverConfig = read("src/main/resources/assets/box3js/template/tsconfig.server.json");
  const clientConfig = read("src/main/resources/assets/box3js/template/tsconfig.client.json");

  if (serverIndex.includes("../client") || serverIndex.includes("./client")) {
    fail("Server DTS index references client types");
  }
  if (clientIndex.includes("../server") || clientIndex.includes("./server")) {
    fail("Client DTS index references server types");
  }
  if (serverConfig.includes("src/client") || serverConfig.includes("types/client")) {
    fail("tsconfig.server.json includes client sources or types");
  }
  if (clientConfig.includes("src/server") || clientConfig.includes("types/server")) {
    fail("tsconfig.client.json includes server sources or types");
  }
}

function verifyEventTokens() {
  const javaFiles = [
    "src/main/java/com/box3lab/box3js/script/Box3JSWorld.java",
    "src/main/java/com/box3lab/box3js/script/Box3JSPlayer.java",
    "src/main/java/com/box3lab/box3js/script/Box3JSRemoteChannel.java",
    "src/main/java/com/box3lab/box3js/client/Box3JSGuiProxy.java",
  ];

  for (const file of javaFiles) {
    const source = read(file);
    for (const match of source.matchAll(/public\s+([A-Za-z0-9_<>, ?]+)\s+(on[A-Z]\w*)\s*\(/g)) {
      const returnType = match[1].trim();
      const method = match[2];
      if (returnType !== "GameEventHandlerToken") {
        fail(`${file}: ${method} returns ${returnType}, expected GameEventHandlerToken`);
      }
    }
  }

  for (const file of listFiles("src/main/resources/assets/box3js/template/types")) {
    if (!file.endsWith(".d.ts")) continue;
    const path = `src/main/resources/assets/box3js/template/types/${file}`;
    const source = read(path);
    for (const match of source.matchAll(/^\s*(on[A-Z]\w*)\s*\([\s\S]*?\):\s*([^;\n]+);/gm)) {
      const method = match[1];
      const returnType = match[2].trim();
      if (returnType !== "GameEventHandlerToken") {
        fail(`${path}: ${method} returns ${returnType}, expected GameEventHandlerToken`);
      }
    }
  }
}

function findMatchingBrace(source, openIndex) {
  let depth = 0;
  for (let i = openIndex; i < source.length; i++) {
    const ch = source[i];
    if (ch === "{") {
      depth++;
    } else if (ch === "}") {
      depth--;
      if (depth === 0) {
        return i;
      }
    }
  }
  return -1;
}

function stripBlockAndLineComments(source) {
  return source
    .replace(/\/\*[\s\S]*?\*\//g, "")
    .replace(/\/\/.*$/gm, "");
}

function stripCommentsPreserveLength(source) {
  return source.replace(/\/\*[\s\S]*?\*\//g, (comment) => " ".repeat(comment.length))
    .replace(/\/\/.*$/gm, (comment) => " ".repeat(comment.length));
}

function extractDtsInterfaceMembers(paths, interfaceName) {
  const files = Array.isArray(paths) ? paths : [paths];
  const members = new Set();
  for (const path of files) {
    const source = stripBlockAndLineComments(read(path));
    const interfaceRe = new RegExp(`(?:interface|declare\\s+class|class)\\s+${interfaceName}\\b[^\\{]*\\{`, "g");
    let match;
    while ((match = interfaceRe.exec(source)) !== null) {
      const open = source.indexOf("{", match.index);
      const close = findMatchingBrace(source, open);
      if (close === -1) {
        fail(`${path}: interface ${interfaceName} has no closing brace`);
        continue;
      }
      const body = source.slice(open + 1, close);
      let parenDepth = 0;
      let braceDepth = 0;
      let bracketDepth = 0;
      for (const line of body.split(/\r?\n/)) {
        if (parenDepth === 0 && braceDepth === 0 && bracketDepth === 0) {
          const member = line.trim().match(/^(?:static\s+)?(?:readonly\s+)?([A-Za-z_$]\w*)\??\s*(?:<[^;{(]*>)?\s*[:(]/);
          if (member) {
            if (member[1] !== "constructor") {
              members.add(member[1]);
            }
          }
        }
        for (const ch of line) {
          if (ch === "(") parenDepth++;
          if (ch === ")") parenDepth = Math.max(0, parenDepth - 1);
          if (ch === "{") braceDepth++;
          if (ch === "}") braceDepth = Math.max(0, braceDepth - 1);
          if (ch === "[") bracketDepth++;
          if (ch === "]") bracketDepth = Math.max(0, bracketDepth - 1);
        }
      }
    }
  }
  return members;
}

function updateDepths(line, state) {
  let inString = null;
  let escaped = false;
  for (const ch of line) {
    if (inString) {
      if (escaped) {
        escaped = false;
      } else if (ch === "\\") {
        escaped = true;
      } else if (ch === inString) {
        inString = null;
      }
      continue;
    }
    if (ch === "\"" || ch === "'" || ch === "`") {
      inString = ch;
    } else if (ch === "(") {
      state.parenDepth++;
    } else if (ch === ")") {
      state.parenDepth = Math.max(0, state.parenDepth - 1);
    } else if (ch === "{") {
      state.braceDepth++;
    } else if (ch === "}") {
      state.braceDepth = Math.max(0, state.braceDepth - 1);
    } else if (ch === "[") {
      state.bracketDepth++;
    } else if (ch === "]") {
      state.bracketDepth = Math.max(0, state.bracketDepth - 1);
    }
  }
}

function extractDtsInterfaceMemberDetails(paths, interfaceName) {
  const files = Array.isArray(paths) ? paths : [paths];
  const members = new Map();
  for (const path of files) {
    const source = read(path);
    const searchable = stripCommentsPreserveLength(source);
    const interfaceRe = new RegExp(`(?:interface|declare\\s+class|class)\\s+${interfaceName}\\b[^\\{]*\\{`, "g");
    let match;
    while ((match = interfaceRe.exec(searchable)) !== null) {
      const open = searchable.indexOf("{", match.index);
      const close = findMatchingBrace(searchable, open);
      if (close === -1) {
        fail(`${path}: interface ${interfaceName} has no closing brace`);
        continue;
      }
      const body = source.slice(open + 1, close);
      const state = { parenDepth: 0, braceDepth: 0, bracketDepth: 0 };
      let pendingComment = "";
      let commentBuffer = "";
      let inDocComment = false;

      for (const line of body.split(/\r?\n/)) {
        const trimmed = line.trim();
        if (trimmed.startsWith("/**")) {
          inDocComment = true;
          commentBuffer = `${line}\n`;
          if (trimmed.includes("*/")) {
            inDocComment = false;
            pendingComment = commentBuffer;
            commentBuffer = "";
          }
          continue;
        }
        if (inDocComment) {
          commentBuffer += `${line}\n`;
          if (trimmed.includes("*/")) {
            inDocComment = false;
            pendingComment = commentBuffer;
            commentBuffer = "";
          }
          continue;
        }

        if (state.parenDepth === 0 && state.braceDepth === 0 && state.bracketDepth === 0) {
          const lineWithoutComment = line.replace(/\/\/.*$/, "");
          const member = lineWithoutComment.trim().match(/^(?:static\s+)?(?:readonly\s+)?([A-Za-z_$]\w*)\??\s*(?:<[^;{(]*>)?\s*[:(]/);
          if (member) {
            const name = member[1];
            if (name === "constructor") {
              pendingComment = "";
              continue;
            }
            const documented = pendingComment.includes("@zh") && pendingComment.includes("@en");
            const current = members.get(name) ?? { documented: false, locations: [] };
            current.documented ||= documented;
            current.locations.push(path);
            members.set(name, current);
            pendingComment = "";
          } else if (trimmed !== "" && !trimmed.startsWith("//")) {
            pendingComment = "";
          }
        }

        updateDepths(stripBlockAndLineComments(line), state);
      }
    }
  }
  return members;
}

function extractClassBody(source, className) {
  const classRe = new RegExp(`\\bclass\\s+${className}\\b[^\\{]*\\{`);
  const match = source.match(classRe);
  if (!match) {
    return null;
  }
  const open = source.indexOf("{", match.index);
  const close = findMatchingBrace(source, open);
  return close === -1 ? null : source.slice(open + 1, close);
}

function javaPropertyName(method) {
  if (/^(get|set)[A-Z]/.test(method)) {
    return method.slice(3, 4).toLowerCase() + method.slice(4);
  }
  if (/^is[A-Z]/.test(method)) {
    return method.slice(2, 3).toLowerCase() + method.slice(3);
  }
  return method;
}

function extractJavaApiMembers(path, options = {}) {
  const source = read(path);
  const ignore = new Set(options.ignore ?? []);
  const accessorProperties = new Set(options.accessorProperties ?? []);
  const keepMethodNames = new Set(options.keepMethodNames ?? []);
  const members = new Set();
  const defaultClassName = path.split("/").pop().replace(/\.java$/, "");
  const javaBody = options.nestedClass
    ? extractClassBody(source, options.nestedClass)
    : extractClassBody(source, options.className ?? defaultClassName);

  if (!javaBody) {
    fail(`${path}: Java API body was not found`);
    return members;
  }

  const fieldRe = /^\s*public\s+(?:static\s+)?(?:final\s+)?[A-Za-z0-9_.<>\[\]?]+\s+([A-Za-z_$]\w*(?:\s*,\s*[A-Za-z_$]\w*)*)\s*(?:=|;)/gm;
  let field;
  while ((field = fieldRe.exec(javaBody)) !== null) {
    const before = javaBody.slice(0, field.index);
    const depth = (before.match(/\{/g) ?? []).length - (before.match(/\}/g) ?? []).length;
    if (depth === 0) {
      for (const name of field[1].split(",").map((part) => part.trim())) {
        if (!ignore.has(name)) {
          members.add(name);
        }
      }
    }
  }

  const methodRe = /^\s*public\s+(?:static\s+)?(?!class\b|interface\b)([A-Za-z0-9_.<>\[\], ?]+)\s+([A-Za-z_$]\w*)\s*\(/gm;
  let match;
  while ((match = methodRe.exec(javaBody)) !== null) {
    const before = javaBody.slice(0, match.index);
    const depth = (before.match(/\{/g) ?? []).length - (before.match(/\}/g) ?? []).length;
    if (depth !== 0) {
      continue;
    }
    const method = match[2];
    if (ignore.has(method)) {
      continue;
    }
    const property = javaPropertyName(method);
    if (accessorProperties.has(property)) {
      members.add(property);
      if (keepMethodNames.has(method)) {
        members.add(method);
      }
    } else {
      members.add(method);
    }
  }
  return members;
}

function extractJavaScriptableMembers(path, objectVariable) {
  const source = read(path);
  const members = new Set();
  const escapedVariable = objectVariable.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  const propertyRe = new RegExp(
    `ScriptableObject\\.putProperty\\(\\s*${escapedVariable}\\s*,\\s*"([^"]+)"`,
    "g",
  );
  let match;
  while ((match = propertyRe.exec(source)) !== null) {
    members.add(match[1]);
  }
  return members;
}

function hasDtsConst(paths, name, type) {
  const files = Array.isArray(paths) ? paths : [paths];
  // GlobalFunction entries use `declare function` instead of `declare const`
  if (type === "GlobalFunction") {
    const funcRe = new RegExp(`declare\\s+function\\s+${name}\\s*\\(`);
    return files.some((path) => funcRe.test(read(path)));
  }
  const escapedType = type.replace(/[.*+?^${}()|[\]\\]/g, "\\$&").replace(/\s+/g, "\\s*");
  const declarationRe = new RegExp(`declare\\s+const\\s+${name}\\s*:\\s*${escapedType}\\s*;`);
  return files.some((path) => declarationRe.test(read(path)));
}

function hasRuntimeScopeBinding(path, name) {
  const source = read(path);
  const bindingRe = new RegExp(`ScriptableObject\\.putProperty\\(\\s*scope\\s*,\\s*"${name}"`);
  return bindingRe.test(source);
}

function verifyGlobalDeclarations() {
  for (const global of apiManifest.globals) {
    if (!hasDtsConst(global.dts, global.name, global.type)) {
      fail(`${global.side}: DTS missing global declaration 'declare const ${global.name}: ${global.type};'`);
    }
    if (global.runtimeContains && !read(global.runtime).includes(global.runtimeContains)) {
      fail(`${global.side}: runtime does not initialize global '${global.name}' via ${global.runtimeContains}`);
    } else if (!global.runtimeContains && !hasRuntimeScopeBinding(global.runtime, global.name)) {
      fail(`${global.side}: runtime does not bind global '${global.name}'`);
    }
  }
}

function extractDtsPublicTypeNames() {
  const names = new Set();
  for (const file of listFiles("src/main/resources/assets/box3js/template/types")) {
    if (!file.endsWith(".d.ts")) continue;
    const source = read(`src/main/resources/assets/box3js/template/types/${file}`);
    for (const match of source.matchAll(/^(?:declare\s+class|interface)\s+([A-Za-z_$]\w*)/gm)) {
      names.add(match[1]);
    }
  }
  return names;
}

function extractDtsGlobalNames() {
  const names = new Set();
  for (const file of listFiles("src/main/resources/assets/box3js/template/types")) {
    if (!file.endsWith(".d.ts")) continue;
    const source = read(`src/main/resources/assets/box3js/template/types/${file}`);
    for (const match of source.matchAll(/^declare\s+const\s+([A-Za-z_$]\w*)\s*:/gm)) {
      names.add(match[1]);
    }
  }
  return names;
}

function verifyManifestCoverage() {
  const coveredTypes = new Set(apiManifest.apis.map((api) => api.iface));
  const ignoredTypes = new Set(apiManifest.ignoredDtsTypes ?? []);
  for (const typeName of extractDtsPublicTypeNames()) {
    if (!coveredTypes.has(typeName) && !ignoredTypes.has(typeName)) {
      fail(`manifest: public DTS type '${typeName}' is not covered by tools/box3js-api-manifest.json`);
    }
  }

  const coveredGlobals = new Set(apiManifest.globals.map((global) => global.name));
  const ignoredGlobals = new Set(apiManifest.ignoredGlobals ?? []);
  for (const globalName of extractDtsGlobalNames()) {
    if (!coveredGlobals.has(globalName) && !ignoredGlobals.has(globalName)) {
      fail(`manifest: global DTS const '${globalName}' is not covered by tools/box3js-api-manifest.json`);
    }
  }
}

function verifyJavaDtsApiParity() {
  for (const mapping of apiManifest.apis) {
    const javaMembers = mapping.expectedMembers
      ? new Set(mapping.expectedMembers)
      : mapping.javaObject
      ? extractJavaScriptableMembers(mapping.javaObject.path, mapping.javaObject.variable)
      : extractJavaApiMembers(mapping.java, {
        ignore: mapping.ignoreJava,
        nestedClass: mapping.nestedClass,
        className: mapping.className,
        accessorProperties: mapping.accessorProperties,
        keepMethodNames: mapping.keepMethodNames,
      });
    const dtsMembers = extractDtsInterfaceMembers(mapping.dts, mapping.iface);

    for (const member of javaMembers) {
      if (!dtsMembers.has(member)) {
        fail(`${mapping.name}: Java exposes '${member}' but ${mapping.iface} DTS does not declare it`);
      }
    }

    for (const member of dtsMembers) {
      if (!javaMembers.has(member)) {
        fail(`${mapping.name}: ${mapping.iface} DTS declares '${member}' but Java does not expose it`);
      }
    }
  }
}

function docsContainApiMember(docs, prefix, member) {
  const token = `${prefix}.${member}`;
  const headingRe = new RegExp(`^#{2,4}\\s+\`?${token.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")}\\b`, "m");
  return docs.some((path) => {
    const source = read(path);
    return source.includes(token) || headingRe.test(source);
  });
}

function verifyDtsDocumentation() {
  for (const mapping of apiManifest.apis) {
    const details = extractDtsInterfaceMemberDetails(mapping.dts, mapping.iface);
    for (const [member, info] of details.entries()) {
      if (!info.documented) {
        fail(`${mapping.name}: ${mapping.iface}.${member} DTS member must include @zh and @en documentation`);
      }
    }
  }
}

function verifyDocsApiSync() {
  for (const mapping of apiManifest.apis) {
    if (!mapping.docs || !mapping.prefix) {
      continue;
    }
    const members = extractDtsInterfaceMembers(mapping.dts, mapping.iface);
    for (const member of members) {
      if (!docsContainApiMember(mapping.docs, mapping.prefix, member)) {
        fail(`${mapping.name}: docs/api missing ${mapping.prefix}.${member} from ${mapping.docs.join(", ")}`);
      }
    }
  }
}

function verifyEntrypoints() {
  const build = read("src/main/resources/assets/box3js/template/build.mjs");
  const engine = read("src/main/java/com/box3lab/box3js/script/Box3ScriptEngine.java");
  if (!build.includes("src/server/app.ts") || !build.includes("dist/server.js")) {
    fail("build.mjs must build src/server/app.ts to dist/server.js");
  }
  if (!build.includes("src/client/app.ts") || !build.includes("dist/client.js")) {
    fail("build.mjs must build src/client/app.ts to dist/client.js");
  }
  if (engine.includes("app.js")) {
    fail("Box3ScriptEngine still references legacy app.js");
  }
}

verifyTemplateRecursive();
verifyRuntimeTypeSplit();
verifyEventTokens();
verifyGlobalDeclarations();
verifyJavaDtsApiParity();
verifyDtsDocumentation();
verifyDocsApiSync();
verifyEntrypoints();

if (failures.length > 0) {
  console.error("Box3JS project verification failed:");
  for (const failure of failures) {
    console.error(`- ${failure}`);
  }
  process.exit(1);
}

console.log("Box3JS project verification passed.");
