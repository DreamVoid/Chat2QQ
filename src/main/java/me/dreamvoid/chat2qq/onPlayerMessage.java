package me.dreamvoid.chat2qq;

import me.clip.placeholderapi.PlaceholderAPI;
import me.dreamvoid.miraimc.api.MiraiBot;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class onPlayerMessage implements Listener {
    private final main plugin;
    public onPlayerMessage(main plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent e){
        if(!(plugin.getConfig().getBoolean("general.require-command-to-chat",false))){
            boolean allowWorld = false;

            // 判断玩家所处世界
            for(String world : plugin.getConfig().getStringList("general.available-worlds")){
                if(e.getPlayer().getWorld().getName().equalsIgnoreCase(world)){
                    allowWorld = true;
                    break;
                }
            }
            if(plugin.getConfig().getBoolean("general.available-worlds-use-as-blacklist")) allowWorld = !allowWorld;

            if(allowWorld){
                String formatText = plugin.getConfig().getString("bot.group-chat-format")
                        .replace("%player%",e.getPlayer().getName())
                        .replace("%message%",e.getMessage());
                if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI")!=null){
                    formatText = PlaceholderAPI.setPlaceholders(e.getPlayer(),formatText);
                }
                MiraiBot.Instance.sendGroupMessage(plugin.getConfig().getLong("bot.botaccount"),plugin.getConfig().getLong("bot.groupid"),formatText);
            }
        }
    }
}
