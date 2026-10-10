package com.mira.pinata;

import com.mira.pinata.command.PinataCommand;
import com.mira.pinata.gui.AdminGuiService;
import com.mira.pinata.listener.AdminGuiListener;
import com.mira.pinata.listener.ChatEditListener;
import com.mira.pinata.listener.PinataListener;
import com.mira.pinata.service.PinataManager;
import com.mira.pinata.service.PinataStatsService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class MiraPinataPlugin extends JavaPlugin {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final String CHAT_PREFIX = "&5&lMira &8>> &r";

    private PinataManager manager;
    private AdminGuiService gui;
    private PinataStatsService stats;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        stats = new PinataStatsService(this);
        manager = new PinataManager(this);
        gui = new AdminGuiService(this, manager);

        var command = getCommand("mpinata");
        if (command != null) command.setExecutor(new PinataCommand(gui));

        getServer().getPluginManager().registerEvents(new PinataListener(manager), this);
        getServer().getPluginManager().registerEvents(new AdminGuiListener(this, gui, manager), this);
        getServer().getPluginManager().registerEvents(new ChatEditListener(this, gui), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PinataPlaceholderExpansion(this, stats).register();
        }

        manager.startScheduler();
        getLogger().info("MiraPinata v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.stopEvent(false);
    }

    public PinataStatsService stats() { return stats; }
    public PinataManager manager() { return manager; }

    public Component component(String text) {
        return LEGACY.deserialize(text == null ? "" : text).decoration(TextDecoration.ITALIC, false);
    }

    public String colour(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public void msg(CommandSender sender, String text) {
        sender.sendMessage(component(CHAT_PREFIX + text));
    }

    public void broadcast(String text) {
        Bukkit.broadcast(component(CHAT_PREFIX + text));
    }
}
