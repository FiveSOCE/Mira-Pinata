package com.mira.pinata.service;

import com.mira.pinata.MiraPinataPlugin;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class PinataManager {
    private final MiraPinataPlugin plugin;
    private Zombie pinata;
    private BossBar bossBar;
    private int maxHits;
    private int remainingHits;
    private final Map<UUID, Integer> hits = new HashMap<>();
    private BukkitTask countdownTask;
    private BukkitTask effectTask;
    private BukkitTask scheduleTask;
    private String lastScheduledRun = "";

    public PinataManager(MiraPinataPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean active() {
        return pinata != null && pinata.isValid() && !pinata.isDead();
    }

    public boolean countingDown() {
        return countdownTask != null;
    }

    public Zombie pinata() {
        return pinata;
    }

    public int scaledHitsForCurrentPlayers() {
        int players = Math.max(1, Bukkit.getOnlinePlayers().size());
        return ((players - 1) / 5 + 1) * 50;
    }

    public void startScheduler() {
        if (scheduleTask != null) scheduleTask.cancel();
        scheduleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!plugin.getConfig().getBoolean("schedule.enabled", false)) return;
            String wanted = plugin.getConfig().getString("schedule.time", "20:00");
            String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            String runKey = LocalDate.now() + " " + now;
            if (!now.equals(wanted) || runKey.equals(lastScheduledRun)) return;
            lastScheduledRun = runKey;
            startCountdown();
        }, 20L, 20L);
    }

    public boolean startCountdown() {
        if (active() || countingDown()) return false;
        Location spawn = configuredSpawn();
        if (spawn == null) return false;

        int seconds = Math.max(1, plugin.getConfig().getInt("schedule.countdown-seconds", 30));
        plugin.broadcast(plugin.getConfig().getString("messages.countdown", "&eThe Mira Pinata spawns in %seconds% seconds!")
                .replace("%seconds%", Integer.toString(seconds)));

        final int[] left = {seconds};
        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            left[0]--;
            if (left[0] <= 0) {
                countdownTask.cancel();
                countdownTask = null;
                spawnNow();
            }
        }, 20L, 20L);
        return true;
    }

    public void spawnNow() {
        if (active()) return;
        Location location = configuredSpawn();
        if (location == null) return;

        boolean autoScale = plugin.getConfig().getBoolean("boss.auto-scale-health", true);
        maxHits = autoScale
                ? scaledHitsForCurrentPlayers()
                : Math.max(1, plugin.getConfig().getInt("boss.hits", 250));
        remainingHits = maxHits;
        hits.clear();

        pinata = location.getWorld().spawn(location, Zombie.class, zombie -> {
            zombie.setPersistent(true);
            zombie.setRemoveWhenFarAway(false);
            zombie.setCanPickupItems(false);
            zombie.setAdult();
            zombie.setCustomName(plugin.colour(plugin.getConfig().getString("boss.name", "&6&lMira Pinata")));
            zombie.setCustomNameVisible(true);
            zombie.setSilent(false);
            if (zombie.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
                zombie.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(Math.max(0.0, plugin.getConfig().getDouble("boss.attack-damage", 2.0)));
            }
            applyGear(zombie);
        });

        String cleanName = ChatColor.stripColor(pinata.getCustomName() == null ? "Mira Pinata" : pinata.getCustomName());
        bossBar = Bukkit.createBossBar(cleanName + " - " + maxHits + " hits", BarColor.PURPLE, BarStyle.SEGMENTED_10);
        bossBar.setProgress(1.0);
        for (Player player : Bukkit.getOnlinePlayers()) bossBar.addPlayer(player);

        String configuredName = plugin.getConfig().getString("boss.name", "&6&lMira Pinata");
        plugin.broadcast(plugin.getConfig().getString("messages.spawned", "&6&l%name% &ahas spawned!")
                .replace("%name%", configuredName));
        startRandomEffects();
    }

    private void applyGear(Zombie zombie) {
        EntityEquipment eq = zombie.getEquipment();
        if (eq == null) return;
        eq.setHelmet(getItem("gear.helmet"));
        eq.setChestplate(getItem("gear.chestplate"));
        eq.setLeggings(getItem("gear.leggings"));
        eq.setBoots(getItem("gear.boots"));

        ItemStack weapon = getItem("gear.weapon");
        if (weapon == null || weapon.getType().isAir()) weapon = new ItemStack(Material.GOLDEN_SWORD);
        weapon = weapon.clone();
        int knockback = Math.max(0, plugin.getConfig().getInt("boss.knockback", 10));
        if (knockback > 0) weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, knockback);
        eq.setItemInMainHand(weapon);
        eq.setItemInMainHandDropChance(0f);
        eq.setHelmetDropChance(0f);
        eq.setChestplateDropChance(0f);
        eq.setLeggingsDropChance(0f);
        eq.setBootsDropChance(0f);
    }

    public void registerHit(Player player) {
        if (!active()) return;

        remainingHits = Math.max(0, remainingHits - 1);
        hits.merge(player.getUniqueId(), 1, Integer::sum);

        if (plugin.getConfig().getBoolean("rewards.per-hit-random-item", true)) {
            List<ItemStack> rewards = configuredRewards();
            if (!rewards.isEmpty()) giveRandom(player, rewards);
        }

        if (bossBar != null) bossBar.setProgress(Math.max(0.0, Math.min(1.0, remainingHits / (double) maxHits)));
        pinata.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, pinata.getLocation().add(0, 1, 0), 6, 0.25, 0.35, 0.25, 0.02);
        pinata.getWorld().playSound(pinata.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.45f, 1.45f);

        if (remainingHits <= 0) finishEvent(player.getUniqueId());
    }

    private void startRandomEffects() {
        if (effectTask != null) effectTask.cancel();
        long interval = Math.max(2, plugin.getConfig().getLong("effects.interval-seconds", 10)) * 20L;
        effectTask = Bukkit.getScheduler().runTaskTimer(plugin, this::triggerRandomEffect, interval, interval);
    }

    private void triggerRandomEffect() {
        if (!active()) return;
        List<String> enabled = new ArrayList<>();
        if (plugin.getConfig().getBoolean("effects.speed.enabled", true)) enabled.add("speed");
        if (plugin.getConfig().getBoolean("effects.baby.enabled", true)) enabled.add("baby");
        if (plugin.getConfig().getBoolean("effects.invisibility.enabled", true)) enabled.add("invisibility");
        if (enabled.isEmpty()) return;

        String effect = enabled.get(ThreadLocalRandom.current().nextInt(enabled.size()));
        switch (effect) {
            case "speed" -> pinata.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED,
                    Math.max(1, plugin.getConfig().getInt("effects.speed.duration-ticks", 40)),
                    Math.max(0, plugin.getConfig().getInt("effects.speed.amplifier", 9)), false, true, true));
            case "invisibility" -> pinata.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY,
                    Math.max(1, plugin.getConfig().getInt("effects.invisibility.duration-ticks", 40)),
                    0, false, true, true));
            case "baby" -> {
                pinata.setBaby();
                long duration = Math.max(1, plugin.getConfig().getLong("effects.baby.duration-ticks", 60));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (active()) pinata.setAdult();
                }, duration);
            }
        }
    }

    private void finishEvent(UUID slayerUuid) {
        if (!active()) return;

        String bossName = plugin.getConfig().getString("boss.name", "&6&lMira Pinata");
        Location death = pinata.getLocation().clone();
        pinata.remove();
        pinata = null;
        stopRuntimeTasks();

        Player slayer = Bukkit.getPlayer(slayerUuid);
        String slayerName = slayer != null ? slayer.getName() : Optional.ofNullable(Bukkit.getOfflinePlayer(slayerUuid).getName()).orElse("Unknown");
        plugin.broadcast(plugin.getConfig().getString("messages.slayer", "&6%player% &fHas slain %name%&f!")
                .replace("%player%", slayerName)
                .replace("%name%", bossName));

        plugin.broadcast(plugin.getConfig().getString("messages.defeated", "&a%name% &fhas been defeated!")
                .replace("%name%", bossName));

        Map.Entry<UUID, Integer> top = hits.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        if (top != null) {
            Player topPlayer = Bukkit.getPlayer(top.getKey());
            String playerName = topPlayer != null ? topPlayer.getName() : Bukkit.getOfflinePlayer(top.getKey()).getName();
            plugin.broadcast(plugin.getConfig().getString("messages.top-hitter", "&6%player% &ewas the top hitter with &f%hits% &ehits!")
                    .replace("%player%", playerName == null ? "Unknown" : playerName)
                    .replace("%hits%", Integer.toString(top.getValue())));
        }

        rewardTopHitter(top == null ? null : top.getKey());
        fireworks(death);
        hits.clear();
    }

    private void rewardTopHitter(UUID top) {
        if (top == null || !plugin.getConfig().getBoolean("rewards.top-hitter-extra-item", true)) return;
        Player player = Bukkit.getPlayer(top);
        if (player == null) return;
        List<ItemStack> rewards = configuredRewards();
        if (!rewards.isEmpty()) giveRandom(player, rewards);
    }

    private List<ItemStack> configuredRewards() {
        List<?> raw = plugin.getConfig().getList("rewards.items", Collections.emptyList());
        List<ItemStack> rewards = new ArrayList<>();
        for (Object obj : raw) {
            if (obj instanceof ItemStack stack && !stack.getType().isAir()) rewards.add(stack.clone());
        }
        return rewards;
    }

    private void giveRandom(Player player, List<ItemStack> rewards) {
        ItemStack reward = rewards.get(ThreadLocalRandom.current().nextInt(rewards.size())).clone();
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(reward);
        for (ItemStack item : leftover.values()) player.getWorld().dropItemNaturally(player.getLocation(), item);
    }

    private void fireworks(Location location) {
        for (int i = 0; i < 12; i++) {
            final int index = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                World world = location.getWorld();
                if (world == null) return;
                ThreadLocalRandom random = ThreadLocalRandom.current();
                Location burst = location.clone().add(random.nextDouble(-3.0, 3.0), 0.5 + random.nextDouble(0.0, 2.0), random.nextDouble(-3.0, 3.0));
                Firework firework = world.spawn(burst, Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();
                Color primary = switch (index % 4) {
                    case 0 -> Color.AQUA;
                    case 1 -> Color.RED;
                    case 2 -> Color.LIME;
                    default -> Color.YELLOW;
                };
                meta.addEffect(FireworkEffect.builder()
                        .withColor(primary, Color.WHITE)
                        .withFade(Color.PURPLE)
                        .flicker(true)
                        .trail(true)
                        .build());
                meta.setPower(1);
                firework.setFireworkMeta(meta);
            }, (i % 6) * 4L);
        }
    }

    public void stopEvent(boolean announce) {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (pinata != null && pinata.isValid()) pinata.remove();
        pinata = null;
        stopRuntimeTasks();
        hits.clear();
        if (announce) plugin.broadcast(plugin.getConfig().getString("messages.stopped", "&cThe Pinata event was stopped."));
    }

    private void stopRuntimeTasks() {
        if (effectTask != null) {
            effectTask.cancel();
            effectTask = null;
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    public void addBossBarPlayer(Player player) {
        if (bossBar != null) bossBar.addPlayer(player);
    }

    public boolean isPinata(org.bukkit.entity.Entity entity) {
        return pinata != null && entity != null && entity.getUniqueId().equals(pinata.getUniqueId());
    }

    public void setSpawn(Location location) {
        plugin.getConfig().set("spawn.world", location.getWorld().getName());
        plugin.getConfig().set("spawn.x", location.getX());
        plugin.getConfig().set("spawn.y", location.getY());
        plugin.getConfig().set("spawn.z", location.getZ());
        plugin.getConfig().set("spawn.yaw", location.getYaw());
        plugin.getConfig().set("spawn.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public Location configuredSpawn() {
        String worldName = plugin.getConfig().getString("spawn.world", "");
        if (worldName == null || worldName.isBlank()) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world,
                plugin.getConfig().getDouble("spawn.x"),
                plugin.getConfig().getDouble("spawn.y"),
                plugin.getConfig().getDouble("spawn.z"),
                (float) plugin.getConfig().getDouble("spawn.yaw"),
                (float) plugin.getConfig().getDouble("spawn.pitch"));
    }

    private ItemStack getItem(String path) {
        Object value = plugin.getConfig().get(path);
        return value instanceof ItemStack stack ? stack.clone() : null;
    }
}
