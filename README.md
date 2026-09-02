# MiraPinata

A GUI-driven server Pinata event for Paper 1.21.11.

Instead of a passive llama, MiraPinata spawns a configurable Zombie boss that fights back while the server dogpiles it for rewards.

## Download

**Current release: MiraPinata v0.1.2**

- [Download MiraPinata-0.1.2.jar](https://github.com/FiveSOCE/Mira-Pinata/releases/download/v0.1.2/MiraPinata-0.1.2.jar)
- [View all releases](https://github.com/FiveSOCE/Mira-Pinata/releases)

## Requirements

- Paper 1.21.11
- Java 21

## Admin GUI

`/mpinata` opens the GUI control panel. Permission: `mirapinata.admin`.

Normal administration is GUI-first. The control panel handles the fixed spawn, schedule/countdown, Zombie name and gear, attack damage, Knockback, automatic or manual hit health, physical reward pool, top-hitter bonus, every chat message/prefix, and all random-effect settings.

All name/message fields support standard legacy `&` colour and formatting codes, including combinations such as `&c&l`.

## Health scaling

Automatic health scaling is enabled by default and uses the online player count when the Pinata actually spawns:

- 1-5 players: 50 hits
- 6-10 players: 100 hits
- 11-15 players: 150 hits
- every additional 5 players adds another 50 hits

The Boss Settings GUI can disable automatic scaling. Editing Manual Hit Health automatically switches to manual mode so the configured value is authoritative.

## Event flow

1. MiraPinata broadcasts the configured countdown warning, 30 seconds by default.
2. A persistent Zombie spawns at the exact saved location.
3. The Zombie fights players normally using its configured gear and damage.
4. Its weapon receives the configured Knockback enchantment, level 10 by default.
5. Every valid player hit counts as exactly one Pinata hit regardless of weapon damage.
6. Every successful hit immediately rolls one random item from the configured reward pool for that hitter.
7. A boss bar shows remaining event health.
8. Random effects periodically fire. Current effects are Speed, baby mode, and temporary invisibility. No effect teleports the Pinata.
9. The Pinata is protected from environmental damage and daylight combustion.
10. The final hitter is announced with the configurable Slayer message using `%player%` and `%name%`.
11. The top hitter can receive one additional reward.
12. A large staggered firework finale celebrates the kill.

## Rewards

Open the Rewards GUI and place exact ItemStacks in the first five rows. Item metadata, names, enchantments, lore, quantities and custom data are preserved.

When Per-Hit Reward is enabled, each valid hit immediately gives one random configured reward to the hitter. If their inventory is full, the item is dropped naturally at their location. Top Hitter Bonus remains separately configurable.

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
