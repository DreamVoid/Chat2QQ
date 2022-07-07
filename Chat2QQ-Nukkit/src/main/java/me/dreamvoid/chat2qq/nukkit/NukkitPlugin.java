package me.dreamvoid.chat2qq.nukkit;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import me.dreamvoid.chat2qq.nukkit.listener.onGroupMessage;
import me.dreamvoid.chat2qq.nukkit.listener.onPlayerJoin;
import me.dreamvoid.chat2qq.nukkit.listener.onPlayerMessage;
import me.dreamvoid.chat2qq.nukkit.listener.onPlayerQuit;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.httpapi.MiraiHttpAPI;
import me.dreamvoid.miraimc.httpapi.exception.AbnormalStatusException;
import me.dreamvoid.miraimc.nukkit.utils.MetricsLite;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class NukkitPlugin extends PluginBase {
    @Override
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new onGroupMessage(this), this);
        getServer().getPluginManager().registerEvents(new onPlayerMessage(this), this);
        getServer().getPluginManager().registerEvents(new onPlayerJoin(this), this);
        getServer().getPluginManager().registerEvents(new onPlayerQuit(this), this);
        if (getConfig().getBoolean("general.allow-bStats", true)) {
            int pluginId = 12765;
            new MetricsLite(this, pluginId);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("qchat")){
            String playerName;
            boolean allowConsole = getConfig().getBoolean("general.allow-console-chat", false);
            boolean inBlackList = false;

            if(sender instanceof Player){
                playerName = ((Player) sender).getDisplayName();

                for(String t : getConfig().getStringList("blacklist.player")){
                    if (t.equalsIgnoreCase(playerName)) {
                        inBlackList = true;
                        break;
                    }
                }

            } else {
                if(allowConsole){
                    playerName = getConfig().getString("general.console-name", "控制台");
                } else {
                    sender.sendMessage(TextFormat.colorize('&',"&c控制台不能执行此命令！"));
                    return true;
                }
            }

            StringBuilder message = new StringBuilder();
            Arrays.stream(args).forEach(arg -> message.append(arg).append(" "));
            inBlackList = inBlackList || getConfig().getStringList("blacklist.word").stream().anyMatch(t -> message.toString().contains(t));

            if(!inBlackList){
                String formatText = getConfig().getString("bot.group-chat-format")
                        .replace("%player%", playerName)
                        .replace("%message%", message);
                getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {
                    @Override
                    public void onRun() {
                        getConfig().getLongList("bot.bot-accounts").forEach(bot -> getConfig().getLongList("bot.group-ids").forEach(group -> {
                            try {
                                MiraiBot.getBot(bot).getGroup(group).sendMessageMirai(formatText);
                            } catch (NoSuchElementException e) {
                                if (MiraiHttpAPI.Bots.containsKey(bot)) {
                                    try {
                                        MiraiHttpAPI.INSTANCE.sendGroupMessage(MiraiHttpAPI.Bots.get(bot), group, formatText);
                                    } catch (IOException | AbnormalStatusException ex) {
                                        getLogger().warning("使用" + bot + "发送消息时出现异常，原因: " + ex);
                                    }
                                } else getLogger().warning("指定的机器人" + bot + "不存在，是否已经登录了机器人？");
                            }
                        }));
                    }
                });
                sender.sendMessage(TextFormat.colorize('&', "&a已发送QQ群聊天消息！"));
                if (getConfig().getBoolean("general.command-also-broadcast-to-chat") && sender instanceof Player) {
                    ((Player) sender).chat(message.toString());
                }
            }
        }
        if(command.getName().equalsIgnoreCase("chat2qq")){
            if(args.length>=1 && args[0].equalsIgnoreCase("reload")){
                if(sender.hasPermission("miraimc.command.chat2qq")){
                    reloadConfig();
                    sender.sendMessage(TextFormat.colorize('&',"&a配置文件已经重新载入！"));
                } else sender.sendMessage(TextFormat.colorize('&',"&c你没有足够的权限使用此命令！"));
            } else {
                sender.sendMessage("This server is running "+getDescription().getName()+" version "+getDescription().getVersion()+" by "+ getDescription().getAuthors().toString().replace("[","").replace("]","")+" (MiraiMC version "+ getServer().getPluginManager().getPlugin("MiraiMC").getDescription().getVersion()+")");
            }
        }
        return true;
    }
}
