package com.box3lab.box3js.script;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

public class Box3ScriptCommand {

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
                                            Path filePath = resolve(input, server);
                                            if (!Files.exists(filePath)) {
                                                src.sendFailure(Component.literal("File not found: " + filePath));
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
                                                        () -> Component.literal("Executed: " + filePath.getFileName()), false);
                                            } catch (IOException e) {
                                                src.sendFailure(Component.literal("Failed to read file: " + e.getMessage()));
                                            } catch (Exception e) {
                                                src.sendFailure(Component.literal("Script error: " + e.getMessage()));
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
                                                        Component.literal("Project already exists: " + name));
                                                return 0;
                                            }
                                            try {
                                                copyTemplate(projectDir, name);
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.literal("Project created: " + name
                                                                + "\n  cd config/box3/script/" + name
                                                                + "\n  npm install && npm run build"
                                                                + "\nUse /box3script on " + name + " to enable it."),
                                                        false);
                                            } catch (IOException e) {
                                                ctx.getSource().sendFailure(
                                                        Component.literal("Failed to create: " + e.getMessage()));
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
                                        StringBuilder sb = new StringBuilder("§6=== Projects ===\n");
                                        projects.forEach((name, enabled) -> {
                                            String status = enabled ? "§a[ON]" : "§c[OFF]";
                                            sb.append("  ").append(status).append("  §f").append(name).append("\n");
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
                                                    () -> Component.literal("Enabled: " + project),
                                                    false);
                                            return 1;
                                        }))
                                .then(literal("all")
                                        .executes(ctx -> {
                                            Box3ScriptConfig.get().setAllEnabled(true);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("All projects enabled."),
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
                                        }))
                                .then(literal("all")
                                        .executes(ctx -> {
                                            Box3ScriptConfig.get().setAllEnabled(false);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("All projects disabled."),
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
                                            Path appJs = resolve(project, server).resolve("app.js");
                                            if (!Files.exists(appJs)) {
                                                src.sendFailure(Component.literal("app.js not found: " + appJs));
                                                return 0;
                                            }
                                            try {
                                                Box3ScriptEngine.get().init(server);
                                                Box3ScriptEngine.get().setCurrentProject(project);
                                                Box3ScriptEngine.get().eval("require('./app')");
                                                src.sendSuccess(
                                                        () -> Component.literal("Executed: " + project + "/app.js"), false);
                                            } catch (Exception e) {
                                                src.sendFailure(Component.literal("Script error: " + e.getMessage()));
                                                e.printStackTrace();
                                            }
                                            return 1;
                                        })))
        );
    }

    private static final String[] TEMPLATE_FILES = {
            "gitignore.template",
            "package.json",
            "tsconfig.json",
            "build.mjs",
            "src/app.ts",
            "types/globals.d.ts",
    };

    /**
     * Copies the TypeScript project template from classpath to the target directory.
     */
    private static void copyTemplate(Path projectDir, String projectName) throws IOException {
        Files.createDirectories(projectDir);
        for (String relPath : TEMPLATE_FILES) {
            // gitignore.template → .gitignore
            String destName = relPath.equals("gitignore.template") ? ".gitignore" : relPath;
            Path dest = projectDir.resolve(destName);
            Files.createDirectories(dest.getParent());
            String resourcePath = "/assets/box3js/template/" + relPath;
            try (InputStream in = Box3ScriptCommand.class.getResourceAsStream(resourcePath)) {
                if (in == null) {
                    throw new IOException("Template file not found: " + resourcePath);
                }
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            // Replace placeholders in app.ts
            if (relPath.equals("src/app.ts")) {
                String content = Files.readString(dest);
                content = content.replace("PROJECT_NAME", projectName);
                Files.writeString(dest, content);
            }
        }
    }

    private static Path resolve(String input, MinecraftServer server) {
        Path p = Path.of(input);
        if (p.isAbsolute()) return p;
        return Box3ScriptConfig.get().getScriptDir(server).resolve(input).normalize();
    }
}
