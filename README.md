# MiraPinata

A GUI-driven server Pinata event for Paper 1.21.11.

Instead of a passive llama, MiraPinata spawns a configurable Zombie boss that fights back while the server dogpiles it for rewards.

## Download

**Current release: MiraPinata v0.1.3**

- [Download MiraPinata-0.1.3.jar](https://github.com/FiveSOCE/Mira-Pinata/releases/download/v0.1.3/MiraPinata-0.1.3.jar)
- [View all releases](https://github.com/FiveSOCE/Mira-Pinata/releases)

## Requirements

- Paper 1.21.11
- Java 21

## Admin GUI

`/mpinata` opens the GUI control panel. Permission: `mirapinata.admin`.

Normal administration is GUI-first. The control panel handles the fixed spawn, schedule/countdown, Zombie name and gear, attack damage, Knockback, automatic or manual hit health, real-hit charge threshold, exact reward items and drop chances, top-hitter bonus, every chat message/prefix, and all random-effect settings.

All name/message fields support standard legacy `&` colour and formatting codes, including combinations such as `&c&l`.

## Health scaling

Automatic health scaling is enabled by default and uses the online player count when the Pinata actually spawns:

- 1-5 players: 50 hits
- 6-10 players: 100 hits
- 11-15 players: 150 hits
- every additional 5 players adds another 50 hits

The Boss Settings GUI can disable automatic scaling. Editing Manual Hit Health automatically switches to manual mode so the configured value is authoritative.

## Real combat hits

Melee attacks only count when enough of that player's real attack-speed cooldown has elapsed. The default threshold is 90% and can be edited in Boss Settings.

The calculation uses the player's current attack-speed attribute, so swords, axes, fists and modified attack-speed equipment naturally have different valid hit intervals. Spam clicking may animate on the client but does not reduce Pinata event health and does not roll loot.

Accepted hits are no longer cancelled. MiraPinata lets Minecraft process a tiny real damage event so the Zombie receives normal hurt feedback and combat knockback, then restores its combat body health while the separate event hit counter remains authoritative.

## Event flow

1. MiraPinata broadcasts the configured countdown warning, 30 seconds by default.
2. A persistent Zombie spawns at the exact saved location.
3. The Zombie fights players normally using its configured gear and damage.
4. Its weapon receives the configured Knockback enchantment, level 10 by default.
5. Only accepted real combat hits remove one Pinata event hit.
6. Each accepted hit independently rolls every configured reward against that reward's own drop percentage.
7. A boss bar shows remaining event health.
8. Random effects periodically fire. Current effects are Speed, baby mode, and temporary invisibility. No effect teleports the Pinata.
9. The Pinata is protected from environmental damage and daylight combustion.
10. The final hitter is announced with the configurable Slayer message using `%player%` and `%name%`.
11. The top hitter can receive one additional random pool reward.
12. A large staggered firework finale celebrates the kill.

## Rewards

Open the Rewards GUI and place exact ItemStacks in the first five rows. Item metadata, names, enchantments, lore, quantities and custom data are preserved.

Right-click any configured reward to enter an independent drop chance from `0` to `100`. Decimal rare-drop chances are supported, for example `5`, `1`, `0.5`, `0.1` and `0.01` percent.

Each accepted hit independently rolls every reward. A hit may therefore drop nothing, one reward, or multiple rewards depending on the configured percentages. This gives common loot and ultra-rare loot separate controls without forcing one item every hit.

Legacy v0.1.2 reward pools are migrated automatically the first time the Rewards GUI is opened. Their initial independent chances are evenly divided so the old pool averages roughly one successful reward roll per hit before you tune it.

## Gear

The Gear GUI accepts exact ItemStacks for the Zombie's helmet, chestplate, leggings, boots and weapon. The configured Knockback level is added to the weapon when the Pinata spawns. If no weapon is configured, a Golden Sword is used.

## Random effects

All effect settings are GUI controlled:

- Speed burst, default Speed X for 2 seconds
- temporary baby Zombie mode
- temporary invisibility
- effect duration/strength
- interval between random effects

No effect teleports or relocates the Pinata.

## Message placeholders

- countdown: `%seconds%`
- spawned/defeated: `%name%`
- slayer: `%player%`, `%name%`
- top hitter: `%player%`, `%hits%`
