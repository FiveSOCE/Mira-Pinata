package com.mira.pinata;

import com.mira.pinata.service.PinataStatsService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public final class PinataPlaceholderExpansion extends PlaceholderExpansion {
    private final MiraPinataPlugin plugin;
    private final PinataStatsService stats;

    public PinataPlaceholderExpansion(MiraPinataPlugin plugin, PinataStatsService stats) {
        this.plugin = plugin;
        this.stats = stats;
    }

    @Override public @NotNull String getIdentifier() { return "mirapinata"; }
    @Override public @NotNull String getAuthor() { return "FiveS"; }
    @Override public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        return switch (params.toLowerCase()) {
            case "last_boss" -> stats.lastBoss();
            case "last_winner", "last_slayer" -> stats.lastSlayer();
            case "last_top_hitter", "top_hitter" -> stats.lastTopHitter();
            case "last_top_hits", "top_hits" -> Integer.toString(stats.lastTopHits());
            default -> null;
        };
    }
}
