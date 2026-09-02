package com.mira.pinata;

import com.mira.pinata.command.PinataCommand;
import com.mira.pinata.gui.AdminGuiService;
import com.mira.pinata.listener.AdminGuiListener;
import com.mira.pinata.listener.ChatEditListener;
import com.mira.pinata.listener.PinataListener;
import com.mira.pinata.service.PinataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class MiraPinataPlugin extends JavaPlugin {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private PinataManager manager;
    private AdminGuiService gui;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        manager = new PinataManager(this);
        gui = new AdminGuiService(this, manager);

        var command = getCommand("mpinata");
        if (command != null) command.setExecutor(new PinataCommand(gui));

        getServer().getPluginManager().registerEvents(new PinataListener(manager), this);
        getServer().getPluginManager().registerEvents(new AdminGuiListener(this, gui, manager), this);
        getServer().getPluginManager().registerEvents(new ChatEditListener(this, gui), this);

        manager.startScheduler();
        getLogger().info("MiraPinata v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.stopEvent(false);
    }

    public Component component(String text) {
        return LEGACY.deserialize(text == null ? "" : text).decoration(TextDecoration.ITALIC, false);
    }

    public String colour(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public void msg(CommandSender sender, String text) {
        sender.sendMessage(component(getConfig().getString("messages.prefix", "&5[MiraPinata] &r") + text));
    }

    public void broadcast(String text) {
        Bukkit.broadcast(component(getConfig().getString("messages.prefix", "&5[MiraPinata] &r") + text));
    }
}
