// Server script: world logic, players, voxels, storage, and server-side events.
world.onPlayerJoin((entity: GamePlayerEntity) => {
  const p = entity.player;

  world.say(`§e${p.name} §7joined the server`);
  p.directMessage("§6Welcome to §ePROJECT_NAME§6!");
  p.directMessage("§7Type §e!hello §7to say hi");

  if (entity.hasBox3JSClient()) {
    remoteChannel.sendClientEvent(entity, {
      type: "welcome",
      message: `Welcome, ${p.name}`,
    });
  }
});

world.onChat((entity: GamePlayerEntity, message: string, _tick: number) => {
  const p = entity.player;

  if (message === "!hello") {
    world.say(`§e${p.name}§7: Hello World!`);
  }
});

remoteChannel.onServerEvent<{ type: string; fps?: number }>((event) => {
  if (event.args.type === "clientReady") {
    console.log(`[PROJECT_NAME] client ready: ${event.entity.player.name}`);
  }
});

let announceTicks = 0;
world.onTick(function () {
  announceTicks++;

  if (announceTicks >= 100) {
    announceTicks = 0;

    const players = world.querySelectorAll("*");
    for (let i = 0; i < players.length; i++) {
      const p = players[i].player;
      if (p) {
        p.actionBar(
          `§a⚡ PROJECT_NAME is running §7| §f${String(players.length)} §7online`,
        );
      }
    }
  }
});

console.log("[PROJECT_NAME] server loaded");
