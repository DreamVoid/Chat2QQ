package me.dreamvoid.chat2qq.bukkit.listener;

import me.dreamvoid.chat2qq.bukkit.BukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.internal.httpapi.MiraiHttpAPI;
import me.dreamvoid.miraimc.internal.httpapi.exception.AbnormalStatusException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class onPlayerQuit implements Listener {
    private final BukkitPlugin plugin;
    public onPlayerQuit(BukkitPlugin plugin){
        this.plugin = plugin;
    }
    private static HashMap<Player,Boolean> cache = new HashMap<>();

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false) && !e.getPlayer().hasPermission("chat2qq.quit.silent") && !cache.containsKey(e.getPlayer())){
            new BukkitRunnable() {
                @Override
                public void run() {
                    String message = plugin.getConfig().getString("bot.player-quit-message").replace("%player%", e.getPlayer().getName());
                    plugin.getConfig().getLongList("bot.bot-accounts").forEach(bot -> plugin.getConfig().getLongList("bot.group-ids").forEach(group -> {
                        try {
                            MiraiBot.getBot(bot).getGroup(group).sendMessageMirai(message);
                        } catch (NoSuchElementException e) {
                            try {
                                MiraiHttpAPI.INSTANCE.sendGroupMessage(MiraiHttpAPI.Bots.get(bot), group, message);
                            } catch (IOException | AbnormalStatusException ex) {
                                plugin.getLogger().warning("使用" + bot + "发送消息时出现异常，原因: " + ex);
                            }
                        } finally {
                            int interval = plugin.getConfig().getInt("bot.player-quit-message-interval");
                            if(interval > 0) {
                                cache.put(e.getPlayer(), true);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        cache.remove(e.getPlayer());
                                    }
                                }.runTaskLaterAsynchronously(plugin,interval * 20L);
                            }
                        }
                    }));
                }
            }.runTaskAsynchronously(plugin);
        }
    }
}
