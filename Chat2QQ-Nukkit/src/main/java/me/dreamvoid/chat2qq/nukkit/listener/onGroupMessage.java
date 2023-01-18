package me.dreamvoid.chat2qq.nukkit.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.TextFormat;
import me.dreamvoid.chat2qq.nukkit.NukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.api.MiraiMC;
import me.dreamvoid.miraimc.nukkit.event.message.passive.MiraiGroupMessageEvent;

import java.util.Map;
import java.util.Objects;
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

        // 预处理
        if(plugin.getConfig().getBoolean("general.pretreatment.enabled",false)){
            for(Map<?, ?> config : plugin.getConfig().getMapList("general.pretreatment.list")){
                // 前缀匹配
                if(config.get("prefix") != null && message.startsWith((String) config.get("prefix"))){
                    if(config.get("send") != null){
                        return;
                    }
                    else if(config.get("to_all") != null){
                        message = (String) config.get("to_all");
                    }
                    else if(config.get("to_replace") != null){
                        message = message.replace((String) config.get("prefix"), (String) config.get("to_replace"));
                    }
                }

                // 包含
                else if(config.get("contain") != null && message.contains((String) config.get("prefix"))){
                    if(config.get("send") != null){
                        return;
                    }
                    else if(config.get("to_replace") != null){
                        message = message.replace((String) config.get("contain"), (String) config.get("to_replace"));
                    }
                    else if(config.get("to_all") != null){
                        message = (String) config.get("to_all");
                    }
                }

                // 相等
                else if(config.get("equal") != null && Objects.equals(message, config.get("equal"))){
                    if(config.get("send") != null){
                        return;
                    }
                    else if(config.get("to_all") != null){
                        message = (String) config.get("to_all");
                    }
                }

                // 正则匹配
                else if(config.get("regular") != null && Pattern.compile((String) config.get("regular")).matcher(message).find()){
                    if(config.get("send") != null){
                        return;
                    }
                    else if(config.get("to_regular") != null){
                        message = message.replaceAll((String) config.get("regular"), (String) config.get("to_regular"));
                    }
                    else if(config.get("to_all") != null){
                        message = (String) config.get("to_all");
                    }
                }
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
