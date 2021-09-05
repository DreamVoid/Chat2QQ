package me.dreamvoid.chat2qq;

import me.clip.placeholderapi.PlaceholderAPI;
import me.dreamvoid.chat2qq.listener.onGroupMessage;
import me.dreamvoid.chat2qq.listener.onPlayerJoin;
import me.dreamvoid.chat2qq.listener.onPlayerMessage;
import me.dreamvoid.chat2qq.listener.onPlayerQuit;
import me.dreamvoid.chat2qq.utils.Metrics;
import me.dreamvoid.miraimc.api.MiraiBot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class BukkitPlugin extends JavaPlugin implements Listener, CommandExecutor {

    @Override // 加载插件
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
    }

    @Override // 启用插件
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new onGroupMessage(this), this);
        Bukkit.getPluginManager().registerEvents(new onPlayerMessage(this), this);
        Bukkit.getPluginManager().registerEvents(new onPlayerJoin(this), this);
        Bukkit.getPluginManager().registerEvents(new onPlayerQuit(this), this);
        getCommand("qchat").setExecutor(this);
        getCommand("chat2qq").setExecutor(this);
        if(getConfig().getBoolean("general.allow-bStats",true)){
            int pluginId = 12193;
            new Metrics(this, pluginId);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(command.getName().equalsIgnoreCase("qchat")){
            String playerName;
            boolean allowWorld = false;
            boolean isPlayer = false;
            boolean allowConsole = getConfig().getBoolean("general.allow-console-chat", false);

            if(sender instanceof Player){
                isPlayer = true;
                Player player = (Player) sender;
                playerName = player.getDisplayName();
                // 判断玩家所处世界
                for(String world : getConfig().getStringList("general.available-worlds")){
                    if(player.getWorld().getName().equalsIgnoreCase(world)){
                        allowWorld = true;
                        break;
                    }
                }
                if(getConfig().getBoolean("general.available-worlds-use-as-blacklist")) allowWorld = !allowWorld;

            } else {
                if(allowConsole){
                    playerName = getConfig().getString("general.console-name", "控制台");
                    allowWorld = true;
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c控制台不能执行此命令！"));
                    return true;
                }
            }

            if(allowWorld) {
                StringBuilder message = new StringBuilder();
                for(String arg : args){ message.append(arg).append(" "); }
                String formatText = getConfig().getString("bot.group-chat-format")
                        .replace("%player%", playerName)
                        .replace("%message%", message);
                if(isPlayer && Bukkit.getPluginManager().getPlugin("PlaceholderAPI")!=null){
                    formatText = PlaceholderAPI.setPlaceholders((Player) sender,formatText);
                }
                String finalFormatText = formatText;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        MiraiBot.getBot(getConfig().getLong("bot.botaccount")).getGroup(getConfig().getLong("bot.groupid")).sendMessage(finalFormatText);
                    }
                }.runTaskAsynchronously(this);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&a已发送QQ群聊天消息！"));
                if(getConfig().getBoolean("general.command-also-broadcast-to-chat") && sender instanceof Player){
                    Player player = (Player) sender;
                    player.chat(message.toString());
                }
            }
        }
        if(command.getName().equalsIgnoreCase("chat2qq")){
            if(args.length>=1 && args[0].equalsIgnoreCase("reload")){
                if(sender.hasPermission("miraimc.command.chat2qq")){
                    reloadConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&a配置文件已经重新载入！"));
                } else sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c你没有足够的权限使用此命令！"));
            } else {
                sender.sendMessage("This server is running "+getDescription().getName()+" version "+getDescription().getVersion()+" by "+ getDescription().getAuthors().toString().replace("[","").replace("]","")+" (MiraiMC version "+Bukkit.getPluginManager().getPlugin("MiraiMC").getDescription().getVersion()+")");
            }
        }
        return true;
    }
}
