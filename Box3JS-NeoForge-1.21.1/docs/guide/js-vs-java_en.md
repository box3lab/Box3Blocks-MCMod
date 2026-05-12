# JS Scripting vs Native Java Mod Development

This guide helps you decide: **when to use Box3JS scripting, and when to write a native Java mod.**

## Overview

| Aspect | Box3JS (JS/TS) | Native Java Mod |
|--------|---------------|-----------------|
| **Barrier to entry** | JavaScript knowledge enough | Requires Java + Gradle + MC modding knowledge |
| **Dev speed** | Edit → build → reload (seconds) | Edit → compile → restart MC (minutes) |
| **Hot reload** | Supported (`/box3script reload`) | Not supported; restart required per change |
| **Publishing** | `/box3script compile` → JAR | `gradlew build` → JAR |
| **Performance** | Medium (Rhino interpreted) | High (JIT-compiled bytecode) |
| **API coverage** | High-level wrappers (100+ methods) | Full Minecraft/NeoForge API |
| **Type safety** | TypeScript declarations | Java static types |
| **Debugging** | console.log + server output | IDE breakpoint debugging |
| **Dependency mgmt** | npm (build-time only) | Gradle/Maven |
| **Client features** | Limited (UI/input/audio/chat) | Full (rendering, models, GUI, net protocol) |
| **Custom blocks/items** | JSON config + compile-time gen | Java classes + registration |
| **Modify vanilla behavior** | No (no Mixin) | Yes (Mixin/ASM/CoreMod) |
| **Team collaboration** | JS source + Git | Java source + Git + Gradle |

---

## Dev Experience Comparison

### Box3JS Advantages

#### 1. Extremely Low Barrier

```js
// Box3JS — 5 lines, instant effect
world.onChat((entity, message) => {
  if (message === "!heal") {
    entity.player.hp = entity.player.maxHp;
    entity.player.directMessage("Healed!");
    return false;
  }
  return true;
});
```

```java
// Native Java mod — 3 files, event registration, chat event handling
@Mod("myhealmod")
public class HealMod {
    public HealMod(IEventBus bus) {
        bus.addListener(ServerChatEvent.class, this::onChat);
    }
    private void onChat(ServerChatEvent event) {
        if (event.getMessage().getString().equals("!heal")) {
            ServerPlayer player = event.getPlayer();
            player.setHealth(player.getMaxHealth());
            player.sendSystemMessage(Component.literal("Healed!"));
            event.setCanceled(true);
        }
    }
}
```

**No Gradle, no modding environment setup, no compile waits.** If you know JS, you can write.

#### 2. Second-Level Hot Reload

This is Box3JS's **single biggest productivity advantage**.

| Action | Box3JS | Java Mod |
|--------|--------|---------|
| Change one line | build(3s) + reload(1s) = **4 seconds** | compile(10-60s) + restartMC(30-120s) = **40-180 seconds** |
| Test a chat command | Edit → build → reload in-game | Edit → compile → restart MC → enter world |
| Iterations per day | **50+** | 5–10 |

For gameplay scripts (mini-games, RPG mechanics, economy systems), hot reload is **irreplaceable** — gameplay needs constant tuning, and you can't afford to wait for restarts.

#### 3. Simplified API Design

Box3JS's high-level APIs hide Minecraft's complexity:

```js
// Box3JS: give a player an item
player.giveItem("minecraft:diamond_sword", 1);

// Java: need ItemStack, Inventory, add
ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
player.getInventory().add(sword);
```

```js
// Box3JS: scoreboard in one line
world.addScoreboard("kills");
world.setScore("Steve", "kills", 5);

// Java: Scoreboard → Objective → Score, three layers deep
```

#### 4. One-Click Project Scaffolding

`/box3script create` generates a complete project with:
- TypeScript config + type declarations
- Build pipeline (Babel + esbuild)
- ESLint code checking
- Server/client dual entry points

Compare: a Java mod requires manually creating a Gradle project, configuring NeoForge MDG, writing mods.toml, registering event buses... beginner guides are typically 50+ pages.

#### 5. Ideal for Rapid Prototyping

Before committing to a full Java mod, prototype gameplay with Box3JS:

```
Idea → 30min Box3JS script → test with friends → tweak → gameplay validated
                                                        ↓
                                        Decide to ship full mod → rewrite in Java
```

---

### Box3JS Disadvantages

#### 1. Performance Overhead

Rhino is an **interpreted** JS engine (no JIT), single-threaded. Performance-sensitive operations (e.g., scanning thousands of entities per tick) can bottleneck.

| Scenario | Box3JS | Java |
|----------|--------|------|
| Chat commands | Imperceptible | Imperceptible |
| 100 entities per tick | Acceptable | Acceptable |
| 10,000 entities per tick | **May lag** | Acceptable |
| Complex pathfinding math | **Noticeably slow** | Fast |
| Fill entire Y=0 chunk region | **Very slow** | Fast |

