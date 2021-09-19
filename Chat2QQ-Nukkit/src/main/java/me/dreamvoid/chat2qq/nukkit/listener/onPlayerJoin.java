package me.dreamvoid.chat2qq.nukkit.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.scheduler.AsyncTask;
import me.dreamvoid.chat2qq.nukkit.NukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;

public class onPlayerJoin implements Listener {
    private final NukkitPlugin plugin;
    public onPlayerJoin(NukkitPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false)&&!e.getPlayer().hasPermission("chat2qq.join.silent")){
            plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
                @Override
                public void onRun() {
                    MiraiBot.getBot(plugin.getConfig().getLong("bot.botaccount")).getGroup(plugin.getConfig().getLong("bot.groupid")).sendMessageMirai(plugin.getConfig().getString("bot.player-join-message").replace("%player%",e.getPlayer().getName()));
                }
            });
        }
    }
}
