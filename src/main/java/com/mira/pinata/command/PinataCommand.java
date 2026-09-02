package com.mira.pinata.command;

import com.mira.pinata.gui.AdminGuiService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PinataCommand implements CommandExecutor {
    private final AdminGuiService gui;

    public PinataCommand(AdminGuiService gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("MiraPinata: this command is player-only.");
            return true;
        }
        gui.openMain(player);
        return true;
    }
}
