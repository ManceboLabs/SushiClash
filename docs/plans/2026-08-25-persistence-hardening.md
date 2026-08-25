# Persistence Hardening Implementation Plan

> **For agentic workers:** Use `mobiai-mobile-executing-plans-with-subagents` to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Make persisted reads and writes explicit, recoverable, and testable while preserving the existing MVVM/repository architecture and H4/M1 safety guarantees.

**Architecture:** Inject the existing Preferences `DataStore` into `AppPreferencesDataStore` so production keeps the Context-backed store while JVM tests use a temporary real store. Represent reads as a small typed state (`Missing`, `Data`, `Corrupted`, `Unavailable`), map only documented theme/onboarding defaults at repository boundaries, and expose recoverable UI flags only for history, roulette participants, settings writes, and counter startup/actions. Active-game validation and recovery remain centralized in the existing atomic H4 restoration transaction.

**Tech Stack:** Kotlin, Preferences DataStore, Coroutines/Flow, Jetpack ViewModel, Compose, JUnit, MockK, Turbine, test-only `org.json`.

**Platform:** Android

---

### Task 1: Make AppPreferencesDataStore injectable and define explicit read states

**Files:**
- Create: `app/src/main/java/com/mancebolabs/sushiclash/domain/model/PersistenceReadState.kt`
- Create: `app/src/main/java/com/mancebolabs/sushiclash/data/datastore/PersistenceLogger.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/data/datastore/AppPreferencesDataStore.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/di/AppContainer.kt`
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Test: `app/src/test/java/com/mancebolabs/sushiclash/data/datastore/AppPreferencesDataStoreTest.kt`

- [ ] Write a failing JVM test that creates a temporary real Preferences DataStore and constructs `AppPreferencesDataStore` with it.
- [ ] Run `./gradlew testDebugUnitTest --tests "com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStoreTest"` and verify the constructor/test support is missing.
- [ ] Add injected `DataStore<Preferences>` and logger dependencies, retaining a Context constructor/factory for production.
- [ ] Add `PersistenceReadState.Missing`, `Data<T>`, `Corrupted`, and `Unavailable`; no state carries a raw exception.
- [ ] Add a reusable flow boundary that logs only operation metadata, converts `IOException` to `Unavailable`, retries subsequent upstream reads, and rethrows unexpected exceptions.
- [ ] Add test-only `org.json` so real JSON codecs execute in JVM tests without a new production dependency.
- [ ] Run the focused test and verify it passes.

### Task 2: Harden active-game restoration and round-trip behavior

**Files:**
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/data/datastore/AppPreferencesDataStore.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/domain/model/FinishGameResult.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/domain/repository/GameRepository.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/data/repository/GameRepositoryImpl.kt`
- Test: `app/src/test/java/com/mancebolabs/sushiclash/data/datastore/AppPreferencesDataStoreTest.kt`
- Test: `app/src/test/java/com/mancebolabs/sushiclash/game/GameRepositoryImplTest.kt`

- [ ] Write failing real-DataStore tests for valid Solo/Group round trips, malformed players JSON, invalid game/roulette enums, legacy session migration, and active recovery preserving history/theme/onboarding.
- [ ] Write a failing read-I/O test proving `Unavailable` is distinct from missing data and the flow can later recover.
- [ ] Run focused tests and verify expected failures.
- [ ] Reuse `GameStateValidator` inside the existing atomic `edit`: corrupted/invalid active state clears only active keys; missing state remains legitimate `NoActiveGame`; I/O returns a recoverable restore result without clearing unknown data.
- [ ] Keep M1 finish atomic and return failure for corrupt active/history state or write I/O.
- [ ] Add non-sensitive logging for decode/read/write failures; do not log serialized payloads or player data.
- [ ] Run focused tests and H1/H4/M1 regression tests.

### Task 3: Make history and participant corruption explicit

**Files:**
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/domain/repository/HistoryRepository.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/domain/repository/ParticipantsRepository.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/data/repository/HistoryRepositoryImpl.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/data/repository/ParticipantsRepositoryImpl.kt`
- Modify: `app/src/test/java/com/mancebolabs/sushiclash/testutil/FakeHistoryRepository.kt`
- Modify: `app/src/test/java/com/mancebolabs/sushiclash/testutil/FakeParticipantsRepository.kt`
- Test: `app/src/test/java/com/mancebolabs/sushiclash/data/datastore/AppPreferencesDataStoreTest.kt`
- Test: `app/src/test/java/com/mancebolabs/sushiclash/game/ParticipantsRepositoryImplTest.kt`

