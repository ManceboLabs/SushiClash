# Development Notes

## Testing policy

Every future feature, bugfix or refactor must include the necessary tests and update comments/documentation when behavior changes. Code comments must be written in English.

## Running tests

```bash
./gradlew testDebugUnitTest
```

## Test layout

Unit tests live under `app/src/test/java/com/mancebolabs/sushiclash/`:

- `counter/` — counter screen ViewModel and gameplay flow
- `game/` — setup rules and repository behavior
- `history/` — ranking logic and history ViewModel
- `roulette/` — random trigger logic and auto-spin navigation state
- `settings/` — theme persistence and settings actions
- `onboarding/` — first-launch onboarding startup integration
- `wheel/` — wheel ViewModel and auto-spin rules
- `testutil/` — fakes, dispatcher rule, shared fixtures

## Randomness in tests

Roulette target generation and wheel spin selection use `RandomProvider`. Tests inject `FakeRandomProvider` for deterministic values.

## App lifecycle states

The counter screen uses three startup states:

1. **Loading** — persisted state not yet read (prevents UI flicker). Also used while first-launch onboarding is shown by the app shell.
2. **NoActiveGame** — no active match; setup opens only after "Empezar partida"
3. **ActiveGame** — counters and gameplay controls are shown

First-launch onboarding is handled as a dedicated `onboarding/FIRST_LAUNCH` navigation route before tabs render. Manual replay from Settings navigates to `onboarding/SETTINGS` and returns with `popBackStack()`. The bottom navigation bar is hidden on all onboarding routes.

Finishing a game opens a confirmation dialog first. The active game stays in persistence until the user chooses Guardar or No guardar, so Cancel and unexpected app closure do not lose in-progress counters. The finish dialog itself is not restored on relaunch.

## Compose Previews

- Keep `@Preview` functions at the bottom of the same file as the Composable they showcase.
- Do not create separate preview-only files (for example `*Preview.kt` or `PreviewData.kt`) unless shared preview infrastructure is genuinely reused across many screens.
- Preview functions should be `private` when possible.
- Use `ItamaePreviewTheme` from `ui/theme/Theme.kt` for theme and floating-nav insets; do not wire real ViewModels, DataStore, or navigation controllers in previews.
- Keep preview sample data local to the file unless it is reused heavily by many previews.
