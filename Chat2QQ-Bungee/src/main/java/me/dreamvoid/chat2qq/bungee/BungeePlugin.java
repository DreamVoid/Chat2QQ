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
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");


        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, new onGroupMessage(this));
        getProxy().getPluginManager().registerListener(this, new onPlayerJoin(this));
        getProxy().getPluginManager().registerListener(this, new onPlayerQuit(this));
        getProxy().getPluginManager().registerCommand(this, new chat2qq(this,"chat2qq","miraimc.command.chat2qq", "chat2qq"));
        getProxy().getPluginManager().registerCommand(this, new qchat(this,"qchat","miraimc.command.qchat", "qchat"));
        if(getConfig().getBoolean("general.allow-bStats",true)){
            int pluginId = 14108;
            new Metrics(this, pluginId);
        }
    }

    public Configuration getConfig(){
        return configuration;
    }

    public void reloadConfig(){
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
