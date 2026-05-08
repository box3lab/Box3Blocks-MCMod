// Welcome players when they join the server.
world.onPlayerJoin((entity: GamePlayerEntity) => {
  const p = entity.player;

  world.say(`§e${p.name} §7joined the server`);
  p.directMessage("§6Welcome to §eb §6!");
  p.directMessage("§7Type §e!hello §7to say hi");
});

// Handle chat commands sent by players.
world.onChat((entity: GamePlayerEntity, message: string, _tick: number) => {
  const p = entity.player;

  // Respond to a simple hello command.
  if (message === "!hello") {
    world.say(`§e${p.name}§7: Hello World!`);
  }
});

// Broadcast a periodic status message every 5 seconds (100 ticks).
let announceTicks = 0;
world.onTick(function () {
  announceTicks++;

  if (announceTicks >= 100) {
    announceTicks = 0;

    // Count online entities and show runtime status in each player's action bar.
    const players = world.querySelectorAll("*");
    for (let i = 0; i < players.length; i++) {
      const p = players[i].player;
      if (p) {
        p.actionBar(
          `§a⚡ b is running §7| §f${String(players.length)} §7online`,
        );
      }
    }
  }
});

console.log("[b] loaded - Hello World!");
