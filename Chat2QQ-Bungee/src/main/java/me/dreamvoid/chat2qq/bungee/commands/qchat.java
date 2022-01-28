package me.dreamvoid.chat2qq.bungee.commands;

import me.dreamvoid.chat2qq.bungee.BungeePlugin;
import me.dreamvoid.miraimc.internal.httpapi.MiraiHttpAPI;
import me.dreamvoid.miraimc.internal.httpapi.exception.AbnormalStatusException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;

public class qchat extends Command {
    private final BungeePlugin plugin;

    public qchat(BungeePlugin plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String playerName;
        boolean allowWorld = false;
        boolean isPlayer = false;
        boolean allowConsole = plugin.getConfig().getBoolean("general.allow-console-chat", false);

        if(sender instanceof ProxiedPlayer){
            isPlayer = true;
            ProxiedPlayer player = (ProxiedPlayer) sender;
            playerName = player.getDisplayName();
            // 判断玩家所处世界
            for(String server : plugin.getConfig().getStringList("general.available-servers")){
                if(player.getServer().getInfo().getName().equalsIgnoreCase(server)){
                    allowWorld = true;
                    break;
                }
            }
            if(plugin.getConfig().getBoolean("general.available-servers-use-as-blacklist")) allowWorld = !allowWorld;

        } else {
            if(allowConsole){
                playerName = plugin.getConfig().getString("general.console-name", "控制台");
                allowWorld = true;
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',"&c控制台不能执行此命令！")));
                return;
            }
        }

        if(allowWorld) {
            StringBuilder message = new StringBuilder();
            for(String arg : args){ message.append(arg).append(" "); }
            String formatText = plugin.getConfig().getString("bot.group-chat-format")
                    .replace("%player%", playerName)
                    .replace("%message%", message);
            plugin.getProxy().getScheduler().runAsync(plugin, () -> plugin.getConfig().getLongList("bot.bot-accounts").forEach(bot -> plugin.getConfig().getLongList("bot.group-ids").forEach(group -> {
                try {
                    MiraiHttpAPI.INSTANCE.sendGroupMessage(MiraiHttpAPI.Bots.get(bot), group, formatText);
                } catch (IOException | AbnormalStatusException ex) {
                    plugin.getLogger().warning("使用" + bot + "发送消息时出现异常，原因: " + ex);
                }
            })));
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',"&a已发送QQ群聊天消息！")));
            if(plugin.getConfig().getBoolean("general.command-also-broadcast-to-chat") && sender instanceof ProxiedPlayer){
                ProxiedPlayer player = (ProxiedPlayer) sender;
                player.chat(message.toString());
            }
        }
    }
}
