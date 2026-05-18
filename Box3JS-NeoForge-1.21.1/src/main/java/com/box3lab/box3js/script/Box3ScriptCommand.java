package com.box3lab.box3js.script;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.box3lab.box3js.standalone.Box3ScriptCompiler;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class Box3ScriptCommand {

    private static Box3ScriptWatcher watcher;

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                literal("box3script")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> showStatus(ctx.getSource()))
                        .then(createCommand())
                        .then(startCommand())
                        .then(stopCommand())
                        .then(reloadCommand())
                        .then(watchCommand())
                        .then(sandboxCommand())
                        .then(compileCommand()));
    }

    // ═══════════════════════════════════════════════════════════
    // /box3script — 无参，显示状态
    // ═══════════════════════════════════════════════════════════

    private static int showStatus(CommandSourceStack source) {
        var config = Box3ScriptConfig.get();
        config.discover(source.getServer());
        var projects = config.listProjects();
        var sandbox = Box3ScriptEngine.get().getSandbox();
        var engine = Box3ScriptEngine.get();
        boolean watcherOn = watcher != null && watcher.isRunning();

        if (projects.isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                    "\n§6  Box3JS Script Engine\n\n"
                            + "  §7No projects found.\n\n"
                            + "  §f/box3script create <name>  §7Create a new project\n"
                            + "  §7Projects live in §fconfig/box3/script/<name>/\n"),
                    false);
            return 1;
        }

        // Count stats
        int enabledCount = 0;
        int loadedCount = 0;
        int sandboxCount = 0;
        for (var entry : projects.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue()))
                enabledCount++;
            if (engine.isProjectLoaded(entry.getKey()))
                loadedCount++;
            if (sandbox.isEnabled(entry.getKey()))
                sandboxCount++;
        }
        int total = projects.size();

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("§6  ══ Box3JS Script Engine ══\n");
        sb.append("\n");

        // Watch + Sandbox status line
        sb.append("  §7Watch: ");
        sb.append(watcherOn ? "§a● Active" : "§8○ Inactive");
        sb.append("    §7Sandbox: ");
        sb.append(sandboxCount > 0 ? "§d● " + sandboxCount + " project(s)" : "§8○ None");
        sb.append("\n\n");

        // Summary
        sb.append("  §7Projects: §f").append(enabledCount).append("§7/").append(total)
                .append(" enabled  §8|  §f").append(loadedCount)
                .append(" §7loaded\n\n");

        // Divider
        sb.append("  §8────────────────────────────\n");

        // Project list
        projects.forEach((name, enabled) -> {
            boolean loaded = engine.isProjectLoaded(name);
            boolean sandboxed = sandbox.isEnabled(name);

            String icon;
            if (loaded) {
                icon = "§a◉"; // ◉ running
            } else if (enabled) {
                icon = "§e○"; // ○ enabled but not loaded
            } else {
                icon = "§8◌"; // ◌ disabled
            }

            sb.append("  ").append(icon).append(" §f").append(name);

            // Badges
            if (sandboxed) {
                sb.append(" §d▐SANDBOX▌");
            }
            if (enabled && !loaded) {
                sb.append(" §7§o(pending)");
            }

            sb.append("\n");
        });

        // Footer
        sb.append("  §8────────────────────────────\n");

        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    // /box3script create <name>
    // ═══════════════════════════════════════════════════════════

    private static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return literal("create")
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            // Validate modId against NeoForge naming rules
                            String validationError = Box3ScriptCompiler.validateModId(name);
                            if (validationError != null) {
                                ctx.getSource().sendFailure(
                                        Component.literal("§cInvalid project name: " + validationError
                                                + "\n§7NeoForge modId: [a-z][a-z0-9_]{1,63} (2-64 chars)"));
                                return 0;
                            }
                            Path projectDir = scriptDir(ctx.getSource().getServer())
                                    .resolve(name).normalize();
                            if (Files.exists(projectDir)) {
                                ctx.getSource().sendFailure(
                                        Component.literal("§cAlready exists: " + name));
                                return 0;
                            }
                            try {
                                Box3ScriptTemplate.copyTo(projectDir, name);
                                Component msg = Component.literal(
                                        "§aProject created: §f" + name + "\n")
                                        .append(clickableCmd("cd config/box3/script/" + name))
                                        .append(Component.literal(
                                                "\n§7  1. cd config/box3/script/" + name
                                                        + "\n  2. npm install && npm run build"
                                                        + "\n  3. /box3script start " + name
                                                        + "  §8(enable)"));
                                ctx.getSource().sendSuccess(() -> msg, false);
                            } catch (IOException e) {
                                ctx.getSource().sendFailure(
                                        Component.literal("§cFailed: " + e.getMessage()));
                            }
                            return 1;
                        }));
    }

    // ═══════════════════════════════════════════════════════════
    // /box3script start [project|all]
    // ═══════════════════════════════════════════════════════════

    private static LiteralArgumentBuilder<CommandSourceStack> startCommand() {
        return literal("start")
                .executes(ctx -> startAll(ctx.getSource()))
                .then(literal("all")
                        .executes(ctx -> startAll(ctx.getSource())))
                .then(argument("project", StringArgumentType.word())
                        .suggests(Box3ScriptCommand::suggestProjects)
                        .executes(ctx -> startOne(ctx.getSource(),
                                StringArgumentType.getString(ctx, "project"))));
    }

    private static int startAll(CommandSourceStack source) {
        Box3ScriptConfig.get().setAllEnabled(true);
        return safeRun(source, "§aAll projects enabled & loaded", () -> {
            Box3ScriptEngine engine = Box3ScriptEngine.get();
            engine.withErrorReporter(chatReporter(source));
            try {
                engine.reset();
                engine.autoLoad(source.getServer());
            } finally {
                engine.clearErrorReporter();
            }
        });
    }

    private static int startOne(CommandSourceStack source, String project) {
        var config = Box3ScriptConfig.get();
        config.discover(source.getServer());
        if (!config.listProjects().containsKey(project)) {
            source.sendFailure(Component.literal("§cUnknown project: " + project));
            return 0;
        }
        config.setEnabled(project, true);
        return safeRun(source, "§a◉ ON   §7" + project, () -> {
            Box3ScriptEngine engine = Box3ScriptEngine.get();
            engine.withErrorReporter(chatReporter(source));
            engine.setCurrentProject(project);
            try {
                engine.eval("require('./server')");
            } finally {
                engine.setCurrentProject(null);
                engine.clearErrorReporter();
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    // /box3script stop [project|all]
    // ═══════════════════════════════════════════════════════════

    private static LiteralArgumentBuilder<CommandSourceStack> stopCommand() {
        return literal("stop")
                .executes(ctx -> stopAll(ctx.getSource()))
                .then(literal("all")
                        .executes(ctx -> stopAll(ctx.getSource())))
                .then(argument("project", StringArgumentType.word())
                        .suggests(Box3ScriptCommand::suggestProjects)
                        .executes(ctx -> stopOne(ctx.getSource(),
                                StringArgumentType.getString(ctx, "project"))));
    }

    private static int stopAll(CommandSourceStack source) {
        Box3ScriptConfig.get().setAllEnabled(false);
        Box3ScriptEngine.get().reset();
        source.sendSuccess(
                () -> Component.literal("§cAll projects disabled & unloaded"), false);
        return 1;
    }

    private static int stopOne(CommandSourceStack source, String project) {
        var config = Box3ScriptConfig.get();
        config.discover(source.getServer());
        if (!config.listProjects().containsKey(project)) {
            source.sendFailure(Component.literal("§cUnknown project: " + project));
            return 0;
        }
        config.setEnabled(project, false);
        Box3ScriptEngine.get().removeProject(project);
        source.sendSuccess(
                () -> Component.literal("§c◉ OFF  §7" + project + "  §8(disabled)"), false);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    // /box3script reload [project]
    // ═══════════════════════════════════════════════════════════

    private static LiteralArgumentBuilder<CommandSourceStack> reloadCommand() {
        return literal("reload")
                .executes(ctx -> safeRun(ctx.getSource(), "§aAll enabled projects reloaded", () -> {
                    Box3ScriptEngine engine = Box3ScriptEngine.get();
                    engine.withErrorReporter(chatReporter(ctx.getSource()));
                    try {
                        engine.reset();
                        engine.autoLoad(ctx.getSource().getServer());
                    } finally {
                        engine.clearErrorReporter();
                    }
                }))
                .then(argument("project", StringArgumentType.word())
                        .suggests(Box3ScriptCommand::suggestProjects)
                        .executes(ctx -> {
                            String project = StringArgumentType.getString(ctx, "project");
                            Box3ScriptConfig.get().setEnabled(project, true);
                            return safeRun(ctx.getSource(), "§aReloaded: §f" + project, () -> {
                                Box3ScriptEngine engine = Box3ScriptEngine.get();
                                engine.withErrorReporter(chatReporter(ctx.getSource()));
                                engine.setCurrentProject(project);
                                try {
                                    engine.removeProject(project);
                                    engine.eval("require('./server')");
                                } finally {
                                    engine.setCurrentProject(null);
                                    engine.clearErrorReporter();
                                }
                            });
                        }));
    }

    // ═══════════════════════════════════════════════════════════
    // /box3script watch — 切换文件监听
    // ═══════════════════════════════════════════════════════════

    private static LiteralArgumentBuilder<CommandSourceStack> watchCommand() {
        return literal("watch")
                .executes(ctx -> {
                    if (watcher == null) {
                        watcher = new Box3ScriptWatcher(ctx.getSource().getServer());
                    }
                    if (watcher.isRunning()) {
                        watcher.stop();
                        ctx.getSource().sendSuccess(
                                () -> Component.literal("§c◉ File watching stopped"), false);
                    } else {
                        watcher.start();
                        ctx.getSource().sendSuccess(
                                () -> Component.literal("§a◉ File watching active — auto-reload on change"), false);
                    }
                    return 1;
                });
    }

    // ═══════════════════════════════════════════════════════════
    // /box3script sandbox <project> — 切换沙盒
    // ═══════════════════════════════════════════════════════════

    private static LiteralArgumentBuilder<CommandSourceStack> sandboxCommand() {
        return literal("sandbox")
                .then(argument("project", StringArgumentType.word())
                        .suggests(Box3ScriptCommand::suggestProjects)
                        .executes(ctx -> {
                            String project = StringArgumentType.getString(ctx, "project");
                            var sb = Box3ScriptEngine.get().getSandbox();
                            if (sb.isEnabled(project)) {
                                var summary = sb.disable(project);
                                String detail = summary.hasAny()
                                        ? " §8— restored: " + summary.toMessage()
                                        : "";
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal(
                                                "§c▐SANDBOX▌ OFF §7" + project + detail),
                                        false);
                            } else {
                                sb.enable(project);
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal(
                                                "§d▐SANDBOX▌ ON  §7" + project
                                                        + " §8— tracking changes for rollback"),
                                        false);
                            }
                            return 1;
                        }));
    }

    // ═══════════════════════════════════════════════════════════
    // /box3script compile <project>     — 编译为独立 JAR
    // /box3script compile runtime       — 构建共享 Rhino 运行时 JAR
    // ═══════════════════════════════════════════════════════════

    private static LiteralArgumentBuilder<CommandSourceStack> compileCommand() {
        return literal("compile")
                .then(argument("project", StringArgumentType.word())
                        .suggests(Box3ScriptCommand::suggestProjects)
                        .executes(ctx -> {
                            String project = StringArgumentType.getString(ctx, "project");
                            var config = Box3ScriptConfig.get();
                            config.discover(ctx.getSource().getServer());
                            if (!config.listProjects().containsKey(project)) {
                                ctx.getSource().sendFailure(
                                        Component.literal("§cUnknown project: " + project));
                                return 0;
                            }

                            Path projectDir = scriptDir(ctx.getSource().getServer())
                                    .resolve(project).normalize();
                            Path serverJs = projectDir.resolve("dist/server.js");
                            if (!Files.exists(serverJs)) {
                                ctx.getSource().sendFailure(
                                        Component.literal("§cdist/server.js not found — run 'npm run build' first"));
                                return 0;
                            }

                            // Read package.json for metadata
                            String[] info = Box3ScriptCompiler.readPackageInfo(projectDir);
                            String modId = info[0];
                            String validationError = Box3ScriptCompiler.validateModId(modId);
                            if (validationError != null) {
                                ctx.getSource().sendFailure(
                                        Component.literal("§cInvalid modId: " + validationError
                                                + "\n§7NeoForge modId: [a-z][a-z0-9_]{1,63} (2-64 chars)"
                                                + "\n§7Fix: rename your project or set '--modId' in package.json"));
                                return 0;
                            }
                            String displayName = info[1];
                            String modVersion = info[2];
                            String description = info[3];
                            String author = info[4];
                            String license = info[5];
                            String homepage = info[6];
                            String bugsUrl = info[7];
                            String logoFile = info[8];

                            Path outputJar = projectDir.resolve(
                                    "dist/" + modId + "-" + modVersion + ".jar");
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal(
                                            "§7Compiling §f" + project
                                                    + " §7→ §f" + modId + "-" + modVersion + ".jar§7..."),
                                    false);

                            // Get the running Box3JS mod version for dependency range
                            String box3jsVersion = net.neoforged.fml.ModList.get()
                                    .getModContainerById(Box3ScriptCompiler.BOX3JS_MOD_ID)
                                    .map(c -> c.getModInfo().getVersion().toString())
                                    .orElse("0");

                            String finalModId = modId;
                            String finalDisplayName = displayName;
                            String finalModVersion = modVersion;
                            String finalDescription = description;
                            String finalAuthor = author;
                            String finalLicense = license;
                            String finalHomepage = homepage;
                            String finalBugsUrl = bugsUrl;
                            String finalLogoFile = logoFile;
                            String finalBox3jsVersion = box3jsVersion;
                            // Run on background thread to avoid blocking server
                            CompletableFuture.runAsync(() -> {
                                try {
                                    new Box3ScriptCompiler(
                                            projectDir, outputJar, finalModId, finalDisplayName,
                                            finalModVersion, finalDescription, finalAuthor, finalLicense,
                                            finalHomepage, finalBugsUrl, finalLogoFile, finalBox3jsVersion)
                                            .compile();
                                    ctx.getSource().getServer().execute(() -> {
                                        String jarPath = outputJar.toAbsolutePath().toString();
                                        Component msg = Component.literal(
                                                "§aCompiled: §f" + jarPath + "\n")
                                                .append(Component.literal(
                                                        "§7Deploy this JAR alongside box3js in mods/."));
                                        ctx.getSource().sendSuccess(() -> msg, false);
                                    });
                                } catch (Exception e) {
                                    String err = e.getMessage();
                                    if (err == null)
                                        err = e.getClass().getSimpleName();
                                    String finalErr = err;
                                    ctx.getSource().getServer().execute(() -> {
                                        ctx.getSource().sendFailure(
                                                Component.literal("§cCompile failed: " + finalErr));
                                    });
                                }
                            });

                            return 1;
                        }));
    }

    // ═══════════════════════════════════════════════════════════
    // helpers
    // ═══════════════════════════════════════════════════════════

    private static Consumer<String> chatReporter(CommandSourceStack source) {
        return msg -> source.sendFailure(Component.literal(msg));
    }

    private static int safeRun(CommandSourceStack source, String successMsg, Runnable action) {
        try {
            action.run();
            source.sendSuccess(() -> Component.literal(successMsg), false);
            return 1;
        } catch (Exception e) {
            String err = e.getMessage();
            if (err == null) {
                err = e.getClass().getSimpleName();
            }
            source.sendFailure(Component.literal("§c" + err));
            Box3ScriptEngine.get().reportError(err);
            return 0;
        }
    }

    private static CompletableFuture<Suggestions> suggestProjects(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        var config = Box3ScriptConfig.get();
        config.discover(ctx.getSource().getServer());
        for (String name : config.listProjects().keySet()) {
            if (builder.getRemaining().isEmpty() || name.startsWith(builder.getRemaining())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }

    private static Component clickableCmd(String text) {
        return Component.literal("  §b§n" + text + "§r")
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.COPY_TO_CLIPBOARD, text))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("点击复制"))));
    }

    private static Path scriptDir(net.minecraft.server.MinecraftServer server) {
        return Box3ScriptConfig.get().getScriptDir(server);
    }
}
