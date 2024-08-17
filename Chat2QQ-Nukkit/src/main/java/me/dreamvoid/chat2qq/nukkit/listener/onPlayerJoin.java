package me.dreamvoid.chat2qq.nukkit.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.scheduler.AsyncTask;
import me.dreamvoid.chat2qq.nukkit.NukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class onPlayerJoin implements Listener {
    private final NukkitPlugin plugin;
    public onPlayerJoin(NukkitPlugin plugin){
        this.plugin = plugin;
    }
    private static final ArrayList<Player> cache = new ArrayList<>();

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false)&&!e.getPlayer().hasPermission("chat2qq.join.silent") && !cache.contains(e.getPlayer())){
            plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
                @Override
                public void onRun() {
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
                                plugin.getServer().getScheduler().scheduleDelayedTask(plugin, new AsyncTask() {
                                    @Override
                                    public void onRun() {
                                        cache.remove(e.getPlayer());
                                    }
                                },interval * 1000, true);
                            }
                        }
                    }));
                }
            });
        }
    }
}
