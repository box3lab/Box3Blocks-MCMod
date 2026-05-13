// @ts-check
import tseslint from "typescript-eslint";

/**
 * ESLint Rules：https://eslint.org/docs/latest/rules
 * 0 = off, 1 = warn, 2 = error
 * @type {Partial<import('eslint/rules').ESLintRules>}
 */
const baseRules = {
  "no-useless-concat": 2,
  "prefer-template": 1,
  "no-cond-assign": 1,
  "no-const-assign": 2,
  "no-dupe-keys": 2,
  "no-dupe-args": 1,
  "no-eval": 1,
  "no-floating-decimal": 1,
  "no-func-assign": 2,
  "no-nested-ternary": 1,
  "no-unneeded-ternary": 1,
  "no-use-before-define": [2, { functions: false }],
  "no-redeclare": 2,
  "no-var": 2,
  curly: [2, "all"],
  eqeqeq: 2,
  semi: [1, "always"],
  "no-void": [2, { allowAsStatement: true }],
  "no-multiple-empty-lines": [1, { max: 6 }],
  "no-console": 0,
  "no-dupe-class-members": 0,
  "no-param-reassign": 0,
  "max-classes-per-file": 0,
  "class-methods-use-this": 0,
  "no-await-in-loop": 0,
  "prefer-destructuring": [
    2,
    { array: false, object: true },
    { enforceForRenamedProperties: false },
  ],

  "no-prototype-builtins": 2,
  "no-restricted-syntax": [
    2,
    {
      selector: "ForInStatement",
      message:
        "for..in loops iterate over the entire prototype chain, which is virtually never what you want. Use Object.{keys,values,entries}, and iterate over the resulting array.",
    },
  ],
};

/**
 * TypeScript Rules：https://typescript-eslint.io/rules
 * 0 = off, 1 = warn, 2 = error
 * @type {Record<string, any>}
 */
const typescriptRules = {
  "@typescript-eslint/no-require-imports": 0,
  "@typescript-eslint/no-unused-vars": [1, { argsIgnorePattern: "^_" }],
  "@typescript-eslint/consistent-type-imports": 2,
  "@typescript-eslint/ban-ts-comment": 0,
  "@typescript-eslint/naming-convention": 0,
  "@typescript-eslint/no-throw-literal": 0,
  "@typescript-eslint/no-explicit-any": 2,
  "@typescript-eslint/no-non-null-assertion": 2,
  "@typescript-eslint/explicit-function-return-type": 2,
  "@typescript-eslint/no-unused-expressions": 2,
  "@typescript-eslint/switch-exhaustiveness-check": 2,
  "@typescript-eslint/restrict-template-expressions": [
    2,
    { allowNumber: true },
  ],
  "@typescript-eslint/prefer-optional-chain": 1,
  "prefer-const": 1,
};

/** @type {import('eslint').Linter.Config[]} */
export default [
  {
    ignores: [
      "**/node_modules/**",
      "**/dist/**",
      "**/types/**",
      "**/tsconfig.json",
      "**/*.d.ts",
    ],
  },
  ...tseslint.configs.recommended,
  ...tseslint.configs.strictTypeChecked.map((c) => ({
    ...c,
    files: ["**/*.ts"],
  })),
  {
    files: ["**/*.ts"],
    languageOptions: {
      parser: tseslint.parser,
      parserOptions: {
        project: ['./tsconfig.server.json', './tsconfig.client.json'],
        tsconfigRootDir: import.meta.dirname,
      },
    },
    rules: {
      ...baseRules,
      ...typescriptRules,
    },
  },
  {
    files: ["**/*.{js,mjs,cjs}"],
    rules: {
      ...baseRules,
    },
  },
];
