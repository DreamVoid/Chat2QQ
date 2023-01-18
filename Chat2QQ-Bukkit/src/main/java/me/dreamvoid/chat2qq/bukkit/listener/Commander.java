package me.dreamvoid.chat2qq.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Commander implements CommandSender {
    public List<String> message = new ArrayList<>();

    @Override
    public void sendMessage(String message) {
        this.message.add(message);
    }

    @Override
    public void sendMessage(String[] messages) {
        message.addAll(Arrays.asList(messages));
    }

    @Override
    public Server getServer() {
        return Bukkit.getConsoleSender().getServer();
    }

    @Override
    public String getName() {
        return "_Chat2QQ_";
    }

    @Override
    public Spigot spigot() {
        return Bukkit.getConsoleSender().spigot();
    }

    @Override
    public boolean isPermissionSet(String s) {
        return Bukkit.getConsoleSender().isPermissionSet(s);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return Bukkit.getConsoleSender().isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(String s) {
        return Bukkit.getConsoleSender().hasPermission(s);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return Bukkit.getConsoleSender().hasPermission(permission);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        return Bukkit.getConsoleSender().addAttachment(plugin, s, b);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return Bukkit.getConsoleSender().addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        return Bukkit.getConsoleSender().addAttachment(plugin, s, b, i);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return Bukkit.getConsoleSender().addAttachment(plugin, i);
    }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment) {

    }

    @Override
    public void recalculatePermissions() {

    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Bukkit.getConsoleSender().getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean b) {

    }
}
