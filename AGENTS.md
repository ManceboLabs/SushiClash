Sushi Clash — AGENTS.md

Project Overview

Sushi Clash is a native Android application developed under Mancebo Labs.

The app allows users to count sushi pieces in Solo or Group mode, use a roulette system, save game history, configure appearance settings, and complete an onboarding tutorial.

The project is intended to be:

* Published on Google Play.
* Maintained as a professional portfolio project.
* Included in the developer’s public Git repository and CV.
* Easy to understand, maintain and extend.

Code quality is therefore a priority.

⸻

Technology

Use the existing project stack.

Primary technologies:

* Kotlin
* Jetpack Compose
* Material 3
* Coroutines
* Flow / StateFlow
* Local persistence
* JUnit
* MockK when required
* Compose Preview

Do not introduce new libraries unless they provide clear value.

Avoid unnecessary dependencies.

⸻

General Development Rules

Before modifying code:

1. Inspect the existing implementation.
2. Understand the current architecture and state flow.
3. Identify the smallest safe change.
4. Preserve existing behavior unless the task explicitly requests otherwise.

Do not blindly rewrite working code.

Do not perform unrelated refactors during feature implementation.

If a larger refactor is necessary, explain why before implementing it.

⸻

Architecture

Follow the existing architecture unless there is a strong reason to improve it.

Maintain clear separation between:

* UI
* UI state
* ViewModels
* Business logic
* Persistence
* Models

Business logic should not live inside Composables.

Composable functions should primarily render state and emit user actions.

Prefer stateless Composables when practical.

Avoid tightly coupling UI components to ViewModels.

⸻

State Management

Use immutable UI state whenever practical.

Use StateFlow for observable ViewModel state.

Avoid duplicated sources of truth.

Persist only data that must survive application restarts.

Temporary UI state such as dialog visibility should not be persisted unless required by product behavior.

Be especially careful with:

* Active game state
* Finished game state
* Roulette triggers
* Onboarding state
* Theme state
* Historical games

⸻

Jetpack Compose

Follow modern Compose practices.

Avoid unnecessary recompositions.

Use stable state structures when appropriate.

Extract reusable components when this improves readability, but do not over-componentize simple UI.

Respect system insets and different screen sizes.

The UI must work correctly with:

* Gesture navigation
* 3-button navigation
* Small Android devices
* Large Android devices
* Light mode
* Dark mode

⸻

Compose Previews

Every relevant Composable should have a Compose Preview when practical.

Rules:

* Previews must be located at the bottom of the same Kotlin file as the Composable.
* Do not create separate preview-only files unless absolutely necessary.
* Use fake/static data.
* Do not use real ViewModels.
* Do not access persistence.
* Do not require navigation controllers when avoidable.

Important screens should include:

* Light mode preview
* Dark mode preview

Complex responsive layouts should also include useful device-size previews.

⸻

Testing

Every feature, bugfix or relevant refactor must include the necessary tests.

Do not consider a task finished until existing tests pass.

Prefer unit tests for:

* ViewModels
* Business logic
* State transitions
* History calculations
* Roulette logic
* Persistence behavior

UI tests should only be added when they provide stable and meaningful value.

Tests must cover regressions for bugs being fixed.

Use deterministic logic in tests.

Random behavior must be abstracted so it can be controlled during testing.

⸻

Verification Before Finishing a Task

Before reporting a task as complete:

1. Ensure the project compiles.
2. Run relevant unit tests.
3. Run the full test suite when the change has broad impact.
4. Check that Compose previews still compile when modified.
5. Check light and dark mode if UI was changed.
6. Check small-screen behavior if layout was changed.
7. Search for dead or obsolete code introduced by the change.

Do not claim success if verification was not performed.

⸻

Comments and Documentation

Code comments must be written in English.

Do not add comments that simply describe obvious code.

Bad:

// Increment counter
counter++

Good:

// Keep the active game until the user confirms whether the result should be saved.

Comments should explain:

* Non-obvious decisions
* Complex state transitions
* Workarounds
* Important business rules
* Why something is implemented a certain way

Update documentation when architectural or behavioral decisions change.

⸻

Naming

All code identifiers must be written in English.

Use descriptive names.

Avoid abbreviations unless they are standard and obvious.

Prefer names that describe intent rather than implementation.

Example:

Good:

pendingFinishedGame
nextRandomRouletteTarget
hasActiveGame

Avoid vague names such as:

data
value
temp
flag

when a more meaningful name is possible.

⸻

Design System

Maintain the existing Sushi Clash / Itamae Expressive visual language.

Use existing theme colors, typography, spacing, shapes and reusable components before creating new styles.

Avoid hardcoded colors when a theme token already exists.

Dark mode must be treated as a first-class experience.

UI changes must remain visually consistent with the rest of the application.

⸻

Current Main Features

Preserve the behavior of existing features unless explicitly changing them:

* Onboarding tutorial
* Start game flow
* Solo mode
* Group mode
* Up to 6 group players
* Sushi counters
* Individual long-press counter reset
* Roulette
* Fixed roulette trigger
* Progressive random roulette trigger
* Automatic roulette spin
* Finish game flow
* Historical rankings
* Settings
* Light/dark theme selection
* Floating bottom navigation

⸻

Persistence

Changes to persisted data must be backwards-compatible whenever practical.

Do not silently destroy existing user data.

When changing persisted models or keys:

* Review migration implications.
* Provide safe defaults.
* Add tests for affected behavior.

⸻

Performance

Avoid premature optimization.

However:

* Avoid unnecessary recompositions.
* Avoid heavy work on the main thread.
* Avoid repeatedly reading persistence from Composables.
* Avoid unnecessary object allocation in frequently recomposed UI.

⸻

Accessibility

New UI should consider accessibility.

When appropriate:

* Provide content descriptions.
* Maintain sufficient color contrast.
* Keep touch targets accessible.
* Avoid conveying important information through color alone.

⸻

Error Handling

Do not silently ignore unexpected errors.

Use appropriate fallback states where required.

Avoid crashes caused by malformed persisted state.

Do not expose technical errors directly to the user unless useful.

⸻

Git

Do NOT create commits unless explicitly requested.

Do NOT push changes unless explicitly requested.

Do not modify unrelated files.

Keep changes focused on the requested task.

⸻

Dependency Policy

Before adding a dependency:

1. Determine whether the feature can reasonably be implemented with the existing stack.
2. Explain why the dependency is necessary.
3. Prefer well-maintained and established libraries.
4. Avoid adding dependencies for trivial utilities.

⸻

Refactoring Policy

Refactor when it clearly improves:

* Maintainability
* Testability
* Correctness
* Reusability
* Architecture

Do not refactor only for stylistic preference.

Large refactors should be separated from unrelated feature work.

⸻

Definition of Done

A task is complete only when:

* Requested behavior works.
* Existing behavior remains intact.
* Relevant tests exist.
* Tests pass.
* Code compiles.
* Previews are updated when UI changes.
* Important logic has meaningful English comments.
* No unnecessary dependency or complexity was introduced.
* Documentation is updated when required.