package com.mira.pinata.listener;

import com.mira.pinata.service.PinataManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class PinataListener implements Listener {
    private final PinataManager manager;

    public PinataListener(PinataManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!manager.isPinata(event.getEntity())) return;
        event.setCancelled(true);

        Player player = null;
        if (event.getDamager() instanceof Player direct) {
            player = direct;
        } else if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player ranged) player = ranged;
        }
        if (player != null) manager.registerHit(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onOtherDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) return;
        if (manager.isPinata(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onCombust(EntityCombustEvent event) {
        if (manager.isPinata(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (!manager.isPinata(event.getEntity())) return;
        if (event.getTarget() != null && !(event.getTarget() instanceof Player)) event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        manager.addBossBarPlayer(event.getPlayer());
    }
}
