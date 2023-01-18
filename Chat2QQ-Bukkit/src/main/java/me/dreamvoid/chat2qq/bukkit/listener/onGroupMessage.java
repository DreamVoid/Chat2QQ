package me.dreamvoid.chat2qq.bukkit.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import me.dreamvoid.chat2qq.bukkit.BukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.api.MiraiMC;
import me.dreamvoid.miraimc.bukkit.event.message.passive.MiraiGroupMessageEvent;
import me.dreamvoid.miraimc.httpapi.MiraiHttpAPI;
import me.dreamvoid.miraimc.httpapi.exception.AbnormalStatusException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class onGroupMessage implements Listener {
    private final BukkitPlugin plugin;
    public onGroupMessage(BukkitPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onGroupMessageReceive(MiraiGroupMessageEvent e) throws InterruptedException {
        if(plugin.getConfig().getStringList("blacklist.word").stream().anyMatch(s -> e.getMessage().contains(s)) || plugin.getConfig().getLongList("blacklist.qq").contains(e.getSenderID())) return;

        String name = e.getSenderNameCard();
        if(name.equalsIgnoreCase("") && plugin.getConfig().getBoolean("general.use-nick-if-namecard-null",false)){
            name = e.getSenderName();
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

        // 运行指令
        //
        if(plugin.getConfig().getBoolean("general.run-command.enabled",false) &&
                plugin.getConfig().getLongList("general.run-command.qq-group").contains(e.getGroupID())){
            // 前缀匹配
            if(message.startsWith(plugin.getConfig().getString("general.run-command.prefix", "/"))){
                String command = message.substring(plugin.getConfig().getString("general.run-command.prefix", "/").length());
                // 长度限制
                if(command.length() <= plugin.getConfig().getInt("general.run-command.command-max-length", 255)){
                    // 在用户组中查找指令
                    String permission = e.getSenderPermission() == 0 ? "MEMBER" :
                                        e.getSenderPermission() == 1 ? "ADMINISTRATOR" :
                                        e.getSenderPermission() == 2 ? "OWNER" : "MEMBER";
                    // System.out.println(permission);
                    for(String whiteListCommand : plugin.getConfig().getStringList("general.run-command.group."+ permission)){
                        if(command.startsWith(whiteListCommand)){
                            // 执行指令
                            if(plugin.getConfig().getBoolean("general.run-command.return",true)){
                                System.out.println("[Chat2QQ] "+ e.getGroupID() +"."+ e.getSenderID() + "运行指令: /"+ command);

                                Commander Sender = new Commander();
                                Bukkit.getScheduler().callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Sender, command));

                                // 等待指令运行
                                Thread.sleep(plugin.getConfig().getInt("general.qq-groupx.sleep", 500));

                                // 消息处理
                                StringBuilder text = new StringBuilder();
                                if(Sender.message.size() == 1){
                                    text = Optional.ofNullable(Sender.message.get(0)).map(StringBuilder::new).orElse(null);
                                }else if(Sender.message.size() > 1){
                                    for(String m : Sender.message){
                                        text.append(m).append("\n");
                                    }
                                }else{
                                    text = new StringBuilder(plugin.getConfig().getString("general.run-command.message-no-out","message-no-out"));
                                }
                                System.out.println(text);
                                // 处理彩色字符
                                String finalText = String.valueOf(text).replaceAll("§[a-z0-9]", "");

                                // 发送消息
                                MiraiBot.getBot(plugin.getConfig().getLongList("bot.bot-accounts").get(0))
                                        .getGroup(e.getGroupID())
                                        .sendMessageMirai(String.valueOf(finalText));

                            } else {
                                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);

                                // 发送消息
                                MiraiBot.getBot(plugin.getConfig().getLongList("bot.bot-accounts").get(0))
                                        .getGroup(e.getGroupID())
                                        .sendMessageMirai(plugin.getConfig().getString("general.run-command.message-no-out","message-no-out"));
                            }

                            return;
                        }
                    }

                }
            }
        }

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
                else if(config.get("contain") != null && message.contains((String) config.get("contain"))){
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
            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
                formatText = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(MiraiMC.getBind(e.getSenderID())), formatText);
            }
        } else {
            formatText = plugin.getConfig().getString("general.in-game-chat-format")
                    .replace("%groupname%",e.getGroupName())
                    .replace("%groupid%",String.valueOf(e.getGroupID()))
                    .replace("%nick%",name)
                    .replace("%regex_nick%", $regex_nick)
                    .replace("%qq%",String.valueOf(e.getSenderID()))
                    .replace("%message%", message);
        }



        if(plugin.getConfig().getLongList("bot.bot-accounts").contains(e.getBotID()) && plugin.getConfig().getLongList("bot.group-ids").contains(e.getGroupID()) && allowPrefix){
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',formatText));
        }
    }

}
