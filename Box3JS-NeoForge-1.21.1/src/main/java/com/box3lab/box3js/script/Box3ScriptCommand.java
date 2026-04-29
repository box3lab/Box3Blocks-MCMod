package com.box3lab.box3js.script;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

public class Box3ScriptCommand {

    private static Path scriptDir;

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
                                                        () -> Component.literal("Script executed."), false);
                                            } catch (Exception e) {
                                                ctx.getSource().sendFailure(
                                                        Component.literal("Script error: " + e.getMessage()));
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
                                            Path filePath = resolve(input);
                                            if (!Files.exists(filePath)) {
                                                src.sendFailure(Component.literal("File not found: " + filePath));
                                                return 0;
                                            }
                                            try {
                                                Box3ScriptEngine.get().init(server);
                                                Box3ScriptEngine.get().eval(Files.readString(filePath));
                                                src.sendSuccess(
                                                        () -> Component.literal("Executed: " + filePath.getFileName()), false);
                                            } catch (IOException e) {
                                                src.sendFailure(Component.literal("Failed to read file: " + e.getMessage()));
                                            } catch (Exception e) {
                                                src.sendFailure(Component.literal("Script error: " + e.getMessage()));
                                                e.printStackTrace();
                                            }
                                            return 1;
                                        })))
                        // --- stop ---
                        .then(literal("stop")
                                .executes(ctx -> {
                                    Box3ScriptEngine.get().reset();
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("All scripts stopped. Callbacks cleared, scope reset."),
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
                                    if (projects.isEmpty()) {
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("No projects found in config/box3/script/"),
                                                false);
                                    } else {
                                        StringBuilder sb = new StringBuilder("Projects:\n");
                                        projects.forEach((name, enabled) -> {
                                            sb.append("  ").append(enabled ? "§a[ON]" : "§c[OFF]")
                                              .append(" ").append(name).append("\n");
                                        });
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal(sb.toString().trim()),
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
                                                    () -> Component.literal("Enabled: " + project),
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
                                                    () -> Component.literal("Disabled: " + project),
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
                                            () -> Component.literal("Scripts reloaded."),
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
                                            Path appJs = resolve(project).resolve("app.js");
                                            if (!Files.exists(appJs)) {
                                                src.sendFailure(Component.literal("app.js not found: " + appJs));
                                                return 0;
                                            }
                                            try {
                                                Box3ScriptEngine.get().init(server);
                                                Box3ScriptEngine.get().eval(Files.readString(appJs));
                                                src.sendSuccess(
                                                        () -> Component.literal("Executed: " + project + "/app.js"), false);
                                            } catch (IOException e) {
                                                src.sendFailure(Component.literal("Failed to read: " + e.getMessage()));
                                            } catch (Exception e) {
                                                src.sendFailure(Component.literal("Script error: " + e.getMessage()));
                                                e.printStackTrace();
                                            }
                                            return 1;
                                        })))
        );
    }

    private static Path resolve(String input) {
        Path p = Path.of(input);
        if (p.isAbsolute()) return p;
        if (scriptDir == null) {
            scriptDir = Path.of("config", "box3", "script").toAbsolutePath();
        }
        return scriptDir.resolve(input).normalize();
    }
}
