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
    public record Holder(Menu menu) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private final MiraPinataPlugin plugin;
    private final PinataManager manager;
    private final Map<UUID, String> pendingChat = new HashMap<>();

    public AdminGuiService(MiraPinataPlugin plugin, PinataManager manager) { this.plugin = plugin; this.manager = manager; }

    public void openMain(Player player) {
        Inventory inv = base(Menu.MAIN, 27, "&5Mira Pinata Admin");
        inv.setItem(10, button(Material.ZOMBIE_HEAD, "&dBoss Settings", List.of("&7Name, health scaling, real-hit rules, damage and knockback")));
        inv.setItem(11, button(Material.NETHERITE_CHESTPLATE, "&dGear", List.of("&7Set the Zombie's exact equipment")));
        inv.setItem(12, button(Material.CHEST, "&dRewards", List.of("&7Exact loot items and independent drop chances")));
        inv.setItem(13, button(Material.BLAZE_POWDER, "&dRandom Effects", List.of("&7Configure every random effect")));
        inv.setItem(14, button(Material.WRITABLE_BOOK, "&dChat Messages", List.of("&7Edit all Pinata messages with & colour codes")));
        inv.setItem(15, button(Material.CLOCK, "&dSchedule", List.of("&7Automatic event time and countdown")));
        String spawn = manager.configuredSpawn() == null ? "&cNot set" : "&aSet";
        inv.setItem(16, button(Material.COMPASS, "&dSet Spawn Location", List.of("&7Click to use your exact current location", spawn)));
        inv.setItem(22, manager.active() || manager.countingDown() ? button(Material.BARRIER, "&cStop Event", List.of("&7Stops the current event/countdown")) : button(Material.EMERALD, "&aStart Event", List.of("&7Starts the configured countdown")));
        player.openInventory(inv);
    }

    public void openBoss(Player player) {
        Inventory inv = base(Menu.BOSS, 27, "&5Pinata Boss Settings");
        inv.setItem(10, button(Material.NAME_TAG, "&dName", List.of("&f" + plugin.getConfig().getString("boss.name"), "&7Supports & colour/format codes")));
        boolean auto = plugin.getConfig().getBoolean("boss.auto-scale-health", true);
        inv.setItem(11, toggle(Material.COMPARATOR, "&dAutomatic Health Scaling", "boss.auto-scale-health", "&7Current scaled health: &f" + manager.scaledHitsForCurrentPlayers() + " hits"));
        inv.setItem(12, button(Material.REDSTONE, "&dManual Hit Health", List.of("&f" + plugin.getConfig().getInt("boss.hits") + " hits", auto ? "&7Currently ignored while scaling is enabled" : "&aManual health is active", "&7Editing this automatically disables scaling")));
        inv.setItem(13, button(Material.IRON_NUGGET, "&dMelee Charge", List.of("&f" + Math.round(plugin.getConfig().getDouble("boss.minimum-melee-charge", 0.90D) * 100D) + "%", "&7How charged a melee swing must be to count")));
        inv.setItem(14, button(Material.IRON_SWORD, "&dAttack Damage", List.of("&f" + plugin.getConfig().getDouble("boss.attack-damage"), "&7Click to edit")));
        inv.setItem(16, button(Material.PISTON, "&dWeapon Knockback", List.of("&fLevel " + plugin.getConfig().getInt("boss.knockback"), "&7Click to edit")));
        inv.setItem(22, back());
        player.openInventory(inv);
    }

    public void openGear(Player player) {
        Inventory inv = base(Menu.GEAR, 27, "&5Pinata Gear");
        inv.setItem(10, configItem("gear.helmet")); inv.setItem(11, configItem("gear.chestplate")); inv.setItem(12, configItem("gear.leggings")); inv.setItem(13, configItem("gear.boots")); inv.setItem(15, configItem("gear.weapon"));
        inv.setItem(18, label(Material.ARMOR_STAND, "&7Helmet", "&7Place exact item in slot above")); inv.setItem(19, label(Material.ARMOR_STAND, "&7Chestplate", "&7Place exact item in slot above")); inv.setItem(20, label(Material.ARMOR_STAND, "&7Leggings", "&7Place exact item in slot above")); inv.setItem(21, label(Material.ARMOR_STAND, "&7Boots", "&7Place exact item in slot above")); inv.setItem(23, label(Material.GOLDEN_SWORD, "&7Weapon", "&7Place exact item in slot above", "&7Configured Knockback is added on spawn")); inv.setItem(26, back()); player.openInventory(inv);
    }

    public void saveGear(Inventory inv) { plugin.getConfig().set("gear.helmet", cloneOrNull(inv.getItem(10))); plugin.getConfig().set("gear.chestplate", cloneOrNull(inv.getItem(11))); plugin.getConfig().set("gear.leggings", cloneOrNull(inv.getItem(12))); plugin.getConfig().set("gear.boots", cloneOrNull(inv.getItem(13))); plugin.getConfig().set("gear.weapon", cloneOrNull(inv.getItem(15))); plugin.saveConfig(); }

    public void openRewards(Player player) {
        migrateLegacyRewards();
        Inventory inv = Bukkit.createInventory(new Holder(Menu.REWARDS), 54, plugin.component("&5Pinata Rewards"));
        for (int slot = 0; slot < 45; slot++) {
            Object raw = plugin.getConfig().get("rewards.slots." + slot + ".item");
            if (raw instanceof ItemStack stack && !stack.getType().isAir()) inv.setItem(slot, stack.clone());
        }
        ItemStack filler = filler(); for (int i = 45; i < 54; i++) inv.setItem(i, filler.clone());
        inv.setItem(45, button(Material.BOOK, "&eReward Chances", List.of("&7Right-click any reward item", "&7to set its independent drop chance.", "&7Supports decimals: 1, 0.5, 0.01 etc.")));
        inv.setItem(46, toggle(Material.CHEST, "&dPer-Hit Loot Rolls", "rewards.per-hit-enabled", "&7Every accepted combat hit independently rolls every reward"));
        inv.setItem(49, back());
        inv.setItem(52, toggle(Material.GOLD_INGOT, "&6Top Hitter Bonus", "rewards.top-hitter-extra-item", "&7Top hitter gets one extra random pool item"));
        player.openInventory(inv);
    }

    public void saveRewards(Inventory inv) {
        for (int slot = 0; slot < 45; slot++) {
            ItemStack item = inv.getItem(slot);
            String base = "rewards.slots." + slot;
            if (item == null || item.getType().isAir()) {
                plugin.getConfig().set(base + ".item", null);
                continue;
            }
            plugin.getConfig().set(base + ".item", item.clone());
            if (!plugin.getConfig().contains(base + ".chance")) plugin.getConfig().set(base + ".chance", 100.0D);
        }
        plugin.saveConfig();
    }

    public void requestRewardChance(Player player, Inventory inv, int slot) {
        if (slot < 0 || slot >= 45) return;
        ItemStack item = inv.getItem(slot);
        if (item == null || item.getType().isAir()) return;
        saveRewards(inv);
        double current = plugin.getConfig().getDouble("rewards.slots." + slot + ".chance", 100.0D);
        requestChat(player, "rewards.chance." + slot, "&eType the drop chance from 0 to 100 for this reward. Current: &f" + trim(current) + "%&e.");
    }

    private void migrateLegacyRewards() {
        if (plugin.getConfig().isConfigurationSection("rewards.slots") && !Objects.requireNonNull(plugin.getConfig().getConfigurationSection("rewards.slots")).getKeys(false).isEmpty()) return;
        List<?> old = plugin.getConfig().getList("rewards.items", Collections.emptyList());
        List<ItemStack> items = new ArrayList<>();
        for (Object obj : old) if (obj instanceof ItemStack stack && !stack.getType().isAir()) items.add(stack.clone());
        if (items.isEmpty()) return;
        double chance = 100.0D / items.size();
        for (int i = 0; i < Math.min(45, items.size()); i++) {
            plugin.getConfig().set("rewards.slots." + i + ".item", items.get(i));
            plugin.getConfig().set("rewards.slots." + i + ".chance", chance);
        }
        plugin.getConfig().set("rewards.items", null);
        plugin.saveConfig();
    }

    public void openEffects(Player player) {
        Inventory inv = base(Menu.EFFECTS, 36, "&5Pinata Random Effects");
        inv.setItem(10, toggle(Material.SUGAR, "&bSpeed Burst", "effects.speed.enabled", "&7Click to toggle")); inv.setItem(11, button(Material.FEATHER, "&bSpeed Level", List.of("&fSpeed " + (plugin.getConfig().getInt("effects.speed.amplifier", 9)+1), "&7Click to edit"))); inv.setItem(12, button(Material.CLOCK, "&bSpeed Duration", List.of("&f"+ticksToSeconds("effects.speed.duration-ticks")+" seconds", "&7Click to edit")));
        inv.setItem(14, toggle(Material.ZOMBIE_HEAD, "&aBaby Mode", "effects.baby.enabled", "&7Temporarily shrinks the Zombie")); inv.setItem(15, button(Material.CLOCK, "&aBaby Duration", List.of("&f"+ticksToSeconds("effects.baby.duration-ticks")+" seconds", "&7Click to edit")));
        inv.setItem(19, toggle(Material.GLASS, "&fInvisibility", "effects.invisibility.enabled", "&7Temporary invisibility")); inv.setItem(20, button(Material.CLOCK, "&fInvisible Duration", List.of("&f"+ticksToSeconds("effects.invisibility.duration-ticks")+" seconds", "&7Click to edit"))); inv.setItem(22, button(Material.REPEATER, "&dRandom Effect Interval", List.of("&f"+plugin.getConfig().getInt("effects.interval-seconds")+" seconds", "&7One random enabled effect each interval"))); inv.setItem(31, back()); player.openInventory(inv);
    }

    public void openMessages(Player player) {
        Inventory inv = base(Menu.MESSAGES, 36, "&5Pinata Messages");
        inv.setItem(10, messageButton(Material.PAPER, "Prefix", "messages.prefix")); inv.setItem(11, messageButton(Material.CLOCK, "Countdown", "messages.countdown")); inv.setItem(12, messageButton(Material.ZOMBIE_HEAD, "Spawned", "messages.spawned")); inv.setItem(13, messageButton(Material.FIREWORK_ROCKET, "Defeated", "messages.defeated")); inv.setItem(14, messageButton(Material.GOLD_INGOT, "Top Hitter", "messages.top-hitter")); inv.setItem(15, messageButton(Material.COMPASS, "Spawn Not Set", "messages.no-spawn")); inv.setItem(16, messageButton(Material.REDSTONE_TORCH, "Already Active", "messages.already-active")); inv.setItem(19, messageButton(Material.NETHERITE_SWORD, "Slayer", "messages.slayer")); inv.setItem(22, messageButton(Material.BARRIER, "Stopped", "messages.stopped")); inv.setItem(31, back()); player.openInventory(inv);
    }

    public void openSchedule(Player player) { Inventory inv=base(Menu.SCHEDULE,27,"&5Pinata Schedule"); inv.setItem(11,toggle(Material.DAYLIGHT_DETECTOR,"&dAutomatic Schedule","schedule.enabled","&7Run once per configured day/time")); inv.setItem(13,button(Material.CLOCK,"&dDaily Time",List.of("&f"+plugin.getConfig().getString("schedule.time","20:00"),"&7Click to enter 24-hour HH:mm"))); inv.setItem(15,button(Material.REPEATER,"&dCountdown",List.of("&f"+plugin.getConfig().getInt("schedule.countdown-seconds",30)+" seconds","&7Click to edit"))); inv.setItem(22,back()); player.openInventory(inv); }

    public void requestChat(Player player,String path,String prompt){ pendingChat.put(player.getUniqueId(),path); player.closeInventory(); plugin.msg(player,prompt+" &7Type &ccancel &7to stop."); }
    public String takePending(UUID uuid){return pendingChat.remove(uuid);} public boolean hasPending(UUID uuid){return pendingChat.containsKey(uuid);}

    public void applyChat(Player player,String path,String value){
        try {
            if (path.startsWith("rewards.chance.")) {
                int slot = Integer.parseInt(path.substring("rewards.chance.".length()));
                double chance = Double.parseDouble(value);
                if (!Double.isFinite(chance) || chance < 0D || chance > 100D) throw new IllegalArgumentException();
                plugin.getConfig().set("rewards.slots." + slot + ".chance", chance);
                plugin.saveConfig();
                plugin.msg(player, "&aReward chance set to &f" + trim(chance) + "%&a.");
                openRewards(player);
                return;
            }

            switch(path){
                case "boss.name","messages.prefix","messages.countdown","messages.spawned","messages.defeated","messages.slayer","messages.top-hitter","messages.no-spawn","messages.already-active","messages.stopped" -> plugin.getConfig().set(path,value);
                case "schedule.time" -> { try{LocalTime.parse(value,TIME);}catch(DateTimeParseException ex){throw new IllegalArgumentException();} plugin.getConfig().set(path,value); }
                case "boss.hits" -> { plugin.getConfig().set(path,Math.max(1,Integer.parseInt(value))); plugin.getConfig().set("boss.auto-scale-health",false); }
                case "boss.minimum-melee-charge" -> { double pct=Double.parseDouble(value); if(pct>1D)pct/=100D; if(!Double.isFinite(pct)||pct<0.1D||pct>1D)throw new IllegalArgumentException(); plugin.getConfig().set(path,pct); }
                case "effects.interval-seconds","schedule.countdown-seconds" -> plugin.getConfig().set(path,Math.max(1,Integer.parseInt(value)));
                case "boss.knockback" -> plugin.getConfig().set(path,Math.max(0,Integer.parseInt(value))); case "boss.attack-damage" -> plugin.getConfig().set(path,Math.max(0D,Double.parseDouble(value)));
                case "effects.speed.level" -> plugin.getConfig().set("effects.speed.amplifier",Math.max(0,Integer.parseInt(value)-1)); case "effects.speed.duration-seconds" -> plugin.getConfig().set("effects.speed.duration-ticks",secondsToTicks(value)); case "effects.baby.duration-seconds" -> plugin.getConfig().set("effects.baby.duration-ticks",secondsToTicks(value)); case "effects.invisibility.duration-seconds" -> plugin.getConfig().set("effects.invisibility.duration-ticks",secondsToTicks(value)); default -> throw new IllegalArgumentException(); }
            plugin.saveConfig(); plugin.msg(player,"&aUpdated &f"+path+"&a."); openForPath(player,path);
        } catch(RuntimeException ex){ plugin.msg(player,"&cInvalid value. Nothing was changed."); if(path.startsWith("rewards."))openRewards(player); }
    }

    private String trim(double value){return value==Math.rint(value)?Long.toString(Math.round(value)):Double.toString(value);}
    private int secondsToTicks(String value){double s=Double.parseDouble(value);if(!Double.isFinite(s)||s<=0D)throw new IllegalArgumentException();return Math.max(1,(int)Math.round(s*20D));}
    private String ticksToSeconds(String path){double s=plugin.getConfig().getInt(path,40)/20D;return s==Math.rint(s)?Integer.toString((int)s):Double.toString(s);}
    private void openForPath(Player p,String path){if(path.startsWith("boss."))openBoss(p);else if(path.startsWith("messages."))openMessages(p);else if(path.startsWith("effects."))openEffects(p);else if(path.startsWith("schedule."))openSchedule(p);else if(path.startsWith("rewards."))openRewards(p);else openMain(p);}
    public void toggle(String path){plugin.getConfig().set(path,!plugin.getConfig().getBoolean(path,false));plugin.saveConfig();}
    private Inventory base(Menu menu,int size,String title){Inventory inv=Bukkit.createInventory(new Holder(menu),size,plugin.component(title));ItemStack f=filler();for(int i=0;i<size;i++)inv.setItem(i,f.clone());return inv;}
    public ItemStack filler(){ItemStack item=new ItemStack(Material.GRAY_STAINED_GLASS_PANE);ItemMeta meta=item.getItemMeta();meta.displayName(Component.text(" "));meta.setEnchantmentGlintOverride(true);item.setItemMeta(meta);return item;}
    private ItemStack button(Material material,String name,List<String> lore){ItemStack item=new ItemStack(material);ItemMeta meta=item.getItemMeta();meta.displayName(plugin.component(name));meta.lore(lore.stream().map(plugin::component).toList());item.setItemMeta(meta);return item;}
    private ItemStack toggle(Material material,String name,String path,String detail){return button(material,name,List.of(plugin.getConfig().getBoolean(path)?"&aEnabled":"&cDisabled",detail));}
    private ItemStack messageButton(Material material,String name,String path){return button(material,"&d"+name,List.of(plugin.getConfig().getString(path,""),"&7Supports standard & colour/format codes","&7Click and type the new message in chat"));}
    private ItemStack label(Material material,String name,String... lore){return button(material,name,Arrays.asList(lore));} private ItemStack back(){return button(Material.ARROW,"&cBack",List.of("&7Return to the main menu"));}
    private ItemStack configItem(String path){Object raw=plugin.getConfig().get(path);return raw instanceof ItemStack stack?stack.clone():null;} private ItemStack cloneOrNull(ItemStack item){return item==null||item.getType().isAir()||item.getType()==Material.GRAY_STAINED_GLASS_PANE?null:item.clone();}
}
