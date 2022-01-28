package me.dreamvoid.chat2qq.bungee.commands;

import me.dreamvoid.chat2qq.bungee.BungeePlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

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
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',"&a配置文件已经重新载入！")));
            } else sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',"&c你没有足够的权限使用此命令！")));
        } else {
            sender.sendMessage(TextComponent.fromLegacyText("This server is running "+plugin.getDescription().getName()+" version "+plugin.getDescription().getVersion()+" by "+ plugin.getDescription().getAuthor()+" (MiraiMC version "+plugin.getProxy().getPluginManager().getPlugin("MiraiMC").getDescription().getVersion()+")"));
        }
    }
}
