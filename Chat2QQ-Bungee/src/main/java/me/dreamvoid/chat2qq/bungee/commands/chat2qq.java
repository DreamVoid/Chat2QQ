package me.dreamvoid.chat2qq.bungee.commands;

import me.dreamvoid.chat2qq.bungee.BungeePlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.text.MessageFormat;

public class chat2qq extends Command {
    private final BungeePlugin plugin;
    public chat2qq(BungeePlugin plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length>=1 && args[0].equalsIgnoreCase("reload")){
            if(sender.hasPermission("miraimc.command.chat2qq")){
                plugin.reloadConfig();
                sender.sendMessage(new ComponentBuilder("配置文件已经重新载入！").color(ChatColor.GREEN).create());
            } else sender.sendMessage(new ComponentBuilder("你没有足够的权限使用此命令！").color(ChatColor.RED).create());
        } else {
            sender.sendMessage(new TextComponent(MessageFormat.format("This server is running {0} version {1} by {2} (MiraiMC version {3})", plugin.getDescription().getName(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthor(), plugin.getProxy().getPluginManager().getPlugin("MiraiMC").getDescription().getVersion())));
        }
    }
}
