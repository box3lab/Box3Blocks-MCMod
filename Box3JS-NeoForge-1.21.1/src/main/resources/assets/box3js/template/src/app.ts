// ═══════════════════════════════════════════════════
//  PROJECT_NAME
// ═══════════════════════════════════════════════════

// 玩家加入时欢迎
world.onPlayerJoin(function (entity: Entity) {
    var p = entity.player;
    if (!p) return;
    world.say("§e" + p.name + " §7进入了服务器");
    p.directMessage("§6欢迎来到 §ePROJECT_NAME §6！");
    p.directMessage("§7输入 §e!hello §7打个招呼吧");
});

// 聊天命令
world.onChat(function (entity: Entity, message: string, _tick: number) {
    var p = entity.player;
    if (!p) return;

    if (message === "!hello") {
        world.say("§e" + p.name + "§7: Hello World！");
    }
});

// 每 5 秒公告一次
var announceTicks = 0;
world.onTick(function () {
    announceTicks++;
    if (announceTicks >= 100) {
        announceTicks = 0;
        var players = world.querySelectorAll("*");
        for (var i = 0; i < players.length; i++) {
            var p = players[i].player;
            if (p) p.actionBar("§a⚡ PROJECT_NAME 运行中 §7| §f" + players.length + " §7人在线");
        }
    }
});

console.log("[PROJECT_NAME] loaded — Hello World!");
