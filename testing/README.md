# Testing GovReady

How the app was tested for the Terminal Assessment, and how to repeat it. Everything here drives the
real app on an Android emulator through `adb`, so the results can be reproduced from this repository.

## What was tested, and where

| | |
|---|---|
| Device | Android Studio emulator, Pixel 10 Pro XL, Android 17 (API 37), 1344 x 2992 at 480 dpi |
| Build | `app-debug.apk`, built with Gradle 8.9 on JDK 17 |
| Tester | Group 6 (Jared Mesa), October to November 2026 |

## Scripts

| Script | What it checks |
|---|---|
| [`stability_suite.sh`](stability_suite.sh) | **Phase 1, stability.** Forward navigation, back stack, rapid tapping (no duplicate screens), rotation does not navigate and the app stays in portrait, arguments pass correctly, state survives rotation / Home key / process death, and a random-input stress run (3 seeds x 2,000 touches). |
| [`walkthrough.sh`](walkthrough.sh) | **Phase 4, final walkthrough.** Entry screen to final screen, back step by step, every transaction end to end, then five rapid-tap loops. |
| [`ui.py`](ui.py) | Small helper the scripts use to tap by label, tap rapidly, read the screen, and find `adb`. |

Phase 2 (structural cleanup) is a code review rather than a runtime test: unused code, debug logs,
commented-out code and hard-coded route strings were searched for in `app/src/main/java`, and all
routes now live in `Navigation.kt`.

## How to run

### What you need

- **Python 3** on your `PATH` (the helper `ui.py` uses only the standard library).
- **`adb`** from the Android SDK platform-tools. The scripts find it from the `ADB` environment variable,
  `ANDROID_HOME` / `ANDROID_SDK_ROOT`, the default Android Studio location, or `PATH`.
- **A Bash shell.** On Windows use Git Bash (installed with Git for Windows). macOS and Linux have one already.
- **An emulator or a phone** running Android 7.0 (API 24) or higher, with the screen on and unlocked.
- **JDK 17 and Gradle 8.9** only if you build the app yourself. The repository has no Gradle wrapper, so the
  easier way is to press **Run** in Android Studio.

### Steps

1. Start the emulator (or connect a device) and install the app. Press **Run** in Android Studio, or build and
   install the APK yourself:
   ```bash
   gradle assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
2. Check that `adb` sees exactly one device:
   ```bash
   adb devices
   ```
3. Run the scripts from the project root:
   ```bash
   bash testing/walkthrough.sh        # about 11 minutes, 23 steps
   bash testing/stability_suite.sh    # about 16 minutes, 68 checks including the stress run
   ```
4. Read the output. Each check prints `PASS` or `FAIL`, and the last line is `RESULT: N passed, M failed`.
   To see only the problems: `bash testing/stability_suite.sh | grep -E "FAIL|RESULT"`.

### Before you start

- **Do not touch the emulator while a script runs.** The scripts tap the screen themselves, so any other tap
  makes checks fail for no real reason.
- **The scripts erase the app's data** (`pm clear`) before most checks, so saved checklists on that device are
  deleted. Use an emulator, or a phone you do not mind resetting the app on.
- **The stability suite turns auto-rotate off** to test rotation. Turn it back on in the quick settings afterwards.
- **To stop a run early,** press `Ctrl+C` in the terminal. Then make sure no `walkthrough.sh` or
  `stability_suite.sh` process is left running, or it keeps tapping the emulator.

## Results

| Check | Result |
|---|---|
| `stability_suite.sh` (navigation, back stack, rapid taps, arguments, state, rotation, process death, stress) | **68 of 68** checks pass |
| Random-input stress (part of the suite) | 3 seeds x 2,000 touches = 6,000 touches, sent in batches of 100 with a check that the app is still in front: 0 crashes, 0 ANRs, 0 batches lost |
| `walkthrough.sh` | **23 of 23** steps pass, including 5 rapid-tap loops |

Run from this folder on the final code on 4 November 2026. The walkthrough took about 11 minutes and the stability suite about 16 minutes.

**How the stress run works.** It sends touches only (no Back, Home, trackball or random key events, which could leave the app), in 20 batches of 100 per seed. After each batch it checks that GovReady is still the front activity, relaunches it if not, and counts that batch as lost.

**Earlier issue, now fixed.** One stress seed (7) used to crash the app. The stack trace was inside Compose's own scroll-capture code (`ComposeScrollCaptureCallback`, Compose BOM 2024.12.01), not in GovReady code. Updating the Compose BOM to 2025.05.01 removed it: all three seeds are now clean.

### Problems found and fixed

The first rows were found by the tests above on the first build, four came from the mentor's Milestone 2 feedback, and the rest were found in our own use of the app afterwards (saved checklists lost on a full close, the "In progress" card, Back clearing answers); all were fixed and re-tested.
The "before" behaviour was observed by hand and with the same scripts on the earlier code, so it
cannot be reproduced on the current code.

| What the user did | Before the fix | After the fix |
|---|---|---|
| 4 fast taps on **Check requirements** | Two copies of the Purpose screen stacked | One screen; one Back returns Home |
| Back tapped twice, fast | Blank white screen (3 of 3 tries) | Lands on Home every time |
| Rotated with the edit-confirm dialog open | Dialog disappeared | Dialog survives rotation |
| Rotated on the Details screen | Content could not be scrolled in landscape | Scrollable; later the app was locked to portrait, as the Milestone 1 document says |
| Pressed Back on Details, then opened a transaction again | Age, status and first-time were reset | The answers are kept until a checklist is generated |
| Opened a saved checklist, then went Home | It showed up as "In progress" | Only checklists you generated show as "In progress" |
| Android killed the app in the background | Checklist came back blank | Checklist, progress and saved list restored |
| App closed completely and reopened | Saved checklists were gone | Saved list kept in app storage and restored, with its ticks |
| Ticked more items after pressing **Save** | Button still said "Saved", but the saved copy kept the old progress | The saved copy follows the ticks, so "Saved" is true |
| Chose an age or a status on Details | Neither changed the checklist (only the summary line) | Under 18 adds a guardian consent item; Employed and Self-employed add a supporting ID item (sample rules) |
| Changing the age on Details | One tap per year (11 taps for +11) | Hold the button to keep stepping |
| Reading the screens | Text as small as 10 to 12 sp | Nothing below 12 sp; notes and labels at 13 sp; buttons 48 dp tall |

How they were fixed: navigation only acts on a screen that has finished its transition
(`navigateFrom` / `backFrom` in `Navigation.kt`, plus `launchSingleTop`), the confirm dialog state is
`rememberSaveable`, `AppViewModel` keeps its state with `SavedStateHandle` and keeps a saved checklist in step with the ticks.

## Limits of this testing (stated honestly)

- **One emulator, one Android version.** Not tested on physical phones or other API levels.
- **Rapid taps are adb-fast**, tens of milliseconds apart, which is faster than a person but not the same as a person.
- **The scripts find controls by their label text.** Changing a label (for example "Generate my checklist") needs the script updated too.
- **Visual alignment with the Figma prototype is not tested automatically.** It was compared by eye against the Figma frames; some visual details differ (see the main README and the presentation).
- **The random-input run counts crashes in the GovReady process only.** An earlier run with system keys enabled crashed the emulator's own System UI (`com.android.systemui`), which is an emulator problem, not an app crash, so system keys are turned off for this run.
- **There are no unit tests or Compose UI tests yet.** Adding them is a sensible next step.
- **Sample data only.** The tests check behaviour, not whether the requirement data is correct.
