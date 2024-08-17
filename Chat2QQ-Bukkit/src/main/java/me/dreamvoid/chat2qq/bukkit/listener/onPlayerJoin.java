package me.dreamvoid.chat2qq.bukkit.listener;

import me.dreamvoid.chat2qq.bukkit.BukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class onPlayerJoin implements Listener {
    private final BukkitPlugin plugin;
    public onPlayerJoin(BukkitPlugin plugin){
        this.plugin = plugin;
    }
    private static final ArrayList<Player> cache = new ArrayList<>();

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false) && !e.getPlayer().hasPermission("chat2qq.join.silent") && !cache.contains(e.getPlayer())){
            new BukkitRunnable() {
                @Override
                public void run() {
                    String message = plugin.getConfig().getString("bot.player-join-message").replace("%player%", e.getPlayer().getName());
                    plugin.getConfig().getLongList("bot.bot-accounts").forEach(bot -> plugin.getConfig().getLongList("bot.group-ids").forEach(group -> {
                        try {
                            MiraiBot.getBot(bot).getGroup(group).sendMessageMirai(message);
                        } catch (NoSuchElementException e) {
                            plugin.getLogger().warning("指定的机器人" + bot + "不存在，是否已经登录了机器人？");
                        } finally {
                            int interval = plugin.getConfig().getInt("bot.player-join-message-interval");
                            if(interval > 0) {
                                cache.add(e.getPlayer());
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
