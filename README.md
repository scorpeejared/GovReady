# GovReady

**Know exactly what to bring before you go.**

GovReady is an Android app that tells you which documents and requirements you need before visiting a government office. You pick a transaction, answer a few quick questions, and the app builds a personalized checklist you can tick off and save for later. Nothing is ever submitted to any agency, so there is no login and no identity is needed.

> **Group 6** · Prototype build. All requirements, fees and offices shown are **sample data** for demonstration only.

## Team

| Name | Role |
|------|------|
| _Jared Mesa_ | _Role_ |
| _Adrian San Juan_ | _Role_ |
| _Djanhdevik Paragua_ | _Role_ |

## What it does

1. **Home**: the entry point. Start a new check, resume the checklist you were working on ("In progress"), see recently saved checklists, or jump straight into a popular transaction.
2. **What do you need?**: choose the transaction you are preparing for: Driver's License, Passport, NBI Clearance or Postal ID.
3. **Details**: answer three short questions (age, status, first-time applicant or not) so the checklist fits your situation.
4. **Your checklist**: a personalized list of requirements with a progress bar. Tick items off as you gather them. A "Good to know" section shows whether an appointment is needed, the estimated fees and where to apply. Every checklist carries a "Last verified" badge.
5. **Edit details / Save**: change your answers (the app asks for confirmation first if you already ticked items, because editing rebuilds the checklist) or save the checklist to keep it for later.
6. **Saved checklists**: reopen any saved checklist from "See all" on Home.

### Navigation flow

```
Home ──► What do you need? ──► Details ──► Your checklist ──► (Edit details ─► Details)
 │  └──────────────────────────► Details (via a "Popular" chip)
 ├──► Your checklist (via "In progress" or a saved card)
 └──► Saved checklists ──► Your checklist
```

- **Back** goes one step back in the flow. From a freshly generated checklist it returns to Home.
- Navigation ignores extra taps while a screen is still animating, so rapid tapping never creates duplicate screens or pops the root screen off the stack.
- Form state (age, status, first-time) survives rotation, and so do checked items and the edit-confirmation dialog.

## Tech stack

- **Language:** Kotlin 2.0
- **UI:** Jetpack Compose with Material 3
- **Navigation:** Navigation Compose
- **State:** a single `AppViewModel` shared across screens (in-memory)
- **Build:** Gradle (Kotlin DSL) and Android Gradle Plugin 8.7
- **SDK:** `minSdk 24`, `targetSdk` / `compileSdk 35`

## Project structure

```
GovReady/
├── build.gradle.kts            # Root Gradle config
├── settings.gradle.kts         # Project and module settings
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml      # Version catalog (all dependency versions)
│   └── wrapper/                # Gradle wrapper settings
└── app/
    ├── build.gradle.kts        # App module config and dependencies
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/values/         # strings.xml (app name), themes.xml
        └── java/com/example/govready/
            ├── MainActivity.kt      # Entry point + NavHost wiring all screens together
            ├── Navigation.kt        # Routes (all route/argument names) + safe navigate/back helpers
            ├── Model.kt             # Data classes, sample checklist data, AppViewModel
            ├── Theme.kt             # GovColors palette + Material theme
            ├── Components.kt        # Shared UI: top bar, chips, buttons, verified badge, bottom bar
            ├── HomeScreen.kt        # Home screen
            ├── PurposeScreen.kt     # "What do you need?" transaction picker
            ├── DetailsScreen.kt     # Age / status / first-time questions
            ├── ChecklistScreen.kt   # Generated checklist, progress, edit-confirm dialog
            └── SavedScreen.kt       # Saved checklists list + SavedCard
```

### How the pieces fit

- **`MainActivity.kt`** hosts `GovReadyApp`, which creates the `NavHost` and connects each screen's callbacks to navigation. Screens never navigate on their own; they only call the callbacks they are given.
- **`Navigation.kt`** is the single place where routes and argument names live. `navigateFrom` / `backFrom` only act when the current screen is fully resumed, which is what prevents duplicate screens.
- **`Model.kt`** holds the data. `buildChecklist(details)` turns the user's answers into a `Checklist` (requirements, appointment info, fees, location). `AppViewModel` keeps the current details, checked items and saved checklists.
- **`Theme.kt` / `Components.kt`** keep the look consistent with the approved Week 4 prototype: colors, spacing, typography and shared widgets are defined once and reused.

## Getting started

### Requirements

- Android Studio (recent version) with the Android SDK installed
- **JDK 17** to run Gradle (Gradle 8.9 does not run on newer JDKs such as 25)
- An emulator or a physical Android device running Android 7.0 (API 24) or higher

### Run the app

1. Clone the repository:
   ```bash
   git clone https://github.com/scorpeejared/GovReady.git
   ```
2. Open the `GovReady` folder in Android Studio and let Gradle sync finish.
3. In **Settings → Build, Execution, Deployment → Build Tools → Gradle**, set the Gradle JDK to a JDK 17 or 21 build if sync fails with a Java version error.
4. Pick an emulator or device and press **Run ▶**.

`local.properties` (your local Android SDK path) is generated by Android Studio and is intentionally not committed.

## Known limitations

- **Sample data only.** Requirements, fees and offices are illustrative, not official.
- **In-memory storage.** Saved checklists and progress live only while the app process is alive. If Android kills the app in the background, they are lost. Persisting them (for example with DataStore or Room) is a planned improvement.
- **No real backend or verification.** The "Last verified Sep 1, 2026" badge is static text in this prototype.

## Design

The UI follows the approved Week 4 prototype: Philippine flag blue as the brand color, green for "verified" and checked states, and red or amber for warnings and fees.
