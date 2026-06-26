New Better Combat
=================

A NeoForge **26.1.2 / 26.2** compatible port (dev runtime neo 26.2.0.7-beta, Java 25) of
[Better Combat Rebirth](https://github.com/SanAndreaP/BetterCombatRebirth) by SanAndreasP
(originally MC 1.12.2 / Forge). Mod id: `neobettercombat`.

Controls
--------
- **Left click** – main-hand attack (with the longer/wider reach options applied).
- **Right click** – attack with a whitelisted off-hand weapon (sword, axe, pickaxe, shovel, hoe, …).

Because the off-hand hit clears the target's *post-hit invulnerability*, both hands can damage the
same enemy in the same tick — each weapon deals its own damage.

Configurable features
---------------------
All options live in `config/neobettercombat-common.toml` (also editable in the in-game mod config UI):

| Option | Default | Effect |
| --- | --- | --- |
| `refundEnergyOnMiss` | on | Missed melee attacks don't trigger the weapon cooldown. |
| `longerAttack` | **off** | Melee attacks gain +1 block of reach. |
| `widerAttack` / `widerAttackWidth` | on / 1.5 | Melee attacks hit in a wider area (aim assist). |
| `randomCrits` / `randomCritChance` | on / 0.3 | 30% chance to randomly crit; crits cannot be blocked. |
| `requireFullEnergy` | off | You may only attack when the attack bar is full. |
| `weakerOffhand` / `offhandEfficiency` | on / 0.5 | Off-hand attacks deal 50% damage. |
| `attackAndSprint` | on | Attacking while sprinting no longer interrupts the sprint. |
| `moreSweep` | **off** | Every item can sweep (and deal sweep damage). When off, only sword-tagged weapons sweep. |
| `additionalHitSound` / `additionalCritSound` | on / on | Extra sounds on hit / crit. |
| `enableOffhandAttack` | on | Master toggle for off-hand attacks. |
| `[offhand]` | swords/axes/pickaxes/shovels/hoes on | Category toggles for off-hand-usable items + an `extraWhitelist` list (`#tag` or `id`). |
| `[mainhandBlacklist]` | bow/consume/block/trident on | Toggles for main-hand actions that disable off-hand attacks + an `extraBlacklist` list. |
| `[entityBlacklist]` | armor stands / villagers / owned pets on | Toggles for protected entities + an `extraBlacklist` list. |

The whitelist/blacklist are organised as sub-sections in the in-game config screen: common cases are
on/off toggles, and the `extra*` lists accept custom `#tag` / `id` (and `anim:<use_animation>` for the
main hand) entries with live red/green validation.

Implementation notes / differences from the 1.12.2 original
-----------------------------------------------------------
The 1.12.2 codebase was rewritten against modern NeoForge APIs:
- Capabilities → **data attachments**; `SimpleNetworkWrapper` → **custom payloads** (`StreamCodec`).
- The whitelist/blacklist is **tag/id-based** instead of reflective class names.
- Main-hand attacks are routed to the server which runs the vanilla `Player.attack`, so crits,
  enchantments, sweep and sounds are layered on via NeoForge events (`CriticalHitEvent`,
  `SweepAttackEvent`, `LivingDamageEvent`).
- The extra hit/crit sounds alias vanilla sound events (no extra `.ogg` files shipped).
- The config is fully editable in-game (Mods → New Better Combat → Config), with English and
  Chinese (`zh_cn`) translations.
- Off-hand attacks **sweep** (when `moreSweep` is on or a sword-tagged weapon is held) and
  **consume off-hand weapon durability**.
- Random crits are **unblockable**: a crit disables a shield that is currently blocking.
- The vanilla sweep particle is used for sweep attacks. An **off-hand cooldown indicator** is drawn
  next to the crosshair attack indicator.

Building
--------
`./gradlew build` (Java 25 toolchain). The mod jar is written to `build/libs/`.
