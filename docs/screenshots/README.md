# Screenshots

Deterministic PNG captures of real Compose UI for the GitHub README and future Play Store assets.

## Emulator assumptions

- Portrait phone form factor
- API 24 or higher
- A single device or emulator connected via `adb` (`adb devices` shows one ready target)

## Generate

From the repository root:

```bash
./scripts/generate-screenshots.sh
```

The script:

1. Preferentially resolves `adb` from `$ANDROID_HOME/platform-tools` or `~/Library/Android/sdk/platform-tools`
2. Best-effort disables accessibility button, magnification, and pointer location
3. Runs only `StoreScreenshotTest` via Gradle
4. Pulls PNGs from the durable device path into `docs/screenshots/<locale>/`

## Output tree

```text
docs/screenshots/
  en/
  es/
  de/
  fr/
  zh-CN/
  ja/
    01_onboarding.png
    02_solo.png
    03_group.png
    04_roulette.png
    05_achievements.png
    06_history.png
    07_settings.png
```

Locales match `AppLanguage.languageTag`: `en`, `es`, `de`, `fr`, `zh-CN`, `ja`.

## Durable device path

Captures are mirrored on device to:

```text
/data/local/tmp/sushiclash-screenshots/<locale>/<filename>.png
```

The instrumentation runner uses `clearPackageData`, which wipes the app-private external files directory after each test. The `/data/local/tmp/...` copy survives that wipe so `adb pull` can collect all locales after the suite finishes.

## Expected filenames

| File | Screen |
|------|--------|
| `01_onboarding.png` | Onboarding welcome |
| `02_solo.png` | Active Solo game |
| `03_group.png` | Active Group game |
| `04_roulette.png` | Roulette wheel (idle) |
| `05_achievements.png` | Achievements |
| `06_history.png` | History |
| `07_settings.png` | Settings |
