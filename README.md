# MiraPinata

## Downloads and compatibility

| Server target | Java | Final JAR name | Download |
| --- | --- | --- | --- |
| **Paper 1.18.2** | 17 | `MiraPinata_1.18.2.jar` | [Download 1.18.2 JAR](https://github.com/FiveSOCE/Mira-Pinata/releases/download/dev-1.18.2/MiraPinata_1.18.2.jar) |
| **Paper 1.21.11–26.2** | Java 21 baseline; Paper 26.2 may require Java 25 | `MiraPinata_1.21.11-26.2.jar` | [Download modern JAR](https://github.com/FiveSOCE/Mira-Pinata/releases/download/compat-modern-1.21.11-26.2/MiraPinata_1.21.11-26.2.jar) |

**Build status:** Version-specific direct links are provided where published. A GitHub Actions or Releases link means the corresponding versioned JAR has not been verified. Compilation alone does not establish in-game compatibility; intermediate versions 1.19–1.21.10 remain unverified unless specifically documented.

**Installation:** Back up plugin data, remove older JAR copies and install only the build matching your server. Never install both builds together.

[Legacy source](https://github.com/FiveSOCE/Mira-Pinata/tree/compat/1.18.2) · [Modern source](https://github.com/FiveSOCE/Mira-Pinata) · [All releases](https://github.com/FiveSOCE/Mira-Pinata/releases)

## License

**All Rights Reserved.** Copyright (c) 2026 FiveSOCE. This repository is proprietary; no permission to use, copy, modify, or redistribute the software is granted without prior written permission, except as required by applicable law. See [LICENSE](LICENSE).

## Download by Minecraft version

| Paper version | Java | Filename | Download |
| --- | --- | --- | --- |
| **1.18.2** | 17 | `MiraPinata_1.18.2.jar` | [Download legacy JAR](https://github.com/FiveSOCE/Mira-Pinata/releases/download/dev-1.18.2/MiraPinata_1.18.2.jar) |
| **1.21.11–26.2** | Java 21 baseline, newer servers may require Java 25 | `MiraPinata_1.21.11-26.2.jar` | [Download modern JAR](https://github.com/FiveSOCE/Mira-Pinata/releases/download/compat-modern-1.21.11-26.2/MiraPinata_1.21.11-26.2.jar) |

Both named GitHub release assets have been verified after successful CI builds. Compatibility is verified through build/bytecode checks first; full runtime testing and Paper 1.19–1.21.10 validation remain outstanding.

[Legacy source](https://github.com/FiveSOCE/Mira-Pinata/tree/compat/1.18.2) · [Modern source](https://github.com/FiveSOCE/Mira-Pinata/tree/compat/modern-1.21.11-26.2) · [All releases](https://github.com/FiveSOCE/Mira-Pinata/releases)

## Dependencies and installation

- **Required:** Paper matching the selected JAR and suitable Java runtime.
- **Required Mira plugins:** None. Both new builds are independently compiled and their plugin metadata declares **no MiraCore dependency**.
- **Optional:** PlaceholderAPI for leaderboard placeholders, MiraNPC for display integrations, MiraCosmetics for visuals and sounds.
- **Install:** Remove the old `MiraPinata-0.1.8.jar` or other Pinata JAR; install only one version-specific JAR, back up `plugins/MiraPinata/` and restart.

## v0.1.8 per-hit reward fix

- Per-hit loot now awards **at most one reward per accepted hit** instead of independently rolling every reward entry.
- Accepted hits have a configurable global reward chance, defaulting to **70%**.
- Failed reward rolls send the hitter the configurable `%name%'s Pockets are empty!` message.
- Reward entry values now act as relative selection weights inside the loot pool.
- The reward hit chance, weighted reward settings and empty-pockets message are editable through `/mpinata`.
- Top-hitter bonus selection now uses the same weighted reward pool.

MiraPinata is a GUI-driven server boss event for the Mira Paper server suite. It spawns a configurable fighting Zombie Pinata that the server attacks for per-hit rewards, top-hitter bonuses and a final-kill celebration.

## Dependencies and installation

- Paper 1.18.2 or newer (compiled against the 1.18.2 API; newer versions still need runtime verification)
- Java 17 or newer
- CI compile/build passed against Paper 1.18.2 using Java 17 bytecode; see the [build run](https://github.com/FiveSOCE/Mira-Pinata/actions/runs/38015938572)
- No MiraCore dependency
- PlaceholderAPI optional (leaderboard placeholders)
- MiraNPC optional integration
- MiraCosmetics optional integration

## How MiraPinata Works

Administrators configure the event almost entirely through the `/mpinata` GUI: fixed spawn location, schedule/countdown, boss name/variants, gear, attack damage, Knockback, hit-health mode, real-hit charge threshold, exact reward ItemStacks, weighted reward selection, per-hit reward chance, top-hitter bonus, chat messages and random effects.

When the event starts, MiraPinata broadcasts the configured countdown and spawns a persistent Zombie at the saved location. Automatic hit-health scaling can size the event based on online player count, or administrators can switch to a fixed manual hit total. Melee attacks only count when enough of the player's real attack-speed cooldown has elapsed, preventing client spam-clicking from rapidly consuming event health or rolling rewards.

Each accepted hit removes one event hit. When per-hit loot is enabled, the hit first rolls the configured global reward chance. By default there is a 70% chance to receive a reward and a 30% chance to receive nothing. A successful reward roll selects exactly one configured reward using the reward entries as relative weights. Exact ItemStack metadata is preserved. Failed rolls send the hitter the configurable empty-pockets message using `%name%` for the active Pinata variant.

A boss bar tracks remaining event health, random effects such as Speed, baby mode and invisibility can fire during the fight, and the Zombie remains protected from environmental damage/daylight. The final hitter and top hitter can be announced/rewarded, followed by a staggered firework finale.

Current source also tracks event statistics/leaderboard data and supports PlaceholderAPI/MiraNPC display integrations. Common message placeholders include `%seconds%`, `%name%`, `%player%` and `%hits%`.

## Mira event/voucher integration (0.1.6)

MiraPinata exposes its live `PinataManager` through the plugin entrypoint for first-party integrations. MiraItems can safely reject a Pinata Call voucher while the event is already active/counting down, while MiraEvents can start a Mega Pinata through the normal manager lifecycle.

## Commands

| Command | Permission | What it does |
| --- | --- | --- |
| `/mpinata` | `mirapinata.admin` | Opens the MiraPinata administration/control GUI. All normal event configuration, start/stop and editing tools are GUI-driven from this interface. |

## Permissions

| Permission | Default | What it does |
| --- | --- | --- |
| `mirapinata.admin` | OP | Allows access to the MiraPinata administrator GUI and event-management controls. |

## MiraCosmetics Integration (0.1.5)

Moves Pinata spawn, hit, low-health and defeat presentation into MiraCosmetics, removing the old hard-coded hit particle/sound presentation.
