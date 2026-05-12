// Box3JS Client Script
// This script runs on the player's Minecraft client.
// It is automatically sent from the server when a player joins.

// Called every client tick (20 times per second).
client.onTick(() => {
    // Your per-frame logic here
});

// Show overlay text in the action bar.
// ui.showOverlay("Hello from client script!");

// Play a sound on the client.
// audio.playSound("minecraft:block.note_block.pling", 1.0, 1.0);

// Poll keyboard state.
// if (input.isKeyDown("space")) { ... }

// Send a chat message.
// chat.sendMessage("Hello!");

console.log("[client] loaded!");
