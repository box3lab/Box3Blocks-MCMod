// Client script: local UI, input, audio, chat helpers, and client-side events.
remoteChannel.sendServerEvent({ type: "clientReady" });

remoteChannel.onClientEvent<{ type: string; message?: string }>((event) => {
  if (event.args.type === "welcome" && event.args.message) {
    ui.showOverlay(event.args.message);
    audio.playSound("minecraft:block.note_block.pling", 0.6, 1.2);
  }
});

let ticks = 0;
client.onTick(() => {
  ticks++;

  if (ticks % 40 === 0) {
    const player = client.getPlayer();
    if (player) {
      ui.showActionBar(`FPS ${client.getFPS()} | ${player.name}`);
    }
  }
});

console.log("[client] loaded!");
