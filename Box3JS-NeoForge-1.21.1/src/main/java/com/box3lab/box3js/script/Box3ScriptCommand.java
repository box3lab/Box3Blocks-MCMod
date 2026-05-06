package com.box3lab.box3js.script;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

public class Box3ScriptCommand {

    private static Box3ScriptWatcher watcher;

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            literal("box3script")
                .requires(src -> src.hasPermission(2))
                .then(createCommand())
                .then(stopCommand())
                .then(listCommand())
                .then(onCommand())
                .then(offCommand())
                .then(reloadCommand())
                .then(watchCommand())
        );
    }

    // ---- error reporting helpers ----

    /** Returns an error reporter that sends messages to the given command source. */
    private static Consumer<String> chatReporter(CommandSourceStack source) {
        return msg -> source.sendFailure(Component.literal(msg));
    }

    /** Execute an engine operation with error feedback to the command source. */
    private static int safeRun(CommandSourceStack source, String successMsg, Runnable action) {
        try {
            action.run();
            source.sendSuccess(() -> Component.literal(successMsg), false);
            return 1;
        } catch (Exception e) {
            String err = e.getMessage();
            if (err == null) err = e.getClass().getSimpleName();
            source.sendFailure(Component.literal(err));
            Box3ScriptEngine.get().reportError(err);
            return 0;
        }
    }

    // --- create ---

    private static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return literal("create")
            .then(argument("name", StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "name");
                    Path projectDir = scriptDir(ctx.getSource().getServer()).resolve(name).normalize();
                    if (Files.exists(projectDir)) {
                        ctx.getSource().sendFailure(Component.literal("Project already exists: " + name));
                        return 0;
                    }
                    try {
                        Box3ScriptTemplate.copyTo(projectDir, name);
                        Component msg = Component.literal("Project created: " + name + "\n")
                            .append(clickablePath(projectDir))
                            .append(Component.literal("  cd config/box3/script/" + name
                                + "\n  npm install && npm run build"
                                + "\nUse /box3script on " + name + " to enable it."));
                        ctx.getSource().sendSuccess(() -> msg, false);
                    } catch (Exception e) {
                        ctx.getSource().sendFailure(Component.literal("Failed to create: " + e.getMessage()));
                    }
                    return 1;
                }));
    }

    // --- stop ---

    private static LiteralArgumentBuilder<CommandSourceStack> stopCommand() {
        return literal("stop")
            .executes(ctx -> safeRun(ctx.getSource(), "All scripts stopped.", () ->
                Box3ScriptEngine.get().reset()
            ))
            .then(argument("project", StringArgumentType.word())
                .executes(ctx -> {
                    String project = StringArgumentType.getString(ctx, "project");
                    Box3ScriptConfig.get().setEnabled(project, false);
                    return safeRun(ctx.getSource(), "Stopped: " + project, () ->
                        Box3ScriptEngine.get().removeProject(project)
                    );
                }));
    }

    // --- list ---

    private static LiteralArgumentBuilder<CommandSourceStack> listCommand() {
        return literal("list")
            .executes(ctx -> {
                var config = Box3ScriptConfig.get();
                config.discover(ctx.getSource().getServer());
                var projects = config.listProjects();
                if (projects.isEmpty()) {
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("No projects found in config/box3/script/"), false);
                } else {
                    StringBuilder sb = new StringBuilder("§6=== Projects ===\n");
                    projects.forEach((name, enabled) -> {
                        String status = enabled ? "§a[ON]" : "§c[OFF]";
                        sb.append("  ").append(status).append("  §f").append(name).append("\n");
                    });
                    ctx.getSource().sendSuccess(
                        () -> Component.literal(sb.toString().trim()), false);
                }
                return 1;
            });
    }

    // --- on ---

    private static LiteralArgumentBuilder<CommandSourceStack> onCommand() {
        return literal("on")
            .then(argument("project", StringArgumentType.word())
                .executes(ctx -> {
                    String project = StringArgumentType.getString(ctx, "project");
                    Box3ScriptConfig.get().setEnabled(project, true);
                    return safeRun(ctx.getSource(), "Enabled and loaded: " + project, () -> {
                        Box3ScriptEngine engine = Box3ScriptEngine.get();
                        engine.withErrorReporter(chatReporter(ctx.getSource()));
                        engine.setCurrentProject(project);
                        try { engine.eval("require('./app')"); }
                        finally { engine.setCurrentProject(null); engine.clearErrorReporter(); }
                    });
                }))
            .then(literal("all")
                .executes(ctx -> {
                    Box3ScriptConfig.get().setAllEnabled(true);
                    return safeRun(ctx.getSource(), "All projects enabled.", () -> {
                        Box3ScriptEngine engine = Box3ScriptEngine.get();
                        engine.reset();
                        engine.autoLoad(ctx.getSource().getServer());
                    });
                }));
    }

    // --- off ---

    private static LiteralArgumentBuilder<CommandSourceStack> offCommand() {
        return literal("off")
            .then(argument("project", StringArgumentType.word())
                .executes(ctx -> {
                    String project = StringArgumentType.getString(ctx, "project");
                    Box3ScriptConfig.get().setEnabled(project, false);
                    Box3ScriptEngine.get().removeProject(project);
                    ctx.getSource().sendSuccess(() -> Component.literal("Disabled: " + project), false);
                    return 1;
                }))
            .then(literal("all")
                .executes(ctx -> {
                    Box3ScriptConfig.get().setAllEnabled(false);
                    ctx.getSource().sendSuccess(() -> Component.literal("All projects disabled."), false);
                    return 1;
                }));
    }

    // --- reload ---

    private static LiteralArgumentBuilder<CommandSourceStack> reloadCommand() {
        return literal("reload")
            .executes(ctx -> safeRun(ctx.getSource(), "Scripts reloaded.", () -> {
                Box3ScriptEngine engine = Box3ScriptEngine.get();
                engine.withErrorReporter(chatReporter(ctx.getSource()));
                try {
                    engine.reset();
                    engine.autoLoad(ctx.getSource().getServer());
                } finally {
                    engine.clearErrorReporter();
                }
            }));
    }

    // --- watch ---

    private static LiteralArgumentBuilder<CommandSourceStack> watchCommand() {
        return literal("watch")
            .executes(ctx -> {
                if (watcher == null) watcher = new Box3ScriptWatcher(ctx.getSource().getServer());
                if (watcher.isRunning()) {
                    watcher.stop();
                    ctx.getSource().sendSuccess(() -> Component.literal("File watching stopped."), false);
                } else {
                    watcher.start();
                    ctx.getSource().sendSuccess(() -> Component.literal("File watching started. Changes will auto-reload."), false);
                }
                return 1;
            })
            .then(literal("on")
                .executes(ctx -> {
                    if (watcher == null) watcher = new Box3ScriptWatcher(ctx.getSource().getServer());
                    if (watcher.isRunning()) {
                        ctx.getSource().sendSuccess(() -> Component.literal("Already watching."), false);
                    } else {
                        watcher.start();
                        ctx.getSource().sendSuccess(() -> Component.literal("File watching started."), false);
                    }
                    return 1;
                }))
            .then(literal("off")
                .executes(ctx -> {
                    if (watcher != null && watcher.isRunning()) {
                        watcher.stop();
                        ctx.getSource().sendSuccess(() -> Component.literal("File watching stopped."), false);
                    } else {
                        ctx.getSource().sendSuccess(() -> Component.literal("Not watching."), false);
                    }
                    return 1;
                }));
    }

    // --- helpers ---

    private static Component clickablePath(Path path) {
        String absPath = path.toAbsolutePath().toString();
        return Component.literal("  §b§n[Copy path]§r\n")
            .withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, absPath))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Component.literal("Click to copy path"))));
    }

    private static Path scriptDir(net.minecraft.server.MinecraftServer server) {
        return Box3ScriptConfig.get().getScriptDir(server);
    }
}
