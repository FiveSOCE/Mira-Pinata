# MiraPinata

MiraPinata is a GUI-driven server boss event for the Mira Paper server suite. It spawns a configurable fighting Zombie Pinata that the server attacks for per-hit rewards, top-hitter bonuses and a final-kill celebration.

## Download

[**Download MiraPinata v0.1.3**](https://github.com/FiveSOCE/Mira-Pinata/releases/download/v0.1.3/MiraPinata-0.1.3.jar)

## Requirements / Dependencies

- Paper 1.21.11
- Java 21
- MiraCore required by current source
- PlaceholderAPI optional
- MiraNPC optional integration

## How MiraPinata Works

Administrators configure the event almost entirely through the `/mpinata` GUI: fixed spawn location, schedule/countdown, boss name/variants, gear, attack damage, Knockback, hit-health mode, real-hit charge threshold, exact reward ItemStacks, independent drop chances, top-hitter bonus, chat messages and random effects.

When the event starts, MiraPinata broadcasts the configured countdown and spawns a persistent Zombie at the saved location. Automatic hit-health scaling can size the event based on online player count, or administrators can switch to a fixed manual hit total. Melee attacks only count when enough of the player's real attack-speed cooldown has elapsed, preventing client spam-clicking from rapidly consuming event health or rolling rewards.

Each accepted hit removes one event hit and independently rolls every configured reward against that reward's own percentage, allowing a hit to drop zero, one or multiple rewards. Exact ItemStack metadata is preserved. A boss bar tracks remaining event health, random effects such as Speed, baby mode and invisibility can fire during the fight, and the Zombie remains protected from environmental damage/daylight. The final hitter and top hitter can be announced/rewarded, followed by a staggered firework finale.

Current source also tracks event statistics/leaderboard data and supports PlaceholderAPI/MiraNPC display integrations. Common message placeholders include `%seconds%`, `%name%`, `%player%` and `%hits%`.

## Commands

| Command | Permission | What it does |
| --- | --- | --- |
| `/mpinata` | `mirapinata.admin` | Opens the MiraPinata administration/control GUI. All normal event configuration, start/stop and editing tools are GUI-driven from this interface. |

## Permissions

| Permission | Default | What it does |
| --- | --- | --- |
| `mirapinata.admin` | OP | Allows access to the MiraPinata administrator GUI and event-management controls. |
