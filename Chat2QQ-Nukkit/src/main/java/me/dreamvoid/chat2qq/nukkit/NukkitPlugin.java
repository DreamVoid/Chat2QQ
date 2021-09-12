package me.dreamvoid.chat2qq.nukkit;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import me.dreamvoid.chat2qq.nukkit.listener.onPlayerMessage;
import me.dreamvoid.chat2qq.nukkit.listener.onPlayerQuit;
import me.dreamvoid.chat2qq.nukkit.listener.onGroupMessage;
import me.dreamvoid.chat2qq.nukkit.listener.onPlayerJoin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.nukkit.utils.MetricsLite;

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
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("qchat")){
            String playerName;
            boolean allowConsole = getConfig().getBoolean("general.allow-console-chat", false);

            if(sender instanceof Player player){
                playerName = player.getDisplayName();
            } else {
                if(allowConsole){
                    playerName = getConfig().getString("general.console-name", "控制台");
                } else {
                    sender.sendMessage(TextFormat.colorize('&',"&c控制台不能执行此命令！"));
                    return true;
                }
            }


            StringBuilder message = new StringBuilder();
            for(String arg : args){ message.append(arg).append(" "); }
            String formatText = getConfig().getString("bot.group-chat-format")
                    .replace("%player%", playerName)
                    .replace("%message%", message);
            getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {
                @Override
                public void onRun() {
                    MiraiBot.getBot(getConfig().getLong("bot.botaccount")).getGroup(getConfig().getLong("bot.groupid")).sendMessageMirai(formatText);
                }
            });
            sender.sendMessage(TextFormat.colorize('&',"&a已发送QQ群聊天消息！"));
            if(getConfig().getBoolean("general.command-also-broadcast-to-chat") && sender instanceof Player player){
                player.chat(message.toString());
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
