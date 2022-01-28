package me.dreamvoid.chat2qq.nukkit.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.scheduler.AsyncTask;
import me.dreamvoid.chat2qq.nukkit.NukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.internal.httpapi.MiraiHttpAPI;
import me.dreamvoid.miraimc.internal.httpapi.exception.AbnormalStatusException;

import java.io.IOException;
import java.util.NoSuchElementException;

public class onPlayerQuit implements Listener {
    private final NukkitPlugin plugin;
    public onPlayerQuit(NukkitPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false)&&!e.getPlayer().hasPermission("chat2qq.quit.silent")){
            plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
                @Override
                public void onRun() {
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
                        }
                    }));
                }
            });
        }
    }
}
