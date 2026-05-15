import { defineConfig } from 'vitepress'

const cnNav = [
  { text: '首页', link: '/' },
  { text: '指南', link: '/guide/README' },
  { text: '教程', link: '/tutorial/README' },
  { text: 'API', link: '/api/README' },
]

const enNav = [
  { text: 'Home', link: '/en/' },
  { text: 'Guide', link: '/en/guide/README' },
  { text: 'Tutorials', link: '/en/tutorial/README' },
  { text: 'API', link: '/en/api/README' },
]

const cnSidebar = [
  {
    text: '快速开始',
    collapsed: false,
    items: [
      { text: '文档索引', link: '/overview' },
      { text: '总览', link: '/guide/README' },
      { text: '快速开始', link: '/guide/getting-started' },
      { text: '常用配方', link: '/guide/recipes' },
      { text: '常见问题', link: '/guide/faq' },
      { text: '运行原理', link: '/guide/architecture' },
      { text: 'JS vs Java', link: '/guide/js-vs-java' },
      { text: '关于 Box3JS', link: '/guide/about-box3js' },
    ],
  },
  {
    text: '教程',
    collapsed: false,
    items: [
      { text: '总览', link: '/tutorial/README' },
      { text: '01 — 基础入门', link: '/tutorial/01-basics' },
      { text: '02 — 玩家与物品', link: '/tutorial/02-player-items' },
      { text: '03 — 事件与实体', link: '/tutorial/03-events-entities' },
      { text: '04 — 进阶系统', link: '/tutorial/04-advanced-systems' },
      { text: '05 — 实战案例', link: '/tutorial/05-examples' },
      { text: '06 — 客户端脚本', link: '/tutorial/06-client-scripting' },
    ],
  },
  {
    text: 'API 参考',
    collapsed: false,
    items: [
      { text: '总览', link: '/api/README' },
      {
        text: '服务端 API',
        collapsed: false,
        items: [
          { text: 'Server', link: '/api/server' },
          { text: 'World', link: '/api/world' },
          { text: 'Entity', link: '/api/entity' },
          { text: 'Player', link: '/api/player' },
          { text: 'Voxels', link: '/api/voxels' },
          { text: 'Registries', link: '/api/registries' },
        ],
      },
      {
        text: '客户端 API',
        collapsed: false,
        items: [
          { text: 'Client', link: '/api/client' },
        ],
      },
      {
        text: '共用 API',
        collapsed: false,
        items: [
          { text: 'Storage', link: '/api/storage' },
          { text: 'Database', link: '/api/database' },
          { text: 'HTTP', link: '/api/http' },
          { text: 'Math', link: '/api/math' },
        ],
      },
      {
        text: '命令参考',
        collapsed: false,
        items: [
          { text: '/box3script', link: '/api/commands' },
        ],
      },
    ],
  },
]

const enSidebar = [
  {
    text: 'Get Started',
    collapsed: false,
    items: [
      { text: 'Overview', link: '/en/guide/README' },
      { text: 'Getting Started', link: '/en/guide/getting-started' },
      { text: 'Recipes', link: '/en/guide/recipes' },
      { text: 'FAQ', link: '/en/guide/faq' },
      { text: 'Architecture', link: '/en/guide/architecture' },
      { text: 'JS vs Java', link: '/en/guide/js-vs-java' },
      { text: 'About Box3JS', link: '/en/guide/about-box3js' },
    ],
  },
  {
    text: 'Tutorials',
    collapsed: false,
    items: [
      { text: 'Overview', link: '/en/tutorial/README' },
      { text: '01 — Basics', link: '/en/tutorial/01-basics' },
      { text: '02 — Player & Items', link: '/en/tutorial/02-player-items' },
      { text: '03 — Events & Entities', link: '/en/tutorial/03-events-entities' },
      { text: '04 — Advanced Systems', link: '/en/tutorial/04-advanced-systems' },
      { text: '05 — Examples', link: '/en/tutorial/05-examples' },
      { text: '06 — Client Scripting', link: '/en/tutorial/06-client-scripting' },
    ],
  },
  {
    text: 'API Reference',
    collapsed: false,
    items: [
      { text: 'Overview', link: '/en/api/README' },
      {
        text: 'Server-side API',
        collapsed: false,
        items: [
          { text: 'Server', link: '/en/api/server' },
          { text: 'World', link: '/en/api/world' },
          { text: 'Entity', link: '/en/api/entity' },
          { text: 'Player', link: '/en/api/player' },
          { text: 'Voxels', link: '/en/api/voxels' },
          { text: 'Registries', link: '/en/api/registries' },
        ],
      },
      {
        text: 'Client-side API',
        collapsed: false,
        items: [
          { text: 'Client', link: '/en/api/client' },
        ],
      },
      {
        text: 'Shared API',
        collapsed: false,
        items: [
          { text: 'Storage', link: '/en/api/storage' },
          { text: 'Database', link: '/en/api/database' },
          { text: 'HTTP', link: '/en/api/http' },
          { text: 'Math', link: '/en/api/math' },
        ],
      },
      {
        text: 'CLI Reference',
        collapsed: false,
        items: [
          { text: '/box3script', link: '/en/api/commands' },
        ],
      },
    ],
  },
]

export default defineConfig({
  title: 'Box3JS',
  description: 'Minecraft NeoForge 1.21.1 的 JavaScript/TypeScript 脚本引擎',
  lastUpdated: true,
  cleanUrls: true,
  ignoreDeadLinks: true,

  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }],
  ],

  locales: {
    root: {
      label: '简体中文',
      lang: 'zh-CN',
      title: 'Box3JS',
      description: '基于 Mozilla Rhino 引擎，为 Minecraft NeoForge 1.21.1 提供 JS/TS 双端脚本能力 — 神奇代码岛同款编程体验',
      themeConfig: {
        nav: cnNav,
        sidebar: cnSidebar,
        editLink: {
          pattern: 'https://github.com/box3lab/Box3JS/edit/main/Box3JS-NeoForge-1.21.1/docs/:path',
        },
        lastUpdated: {
          text: '最后更新',
        },
        docFooter: {
          prev: '上一页',
          next: '下一页',
        },
        footer: {
          message: '基于 MIT 许可证发布',
        },
      },
    },
    en: {
      label: 'English',
      lang: 'en-US',
      title: 'Box3JS',
      description: 'JavaScript/TypeScript dual-side scripting engine for Minecraft NeoForge 1.21.1 — same programming experience as Box3',
      themeConfig: {
        nav: enNav,
        sidebar: enSidebar,
        editLink: {
          pattern: 'https://github.com/box3lab/Box3JS/edit/main/Box3JS-NeoForge-1.21.1/docs/en/:path',
        },
        lastUpdated: {
          text: 'Last updated',
        },
        docFooter: {
          prev: 'Previous page',
          next: 'Next page',
        },
        footer: {
          message: 'Released under the MIT License.',
        },
      },
    },
  },

  themeConfig: {
    logo: false,

    socialLinks: [
      { icon: 'github', link: 'https://github.com/box3lab/Box3JS' },
    ],

    search: {
      provider: 'local',
    },
  },
})
