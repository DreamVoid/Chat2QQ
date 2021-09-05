package me.dreamvoid.chat2qq.listener;

import me.dreamvoid.chat2qq.BukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class onPlayerJoin implements Listener {
    private final BukkitPlugin plugin;
    public onPlayerJoin(BukkitPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false)){
            new BukkitRunnable() {
                @Override
                public void run() {
                    MiraiBot.getBot(plugin.getConfig().getLong("bot.botaccount")).getGroup(plugin.getConfig().getLong("bot.groupid")).sendMessage(plugin.getConfig().getString("bot.player-join-message").replace("%player%",e.getPlayer().getName()));
                }
            }.runTaskAsynchronously(plugin);
        }
    }
}
