# Automated Store/README Screenshot Generation Plan

> **For agentic workers:** Use `mobiai-mobile-executing-plans-with-subagents` (recommended) or `mobiai-mobile-executing-plans` to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Generate deterministic, real-Compose PNG screenshots of Sushi Clash for the GitHub README and future Play Store assets, in all six supported languages, without changing production behavior.

**Architecture:** Dedicated instrumented suite (`StoreScreenshotTest`) reuses existing `AppContainerTestOverrides`, Compose helpers, and `clearPackageData`. Captures via `UiDevice.takeScreenshot` into app external cache, then a small shell script pulls files into `docs/screenshots/<locale>/`. Random chef events are suppressed by disabling the persisted preference and using a deterministic chef `RandomProvider` with a high first target.

**Tech Stack:** Existing Compose UI Test + Espresso/UiAutomator (`UiDevice`), AppCompat per-app locales, Gradle instrumented filter, bash pull script.

**Platform:** Android

---

## File map

| Action | Path |
|--------|------|
| Create | `app/src/androidTest/.../screenshots/StoreScreenshotTest.kt` |
| Create | `app/src/androidTest/.../screenshots/ScreenshotCapture.kt` |
| Create | `app/src/androidTest/.../screenshots/ScreenshotDemoState.kt` |
| Create | `scripts/generate-screenshots.sh` |
| Create/Modify | `docs/screenshots/README.md` (generation instructions) |
| Modify | `README.md` (gallery only, after EN PNGs exist) |
| Optional | `DEVELOPMENT_NOTES.md` short pointer to screenshot docs |

No production `app/src/main` behavior changes unless a tiny test-only hook is unavoidable (prefer none).

---

## Screenshot set (7)

| File | State |
|------|--------|
| `01_onboarding.png` | Welcome step (chef greeting GIF + speech bubble); nav hidden |
| `02_solo.png` | Active Solo game, count ≈ 12, no overlays/dialogs |
| `03_group.png` | Active Group, 4 players, varied counts, short locale-friendly names |
| `04_roulette.png` | Wheel with ≥4 participants, idle (not spinning) |
| `05_achievements.png` | Achievements screen after unlocking first game + some sushi progress |
| `06_history.png` | History with at least one Solo + one Group saved entry |
| `07_settings.png` | Settings scrolled to top; Light theme; random animations off is OK for capture prep but prefer showing toggles in default-looking state for the settings shot |

Locales → directories: `en`, `es`, `de`, `fr`, `zh-CN`, `ja` (match `AppLanguage.languageTag`).

---

### Task 1: Capture helpers

**Files:**
- Create: `ScreenshotCapture.kt`
- Create: `ScreenshotDemoState.kt`

- [x] **Step 1:** Helper that waits for Compose idle, settles 300–500ms, then `UiDevice.takeScreenshot(File)` to `context.getExternalFilesDir("screenshots")/<locale>/<name>.png`
- [x] **Step 2:** Demo names per locale (short ASCII where possible; CJK where natural)
- [x] **Step 3:** Apply locale via `AppContainer.languageRepository().setAppLanguage(...)` + activity recreate + wait for translated string
- [x] **Step 4:** Suppress random chef: `setRandomChefAnimationsEnabled(false)` + `chefRandomProvider = FakeRandomProvider` with large initial target; `completeGifCyclesImmediately = true` for celebrations dismissal only

---

### Task 2: StoreScreenshotTest suite

**Files:**
- Create: `StoreScreenshotTest.kt`
- Reuse: `SushiClashTestSupport.kt` helpers

- [x] **Step 1:** One parameterized or looped test over 6 languages (or 6 `@Test` methods + shared runner) named clearly for Gradle `--tests`
- [x] **Step 2:** Per locale: clear state (`clearPackageData` already on runner), set language, capture onboarding welcome → finish/skip → build Solo → capture → finish/save → Group → capture → add wheel participants → capture → unlock achievements path → capture → history → capture → settings → capture
- [x] **Step 3:** Ensure no celebration/finish dialogs remain; dismiss with existing helpers
- [x] **Step 4:** Validate EN-only first with Gradle filter before full 6-locale run

---

### Task 3: Pull script + docs

**Files:**
- Create: `scripts/generate-screenshots.sh`
- Modify: `docs/screenshots/README.md`

- [x] **Step 1:** Script runs `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=...StoreScreenshotTest`
- [x] **Step 2:** `adb pull` from device path into `docs/screenshots/<locale>/`
- [x] **Step 3:** Document emulator assumption (portrait phone, API 24+), command, output tree

---

### Task 4: README gallery integration

**Files:**
- Modify: `README.md` (screenshots section only)

- [x] **Step 1:** After EN PNGs exist, replace placeholder table/commented gallery with compact HTML rows (≈3–4 images/row, width ~180)
- [x] **Step 2:** Use strongest EN shots: onboarding, solo, group, roulette, achievements, history (settings optional if space)
- [x] **Step 3:** Keep existing README structure; no full rewrite

---

### Task 5: Verify sizes & report

- [x] **Step 1:** Confirm 6×7 = 42 PNGs exist and are non-empty
- [x] **Step 2:** Report total size; lossless optimize only if a file is unreasonably large
- [x] **Step 3:** Do **not** commit or push
- [x] **Step 4:** Do **not** run the full instrumented regression suite

---

## Out of scope

- Play Store marketing frames / device bezels
- Production demo flags or screenshot buttons
- Committing / pushing
- Full `connectedDebugAndroidTest` suite

---

## Approval checkpoint

Please approve this plan (or request changes) before implementation starts.
