package me.dreamvoid.chat2qq.bungee.listener;

import me.dreamvoid.chat2qq.bungee.BungeePlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class onPlayerQuit implements Listener {
    private final BungeePlugin plugin;
    public onPlayerQuit(BungeePlugin plugin){
        this.plugin = plugin;
    }
    private static final ArrayList<ProxiedPlayer> cache = new ArrayList<>();

    @EventHandler
    public void onPlayerQuitEvent(PlayerDisconnectEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false)&&!e.getPlayer().hasPermission("chat2qq.quit.silent") && !cache.contains(e.getPlayer())){
            plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                String message = plugin.getConfig().getString("bot.player-quit-message").replace("%player%", e.getPlayer().getName());
                plugin.getConfig().getLongList("bot.bot-accounts").forEach(bot -> plugin.getConfig().getLongList("bot.group-ids").forEach(group -> {
                    try {
                        MiraiBot.getBot(bot).getGroup(group).sendMessageMirai(message);
                    } finally {
                        int interval = plugin.getConfig().getInt("bot.player-quit-message-interval");
                        if(interval > 0) {
                            cache.add(e.getPlayer());
                            plugin.getProxy().getScheduler().schedule(plugin, () -> cache.remove(e.getPlayer()),interval, TimeUnit.SECONDS);
                        }
                    }
                }));
            });
        }
    }
}
