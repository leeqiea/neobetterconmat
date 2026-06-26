# New Better Combat

**New Better Combat** is an independent NeoForge combat mod for **Minecraft / NeoForge 26.1.2 and 26.2**, built for modern NeoForge APIs and Java 25.

It adds configurable melee combat improvements, including enhanced main-hand attacks, off-hand weapon attacks, wider melee hit detection, optional longer reach, random critical hits, sprint-friendly attacking, sweep attack options, extra combat sounds, and an in-game configuration screen.

This project is inspired by [Better Combat Rebirth](https://github.com/SanAndreaP/BetterCombatRebirth) by SanAndreasP, but it is an independent implementation. It is not a fork, not an official update, and not affiliated with the original author.

Mod id: `neobettercombat`

---

## Features

* Enhanced main-hand melee attacks
* Right-click off-hand weapon attacks
* Configurable off-hand weapon whitelist
* Optional longer melee reach
* Optional wider melee hit detection
* Optional random critical hits
* Optional full-energy attack requirement
* Configurable off-hand damage multiplier
* Attacking while sprinting no longer interrupts sprinting
* Optional sweep attacks for more items
* Extra hit and critical hit sounds
* Protected entity blacklist
* Main-hand action blacklist for off-hand attack prevention
* In-game configuration screen
* English and Simplified Chinese translations
* Off-hand cooldown indicator next to the crosshair attack indicator

---

## Controls

* **Left click** — main-hand attack.
* **Right click** — off-hand attack with a whitelisted weapon.

By default, off-hand attacks can be enabled for common tool and weapon categories such as swords, axes, pickaxes, shovels, and hoes.

Off-hand attacks use their own weapon damage and can be configured separately from main-hand attacks.

---

## Important Multiplayer Note

This mod changes combat behavior.

For multiplayer, **install this mod on both the client and the server**. Server owners should make sure that all players use the same mod version and that the modified combat behavior is intentionally enabled.

---

## Configuration

All options are stored in:

```text
config/neobettercombat-common.toml
```

The config can also be edited in-game through:

```text
Mods -> New Better Combat -> Config
```

---

## Configurable Options

| Option                                       | Default                                      | Effect                                                                                           |
| -------------------------------------------- | -------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| `refundEnergyOnMiss`                         | on                                           | Missed melee attacks do not trigger the weapon cooldown.                                         |
| `longerAttack`                               | off                                          | Melee attacks gain +1 block of reach.                                                            |
| `widerAttack` / `widerAttackWidth`           | on / 1.5                                     | Melee attacks use wider hit detection for smoother close-range combat.                           |
| `randomCrits` / `randomCritChance`           | on / 0.3                                     | 30% chance to randomly crit. Random crits cannot be blocked.                                     |
| `requireFullEnergy`                          | off                                          | Attacks are only allowed when the attack bar is full.                                            |
| `weakerOffhand` / `offhandEfficiency`        | on / 0.5                                     | Off-hand attacks deal 50% damage.                                                                |
| `attackAndSprint`                            | on                                           | Attacking while sprinting no longer interrupts sprinting.                                        |
| `moreSweep`                                  | off                                          | Allows more items to perform sweep attacks. When disabled, only sword-tagged weapons can sweep.  |
| `additionalHitSound` / `additionalCritSound` | on / on                                      | Plays extra sounds on hit and crit.                                                              |
| `enableOffhandAttack`                        | on                                           | Master toggle for off-hand attacks.                                                              |
| `[offhand]`                                  | swords / axes / pickaxes / shovels / hoes on | Category toggles for items that can be used for off-hand attacks, plus an `extraWhitelist` list. |
| `[mainhandBlacklist]`                        | bow / consume / block / trident on           | Toggles for main-hand actions that disable off-hand attacks, plus an `extraBlacklist` list.      |
| `[entityBlacklist]`                          | armor stands / villagers / owned pets on     | Toggles for protected entities, plus an `extraBlacklist` list.                                   |

---

## Whitelist and Blacklist Entries

The off-hand whitelist, main-hand blacklist, and entity blacklist are organized as sub-sections in the in-game config screen.

Common cases are controlled through simple on/off toggles.

The `extraWhitelist` and `extraBlacklist` lists support custom entries such as:

```text
#tag
namespace:item_id
namespace:entity_type
anim:<use_animation>
```

Examples:

```text
#minecraft:swords
minecraft:diamond_sword
minecraft:villager
anim:bow
```

The in-game config screen provides live red/green validation for custom entries.

---


## Differences from Better Combat Rebirth

This project is inspired by Better Combat Rebirth, but it is not a direct source-code fork.

The implementation has been rewritten for modern NeoForge and uses current APIs, modern config handling, tag/id-based matching, modern networking, and updated client-side UI behavior.

No original source code or original assets from Better Combat Rebirth are required for this project.

---

## Compatibility

| Item                         | Requirement              |
| ---------------------------- | ------------------------ |
| Loader                       | NeoForge                 |
| Minecraft / NeoForge version | 26.1.2 / 26.2            |
| Development runtime          | NeoForge 26.2.0.7-beta   |
| Java                         | Java 25                  |
| Client                       | Required                 |
| Server                       | Required for multiplayer |

---

## Building

Use the Java 25 toolchain.

```bash
./gradlew build
```

The compiled mod jar will be generated in:

```text
build/libs/
```

---

## Credits

Inspired by [Better Combat Rebirth](https://github.com/SanAndreaP/BetterCombatRebirth) by SanAndreasP.

New Better Combat implementation by leeqia.

This project is unofficial and is not affiliated with or endorsed by SanAndreasP, Mojang, Microsoft, or NeoForge.

---

## License

This project is licensed under the MIT License.

See the `LICENSE` file for details.
