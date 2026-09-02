package com.mira.pinata.listener;

import com.mira.pinata.MiraPinataPlugin;
import com.mira.pinata.gui.AdminGuiService;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatEditListener implements Listener {
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private final MiraPinataPlugin plugin;
    private final AdminGuiService gui;

    public ChatEditListener(MiraPinataPlugin plugin, AdminGuiService gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!gui.hasPending(player.getUniqueId())) return;
        event.setCancelled(true);
        String value = PLAIN.serialize(event.message()).trim();
        String path = gui.takePending(player.getUniqueId());
        if (path == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (value.equalsIgnoreCase("cancel")) {
                plugin.msg(player, "&cEdit cancelled.");
                gui.openMain(player);
                return;
            }
            gui.applyChat(player, path, value);
        });
    }
}
