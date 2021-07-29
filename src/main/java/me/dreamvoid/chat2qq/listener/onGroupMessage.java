package me.dreamvoid.chat2qq.listener;

import me.dreamvoid.chat2qq.BukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.bukkit.event.MiraiGroupMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class onGroupMessage implements Listener {
    private final BukkitPlugin plugin;
    public onGroupMessage(BukkitPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onGroupMessageReceive(MiraiGroupMessageEvent e){
        boolean allowPrefix = false;
        String name = e.getSenderNameCard();
        if(name.equalsIgnoreCase("") && plugin.getConfig().getBoolean("general.use-nick-if-namecard-null",false)){
            name = MiraiBot.getBot(e.getBotID()).getGroup(e.getGroupID()).getMember(e.getSenderID()).getNick();
        }
        String formatText = plugin.getConfig().getString("general.in-game-chat-format")
                .replace("%groupname%",e.getGroupName())
                .replace("%groupid%",String.valueOf(e.getGroupID()))
                .replace("%nick%",name)
                .replace("%qq%",String.valueOf(e.getSenderID()))
                .replace("%message%",e.getMessage());

        if(plugin.getConfig().getBoolean("general.replace-image-string",true)){
            String[] regexs = {"\\[mirai:image.*.jpg\\]","\\[mirai:image.*.png\\]","\\[mirai:image.*.gif\\]","\\[mirai:image.*.mirai\\]"};
            for(String regex: regexs){
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(formatText);
                formatText = m.replaceAll("[图片]");
            }
        }

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
