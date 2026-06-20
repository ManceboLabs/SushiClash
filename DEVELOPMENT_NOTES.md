# Development Notes

## Testing policy

Every future feature, bugfix or refactor must include the necessary tests and update comments/documentation when behavior changes. Code comments must be written in English.

## Running tests

```bash
./gradlew testDebugUnitTest
```

## Test layout

Unit tests live under `app/src/test/java/com/mancebolabs/sushicounter/`:

- `counter/` — counter screen ViewModel and gameplay flow
- `game/` — setup rules and repository behavior
- `history/` — ranking logic and history ViewModel
- `roulette/` — random trigger logic and auto-spin navigation state
- `settings/` — theme persistence and settings actions
- `wheel/` — wheel ViewModel and auto-spin rules
- `testutil/` — fakes, dispatcher rule, shared fixtures

## Randomness in tests

Roulette target generation and wheel spin selection use `RandomProvider`. Tests inject `FakeRandomProvider` for deterministic values.

## App lifecycle states

The counter screen uses three startup states:

1. **Loading** — persisted state not yet read (prevents UI flicker)
2. **NoActiveGame** — no active match; setup opens only after "Empezar partida"
3. **ActiveGame** — counters and gameplay controls are shown

Finishing a game clears persisted active state immediately. A `FinishedGameSnapshot` is kept in memory only until the user saves or discards it.