**Rule of thumb:** If `onTick` takes >1ms, consider optimizing or switching to Java.

#### 2. Incomplete API Coverage

Box3JS wraps 100+ common APIs, but not everything:

| What you want | Box3JS | Java |
|-------------|--------|------|
| Modify recipes | No | Yes `RecipeManager` |
| Custom GUI (chest UI) | No | Yes `MenuProvider` / `Screen` |
| Modify mob AI | Partial (setAI/setTarget) | Yes Brain/Memory system |
| Custom dimensions | No | Yes `DimensionType` |
| Datapacks / loot tables | No | Yes full support |
| Network protocol | High-level (remoteChannel) | Yes low-level `CustomPayload` |
| Modify vanilla classes | No | Yes Mixin / ASM |
| Render custom models | No | Yes full render pipeline |

#### 3. No Breakpoint Debugging

Only `console.log` output for debugging. No IDE breakpoints, variable watches, or stack traces. Complex bug diagnosis is harder.

#### 4. Limited Client Features

Client scripts can do:
- Key input detection
- Screen UI display
- Sound/music playback
- Chat send/receive

But cannot do:
- Custom rendering (models, particles, GUI)
- HUD modification
- Custom shaders
- Full keyboard/mouse interception (only polling and simple callbacks)

#### 5. ES5 Limitations

Rhino 1.9.1 only supports ES5 syntax. You cannot use:
- `let` / `const` (Babel compiles to `var`)
- Arrow functions (Babel compiles to `function`)
- `async` / `await`
- `Promise`
- `class`
- Template literals
- Destructuring

But **Babel compiles everything to ES5**, so you write modern TS and the build converts it automatically.

#### 6. Deployment Requires Box3JS

Compiled JARs depend on Box3JS as a runtime. Users need both Box3JS + your JAR installed. Pure Java mods are self-contained.

---

## Decision Tree

```
What do you want to build?
│
├─ Mini-game (PvP/parkour/racing)
│  └─ → Box3JS  Hot reload for fast iteration
│
├─ Chat commands / economy / claims
│  └─ → Box3JS  Mostly API calls
│
├─ RPG mechanics (skills/dungeons/quests)
│  └─ → Box3JS  Lots of logic, needs frequent tuning
│
├─ Custom events (welcome/death penalty)
│  └─ → Box3JS  Simple event response
│
├─ Server admin tools
│  └─ → Box3JS  Fast development
│
├─ Decorative blocks/items
│  └─ → Box3JS  registries JSON config
│
├─ Heavy computation (pathfinding/many entities)
│  └─ → Java  Performance required
│
├─ Custom GUI / rendering / models
│  └─ → Java  Box3JS doesn't support
│
├─ Modify vanilla mechanics (recipes/loot/mob AI)
│  └─ → Java  Mixin required
│
├─ Full large-scale mod (100+ blocks/mobs/dims)
│  └─ → Java  Box3JS architecture unsuitable
│
└─ Ship as standalone mod to CurseForge/Modrinth
   └─ → Depends on complexity
       Simple gameplay → Box3JS compile JAR
       Lots of content → Native Java mod
```

---

## Hybrid Approach

Best practice: **Box3JS for gameplay, Java for infrastructure**.

```
┌──────────────────────────────────┐
│  Java Mod (low-level capabilities)│
│  - Custom blocks/items registry  │
│  - Custom entities/mobs          │
│  - Mixin to modify vanilla       │
│  - Network protocol extension    │
└──────────┬───────────────────────┘
           │ exposes APIs to
           ▼
┌──────────────────────────────────┐
│  Box3JS Script (gameplay logic) │
│  - Mini-game rules              │
│  - Chat commands                │
│  - Event responses              │
│  - Economy/leveling systems     │
└──────────────────────────────────┘
```

A real-world architecture example:
- Java mod adds custom weapons, custom mobs, a new dimension
- Box3JS scripts define wave rules, boss skill patterns, quest triggers
- Gameplay designers can independently edit scripts without touching Java

---

## Summary

| Choose Box3JS | Choose Java |
|--------------|------------|
| You're building gameplay/mini-games | You need to modify vanilla mechanics |
| You need rapid iteration | You need custom rendering/models |
| Your team has JS developers | Your team is primarily Java developers |
| Logic is complex but no rendering | Project has many custom blocks/entities/dims |
| You want to prototype before committing | You're publishing to CurseForge/Modrinth |
| You need hot reload | You need maximum performance |
| Project is server-side focused | Project needs client-side rendering |

**Neither is better — only better-suited to the current project.** For server-side gameplay development, Box3JS's productivity advantages are overwhelming: hot reload + low barrier + rich API. For projects needing vanilla mechanic modification or custom rendering, Java is essential.
