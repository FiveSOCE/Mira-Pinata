package com.mira.pinata.service;

import com.mira.pinata.MiraPinataPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class PinataStatsService {
    private final MiraPinataPlugin plugin;
    private final File file;
    private final YamlConfiguration data;

    public PinataStatsService(MiraPinataPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void record(String bossName, UUID slayer, String slayerName, UUID topHitter, String topName, int topHits) {
        data.set("last.boss", bossName);
        data.set("last.slayer.uuid", slayer.toString());
        data.set("last.slayer.name", slayerName);
        if (topHitter != null) {
            data.set("last.top-hitter.uuid", topHitter.toString());
            data.set("last.top-hitter.name", topName);
            data.set("last.top-hitter.hits", topHits);
        }
        data.set("last.finished-at", System.currentTimeMillis());
        save();
    }

    public String lastBoss() { return data.getString("last.boss", "None"); }
    public String lastSlayer() { return data.getString("last.slayer.name", "None"); }
    public String lastTopHitter() { return data.getString("last.top-hitter.name", "None"); }
    public int lastTopHits() { return data.getInt("last.top-hitter.hits", 0); }

    private void save() {
        try { data.save(file); }
        catch (IOException ex) { plugin.getLogger().warning("Could not save stats.yml: " + ex.getMessage()); }
    }
}
