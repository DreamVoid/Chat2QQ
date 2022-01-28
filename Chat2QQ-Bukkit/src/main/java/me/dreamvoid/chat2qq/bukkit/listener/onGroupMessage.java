package me.dreamvoid.chat2qq.bukkit.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import me.dreamvoid.chat2qq.bukkit.BukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.api.MiraiMC;
import me.dreamvoid.miraimc.bukkit.event.MiraiGroupMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class onGroupMessage implements Listener {
    private final BukkitPlugin plugin;
    public onGroupMessage(BukkitPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onGroupMessageReceive(MiraiGroupMessageEvent e){
        String name = e.getSenderNameCard();
        if(name.equalsIgnoreCase("") && plugin.getConfig().getBoolean("general.use-nick-if-namecard-null",false) && e.getType() == 0){
            name = MiraiBot.getBot(e.getBotID()).getGroup(e.getGroupID()).getMember(e.getSenderID()).getNick();
        }
        String formatText;
        if(plugin.getConfig().getBoolean("general.use-miraimc-bind",true) && !MiraiMC.getBinding(e.getSenderID()).equals("")){
            formatText = plugin.getConfig().getString("general.bind-chat-format")
                    .replace("%groupname%",e.getGroupName())
                    .replace("%groupid%",String.valueOf(e.getGroupID()))
                    .replace("%nick%",name)
                    .replace("%qq%",String.valueOf(e.getSenderID()))
                    .replace("%message%",e.getMessage());
            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
                formatText = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(UUID.fromString(MiraiMC.getBinding(e.getSenderID()))),formatText);
            }
        } else formatText = plugin.getConfig().getString("general.in-game-chat-format")
                    .replace("%groupname%",e.getGroupName())
                    .replace("%groupid%",String.valueOf(e.getGroupID()))
                    .replace("%nick%",name)
                    .replace("%qq%",String.valueOf(e.getSenderID()))
                    .replace("%message%",e.getMessage());

        // 判断消息是否带前缀
        boolean allowPrefix = false;
        if(plugin.getConfig().getBoolean("bot.requite-special-word-prefix.enabled",false)){
            for(String prefix : plugin.getConfig().getStringList("bot.requite-special-word-prefix.prefix")){
                if(e.getMessage().startsWith(prefix)){
                    allowPrefix = true;
                    formatText = formatText.replace(prefix,"");
                    break;
                }
            }
        } else allowPrefix = true;

        if(plugin.getConfig().getLongList("bot.bot-accounts").contains(e.getBotID()) && plugin.getConfig().getLongList("bot.group-ids").contains(e.getGroupID()) && allowPrefix){
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',formatText));
        }
    }

}
