package com.box3lab.box3js.script;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
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
                                                String absPath = projectDir.toAbsolutePath().toString();
                                                Component msg = Component.literal("Project created: " + name + "\n")
                                                        .append(Component.literal("  §b§n[Copy path]§r\n")
                                                                .withStyle(style -> style
                                                                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, absPath))
                                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy path")))))
                                                        .append(Component.literal("  cd config/box3/script/" + name
                                                                + "\n  npm install && npm run build"
                                                                + "\nUse /box3script on " + name + " to enable it."));
                                                ctx.getSource().sendSuccess(() -> msg, false);
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
                                            () -> Component.literal("All scripts stopped."),
                                            false);
                                    return 1;
                                })
                                .then(argument("project", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String project = StringArgumentType.getString(ctx, "project");
                                            Box3ScriptConfig.get().setEnabled(project, false);
                                            var server = ctx.getSource().getServer();
                                            Box3ScriptEngine.get().reset();
                                            Box3ScriptEngine.get().autoLoad(server);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Stopped and disabled: " + project),
                                                    false);
                                            return 1;
                                        })))
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
