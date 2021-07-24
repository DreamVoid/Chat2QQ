package com.example.demo;

import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.bukkit.event.MiraiFriendMessageEvent;
import me.dreamvoid.miraimc.bukkit.event.MiraiGroupMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class commandexample extends JavaPlugin implements Listener {

    private MiraiBot mirai;

    @Override // 加载插件
    public void onLoad() { }

    @Override // 启用插件
    public void onEnable() {
        this.mirai = MiraiBot.Instance;
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override // 禁用插件
    public void onDisable() { }

    @EventHandler
    public void onFriendMessageReceive(MiraiFriendMessageEvent e){
        if(e.getMessage().equals("在线人数")) {
            mirai.sendFriendMessage(e.getBotID(), e.getSenderID(), "当前在线人数：" + Bukkit.getServer().getOnlinePlayers().size()+"人");
        }
    }

    @EventHandler
    public void onGroupMessageReceive(MiraiGroupMessageEvent e){
        if(e.getMessage().equals("在线人数")) {
            mirai.sendGroupMessage(e.getBotID(), e.getGroupID(), "当前在线人数：" + Bukkit.getServer().getOnlinePlayers().size()+"人");
        }
    }
}
