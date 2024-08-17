package me.dreamvoid.chat2qq.nukkit.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.scheduler.AsyncTask;
import me.dreamvoid.chat2qq.nukkit.NukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;

import java.util.ArrayList;

public class onPlayerQuit implements Listener {
    private final NukkitPlugin plugin;
    public onPlayerQuit(NukkitPlugin plugin){
        this.plugin = plugin;
    }
    private static final ArrayList<Player> cache = new ArrayList<>();

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false)&&!e.getPlayer().hasPermission("chat2qq.quit.silent") && !cache.contains(e.getPlayer())){
            plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
                @Override
                public void onRun() {
                    String message = plugin.getConfig().getString("bot.player-quit-message").replace("%player%", e.getPlayer().getName());
                    plugin.getConfig().getLongList("bot.bot-accounts").forEach(bot -> plugin.getConfig().getLongList("bot.group-ids").forEach(group -> {
                        MiraiBot.getBot(bot).getGroup(group).sendMessageMirai(message);
                        int interval = plugin.getConfig().getInt("bot.player-quit-message-interval");
                        if(interval > 0) {
                            cache.add(e.getPlayer());
                            plugin.getServer().getScheduler().scheduleDelayedTask(plugin, new AsyncTask() {
                                @Override
                                public void onRun() {
                                    cache.remove(e.getPlayer());
                                }
                            },interval * 1000, true);
                        }
                    }));
                }
            });
        }
    }
}
