package me.dreamvoid.chat2qq.nukkit.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.scheduler.AsyncTask;
import me.dreamvoid.chat2qq.nukkit.NukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.internal.httpapi.MiraiHttpAPI;
import me.dreamvoid.miraimc.internal.httpapi.exception.AbnormalStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class onPlayerJoin implements Listener {
    private final NukkitPlugin plugin;
    public onPlayerJoin(NukkitPlugin plugin){
        this.plugin = plugin;
    }
    private static HashMap<Player,Boolean> cache = new HashMap<>();

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false)&&!e.getPlayer().hasPermission("chat2qq.join.silent") && !cache.containsKey(e.getPlayer())){
            plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
                @Override
                public void onRun() {
                    String message = plugin.getConfig().getString("bot.player-join-message").replace("%player%", e.getPlayer().getName());
                    plugin.getConfig().getLongList("bot.bot-accounts").forEach(bot -> plugin.getConfig().getLongList("bot.group-ids").forEach(group -> {
                        try {
                            MiraiBot.getBot(bot).getGroup(group).sendMessageMirai(message);
                        } catch (NoSuchElementException e) {
                            if (MiraiHttpAPI.Bots.containsKey(bot)) {
                                try {
                                    MiraiHttpAPI.INSTANCE.sendGroupMessage(MiraiHttpAPI.Bots.get(bot), group, message);
                                } catch (IOException | AbnormalStatusException ex) {
                                    plugin.getLogger().warning("使用" + bot + "发送消息时出现异常，原因: " + ex);
                                }
                            } else plugin.getLogger().warning("指定的机器人" + bot + "不存在，是否已经登录了机器人？");
                        } finally {
                            int interval = plugin.getConfig().getInt("bot.player-join-message-interval");
                            if(interval > 0) {
                                cache.put(e.getPlayer(), true);
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
