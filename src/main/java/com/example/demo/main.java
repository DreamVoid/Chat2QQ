package com.example.demo;

import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.bukkit.event.MiraiFriendMessageEvent;
import me.dreamvoid.miraimc.bukkit.event.MiraiGroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin implements Listener {

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
        getLogger().info("接收到好友"+e.getSenderID()+"的消息: "+e.getMessage());
        mirai.sendFriendMessage(e.getBotID(),e.getSenderID(),"你发送了一条消息："+e.getMessage());
    }

    @EventHandler
    public void onGroupMessageReceive(MiraiGroupMessageEvent e){
        getLogger().info("接收到群聊"+e.getGroupID()+"的消息: "+e.getMessage());

        // [!] 未来可能移除Mirai的消息链改为插件提供的消息链，因此请勿过分依赖消息链
        MessageChain chain = new MessageChainBuilder().append(new At(e.getSenderID())).append(" 你发送了一条消息：").append(e.getMessage()).build();
        mirai.sendFriendMessage(e.getBotID(),e.getSenderID(), chain);
    }
}
