package me.dreamvoid.chat2qq.bukkit.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import me.dreamvoid.chat2qq.bukkit.BukkitPlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.httpapi.MiraiHttpAPI;
import me.dreamvoid.miraimc.httpapi.exception.AbnormalStatusException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.NoSuchElementException;

public class onPlayerMessage implements Listener {
    private final BukkitPlugin plugin;
    public onPlayerMessage(BukkitPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent e){
        if(e.isCancelled()){
            return;
        }

        if(!(plugin.getConfig().getBoolean("general.require-command-to-chat",false))){
            boolean allowWorld = false;
            boolean allowPrefix = false;
            String message = ChatColor.stripColor(e.getMessage());
            String formatText = plugin.getConfig().getString("bot.group-chat-format")
                    .replace("%player%",e.getPlayer().getName())
                    .replace("%message%", message);

            // 判断玩家所处世界
            for(String world : plugin.getConfig().getStringList("general.available-worlds")){
                if(e.getPlayer().getWorld().getName().equalsIgnoreCase(world)){
                    allowWorld = true;
                    break;
                }
            }
            if(plugin.getConfig().getBoolean("general.available-worlds-use-as-blacklist")) allowWorld = !allowWorld;

            // 判断消息是否带前缀
            if(plugin.getConfig().getBoolean("general.requite-special-word-prefix.enabled",false)){
                for(String prefix : plugin.getConfig().getStringList("general.requite-special-word-prefix.prefix")){
                    if(message.startsWith(prefix)){
                        allowPrefix = true;
                        formatText = formatText.substring(prefix.length());
                        break;
                    }
                }
            } else allowPrefix = true;

            if(allowWorld && allowPrefix){
                if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI")!=null){
                    formatText = PlaceholderAPI.setPlaceholders(e.getPlayer(),formatText);
                }
                String finalFormatText = formatText;
                new BukkitRunnable() {
                    @Override
                    public void run() {
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
                }.runTaskAsynchronously(plugin);
            }
        }
    }
}
