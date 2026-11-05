# CLASSIC: validation checklist

Branch: classic-rework; intended version: 0.5.0-beta.1.
Unlike CI, this checklist requires a real Minecraft 26.3 client.

## CI checks

- [ ] Java 25: ./tools/test-core.sh (scene duration, no forced JOIN, quiet
  first minute, eventual flat-world encounter, long-duration bounds,
  persistence/config, permissions, admin stop, disabled state).
- [ ] Decode every OGG, verify locale and textures: python3 tools/check-assets.py.
- [ ] Gradle clean build and JAR artifact from GitHub Actions.

## In-game checks — use a copied survival world

- [ ] 3 minutes of normal daylight: no startup join spam or red overlays.
- [ ] Leave the player exploring normally for at least 15 minutes to assess
  subtle sound cues and observer frequency; 20 minutes of quiet can happen.
- [ ] F8 under a legitimate zaanw operator account opens and functions.
- [ ] Other players cannot use administrator functions.
- [ ] /bth event distant_figure: model renders, stands on solid ground,
  visible from distance and disappears after noticed and looked away from.
- [ ] /bth admin watcher and /bth admin clear really work.
- [ ] Cave, forest, night, village, window, and superflat positioning.
- [ ] EN/RU subtitles; sound volume and accessibility settings.
- [ ] Join, death, dimension change and server stop correctly clean up.
- [ ] Two players on a Fabric server: no cross-player debug access.
- [ ] No corrupted worlds, loading of distant chunks or performance spikes.
- [ ] Client renderer and audio mixins do not crash the game on 26.3.

## Honest known limitations

- Branch changes are verified by automated tests only when CI passes;
  test success does **not** prove the renderer works at runtime.
- No physical world changes, tunnel generation, doors moved, leaf removal,
  block crosses or actual Minecraft world griefing. The unknown_symbol
  scene draws only temporary ash particles if enabled.
- Presets stored in older configs are not deleted; choose Classic explicitly
  in H settings to overwrite previous experimental intensity values.
- A successful CI artifact is a ZIP containing a JAR, not a JAR itself.
