# BetterThanHerobrine — Classic Sightings 0.5.1-beta.1

Work-in-progress for Minecraft Java 26.3 / Fabric. Developed from the
Classic 0.5 branch; the original main and 0.4 branches remain intact.

This build fixes the main weakness of Classic: a scene could spawn an observer
behind solid terrain and still count as a real encounter.

- Short-lived Herobrine sightings now search only **already-loaded** ground,
  prefer the player's forward arc, and require a clear raycast.
- The second eligible encounter favors a Herobrine sighting when the player
  hasn't received one yet, subject to contextual conditions and availability.
- Both the search and first-sighting scheduling have deterministic tests.
- The old F8 Spawn button attempts a flat-direction, 24-block sighting,
  and reports when no suitable position is found instead of silently failing.
- No terrain edits, structure damage, forced join messages or lightning spam.
- The server persists sighting attempts and enforces admin permissions.

**Limitations:** A scheduled Herobrine scene can still fail to spawn if the
world has no safe, loaded, visible ground. Its count tracks *attempts*, not
confirmed visible sightings. No gameplay screenshots or runtime tests have
been made. Real-world terrain edits and physical crosses are not implemented.

Gameplay behavior and compatibility require manual testing after a successful
CI build. Use a copy of a world for initial testing.

- H: local settings (Subtle, Classic, Frequent)
- F8: authorized admin panel for username zaanw **and** operator permission
- /bth event distant_figure: operator-triggered test
- /bth admin watcher: debug spawn
- /bth admin clear: stop all events

For previous details: [Classic documentation](docs/CLASSIC-VALIDATION.md).
