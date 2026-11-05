# BetterThanHerobrine — CLASSIC (0.5.0-beta.1)

An experimental Minecraft horror mod built around the older Herobrine legends.
It stays close to vanilla Minecraft: no forced "joined the game" message,
no lightning spam, no jumpscare overlays. A distant silhouette or a sound
in an empty cave should be unusual enough to remember.

**Development branch:** classic-rework (based on haunting-0.4.0-beta.1).
This is NOT an approved Modrinth release. CI checks compilation and deterministic
simulation tests; Minecraft client/server runtime behavior still needs testing.

### What this implementation actually does

- Original Herobrine entity with a separate model/texture. Observes from a distance
  and can vanish after the player notices it and looks away.
- 12 contextual, bounded scenes: distant figures, tree-line and cave sightings,
  unusual footsteps, cave echoes, sounds near villages, and a window encounter.
- Long randomized quiet periods (4–12 minutes after a completed scene) with a
  separately randomized initial delay. The first seconds are intentionally normal.
- No forced chat introduction, no ambient channel suppression, no automatic
  fog/tint/distortion scenes and no damaging edits to the world.
- Server-authorized F8 debug UI and event testing commands. The old "chaos"
  action now triggers a harmless distant sighting.
- EN/RU localization, local client preferences, persisted event scheduling.

**Not implemented:** permanent or temporary tunnels, missing leaves, physical
block crosses, a full environmental investigation system. The rare unknown_symbol
event is only a short-lived ash-particle illusion; it does not place blocks.
We prefer telling the truth over promising mechanics that are not in the JAR.

### Platform

Minecraft Java **26.3** / Fabric Loader **0.20.1+** /
Fabric API **0.165.0+26.3** / Java **25**.

Build: ./gradlew clean build (Java 25 + network access).
The GitHub workflow "Build BetterThanHerobrine JAR" runs this command,
independent Director tests and asset integrity checks on classic-* pushes.
The workflow artifact contains a JAR inside a ZIP; install the JAR in Prism
and verify it in a **copy of a survival world** before release.

### In game

- **H**: client settings (choose Subtle, Classic, or Frequent).
- **F8**: server-authorized developer panel; access is restricted to the
  zaanw username AND server gamemaster/operator permission.
- /bth status: basic status.
- /bth event distant_figure: manually trigger a sighting, operator only.
- /bth admin watcher: manually spawn Herobrine, authorized developer only.
- /bth admin clear: stop events, authorized developer only.

Server and client must both have this mod and Fabric API installed.
Settings are stored in config/betterthanherobrine-*.properties; old saved
settings are preserved, so use the **Classic** preset if you previously selected
Nightmare. The mod does not contact any external server or collect user data.

[Manual test checklist](docs/CLASSIC-VALIDATION.md) ·
[0.4 development notes](docs/HAUNTING-VALIDATION.md)

Created by **zaan**.
