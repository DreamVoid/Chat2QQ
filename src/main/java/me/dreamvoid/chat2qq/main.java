package me.dreamvoid.chat2qq;

import me.clip.placeholderapi.PlaceholderAPI;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.bukkit.event.MiraiGroupMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class main extends JavaPlugin implements Listener, CommandExecutor {

    private MiraiBot mirai;

    @Override // 加载插件
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
    }

    @Override // 启用插件
    public void onEnable() {
        this.mirai = MiraiBot.Instance;
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("qchat").setExecutor(this);
        getCommand("chat2qq").setExecutor(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGroupMessageReceive(MiraiGroupMessageEvent e){
        boolean allowPrefix = false;

        // 判断消息是否带前缀
        if(getConfig().getBoolean("bot.requite-special-word-prefix.enabled",false)){
            for(String prefix : getConfig().getStringList("bot.requite-special-word-prefix.prefix")){
                if(e.getMessage().startsWith(prefix)){
                    allowPrefix = true;
                    break;
                }
            }
        } else allowPrefix = true;

        if(e.getBotID() == getConfig().getLong("bot.botaccount") && getConfig().getLong("bot.groupid") == e.getGroupID() &&  allowPrefix){
            String formatText = getConfig().getString("general.in-game-chat-format")
                    .replace("%groupname%",e.getGroupName())
                    .replace("%groupid%",String.valueOf(e.getGroupID()))
                    .replace("%nick%",e.getSenderNameCard())
                    .replace("%qq%",String.valueOf(e.getSenderID()))
                    .replace("%message%",e.getMessage());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',formatText));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent e){
        if(!(getConfig().getBoolean("general.require-command-to-chat",false))){
            boolean allowWorld = false;

            // 判断玩家所处世界
            for(String world : getConfig().getStringList("general.available-worlds")){
                if(e.getPlayer().getWorld().getName().equalsIgnoreCase(world)){
                    allowWorld = true;
                    break;
                }
            }
            if(getConfig().getBoolean("general.available-worlds-use-as-blacklist")) allowWorld = !allowWorld;

            if(allowWorld){
                String formatText = getConfig().getString("bot.group-chat-format")
                        .replace("%player%",e.getPlayer().getName())
                        .replace("%message%",e.getMessage());
                if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI")!=null){
                    formatText = PlaceholderAPI.setPlaceholders(e.getPlayer(),formatText);
                }
                mirai.sendGroupMessage(getConfig().getLong("bot.botaccount"),getConfig().getLong("bot.groupid"),formatText);
            }
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
                mirai.sendGroupMessage(getConfig().getLong("bot.botaccount"),getConfig().getLong("bot.groupid"),formatText);
            }
        }
        if(command.getName().equalsIgnoreCase("chat2qq")){
            if(args.length>1 && args[0].equalsIgnoreCase("reload")){
                if(sender.hasPermission("miraimc.command.chat2qq")){
                    reloadConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&a配置文件已经重新载入！"));
                } else sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c你没有足够的权限使用此命令！"));
            } else {
                sender.sendMessage("This server is running "+getDescription().getName()+" version "+getDescription().getVersion()+" by "+ getDescription().getAuthors().toString().replace("[","").replace("]",""));
                sender.sendMessage("MiraiMC version "+Bukkit.getPluginManager().getPlugin("MiraiMC").getDescription().getVersion()+" by "+ Bukkit.getPluginManager().getPlugin("MiraiMC").getDescription().getAuthors().toString().replace("[","").replace("]",""));
            }
        }
        return true;
    }
}