- [ ] Write failing tests proving absent history/participants are `Missing`, valid empty JSON is `Data(emptyList())`, malformed JSON is `Corrupted`, and read I/O is `Unavailable`.
- [ ] Write a failing test proving corrupted participants are not overwritten by automatic seeding; only deliberate user updates may replace them.
- [ ] Run focused tests and verify expected failures.
- [ ] Change decoders and repository flows to preserve all four states instead of returning `emptyList()`.
- [ ] Guard automatic participant seeding against `Corrupted` and `Unavailable`; propagate write failures without reporting success.
- [ ] Run focused tests and verify they pass.

### Task 4: Apply documented defaults for theme/onboarding and surface meaningful UI errors

**Files:**
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/data/datastore/AppPreferencesDataStore.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/data/repository/ThemeRepositoryImpl.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/data/repository/OnboardingRepositoryImpl.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/feature/counter/AppStartupState.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/feature/counter/CounterViewModel.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/feature/counter/CounterScreen.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/feature/history/HistoryViewModel.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/feature/history/HistoryScreen.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/feature/settings/SettingsViewModel.kt`
- Modify: `app/src/main/java/com/mancebolabs/sushiclash/feature/wheel/WheelViewModel.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/com/mancebolabs/sushiclash/counter/CounterViewModelTest.kt`
- Test: `app/src/test/java/com/mancebolabs/sushiclash/history/HistoryViewModelTest.kt`
- Test: `app/src/test/java/com/mancebolabs/sushiclash/settings/SettingsViewModelTest.kt`
- Test: `app/src/test/java/com/mancebolabs/sushiclash/wheel/WheelViewModelTest.kt`

- [ ] Write failing tests for invalid theme fallback to Light, onboarding read failure without startup loop, counter restore/write failure without false success, corrupted history UI error, participant load/write errors, theme write failure, and history-clear write failure.
- [ ] Run each ViewModel test class and verify expected failures.
- [ ] Keep theme/onboarding defaults simple at repository boundaries, while logging corruption/I/O in data; prevent onboarding from waiting forever after a recoverable read failure.
- [ ] Add minimal boolean persistence-error/loading states and retry actions only where users can act; use one centralized generic Spanish message and never expose exception text.
- [ ] Catch only expected persistence failures in ViewModels; cancellation and unexpected programming errors continue to propagate.
- [ ] Update relevant Compose previews for new error states without redesigning screens.
- [ ] Run focused tests and verify they pass.

### Task 5: Real DataStore concurrency and full verification

**Files:**
- Test: `app/src/test/java/com/mancebolabs/sushiclash/data/datastore/AppPreferencesDataStoreTest.kt`
- Test: `app/src/test/java/com/mancebolabs/sushiclash/game/GameRepositoryImplTest.kt`

- [ ] Write failing real-store concurrency tests for increments to the same/different players, concurrent finish retries, and restore racing with creation.
- [ ] Run focused tests and verify the tests expose any remaining non-atomic path.
- [ ] Make only the minimum persistence-boundary correction required; reuse existing DataStore transactions and H1 mutex strategy.
- [ ] Run `./gradlew testDebugUnitTest --tests "com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStoreTest" --rerun-tasks`.
- [ ] Run `./gradlew testDebugUnitTest lintDebug assembleDebug --rerun-tasks`.
- [ ] Run `git diff --check`, review the complete diff, and search for obsolete silent-default APIs.
- [ ] Do not commit or push.

## Scope boundaries

- No Room migration, backup policy, release/signing work, dark-mode changes, accessibility redesign, or unrelated audit fixes.
- No full logging framework; use a tiny injectable logger backed by Android logging.
- No raw exceptions, JSON, history payloads, or player data reach logs or UI.
- File-level DataStore corruption/migration infrastructure beyond Preferences DataStore behavior remains deferred unless a real test proves it is required.
