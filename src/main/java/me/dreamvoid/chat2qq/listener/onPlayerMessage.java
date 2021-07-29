package me.dreamvoid.chat2qq.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import me.dreamvoid.chat2qq.BukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class onPlayerMessage implements Listener {
    private final BukkitPlugin plugin;
    public onPlayerMessage(BukkitPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent e){
        if(!(plugin.getConfig().getBoolean("general.require-command-to-chat",false))){
            boolean allowWorld = false;
            boolean allowPrefix = false;
            String formatText = plugin.getConfig().getString("bot.group-chat-format")
                    .replace("%player%",e.getPlayer().getName())
                    .replace("%message%",e.getMessage());

            // 判断玩家所处世界
            for(String world : plugin.getConfig().getStringList("general.available-worlds")){
                if(e.getPlayer().getWorld().getName().equalsIgnoreCase(world)){
                    allowWorld = true;
                    break;
                }
            }
            if(plugin.getConfig().getBoolean("general.available-worlds-use-as-blacklist")) allowWorld = !allowWorld;

            // 判断消息是否带前缀
            if(plugin.getConfig().getBoolean("general.requite-special-word-prefix.enabled",false)){
                for(String prefix : plugin.getConfig().getStringList("general.requite-special-word-prefix.prefix")){
                    if(e.getMessage().startsWith(prefix)){
                        allowPrefix = true;
                        formatText = formatText.replace(prefix,"");
                        break;
                    }
                }
            } else allowPrefix = true;

            if(allowWorld && allowPrefix){
                if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI")!=null){
                    formatText = PlaceholderAPI.setPlaceholders(e.getPlayer(),formatText);
                }
                String finalFormatText = formatText;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        MiraiBot.getBot(plugin.getConfig().getLong("bot.botaccount")).getGroup(plugin.getConfig().getLong("bot.groupid")).sendMessage(finalFormatText);
                    }
                }.runTaskAsynchronously(plugin);
            }
        }
    }
}
