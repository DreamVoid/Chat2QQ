package me.dreamvoid.chat2qq.nukkit.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.TextFormat;
import me.dreamvoid.chat2qq.nukkit.NukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.api.MiraiMC;
import me.dreamvoid.miraimc.nukkit.event.message.passive.MiraiGroupMessageEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class onGroupMessage implements Listener {
    private final NukkitPlugin plugin;
    public onGroupMessage(NukkitPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onGroupMessageReceive(MiraiGroupMessageEvent e){
        if(plugin.getConfig().getStringList("blacklist.word").stream().anyMatch(s -> e.getMessage().contains(s)) || plugin.getConfig().getLongList("blacklist.qq").contains(e.getSenderID())) return;

        String name = e.getSenderNameCard();
        if(name.equalsIgnoreCase("") && plugin.getConfig().getBoolean("general.use-nick-if-namecard-null",false)){
            name = MiraiBot.getBot(e.getBotID()).getGroup(e.getGroupID()).getMember(e.getSenderID()).getNick();
        }

        String message = e.getMessage();
        // 判断消息是否带前缀
        boolean allowPrefix = false;
        if(plugin.getConfig().getBoolean("bot.requite-special-word-prefix.enabled",false)){
            for(String prefix : plugin.getConfig().getStringList("bot.requite-special-word-prefix.prefix")){
                if(e.getMessage().startsWith(prefix)){
                    allowPrefix = true;
                    message = message.substring(prefix.length());
                    break;
                }
            }
        } else allowPrefix = true;

        // cleanup-name
        String $regex_nick = "%regex_nick%";
        if(plugin.getConfig().getBoolean("general.cleanup-name.enabled",false)){
            Matcher matcher = Pattern.compile(plugin.getConfig().getString("general.cleanup-name.regex")).matcher(name);
            if(matcher.find()){
                $regex_nick = matcher.group(1);
            } else {
                $regex_nick = plugin.getConfig().getString("general.cleanup-name.not-captured")
                        .replace("%groupname%",e.getGroupName())
                        .replace("%groupid%",String.valueOf(e.getGroupID()))
                        .replace("%nick%",name)
//                        .replace("%regex_nick%", "%regex_nick%")
                        .replace("%qq%",String.valueOf(e.getSenderID()))
                        .replace("%message%", message);
            }
        }

        String formatText;
        if(plugin.getConfig().getBoolean("general.use-miraimc-bind",true) && MiraiMC.getBind(e.getSenderID()) != null){
            formatText = plugin.getConfig().getString("general.bind-chat-format")
                    .replace("%groupname%",e.getGroupName())
                    .replace("%groupid%",String.valueOf(e.getGroupID()))
                    .replace("%nick%",name)
                    .replace("%regex_nick%", $regex_nick)
                    .replace("%qq%",String.valueOf(e.getSenderID()))
                    .replace("%message%", message);
        } else formatText = plugin.getConfig().getString("general.in-game-chat-format")
                    .replace("%groupname%",e.getGroupName())
                    .replace("%groupid%",String.valueOf(e.getGroupID()))
                    .replace("%nick%",name)
                    .replace("%regex_nick%", $regex_nick)
                    .replace("%qq%",String.valueOf(e.getSenderID()))
                    .replace("%message%", message);

        if(plugin.getConfig().getLongList("bot.bot-accounts").contains(e.getBotID()) && plugin.getConfig().getLongList("bot.group-ids").contains(e.getGroupID()) && allowPrefix){
            plugin.getServer().broadcastMessage(TextFormat.colorize('&',formatText));
        }
    }
}
