package com.box3lab.box3js.script;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

public class Box3ScriptCommand {

    private static final String I = "box3js.command.";

    public static void register(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        dispatcher.register(
                literal("box3script")
                        .requires(src -> src.hasPermission(2))
                        // --- eval ---
                        .then(literal("eval")
                                .then(argument("code", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String code = StringArgumentType.getString(ctx, "code");
                                            var server = ctx.getSource().getServer();
                                            try {
                                                Box3ScriptEngine.get().init(server);
                                                Box3ScriptEngine.get().eval(code);
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.translatable(I + "eval.success"), false);
                                            } catch (Exception e) {
                                                ctx.getSource().sendFailure(
                                                        Component.translatable(I + "error", e.getMessage()));
                                                e.printStackTrace();
                                            }
                                            return 1;
                                        })))
                        // --- file ---
                        .then(literal("file")
                                .then(argument("path", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String input = StringArgumentType.getString(ctx, "path");
                                            CommandSourceStack src = ctx.getSource();
                                            var server = src.getServer();
                                            Path filePath = resolve(input, server);
                                            if (!Files.exists(filePath)) {
                                                src.sendFailure(Component.translatable(I + "file.not_found", filePath));
                                                return 0;
                                            }
                                            try {
                                                Box3ScriptEngine.get().init(server);
                                                // Detect project name for require() support
                                                Path scriptDir = server.getServerDirectory().resolve("config/box3/script");
                                                Path relative = null;
                                                try { relative = scriptDir.relativize(filePath.toAbsolutePath()); } catch (Exception ignored) {}
                                                if (relative != null && relative.getNameCount() > 1) {
                                                    Box3ScriptEngine.get().setCurrentProject(relative.getName(0).toString());
                                                }
                                                Box3ScriptEngine.get().eval(Files.readString(filePath));
                                                src.sendSuccess(
                                                        () -> Component.translatable(I + "file.executed", filePath.getFileName()), false);
                                            } catch (IOException e) {
                                                src.sendFailure(Component.translatable(I + "file.read_error", e.getMessage()));
                                            } catch (Exception e) {
                                                src.sendFailure(Component.translatable(I + "error", e.getMessage()));
                                                e.printStackTrace();
                                            }
                                            return 1;
                                        })))
                        // --- create ---
                        .then(literal("create")
                                .then(argument("name", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            Path projectDir = resolve(name, ctx.getSource().getServer());
                                            if (Files.exists(projectDir)) {
                                                ctx.getSource().sendFailure(
                                                        Component.translatable(I + "create.exists", name));
                                                return 0;
                                            }
                                            try {
                                                Files.createDirectories(projectDir);
                                                String template = "// " + name + " — Box3JS project\n"
                                                        + "world.onTick(() => {\n"
                                                        + "    // 每 tick 执行\n"
                                                        + "});\n"
                                                        + "\n"
                                                        + "console.log('" + name + " loaded');\n";
                                                Files.writeString(projectDir.resolve("app.js"), template);
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.translatable(I + "create.success", name, name),
                                                        false);
                                            } catch (IOException e) {
                                                ctx.getSource().sendFailure(
                                                        Component.translatable(I + "create.error", e.getMessage()));
                                            }
                                            return 1;
                                        })))
                        // --- stop ---
                        .then(literal("stop")
                                .executes(ctx -> {
                                    Box3ScriptEngine.get().reset();
                                    ctx.getSource().sendSuccess(
                                            () -> Component.translatable(I + "stop"),
                                            false);
                                    return 1;
                                }))
                        // --- list ---
                        .then(literal("list")
                                .executes(ctx -> {
                                    var server = ctx.getSource().getServer();
                                    var config = Box3ScriptConfig.get();
                                    config.discover(server);
                                    var projects = config.listProjects();
                                    var lang = Language.getInstance();
                                    if (projects.isEmpty()) {
                                        ctx.getSource().sendSuccess(
                                                () -> Component.translatable(I + "list.empty"),
                                                false);
                                    } else {
                                        String labelOn  = lang.getOrDefault(I + "list.on", "ON");
                                        String labelOff = lang.getOrDefault(I + "list.off", "OFF");
                                        StringBuilder sb = new StringBuilder(
                                                lang.getOrDefault(I + "list.header", "Projects:") + "\n");
                                        projects.forEach((name, enabled) -> {
                                            String status = enabled ? "§a[" + labelOn + "]" : "§c[" + labelOff + "]";
                                            sb.append("  ").append(status).append(" ").append(name).append("\n");
                                        });
                                        String output = sb.toString().trim();
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal(output),
                                                false);
                                    }
                                    return 1;
                                }))
                        // --- on ---
                        .then(literal("on")
                                .then(argument("project", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String project = StringArgumentType.getString(ctx, "project");
                                            Box3ScriptConfig.get().setEnabled(project, true);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.translatable(I + "on.single", project),
                                                    false);
                                            return 1;
                                        }))
                                .then(literal("all")
                                        .executes(ctx -> {
                                            Box3ScriptConfig.get().setAllEnabled(true);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.translatable(I + "on.all"),
                                                    false);
                                            return 1;
                                        })))
                        // --- off ---
                        .then(literal("off")
                                .then(argument("project", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String project = StringArgumentType.getString(ctx, "project");
                                            Box3ScriptConfig.get().setEnabled(project, false);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.translatable(I + "off.single", project),
                                                    false);
                                            return 1;
                                        }))
                                .then(literal("all")
                                        .executes(ctx -> {
                                            Box3ScriptConfig.get().setAllEnabled(false);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.translatable(I + "off.all"),
                                                    false);
                                            return 1;
                                        })))
                        // --- reload ---
                        .then(literal("reload")
                                .executes(ctx -> {
                                    var server = ctx.getSource().getServer();
                                    Box3ScriptEngine.get().reset();
                                    Box3ScriptEngine.get().autoLoad(server);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.translatable(I + "reload"),
                                            false);
                                    return 1;
                                }))
                        // --- run ---
                        .then(literal("run")
                                .then(argument("project", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String project = StringArgumentType.getString(ctx, "project");
                                            CommandSourceStack src = ctx.getSource();
                                            var server = src.getServer();
                                            Path appJs = resolve(project, server).resolve("app.js");
                                            if (!Files.exists(appJs)) {
                                                src.sendFailure(Component.translatable(I + "run.not_found", appJs));
                                                return 0;
                                            }
                                            try {
                                                Box3ScriptEngine.get().init(server);
                                                Box3ScriptEngine.get().setCurrentProject(project);
                                                Box3ScriptEngine.get().eval("require('./app')");
                                                src.sendSuccess(
                                                        () -> Component.translatable(I + "run.executed", project), false);
                                            } catch (Exception e) {
                                                src.sendFailure(Component.translatable(I + "error", e.getMessage()));
                                                e.printStackTrace();
                                            }
                                            return 1;
                                        })))
        );
    }

    private static Path resolve(String input, MinecraftServer server) {
        Path p = Path.of(input);
        if (p.isAbsolute()) return p;
        return Box3ScriptConfig.get().getScriptDir(server).resolve(input).normalize();
    }
}
