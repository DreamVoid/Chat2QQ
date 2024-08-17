package me.dreamvoid.chat2qq.bukkit.listener;

import me.dreamvoid.chat2qq.bukkit.BukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class onPlayerQuit implements Listener {
    private final BukkitPlugin plugin;
    public onPlayerQuit(BukkitPlugin plugin){
        this.plugin = plugin;
    }
    private static final ArrayList<Player> cache = new ArrayList<>();

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false) && !e.getPlayer().hasPermission("chat2qq.quit.silent") && !cache.contains(e.getPlayer())){
            new BukkitRunnable() {
                @Override
                public void run() {
                    String message = plugin.getConfig().getString("bot.player-quit-message").replace("%player%", e.getPlayer().getName());
                    plugin.getConfig().getLongList("bot.bot-accounts").forEach(bot -> plugin.getConfig().getLongList("bot.group-ids").forEach(group -> {
                        MiraiBot.getBot(bot).getGroup(group).sendMessageMirai(message);
                        int interval = plugin.getConfig().getInt("bot.player-quit-message-interval");
                        if(interval > 0) {
                            cache.add(e.getPlayer());
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    cache.remove(e.getPlayer());
                                }
                            }.runTaskLaterAsynchronously(plugin,interval * 20L);
                        }

                    }));
                }
            }.runTaskAsynchronously(plugin);
        }
    }
}
