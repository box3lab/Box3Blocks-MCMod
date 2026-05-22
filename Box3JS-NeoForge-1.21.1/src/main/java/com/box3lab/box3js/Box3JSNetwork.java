package com.box3lab.box3js;

import com.box3lab.box3js.script.Box3ScriptConfig;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Box3JSNetwork {

    private Box3JSNetwork() {}

    /** Standalone JARs register their (projectName → clientScriptSource) here so the
     *  main mod can send them — avoids the standalone mod sending a box3js-namespaced payload. */
    private static final Map<String, String> STANDALONE_CLIENT_SCRIPTS = new ConcurrentHashMap<>();

    public static void registerStandaloneClientScript(String projectName, String source) {
        STANDALONE_CLIENT_SCRIPTS.put(projectName, source);
        Box3JS.LOGGER.debug("Registered standalone client script: {}", projectName);
    }

    // ── Payloads ──

    public record ClientScriptPayload(String projectName, String scriptSource)
            implements CustomPacketPayload {

        public static final Type<ClientScriptPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Box3JS.MODID, "client_script"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientScriptPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, ClientScriptPayload::projectName,
                        ByteBufCodecs.STRING_UTF8, ClientScriptPayload::scriptSource,
                        ClientScriptPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Server → client: a remote event targeting the client's project handlers. */
    public record ServerEventPayload(String projectName, long tick, String eventJson)
            implements CustomPacketPayload {

        public static final Type<ServerEventPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Box3JS.MODID, "server_event"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ServerEventPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, ServerEventPayload::projectName,
                        ByteBufCodecs.VAR_LONG,      ServerEventPayload::tick,
                        ByteBufCodecs.STRING_UTF8,  ServerEventPayload::eventJson,
                        ServerEventPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Client → server: a remote event targeting the server project's handlers. */
    public record ClientEventPayload(String projectName, String eventJson)
            implements CustomPacketPayload {

        public static final Type<ClientEventPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Box3JS.MODID, "client_event"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientEventPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, ClientEventPayload::projectName,
                        ByteBufCodecs.STRING_UTF8, ClientEventPayload::eventJson,
                        ClientEventPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // ── GUI payloads (client ↔ server) ──

    /** Client → Server: open/manipulate/close a script container GUI. */
    public record GUIServerboundPayload(
        int actionType,        // 0=OPEN, 1=SET_ITEM, 2=REGISTER_CALLBACKS, 3=CLOSE
        String title,          // for OPEN
        int rows,              // for OPEN
        String slotsJson,      // for OPEN (JSON object string of slot→itemId)
        int slot,              // for SET_ITEM
        String itemId,         // for SET_ITEM
        int count,             // for SET_ITEM
        boolean hasSlotClick,  // for REGISTER_CALLBACKS
        boolean hasClose,      // for REGISTER_CALLBACKS
        String loreJson,       // for SET_ITEM (JSON array of lore strings)
        boolean enchanted      // for SET_ITEM (enchantment glint override)
    ) implements CustomPacketPayload {

        public static final Type<GUIServerboundPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Box3JS.MODID, "gui_serverbound"));

        public static final StreamCodec<RegistryFriendlyByteBuf, GUIServerboundPayload> STREAM_CODEC =
                StreamCodec.of(
                    (buf, p) -> {
                        buf.writeVarInt(p.actionType);
                        buf.writeUtf(p.title);
                        buf.writeVarInt(p.rows);
                        buf.writeUtf(p.slotsJson);
                        buf.writeVarInt(p.slot);
                        buf.writeUtf(p.itemId);
                        buf.writeVarInt(p.count);
                        buf.writeBoolean(p.hasSlotClick);
                        buf.writeBoolean(p.hasClose);
                        buf.writeUtf(p.loreJson);
                        buf.writeBoolean(p.enchanted);
                    },
                    buf -> new GUIServerboundPayload(
                        buf.readVarInt(),
                        buf.readUtf(),
                        buf.readVarInt(),
                        buf.readUtf(),
                        buf.readVarInt(),
                        buf.readUtf(),
                        buf.readVarInt(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readUtf(),
                        buf.readBoolean()
                    )
                );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Server → Client: GUI events (slot click, close) for client-side JS callbacks. */
    public record GUIClientboundPayload(
        int eventType,   // 0=SLOT_CLICK, 1=CLOSE
        int slot         // for SLOT_CLICK (ignored for CLOSE)
    ) implements CustomPacketPayload {

        public static final Type<GUIClientboundPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Box3JS.MODID, "gui_clientbound"));

        public static final StreamCodec<RegistryFriendlyByteBuf, GUIClientboundPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, GUIClientboundPayload::eventType,
                        ByteBufCodecs.VAR_INT, GUIClientboundPayload::slot,
                        GUIClientboundPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ── Server-side: send client scripts to a joining player ──

    private static final ResourceLocation CLIENT_SCRIPT_ID =
            ResourceLocation.fromNamespaceAndPath(Box3JS.MODID, "client_script");

    public static void sendClientScripts(ServerPlayer player) {
        // Only send to clients that have the optional channel negotiated
        if (!NetworkRegistry.hasChannel(
                player.connection.getConnection(),
                ConnectionProtocol.PLAY,
                CLIENT_SCRIPT_ID)) {
            return;
        }

        var server = player.getServer();
        if (server == null) return;

        // Send standalone JAR client scripts (registered via registerStandaloneClientScript)
        for (var entry : STANDALONE_CLIENT_SCRIPTS.entrySet()) {
            PacketDistributor.sendToPlayer(player,
                    new ClientScriptPayload(entry.getKey(), entry.getValue()));
            Box3JS.LOGGER.debug("Sent standalone client script '{}' to {}", entry.getKey(), player.getName().getString());
        }

        // Send file-system project client scripts
        Path scriptDir = Box3ScriptConfig.get().getScriptDir(server);
        if (!Files.exists(scriptDir)) return;

        try (var dirs = Files.list(scriptDir)) {
            dirs.filter(Files::isDirectory).forEach(projectDir -> {
                String name = projectDir.getFileName().toString();
                if (!Box3ScriptConfig.get().isEnabled(name)) return;

                Path clientJs = projectDir.resolve("dist/client.js");
                if (!Files.exists(clientJs)) {
                    clientJs = projectDir.resolve("client.js");
                }
                if (!Files.exists(clientJs)) return;

                try {
                    String source = Files.readString(clientJs, StandardCharsets.UTF_8);
                    PacketDistributor.sendToPlayer(player, new ClientScriptPayload(name, source));
                    Box3JS.LOGGER.debug("Sent client script '{}' to {}", name, player.getName().getString());
                } catch (IOException e) {
                    Box3JS.LOGGER.error("Failed to send client script '{}': {}", name, e.getMessage());
                }
            });
        } catch (IOException e) {
            Box3JS.LOGGER.warn("Failed to scan client script directory: {}", scriptDir, e);
        }
    }
}
