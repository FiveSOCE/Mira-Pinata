package com.mira.pinata.listener;

import com.mira.pinata.MiraPinataPlugin;
import com.mira.pinata.gui.AdminGuiService;
import com.mira.pinata.service.PinataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class AdminGuiListener implements Listener {
    private final MiraPinataPlugin plugin;
    private final AdminGuiService gui;
    private final PinataManager manager;

    public AdminGuiListener(MiraPinataPlugin plugin, AdminGuiService gui, PinataManager manager) { this.plugin = plugin; this.gui = gui; this.manager = manager; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof AdminGuiService.Holder holder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory top = event.getView().getTopInventory();
        int raw = event.getRawSlot();
        if (holder.menu() == AdminGuiService.Menu.GEAR) { handleGear(event, player, top, raw); return; }
        if (holder.menu() == AdminGuiService.Menu.REWARDS) { handleRewards(event, player, top, raw); return; }

        event.setCancelled(true);
        if (raw < 0 || raw >= top.getSize()) return;
        switch (holder.menu()) {
            case MAIN -> handleMain(player, raw);
            case BOSS -> handleBoss(player, raw);
            case EFFECTS -> handleEffects(player, raw);
            case MESSAGES -> handleMessages(player, raw);
            case SCHEDULE -> handleSchedule(player, raw);
            default -> { }
        }
    }

    private void handleMain(Player player, int slot) {
        switch (slot) {
            case 10 -> gui.openBoss(player); case 11 -> gui.openGear(player); case 12 -> gui.openRewards(player); case 13 -> gui.openEffects(player); case 14 -> gui.openMessages(player); case 15 -> gui.openSchedule(player);
            case 16 -> { manager.setSpawn(player.getLocation()); plugin.msg(player, "&aPinata spawn set to your exact current location."); gui.openMain(player); }
            case 22 -> { if (manager.active() || manager.countingDown()) manager.stopEvent(true); else if (!manager.startCountdown()) plugin.msg(player, manager.configuredSpawn() == null ? plugin.getConfig().getString("messages.no-spawn", "&cSet the spawn location first.") : plugin.getConfig().getString("messages.already-active", "&cA Pinata event is already active.")); player.closeInventory(); }
        }
    }

    private void handleBoss(Player player, int slot) {
        switch (slot) {
            case 10 -> gui.requestChat(player, "boss.name", "&eType the new Pinata name. Standard & colour/format codes are supported, e.g. &c&l.");
            case 11 -> { gui.toggle("boss.auto-scale-health"); gui.openBoss(player); }
            case 12 -> gui.requestChat(player, "boss.hits", "&eType the manual number of hits. Saving this disables automatic scaling.");
            case 13 -> gui.requestChat(player, "boss.minimum-melee-charge", "&eType the required melee charge as 10-100 or 0.1-1.0. Default is 90%.");
            case 14 -> gui.requestChat(player, "boss.attack-damage", "&eType the Zombie's attack damage.");
            case 16 -> gui.requestChat(player, "boss.knockback", "&eType the weapon Knockback level. 0 disables it.");
            case 22 -> gui.openMain(player);
        }
    }

    private void handleEffects(Player player, int slot) {
        switch (slot) {
            case 10 -> { gui.toggle("effects.speed.enabled"); gui.openEffects(player); }
            case 11 -> gui.requestChat(player, "effects.speed.level", "&eType the Speed level, for example 10.");
            case 12 -> gui.requestChat(player, "effects.speed.duration-seconds", "&eType the Speed duration in seconds.");
            case 14 -> { gui.toggle("effects.baby.enabled"); gui.openEffects(player); }
            case 15 -> gui.requestChat(player, "effects.baby.duration-seconds", "&eType the baby-mode duration in seconds.");
            case 19 -> { gui.toggle("effects.invisibility.enabled"); gui.openEffects(player); }
            case 20 -> gui.requestChat(player, "effects.invisibility.duration-seconds", "&eType the invisibility duration in seconds.");
            case 22 -> gui.requestChat(player, "effects.interval-seconds", "&eType the number of seconds between random effects.");
            case 31 -> gui.openMain(player);
        }
    }

    private void handleMessages(Player player, int slot) {
        switch (slot) {
            case 10 -> gui.requestChat(player, "messages.prefix", "&eType the message prefix. & colour/format codes are supported.");
            case 11 -> gui.requestChat(player, "messages.countdown", "&eType the countdown message. Use %seconds%. & codes are supported.");
            case 12 -> gui.requestChat(player, "messages.spawned", "&eType the spawn message. Use %name%. & codes are supported.");
            case 13 -> gui.requestChat(player, "messages.defeated", "&eType the defeated message. Use %name%. & codes are supported.");
            case 14 -> gui.requestChat(player, "messages.top-hitter", "&eType the top-hitter message. Use %player% and %hits%. & codes are supported.");
            case 15 -> gui.requestChat(player, "messages.no-spawn", "&eType the spawn-not-set message. & codes are supported.");
            case 16 -> gui.requestChat(player, "messages.already-active", "&eType the already-active message. & codes are supported.");
            case 19 -> gui.requestChat(player, "messages.slayer", "&eType the slayer message. Use %player% and %name%. & codes are supported.");
            case 22 -> gui.requestChat(player, "messages.stopped", "&eType the stopped-event message. & codes are supported.");
            case 31 -> gui.openMain(player);
        }
    }

    private void handleSchedule(Player player, int slot) {
        switch (slot) {
            case 11 -> { gui.toggle("schedule.enabled"); gui.openSchedule(player); }
            case 13 -> gui.requestChat(player, "schedule.time", "&eType the daily time in 24-hour HH:mm format.");
            case 15 -> gui.requestChat(player, "schedule.countdown-seconds", "&eType the countdown length in seconds.");
            case 22 -> gui.openMain(player);
        }
    }

    private void handleGear(InventoryClickEvent event, Player player, Inventory top, int raw) {
        boolean allowedTop = raw == 10 || raw == 11 || raw == 12 || raw == 13 || raw == 15;
        if (raw == 26) { event.setCancelled(true); gui.saveGear(top); gui.openMain(player); return; }
        if (raw >= 0 && raw < top.getSize() && !allowedTop) event.setCancelled(true);
        if (event.isShiftClick()) event.setCancelled(true);
    }

    private void handleRewards(InventoryClickEvent event, Player player, Inventory top, int raw) {
        if (raw >= 0 && raw < 45 && event.isRightClick()) {
            ItemStack item = top.getItem(raw);
            if (item != null && !item.getType().isAir()) {
                event.setCancelled(true);
                gui.requestRewardChance(player, top, raw);
            }
            return;
        }
        if (raw == 46) { event.setCancelled(true); gui.saveRewards(top); gui.toggle("rewards.per-hit-enabled"); gui.openRewards(player); return; }
        if (raw == 52) { event.setCancelled(true); gui.saveRewards(top); gui.toggle("rewards.top-hitter-extra-item"); gui.openRewards(player); return; }
        if (raw == 49) { event.setCancelled(true); gui.saveRewards(top); gui.openMain(player); return; }
        if (raw >= 45 && raw < top.getSize()) event.setCancelled(true);
        if (event.isShiftClick()) event.setCancelled(true);
    }

    @EventHandler public void onDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof AdminGuiService.Holder holder)) return;
        int topSize = event.getView().getTopInventory().getSize();
        for (int raw : event.getRawSlots()) { if (raw >= topSize) continue; if (holder.menu() == AdminGuiService.Menu.REWARDS && raw < 45) continue; if (holder.menu() == AdminGuiService.Menu.GEAR && (raw == 10 || raw == 11 || raw == 12 || raw == 13 || raw == 15)) continue; event.setCancelled(true); return; }
    }

    @EventHandler public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof AdminGuiService.Holder holder)) return;
        if (holder.menu() == AdminGuiService.Menu.GEAR) gui.saveGear(event.getInventory());
        if (holder.menu() == AdminGuiService.Menu.REWARDS) gui.saveRewards(event.getInventory());
    }
}
