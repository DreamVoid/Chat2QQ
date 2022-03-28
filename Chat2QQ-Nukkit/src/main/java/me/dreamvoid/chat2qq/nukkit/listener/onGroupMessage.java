package me.dreamvoid.chat2qq.nukkit.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.TextFormat;
import me.dreamvoid.chat2qq.nukkit.NukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.api.MiraiMC;
import me.dreamvoid.miraimc.nukkit.event.MiraiGroupMessageEvent;

public class onGroupMessage implements Listener {
    private final NukkitPlugin plugin;
    public onGroupMessage(NukkitPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onGroupMessageReceive(MiraiGroupMessageEvent e){
        boolean allowPrefix = false;
        String name = e.getSenderNameCard();
        if(name.equalsIgnoreCase("") && plugin.getConfig().getBoolean("general.use-nick-if-namecard-null",false)){
            name = MiraiBot.getBot(e.getBotID()).getGroup(e.getGroupID()).getMember(e.getSenderID()).getNick();
        }
        String formatText;
        if(plugin.getConfig().getBoolean("general.use-miraimc-bind",true) && !MiraiMC.getBinding(e.getSenderID()).equals("")){
            formatText = plugin.getConfig().getString("general.bind-chat-format")
                    .replace("%groupname%",e.getGroupName())
                    .replace("%groupid%",String.valueOf(e.getGroupID()))
                    .replace("%nick%",name)
                    .replace("%qq%",String.valueOf(e.getSenderID()))
                    .replace("%message%",e.getMessage());
        } else formatText = plugin.getConfig().getString("general.in-game-chat-format")
                    .replace("%groupname%",e.getGroupName())
                    .replace("%groupid%",String.valueOf(e.getGroupID()))
                    .replace("%nick%",name)
                    .replace("%qq%",String.valueOf(e.getSenderID()))
                    .replace("%message%",e.getMessage());

        // 判断消息是否带前缀
        if(plugin.getConfig().getBoolean("bot.requite-special-word-prefix.enabled",false)){
            for(String prefix : plugin.getConfig().getStringList("bot.requite-special-word-prefix.prefix")){
                if(e.getMessage().startsWith(prefix)){
                    allowPrefix = true;
                    formatText = formatText.substring(1);
                    break;
                }
            }
        } else allowPrefix = true;

        if(plugin.getConfig().getLongList("bot.bot-accounts").contains(e.getBotID()) && plugin.getConfig().getLongList("bot.group-ids").contains(e.getGroupID()) && allowPrefix){
            plugin.getServer().broadcastMessage(TextFormat.colorize('&',formatText));
        }
    }
}
