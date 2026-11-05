# CHANGELOG

## 0.5.1-beta.1 — CLASSIC SIGHTINGS (experimental)

- More reliable observer placement with a clear player-visible raycast and
  wider ground search limited to currently loaded chunks.
- First observer scheduling no longer gets lost behind endless ambience-only
  events; sighting attempts are remembered across restarts.
- Fixed F8 debug spawn direction when looking up or down; failure is reported.
- Added pure geometry and scheduling regression checks. CI completion and
  Minecraft runtime verification are tracked separately.
- No changes to the production main or previous release branches.

# CHANGELOG

## 0.5.0-beta.1 — CLASSIC REWORK (development branch; runtime not yet verified)

- Replaced 30 noisy scenes with 12 curated classic encounters; no scripted
  join message, forced fog, tint, distortion, music suppression or lightning.
- Added a quiet first 100–240 seconds and randomized 4–12 minute cooldowns
  between scenes; high values in older Nightmare config cannot override pacing.
- Reduced intensity of cue sounds and made sightings distant and contextual.
  Watchers vanish after being noticed and then looked away from.
- Replaced four overblown presets with Subtle, Classic and Frequent; tuned the
  default volume and grace period without overwriting existing user config.
- Legacy admin "chaos" invokes a single distant sighting instead of an
  unavailable Nightmare scene. F8 server-side access checks stay in place.
- Added deterministic first-session and non-spam checks; preserved previous
  permission, persistence, scene-cap and cleanup regression checks.
- 0.4 remains untouched on haunting-0.4.0-beta.1; main is untouched.
- **CI and in-game tests must be checked before claiming this version works.**

# CHANGELOG

## 0.4.0-beta.1 — THE HAUNTING (CI-сборка пройдена, игровые тесты ожидаются)

- Сохранены и расширены существующие системы: 18 → 30 исполняемых сцен, контекст деревень, окон и добычи блоков.
- Добавлены Herobrine EntityType, отдельная модель/текстуры и режимы наблюдения, исчезновения, подхода и неправильного движения Doppelganger.
- Бесконечный цикл молний заменён однократным вступлением с локализованным JOIN и дальнейшим выбором событий Director.
- Восстановлены все четыре пресета; загрузка конфига больше не перезаписывает пользовательские настройки Nightmare-профилем.
- Типизированная панель F8: 10 функций, проверка ника zaanw и серверных прав на каждом запросе, лимит запросов, серверный debug overlay.
- `/bth admin watcher` и `/bth admin chaos` направлены в общий обработчик; chaos теперь конечная сцена.
- 16 воспроизводимых оригинальных OGG, исправлен старт с нулевой громкостью, добавлены звук и шорох; mixer восстанавливает приглушённые каналы без записи options.
- Добавлены reduced visuals, частицы и слабые полосы экранного искажения без вспышек.
- Расширены независимые проверки, clean build CI, ручные предохранители публикации Modrinth и черновик описания.
- Первые три клиентские ошибки API 26.3 исправлены. [GitHub Actions clean build прошла](https://github.com/zaanw/BetterThanHerobrine/actions/runs/41894570463) для коммита 119f3b7 и создала JAR-артефакт. Локальная сеть всё ещё блокирует Maven/API GitHub; игровые проверки и публикация не выполнялись.

## 0.1.0 — исходники первой итерации, 2026-11-04

- Добавлены серверный Horror Director и сохраняемая история игроков.
- Реализованы 18 ограниченных по времени сценариев в ядре; все стадии проверены независимо от Minecraft.
- Написаны наблюдатель, AI, размещение без изменения блоков, команды, сетевые payload и клиентское меню.
- Созданы оригинальная текстура, 12 процедурных звуков, EN/RU локализация.
- Добавлены Gradle Wrapper с проверкой SHA-256, сценарий облачной установки и проверка ресурсов.
- Полная сборка заблокирована доступом к Fabric Maven. Игровая интеграция не скомпилирована, JAR не выпущен, запуск не проверен.
