# MiraPinata

A GUI-driven server Pinata event for Paper 1.21.11.

Instead of a passive llama, MiraPinata spawns a configurable Zombie boss that fights back while the server dogpiles it for rewards.

## Download

**Current release: MiraPinata v0.1.1**

- [Download MiraPinata-0.1.1.jar](https://github.com/FiveSOCE/Mira-Pinata/releases/download/v0.1.1/MiraPinata-0.1.1.jar)
- [View all releases](https://github.com/FiveSOCE/Mira-Pinata/releases)

## Requirements

- Paper 1.21.11
- Java 21

## Admin GUI

`/mpinata` opens the GUI control panel. Permission: `mirapinata.admin`.

Normal administration is GUI-first. The control panel handles:

- exact fixed spawn location
- manual event start/stop
- automatic daily `HH:mm` schedule
- countdown duration
- Zombie name
- hit-based health
- attack damage
- weapon Knockback level
- exact helmet/chestplate/leggings/boots/weapon ItemStacks
- physical reward pool
- participant reward toggle
- top-hitter bonus toggle
- every Pinata chat message and prefix
- random-effect enable/disable states
- Speed level and duration
- baby-mode duration
- invisibility duration
- random-effect interval

Dead GUI space uses the Mira glowing grey stained-glass style. Text/numeric edits close the GUI, accept the new value through chat, save it, then reopen the relevant GUI.

## Event flow

1. MiraPinata broadcasts the configured countdown warning, 30 seconds by default.
2. A persistent Zombie spawns at the exact saved location.
3. The Zombie fights players normally using its configured gear and damage.
4. Its weapon receives the configured Knockback enchantment, level 10 by default.
5. Every valid player hit counts as exactly one Pinata hit regardless of weapon damage.
6. A boss bar shows remaining event health.
7. Random effects periodically fire. Current effects are Speed, baby mode, and temporary invisibility.
8. MiraPinata contains no teleporting effect.
9. The Pinata is protected from environmental damage and daylight combustion, so event health is controlled only by valid player hits.
10. On defeat, participants receive random configured rewards and the top hitter can receive an extra reward.
11. Top hitter results are broadcast and fireworks celebrate the kill.

## Rewards

Open the Rewards GUI and place exact ItemStacks in the first five rows. Item metadata, names, enchantments, lore, quantities and custom data are preserved.

The bottom row controls whether each participant receives one random pool reward and whether the top hitter receives an additional random pool reward.

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
- top hitter: `%player%`, `%hits%`
