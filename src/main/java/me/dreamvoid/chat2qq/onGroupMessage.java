package me.dreamvoid.chat2qq;

import me.dreamvoid.miraimc.bukkit.event.MiraiGroupMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class onGroupMessage implements Listener {
    private final main plugin;
    public onGroupMessage(main plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onGroupMessageReceive(MiraiGroupMessageEvent e){
        boolean allowPrefix = false;
        String formatText = plugin.getConfig().getString("general.in-game-chat-format")
                .replace("%groupname%",e.getGroupName())
                .replace("%groupid%",String.valueOf(e.getGroupID()))
                .replace("%nick%",e.getSenderNameCard())
                .replace("%qq%",String.valueOf(e.getSenderID()))
                .replace("%message%",e.getMessage());

        // 判断消息是否带前缀
        if(plugin.getConfig().getBoolean("bot.requite-special-word-prefix.enabled",false)){
            for(String prefix : plugin.getConfig().getStringList("bot.requite-special-word-prefix.prefix")){
                if(e.getMessage().startsWith(prefix)){
                    allowPrefix = true;
                    formatText = formatText.replace(prefix,"");
                    break;
                }
            }
        } else allowPrefix = true;

        if(e.getBotID() == plugin.getConfig().getLong("bot.botaccount") && e.getGroupID() == plugin.getConfig().getLong("bot.groupid") && allowPrefix){
            System.out.println(1);
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',formatText));
        }
    }

}
