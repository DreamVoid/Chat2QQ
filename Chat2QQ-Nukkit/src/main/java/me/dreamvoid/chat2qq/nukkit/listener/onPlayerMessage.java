package me.dreamvoid.chat2qq.nukkit.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.scheduler.AsyncTask;
import me.dreamvoid.chat2qq.nukkit.NukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.internal.httpapi.MiraiHttpAPI;
import me.dreamvoid.miraimc.internal.httpapi.exception.AbnormalStatusException;

import java.io.IOException;
import java.util.NoSuchElementException;

public class onPlayerMessage implements Listener {
    private final NukkitPlugin plugin;
    public onPlayerMessage(NukkitPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(PlayerChatEvent e){
        if(e.isCancelled()){
            return;
        }

        if(!(plugin.getConfig().getBoolean("general.require-command-to-chat",false))){
            boolean allowPrefix = false;
            String formatText = plugin.getConfig().getString("bot.group-chat-format")
                    .replace("%player%",e.getPlayer().getName())
                    .replace("%message%",e.getMessage());

            // 判断消息是否带前缀
            if(plugin.getConfig().getBoolean("general.requite-special-word-prefix.enabled",false)){
                for(String prefix : plugin.getConfig().getStringList("general.requite-special-word-prefix.prefix")){
                    if(e.getMessage().startsWith(prefix)){
                        allowPrefix = true;
                        formatText = formatText.substring(1);
                        break;
                    }
                }
            } else allowPrefix = true;

            if(allowPrefix){
                String finalFormatText = formatText;
                plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
                    @Override
                    public void onRun() {
                        plugin.getConfig().getLongList("bot.bot-accounts").forEach(bot -> plugin.getConfig().getLongList("bot.group-ids").forEach(group -> {
                            try {
                                MiraiBot.getBot(bot).getGroup(group).sendMessageMirai(finalFormatText);
                            } catch (NoSuchElementException e) {
                                if (MiraiHttpAPI.Bots.containsKey(bot)) {
                                    try {
                                        MiraiHttpAPI.INSTANCE.sendGroupMessage(MiraiHttpAPI.Bots.get(bot), group, finalFormatText);
                                    } catch (IOException | AbnormalStatusException ex) {
                                        plugin.getLogger().warning("使用" + bot + "发送消息时出现异常，原因: " + ex);
                                    }
                                } else plugin.getLogger().warning("指定的机器人" + bot + "不存在，是否已经登录了机器人？");
                            }
                        }));

                    }
                });
            }
        }
    }
}
