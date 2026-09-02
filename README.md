# MiraPinata

A GUI-driven server Pinata event for Paper 1.21.11.

Instead of a passive llama, MiraPinata spawns a configurable Zombie boss that fights back while the server dogpiles it for rewards.

## Download

**Current release: MiraPinata v0.1.0**

- [Download MiraPinata-0.1.0.jar](https://github.com/FiveSOCE/Mira-Pinata/releases/download/v0.1.0/MiraPinata-0.1.0.jar)
- [View all releases](https://github.com/FiveSOCE/Mira-Pinata/releases)

## Requirements

- Paper 1.21.11
- Java 21

## Admin

`/mpinata` opens the GUI control panel. Permission: `mirapinata.admin`.

The GUI controls:

- fixed spawn location
- manual event start/stop
- automatic daily HH:mm schedule
- countdown duration
- Zombie name
- hit-based health
- attack damage
- weapon Knockback level
- exact helmet/chestplate/leggings/boots/weapon ItemStacks
- physical reward pool
- event chat messages
- random effects

Dead GUI space uses the Mira glowing grey stained-glass style.

## Event flow

1. MiraPinata broadcasts the configured countdown warning, 30 seconds by default.
2. A persistent Zombie spawns at the exact saved location.
3. The Zombie fights players normally using its configured gear and damage.
4. Its weapon receives the configured Knockback enchantment, level 10 by default.
5. Every valid player hit counts as exactly one Pinata hit regardless of weapon damage.
6. A boss bar shows remaining event health.
7. Random effects periodically fire. Current effects are Speed X, baby mode, and temporary invisibility.
8. MiraPinata contains no teleporting effect.
9. On defeat, participants receive random configured rewards and the top hitter can receive an extra reward.
10. Top hitter results are broadcast and fireworks celebrate the kill.

## Rewards

Open the Rewards GUI and place exact ItemStacks in the first five rows. Item metadata, names, enchantments, lore, quantities and custom data are preserved.

By default every participant receives one random reward, and the top hitter receives one additional random reward.

## Gear

The Gear GUI accepts exact ItemStacks for the Zombie's helmet, chestplate, leggings, boots and weapon. The configured Knockback level is applied to the weapon when the Pinata spawns.

## Random effects

All random effects are toggled through the GUI:

- Speed X burst
- temporary baby Zombie mode
- temporary invisibility

The interval is GUI editable. No effect teleports or relocates the Pinata.

## Message placeholders

- countdown: `%seconds%`
- spawned/defeated: `%name%`
- top hitter: `%player%`, `%hits%`
