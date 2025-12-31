package me.dreamvoid.chat2qq.bungee;

import me.dreamvoid.chat2qq.bungee.commands.chat2qq;
import me.dreamvoid.chat2qq.bungee.commands.qchat;
import me.dreamvoid.chat2qq.bungee.listener.onGroupMessage;
import me.dreamvoid.chat2qq.bungee.listener.onPlayerJoin;
import me.dreamvoid.chat2qq.bungee.listener.onPlayerQuit;
import me.dreamvoid.chat2qq.bungee.utils.Metrics;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class BungeePlugin extends Plugin {
    Configuration configuration;

    @Override
    public void onLoad() {
        getLogger().info("正在加载配置文件...");
        reloadConfig();
    }

    @Override
    public void onEnable() {
        getLogger().info("正在注册事件监听器...");
        getProxy().getPluginManager().registerListener(this, new onGroupMessage(this));
        getProxy().getPluginManager().registerListener(this, new onPlayerJoin(this));
        getProxy().getPluginManager().registerListener(this, new onPlayerQuit(this));

        getLogger().info("正在注册命令...");
        getProxy().getPluginManager().registerCommand(this, new chat2qq(this,"chat2qq","miraimc.command.chat2qq", "chat2qq"));
        getProxy().getPluginManager().registerCommand(this, new qchat(this,"qchat","miraimc.command.qchat", "qchat"));

        if(getConfig().getBoolean("general.allow-bStats",true)){
            getLogger().info("正在初始化 bStats...");
            int pluginId = 14108;
            new Metrics(this, pluginId);
        }
    }

    public Configuration getConfig(){
        return configuration;
    }

    public void reloadConfig(){
        if (!getDataFolder().exists() && !getDataFolder().mkdir()){
            getLogger().warning("无法创建插件数据文件夹！");
        }

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                getLogger().warning("无法创建默认配置文件，原因: " + e);
            }
        }

        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            getLogger().warning("无法加载配置文件，原因: " + e);
        }
    }
}
