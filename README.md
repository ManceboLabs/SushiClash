# Sushi Clash

A native Android sushi counter for solo play or group nights out. Built with Jetpack Compose by [Mancebo Labs](https://github.com/ManceboLabs).

Count sushi pieces, spin the roulette, track history, and unlock achievements — all on device, with no account required.

---

## Features

- **Solo and Group modes** — one counter for yourself, or up to six named players around the table
- **Tap-to-count sushi buttons** — long-press to reset an individual counter during a game
- **Roulette wheel** — pick a random participant; add or remove names while a group game is active
- **Random roulette triggers** — optional automatic spins on a fixed threshold or progressive random target
- **Game history** — saved solo and group results with rankings
- **Achievements** — 35 unlockable goals based on gameplay, sushi totals, and roulette use
- **Frequent players** — quick name suggestions when starting a new group game
- **Onboarding tutorial** — first-launch walkthrough, replayable from Settings
- **Appearance and feedback** — light/dark theme, sound and haptic feedback toggles
- **App language** — per-app locale override or follow the system language
- **Floating bottom navigation** — Counter, History, Achievements, and Settings

---

## Screenshots

Screenshots are not included yet. When ready, add PNG files under `docs/screenshots/` using the names below, then uncomment the gallery in this section.

| File | Suggested capture |
|------|-------------------|
| `docs/screenshots/counter-solo.png` | Solo counter during an active game |
| `docs/screenshots/counter-group.png` | Group layout with multiple players |
| `docs/screenshots/roulette.png` | Roulette wheel screen |
| `docs/screenshots/history.png` | History rankings |
| `docs/screenshots/achievements.png` | Achievements list |
| `docs/screenshots/settings.png` | Settings screen (theme / language) |

<!--
Gallery (uncomment after adding the PNG files above):

<p align="center">
  <img src="docs/screenshots/counter-solo.png" width="200" alt="Solo counter" />
  <img src="docs/screenshots/counter-group.png" width="200" alt="Group counter" />
  <img src="docs/screenshots/roulette.png" width="200" alt="Roulette" />
</p>
<p align="center">
  <img src="docs/screenshots/history.png" width="200" alt="History" />
  <img src="docs/screenshots/achievements.png" width="200" alt="Achievements" />
  <img src="docs/screenshots/settings.png" width="200" alt="Settings" />
</p>
-->

---

## Tech stack

| Area | Choices |
|------|---------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | Unidirectional data flow with ViewModels and repository interfaces |
| Async / state | Kotlin Coroutines, `StateFlow` |
| Navigation | Navigation Compose |
| Persistence | Preferences DataStore |
| Testing | JUnit, MockK, Turbine, Compose UI tests |
| Build | Android Gradle Plugin 9, Gradle 9, R8 on release |
| Min / target SDK | 24 / 37 |

---

## Architecture

The codebase is organized by responsibility:

- **`feature/`** — Compose screens, UI state, and ViewModels
- **`domain/`** — models, business rules, and repository contracts
- **`data/`** — DataStore access and repository implementations
- **`navigation/`** — app navigation graph and routes
- **`ui/`** — shared theme, spacing, and reusable components
- **`di/`** — manual dependency wiring via `AppContainer`

Game rules, validation, roulette logic, and achievement evaluation live in the domain layer. Composables focus on rendering state and forwarding user actions.

### Persistence

All user data is stored locally:

- A single **Preferences DataStore** file holds active game state, history, achievements, frequent players, and settings
- **AppCompat per-app locales** store the language override
- Complex structures (players, history, achievements) are serialized as JSON inside DataStore preferences
- **Android backup** is enabled with explicit include rules; an install marker excludes stale in-progress games after restore

There are no backend services, accounts, or analytics SDKs.

---

## Testing and CI

| Layer | Coverage |
|-------|----------|
| Unit tests | 255 tests — ViewModels, repositories, persistence, domain logic |
| Instrumented tests | 7 smoke tests — onboarding, counter/finish flow, settings, history (API 24+, run locally with an emulator) |
| CI | GitHub Actions on `main` and pull requests — unit tests, lint, debug build |

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Instrumented tests (device or emulator required):

```bash
./gradlew connectedDebugAndroidTest
```

---

## Supported languages

English, Spanish, German, French, Simplified Chinese, and Japanese.

Strings live under `app/src/main/res/values*` with per-app locale support via `locales_config.xml`.

---

## Privacy

Gameplay data (history, player names, achievements, and settings) is stored **on your device only**. Mancebo Labs does not collect or operate servers for this app.

Optional Android backup may copy allowed app data to your Google or device backup, managed by the operating system.

Full details: [Privacy Policy](https://mancebolabs.github.io/SushiClash/privacy-policy/)

---

## Requirements

- **JDK 17** (matches CI)
- **Android SDK** with API 37 platform and Build Tools 36.0.0
- Android Studio or compatible IDE recommended

---

## Build

Clone the repository, then from the project root:

```bash
./gradlew assembleDebug
```

Install the debug APK on a connected device or emulator:

```bash
./gradlew installDebug
```

Release builds use R8 minification and require upload signing configuration. See `keystore.properties.example` for the expected local layout — credentials are not part of the repository.

---

## Project status

**Version 1.0** — actively maintained portfolio project preparing for Google Play release.

| Item | Status |
|------|--------|
| Core gameplay | Complete |
| Localization (6 languages) | Complete |
| Unit and instrumented tests | Complete |
| CI (unit tests, lint, debug build) | Complete |
| Release signing and R8 | Configured locally |
| Play Store listing | In progress |

---

## License

Copyright © 2026 Mancebo Labs. All rights reserved.

This repository is publicly available for portfolio and educational viewing purposes only.

No permission is granted to copy, modify, distribute, sublicense, or use this source code in other projects without prior written permission from the copyright holder.

---

## Related documentation

- [Privacy Policy (source)](PRIVACY_POLICY.md)
- [Play Data Safety checklist](docs/PLAY_DATA_SAFETY.md)
- [Agent and contribution guidelines](AGENTS.md)
