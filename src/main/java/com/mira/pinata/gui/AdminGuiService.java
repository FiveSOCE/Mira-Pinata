package com.mira.pinata.gui;

import com.mira.pinata.MiraPinataPlugin;
import com.mira.pinata.service.PinataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public final class AdminGuiService {
    public enum Menu { MAIN, BOSS, GEAR, REWARDS, EFFECTS, MESSAGES, SCHEDULE }
    public record Holder(Menu menu) implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private final MiraPinataPlugin plugin;
    private final PinataManager manager;
    private final Map<UUID, String> pendingChat = new HashMap<>();

    public AdminGuiService(MiraPinataPlugin plugin, PinataManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void openMain(Player player) {
        Inventory inv = base(Menu.MAIN, 27, "&5Mira Pinata Admin");
        inv.setItem(10, button(Material.ZOMBIE_HEAD, "&dBoss Settings", List.of("&7Name, hit health, damage and knockback")));
        inv.setItem(11, button(Material.NETHERITE_CHESTPLATE, "&dGear", List.of("&7Set the Zombie's exact equipment")));
        inv.setItem(12, button(Material.CHEST, "&dRewards", List.of("&7Reward pool and distribution settings")));
        inv.setItem(13, button(Material.BLAZE_POWDER, "&dRandom Effects", List.of("&7Configure every random effect")));
        inv.setItem(14, button(Material.WRITABLE_BOOK, "&dChat Messages", List.of("&7Edit all Pinata messages")));
        inv.setItem(15, button(Material.CLOCK, "&dSchedule", List.of("&7Automatic event time and countdown")));
        String spawn = manager.configuredSpawn() == null ? "&cNot set" : "&aSet";
        inv.setItem(16, button(Material.COMPASS, "&dSet Spawn Location", List.of("&7Click to use your exact current location", spawn)));
        inv.setItem(22, manager.active() || manager.countingDown()
                ? button(Material.BARRIER, "&cStop Event", List.of("&7Stops the current event/countdown"))
                : button(Material.EMERALD, "&aStart Event", List.of("&7Starts the configured countdown")));
        player.openInventory(inv);
    }

    public void openBoss(Player player) {
        Inventory inv = base(Menu.BOSS, 27, "&5Pinata Boss Settings");
        inv.setItem(10, button(Material.NAME_TAG, "&dName", List.of("&f" + plugin.getConfig().getString("boss.name"), "&7Click and type a new value in chat")));
        inv.setItem(12, button(Material.REDSTONE, "&dHit Health", List.of("&f" + plugin.getConfig().getInt("boss.hits") + " hits", "&7Every valid player hit removes exactly one")));
        inv.setItem(14, button(Material.IRON_SWORD, "&dAttack Damage", List.of("&f" + plugin.getConfig().getDouble("boss.attack-damage"), "&7Click to edit")));
        inv.setItem(16, button(Material.PISTON, "&dWeapon Knockback", List.of("&fLevel " + plugin.getConfig().getInt("boss.knockback"), "&7Click to edit")));
        inv.setItem(22, back());
        player.openInventory(inv);
    }

    public void openGear(Player player) {
        Inventory inv = base(Menu.GEAR, 27, "&5Pinata Gear");
        inv.setItem(10, configItem("gear.helmet"));
        inv.setItem(11, configItem("gear.chestplate"));
        inv.setItem(12, configItem("gear.leggings"));
        inv.setItem(13, configItem("gear.boots"));
        inv.setItem(15, configItem("gear.weapon"));
        inv.setItem(18, label(Material.ARMOR_STAND, "&7Helmet", "&7Place exact item in slot above"));
        inv.setItem(19, label(Material.ARMOR_STAND, "&7Chestplate", "&7Place exact item in slot above"));
        inv.setItem(20, label(Material.ARMOR_STAND, "&7Leggings", "&7Place exact item in slot above"));
        inv.setItem(21, label(Material.ARMOR_STAND, "&7Boots", "&7Place exact item in slot above"));
        inv.setItem(23, label(Material.GOLDEN_SWORD, "&7Weapon", "&7Place exact item in slot above", "&7Configured Knockback is added on spawn"));
        inv.setItem(26, back());
        player.openInventory(inv);
    }

    public void saveGear(Inventory inv) {
        plugin.getConfig().set("gear.helmet", cloneOrNull(inv.getItem(10)));
        plugin.getConfig().set("gear.chestplate", cloneOrNull(inv.getItem(11)));
        plugin.getConfig().set("gear.leggings", cloneOrNull(inv.getItem(12)));
        plugin.getConfig().set("gear.boots", cloneOrNull(inv.getItem(13)));
        plugin.getConfig().set("gear.weapon", cloneOrNull(inv.getItem(15)));
        plugin.saveConfig();
    }

    public void openRewards(Player player) {
        Inventory inv = Bukkit.createInventory(new Holder(Menu.REWARDS), 54, plugin.component("&5Pinata Rewards"));
        List<?> raw = plugin.getConfig().getList("rewards.items", Collections.emptyList());
        int slot = 0;
        for (Object obj : raw) if (obj instanceof ItemStack stack && slot < 45) inv.setItem(slot++, stack.clone());
        ItemStack filler = filler();
        for (int i = 45; i < 54; i++) inv.setItem(i, filler.clone());
        inv.setItem(46, toggle(Material.CHEST, "&dParticipant Reward", "rewards.participant-random-item", "&7Each participant gets one random pool item"));
        inv.setItem(49, back());
        inv.setItem(52, toggle(Material.GOLD_INGOT, "&6Top Hitter Bonus", "rewards.top-hitter-extra-item", "&7Top hitter gets one extra random pool item"));
        player.openInventory(inv);
    }

    public void saveRewards(Inventory inv) {
        List<ItemStack> rewards = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && !item.getType().isAir()) rewards.add(item.clone());
        }
        plugin.getConfig().set("rewards.items", rewards);
        plugin.saveConfig();
    }

    public void openEffects(Player player) {
        Inventory inv = base(Menu.EFFECTS, 36, "&5Pinata Random Effects");
        inv.setItem(10, toggle(Material.SUGAR, "&bSpeed Burst", "effects.speed.enabled", "&7Click to toggle"));
        inv.setItem(11, button(Material.FEATHER, "&bSpeed Level", List.of("&fSpeed " + (plugin.getConfig().getInt("effects.speed.amplifier", 9) + 1), "&7Click to edit")));
        inv.setItem(12, button(Material.CLOCK, "&bSpeed Duration", List.of("&f" + ticksToSeconds("effects.speed.duration-ticks") + " seconds", "&7Click to edit")));

        inv.setItem(14, toggle(Material.ZOMBIE_HEAD, "&aBaby Mode", "effects.baby.enabled", "&7Temporarily shrinks the Zombie"));
        inv.setItem(15, button(Material.CLOCK, "&aBaby Duration", List.of("&f" + ticksToSeconds("effects.baby.duration-ticks") + " seconds", "&7Click to edit")));

        inv.setItem(19, toggle(Material.GLASS, "&fInvisibility", "effects.invisibility.enabled", "&7Temporary invisibility"));
        inv.setItem(20, button(Material.CLOCK, "&fInvisible Duration", List.of("&f" + ticksToSeconds("effects.invisibility.duration-ticks") + " seconds", "&7Click to edit")));

        inv.setItem(22, button(Material.REPEATER, "&dRandom Effect Interval", List.of("&f" + plugin.getConfig().getInt("effects.interval-seconds") + " seconds", "&7One random enabled effect each interval")));
        inv.setItem(31, back());
        player.openInventory(inv);
    }

    public void openMessages(Player player) {
        Inventory inv = base(Menu.MESSAGES, 36, "&5Pinata Messages");
        inv.setItem(10, messageButton(Material.PAPER, "Prefix", "messages.prefix"));
        inv.setItem(11, messageButton(Material.CLOCK, "Countdown", "messages.countdown"));
        inv.setItem(12, messageButton(Material.ZOMBIE_HEAD, "Spawned", "messages.spawned"));
        inv.setItem(13, messageButton(Material.FIREWORK_ROCKET, "Defeated", "messages.defeated"));
        inv.setItem(14, messageButton(Material.GOLD_INGOT, "Top Hitter", "messages.top-hitter"));
        inv.setItem(15, messageButton(Material.COMPASS, "Spawn Not Set", "messages.no-spawn"));
        inv.setItem(16, messageButton(Material.REDSTONE_TORCH, "Already Active", "messages.already-active"));
        inv.setItem(22, messageButton(Material.BARRIER, "Stopped", "messages.stopped"));
        inv.setItem(31, back());
        player.openInventory(inv);
    }

    public void openSchedule(Player player) {
        Inventory inv = base(Menu.SCHEDULE, 27, "&5Pinata Schedule");
        inv.setItem(11, toggle(Material.DAYLIGHT_DETECTOR, "&dAutomatic Schedule", "schedule.enabled", "&7Run once per configured day/time"));
        inv.setItem(13, button(Material.CLOCK, "&dDaily Time", List.of("&f" + plugin.getConfig().getString("schedule.time", "20:00"), "&7Click to enter 24-hour HH:mm")));
        inv.setItem(15, button(Material.REPEATER, "&dCountdown", List.of("&f" + plugin.getConfig().getInt("schedule.countdown-seconds", 30) + " seconds", "&7Click to edit")));
        inv.setItem(22, back());
        player.openInventory(inv);
    }

    public void requestChat(Player player, String path, String prompt) {
        pendingChat.put(player.getUniqueId(), path);
        player.closeInventory();
        plugin.msg(player, prompt + " &7Type &ccancel &7to stop.");
    }

    public String takePending(UUID uuid) {
        return pendingChat.remove(uuid);
    }

    public boolean hasPending(UUID uuid) {
        return pendingChat.containsKey(uuid);
    }

    public void applyChat(Player player, String path, String value) {
        try {
            switch (path) {
                case "boss.name", "messages.prefix", "messages.countdown", "messages.spawned", "messages.defeated",
                     "messages.top-hitter", "messages.no-spawn", "messages.already-active", "messages.stopped" -> plugin.getConfig().set(path, value);
                case "schedule.time" -> {
                    try {
                        LocalTime.parse(value, TIME);
                    } catch (DateTimeParseException ex) {
                        throw new IllegalArgumentException("Time must be HH:mm");
                    }
                    plugin.getConfig().set(path, value);
                }
                case "boss.hits", "effects.interval-seconds", "schedule.countdown-seconds" -> plugin.getConfig().set(path, Math.max(1, Integer.parseInt(value)));
                case "boss.knockback" -> plugin.getConfig().set(path, Math.max(0, Integer.parseInt(value)));
                case "boss.attack-damage" -> plugin.getConfig().set(path, Math.max(0D, Double.parseDouble(value)));
                case "effects.speed.level" -> plugin.getConfig().set("effects.speed.amplifier", Math.max(0, Integer.parseInt(value) - 1));
                case "effects.speed.duration-seconds" -> plugin.getConfig().set("effects.speed.duration-ticks", secondsToTicks(value));
                case "effects.baby.duration-seconds" -> plugin.getConfig().set("effects.baby.duration-ticks", secondsToTicks(value));
                case "effects.invisibility.duration-seconds" -> plugin.getConfig().set("effects.invisibility.duration-ticks", secondsToTicks(value));
                default -> throw new IllegalArgumentException();
            }
            plugin.saveConfig();
            plugin.msg(player, "&aUpdated &f" + path + "&a.");
            openForPath(player, path);
        } catch (RuntimeException ex) {
            plugin.msg(player, "&cInvalid value. Nothing was changed.");
        }
    }

    private int secondsToTicks(String value) {
        double seconds = Double.parseDouble(value);
        if (!Double.isFinite(seconds) || seconds <= 0D) throw new IllegalArgumentException();
        return Math.max(1, (int) Math.round(seconds * 20D));
    }

    private String ticksToSeconds(String path) {
        double seconds = plugin.getConfig().getInt(path, 40) / 20D;
        return seconds == Math.rint(seconds) ? Integer.toString((int) seconds) : Double.toString(seconds);
    }

    private void openForPath(Player player, String path) {
        if (path.startsWith("boss.")) openBoss(player);
        else if (path.startsWith("messages.")) openMessages(player);
        else if (path.startsWith("effects.")) openEffects(player);
        else if (path.startsWith("schedule.")) openSchedule(player);
        else openMain(player);
    }

    public void toggle(String path) {
        plugin.getConfig().set(path, !plugin.getConfig().getBoolean(path, false));
        plugin.saveConfig();
    }

    private Inventory base(Menu menu, int size, String title) {
        Inventory inv = Bukkit.createInventory(new Holder(menu), size, plugin.component(title));
        ItemStack filler = filler();
        for (int i = 0; i < size; i++) inv.setItem(i, filler.clone());
        return inv;
    }

    public ItemStack filler() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack button(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plugin.component(name));
        meta.lore(lore.stream().map(plugin::component).toList());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack toggle(Material material, String name, String path, String detail) {
        return button(material, name, List.of(plugin.getConfig().getBoolean(path) ? "&aEnabled" : "&cDisabled", detail));
    }

    private ItemStack messageButton(Material material, String name, String path) {
        return button(material, "&d" + name, List.of("&f" + plugin.getConfig().getString(path, ""), "&7Click and type the new message in chat"));
    }

    private ItemStack label(Material material, String name, String... lore) {
        return button(material, name, Arrays.asList(lore));
    }

    private ItemStack back() {
        return button(Material.ARROW, "&cBack", List.of("&7Return to the main menu"));
    }

    private ItemStack configItem(String path) {
        Object raw = plugin.getConfig().get(path);
        return raw instanceof ItemStack stack ? stack.clone() : null;
    }

    private ItemStack cloneOrNull(ItemStack item) {
        return item == null || item.getType().isAir() || item.getType() == Material.GRAY_STAINED_GLASS_PANE ? null : item.clone();
    }
}
