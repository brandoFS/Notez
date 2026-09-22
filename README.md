# Notez

A small two-screen note-taking app for Android, built with Jetpack Compose: a list of
notes and a combined create/edit screen. Notes are stored locally with Room — there is
no backend, account, or sync.

## Features

- Create, edit, and delete notes, sorted by most recently updated
- Swipe a row left to delete, with an undo snackbar
- Drafts survive process death via `SavedStateHandle`
- Light and dark themes
- Empty state when there are no notes

## Architecture

Feature-layered modules with Clean Architecture layering inside each feature —
`presentation → domain ← data`. `domain` is pure Kotlin and depends on nothing but
`:core:domain`.

| Module | Contents |
|---|---|
| `:app` | `NotezApplication`, `MainActivity`, `NavHost`, Koin module assembly |
| `:core:domain` | `Result<D, E>`, `Error`, `DataError`, and the `map`/`onSuccess`/`onFailure` helpers |
| `:core:presentation` | `UiText`, `ObserveAsEvents`, `DataError.toUiText()` |
| `:core:design-system` | `NotezTheme`, color schemes, typography |
| `:feature:notes:domain` | `Note` model, `NoteLocalDataSource` interface |
| `:feature:notes:data` | Room entity/DAO/database, mappers, `RoomNoteDataSource`, `notesDataModule` |
| `:feature:notes:presentation` | MVI screens, type-safe routes, nav graph, `notesPresentationModule` |

Two conventions worth knowing before adding code:

- **`:feature:notes:presentation` must not depend on `:feature:notes:data`.** It talks to
  the `NoteLocalDataSource` interface in `domain`; only `:app` wires the implementation in.
- **"Repository" is reserved for classes that coordinate multiple data sources.** There is
  exactly one source here (Room), so the class is a *data source* — `RoomNoteDataSource`.
  Implementations are named for what makes them unique, never suffixed `Impl`.

Each screen follows MVI: a `State` data class, a sealed `Action` for user intent, a sealed
`Event` channel for one-shot effects (navigation, snackbars), and a ViewModel exposing
`StateFlow<State>`. Every screen splits into a `<Screen>Root` composable that owns the
ViewModel and a pure `<Screen>Screen` composable that takes only `state` and `onAction`,
so it can be previewed and tested in isolation.

Errors are returned, never thrown, as a typed `Result<D, E>`. The data layer catches
SQLite exceptions and maps them to `DataError.Local`; the presentation layer maps those to
localized strings via `UiText`.

## Tech stack

| Concern | Choice |
|---|---|
| UI | Jetpack Compose, Material 3 |
| DI | Koin |
| Local storage | Room |
| Navigation | Compose Navigation (type-safe routes via KotlinX Serialization) |
| Async | Coroutines + Flow |
| Unit tests | JUnit 5, Turbine, AssertK, `kotlinx-coroutines-test` |
| UI tests | `ComposeTestRule` (robot pattern) |
| Build | Gradle 9.5 with convention plugins in `build-logic/`, version catalog in `gradle/libs.versions.toml` |

Targets `compileSdk` 37 / `minSdk` 24, on AGP 9.3.3 and Kotlin 2.4.20.

## Building

```bash
./gradlew assembleDebug              # build
./gradlew installDebug               # build and install on a running device/emulator
./gradlew testDebugUnitTest          # unit tests (JUnit 5)
./gradlew connectedDebugAndroidTest  # Compose UI tests — needs a device/emulator
```

Android Studio supplies its own JDK, so building from the IDE needs no setup.

### One-time CLI setup

Gradle needs `JAVA_HOME` pointing at a JDK 25, matching the toolchain declared in
`gradle/gradle-daemon-jvm.properties`. macOS ships a `/usr/bin/java` stub that is not a
real JDK, so if nothing is configured the build fails with:

```
Unable to locate a Java Runtime.
```

Android Studio's bundled JetBrains Runtime is a JDK 25 and works. Add it to your shell
profile:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

On zsh, `~/.zshrc` covers interactive terminals. Put it in `~/.zshenv` instead if
non-interactive shells — CI scripts, editor tasks, agent tooling — also need it. Verify
with `java -version`.

## Gradle notes

The build uses precompiled script plugins in `build-logic/`, which carry a few sharp edges
worth knowing before editing them:

- Gradle does **not** generate the type-safe `libs` accessor inside precompiled script
  plugins. Catalog entries resolve through the `library("alias")` helper in
  `build-logic/src/main/kotlin/VersionCatalog.kt` instead.
- AGP 9 has built-in Kotlin support and **rejects** the `org.jetbrains.kotlin.android`
  plugin. Don't add it back.
- Gradle 9 requires `junit-platform-launcher` on the test runtime classpath explicitly,
  or JUnit 5 tests fail to start rather than being silently skipped.
- Espresso must be 3.7.0 or newer to run on API 35/36; older versions reflect on
  `InputManager.getInstance()`, which no longer exists.

## Adding a feature

1. Create `:feature:<name>:{domain,data,presentation}` and register them in
   `settings.gradle.kts`.
2. Apply `notez.jvm.library` to `domain`, and `notez.android.library` + `notez.room` to
   `data`; `presentation` gets `notez.android.feature`, which bundles Compose, Koin,
   serialization, and the test stack.
3. Define the model and data source interface in `domain`, the implementation in `data`,
   and the MVI screens in `presentation`.
4. Register the feature's Koin modules in `NotezApplication` and its nav graph in
   `MainActivity`'s `NavHost`.

Features never depend on each other. Anything shared by two features moves into the
matching `:core` module.
