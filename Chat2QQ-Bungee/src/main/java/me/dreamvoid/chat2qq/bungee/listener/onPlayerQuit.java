package me.dreamvoid.chat2qq.bungee.listener;

import me.dreamvoid.chat2qq.bungee.BungeePlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.internal.httpapi.MiraiHttpAPI;
import me.dreamvoid.miraimc.internal.httpapi.exception.AbnormalStatusException;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class onPlayerQuit implements Listener {
    private final BungeePlugin plugin;
    public onPlayerQuit(BungeePlugin plugin){
        this.plugin = plugin;
    }
    private static HashMap<ProxiedPlayer,Boolean> cache = new HashMap<>();

    @EventHandler
    public void onPlayerQuitEvent(PlayerDisconnectEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false)&&!e.getPlayer().hasPermission("chat2qq.quit.silent") && !cache.containsKey(e.getPlayer())){
            plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                String message = plugin.getConfig().getString("bot.player-quit-message").replace("%player%", e.getPlayer().getName());
                plugin.getConfig().getLongList("bot.bot-accounts").forEach(bot -> plugin.getConfig().getLongList("bot.group-ids").forEach(group -> {
                    try {
                        MiraiBot.getBot(bot).getGroup(group).sendMessageMirai(message);
                    } catch (NoSuchElementException e1) {
                        try {
                            MiraiHttpAPI.INSTANCE.sendGroupMessage(MiraiHttpAPI.Bots.get(bot), group, message);
                        } catch (IOException | AbnormalStatusException ex) {
                            plugin.getLogger().warning("使用" + bot + "发送消息时出现异常，原因: " + ex);
                        }
                    } finally {
                        int interval = plugin.getConfig().getInt("bot.player-quit-message-interval");
                        if(interval > 0) {
                            cache.put(e.getPlayer(), true);
                            plugin.getProxy().getScheduler().schedule(plugin, () -> cache.remove(e.getPlayer()),interval, TimeUnit.SECONDS);
                        }
                    }
                }));
            });
        }
    }
}
