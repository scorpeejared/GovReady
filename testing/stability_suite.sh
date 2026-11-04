#!/bin/bash
# Stability suite for GovReady (Phase 1): navigation, back stack, rapid taps, arguments,
# state, rotation, process death, and a random-input stress run.
# Needs: a running emulator/device with the debug app installed, python, and adb (see ui.py).
# Run:   bash testing/stability_suite.sh
DIR="$(cd "$(dirname "$0")" && pwd)"
U="python $DIR/ui.py"
A="$($U adbpath)"
PKG=com.example.govready
PASS=0; FAIL=0

fresh()  { "$A" shell pm clear $PKG >/dev/null; "$A" shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; for _ in $(seq 1 24); do sleep 0.5; $U texts | grep -q "Check requirements" && break; done; sleep 0.5; }
top()    { $U texts | grep -vE "^'Back'" | head -1 | sed "s/ *([0-9,]*)$//; s/^['\"]//; s/['\"]$//"; }
has()    { $U texts | grep -qF "$1"; }
rot()    { "$A" shell settings put system accelerometer_rotation 0; "$A" shell settings put system user_rotation "$1"; sleep 2; }
yesno()  { if has "$1"; then echo yes; else echo no; fi; }
check()  { if [ "$2" = "$3" ]; then echo "PASS  $1"; PASS=$((PASS+1)); else echo "FAIL  $1  (expected '$3', got '$2')"; FAIL=$((FAIL+1)); fi; }
kill_relaunch() { "$A" shell input keyevent 3; sleep 1; "$A" shell am kill $PKG; sleep 1; "$A" shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3; }

rot 0
echo "=== 1. Forward navigation"
fresh; check "entry screen is Home" "$(top)" "GovReady"
$U tap "Check requirements" >/dev/null; sleep 1.2; check "Home -> Purpose" "$(top)" "What do you need?"
$U tap "Passport" >/dev/null; sleep 1.2; check "Purpose -> Details (Passport argument)" "$(top)" "Passport"
$U tap "Generate my checklist" >/dev/null; sleep 1.5; check "Details -> Checklist" "$(top)" "Your checklist"

echo "=== 2. Back stack unwinds"
$U tapback; sleep 1; check "fresh Checklist, Back -> Home" "$(top)" "GovReady"
fresh; $U tap "Passport" >/dev/null; sleep 1.2; $U tap "Generate my checklist" >/dev/null; sleep 1.5
$U tap "Save checklist" >/dev/null; sleep .5; $U tapback; sleep 1; $U tap "See all" >/dev/null; sleep 1.2
check "Home -> Saved (See all)" "$(top)" "Saved checklists"
$U tap "Passport" >/dev/null; sleep 1.2; check "Saved -> Checklist" "$(top)" "Your checklist"
$U tapback; sleep 1; check "Checklist, Back -> Saved" "$(top)" "Saved checklists"
$U tapback; sleep 1; check "Saved, Back -> Home" "$(top)" "GovReady"
echo "--- edit loop"
fresh; $U tap "Passport" >/dev/null; sleep 1.2; $U tap "Generate my checklist" >/dev/null; sleep 1.5
$U tap "Edit details" >/dev/null; sleep 1.2; check "Checklist -> Edit details (no progress, no dialog)" "$(top)" "Passport"
$U tapback; sleep 1; check "Edit details, Back -> Checklist" "$(top)" "Your checklist"

echo "=== 3. Rapid tapping creates no duplicate screens"
fresh; $U rapid "Check requirements" 6 >/dev/null; sleep 1.5; $U tapback; sleep 1
check "6x Check requirements, one Back -> Home" "$(top)" "GovReady"
$U tap "Check requirements" >/dev/null; sleep 1.2; $U rapid "NBI Clearance" 6 >/dev/null; sleep 1.5; $U tapback; sleep 1
check "6x Purpose row, one Back -> Purpose" "$(top)" "What do you need?"
$U tap "NBI Clearance" >/dev/null; sleep 1.2; $U rapid "Generate my checklist" 6 >/dev/null; sleep 1.5; $U tapback; sleep 1
check "6x Generate, one Back -> Home" "$(top)" "GovReady"
fresh; $U rapid "Passport" 6 >/dev/null; sleep 1.5; $U tapback; sleep 1
check "6x Popular chip, one Back -> Home" "$(top)" "GovReady"
for i in 1 2 3; do
  fresh; $U tap "Check requirements" >/dev/null; sleep 1.2
  read BX BY <<<"$($U coords Back)"
  "$A" shell "input tap $BX $BY & sleep 0.03; input tap $BX $BY & sleep 0.03; input tap $BX $BY & wait"; sleep 1.5
  check "triple fast Back tap #$i leaves Home visible (not blank)" "$(top)" "GovReady"
done

echo "=== 4/5. Navigation happens only on user events (rotation must not navigate)"
fresh; $U tap "Check requirements" >/dev/null; sleep 1.2; $U tap "Passport" >/dev/null; sleep 1.2
rot 1; check "rotate on Details keeps Details" "$(top)" "Passport"
check "phone rotated: the app stays in portrait" "$("$A" shell "cat /sdcard/ui.xml" | grep -o 'rotation="[0-9]"' | head -1)" 'rotation="0"'; rot 0
$U tap "Generate my checklist" >/dev/null; sleep 1.5; rot 1; check "rotate on Checklist keeps Checklist" "$(top)" "Your checklist"; rot 0
rot 1; rot 0; rot 1; rot 0; check "4 rapid rotations: still Checklist" "$(top)" "Your checklist"
$U tapback; sleep 1; check "after rotations one Back -> Home (no extra back-stack entries)" "$(top)" "GovReady"
fresh; $U tap "Check requirements" >/dev/null; sleep 1.2; rot 1; check "rotate on Purpose keeps Purpose" "$(top)" "What do you need?"; rot 0
$U tapback; sleep 1; check "...and one Back -> Home" "$(top)" "GovReady"

echo "=== 6. Arguments pass and are retrieved"
fresh; $U tap "Driver's License" >/dev/null; sleep 1.2; check "apostrophe argument (Driver's License) reaches Details" "$(top)" "Driver's License"
$U tap "Employed" >/dev/null; $U tap "No" >/dev/null
for i in 1 2 3; do $U tap "Increase age" >/dev/null; done
$U tap "Generate my checklist" >/dev/null; sleep 1.5
check "age, status and first-time reach the checklist summary" "$(yesno 'Renewal · Employed · age 22')" "yes"
$U tap "Edit details" >/dev/null; sleep 1.2; check "edit=true pre-fills age 22" "$(yesno "'22'")" "yes"
$U tap "Generate my checklist" >/dev/null; sleep 1.5; $U tapback; sleep 1
for t in "Passport" "NBI Clearance" "Postal ID"; do
  $U tap "$t" >/dev/null; sleep 1.2; check "argument '$t' reaches Details title" "$(top)" "$t"; $U tapback; sleep 1
done

echo "=== 7. State does not reset unexpectedly"
fresh; $U tap "Driver's License" >/dev/null; sleep 1.2; $U tap "Generate my checklist" >/dev/null; sleep 1.5
$U tap "Valid government-issued ID" >/dev/null; $U tap "Medical certificate" >/dev/null; sleep .4
check "2 items ticked shows 2 / 5" "$(yesno '2 / 5')" "yes"
rot 1; check "progress survives rotation" "$(yesno '2 / 5')" "yes"; rot 0
"$A" shell input keyevent 3; sleep 1; "$A" shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 2
check "progress survives Home key and resume" "$(yesno '2 / 5')" "yes"
$U tap "Edit details" >/dev/null; sleep 1; check "edit with progress shows the confirm dialog" "$(yesno 'Change your details')" "yes"
rot 1; check "confirm dialog survives rotation" "$(yesno 'Change your details')" "yes"; rot 0
$U tap "Keep checklist" >/dev/null; sleep .8; check "Keep checklist leaves progress intact" "$(yesno '2 / 5')" "yes"
$U tap "Edit details" >/dev/null; sleep 1; $U tap "Change details" >/dev/null; sleep 1.2
check "Change details -> Details" "$(top)" "Driver's License"
$U tapback; sleep 1; check "confirmed edit cleared progress (0 / 5), as documented" "$(yesno '0 / 5')" "yes"
echo "--- process death (Android kills the app in the background)"
$U tap "Valid government-issued ID" >/dev/null; sleep .4; $U tap "Save checklist" >/dev/null; sleep .5
kill_relaunch
check "after process death the checklist is restored" "$(top)" "Your checklist"
check "progress restored after process death" "$(yesno '1 / 5')" "yes"
$U tapback; sleep 1; check "saved list restored after process death" "$(yesno 'See all')" "yes"

echo "=== 9. Every answer shapes the result, Saved stays in sync, the stepper is quick"
fresh; $U tap "Driver's License" >/dev/null; sleep 1.3
read PX PY <<<"$($U coords 'Increase age')"; read MX MY <<<"$($U coords 'Decrease age')"
"$A" shell input swipe $PX $PY $PX $PY 1500; sleep .4
age=$($U texts | grep -oE "'[0-9]{2}'" | head -1 | tr -d "'")
if [ "${age:-0}" -ge 30 ]; then r=fast; else r="slow ($age)"; fi
check "holding + for 1.5 s moves the age by 11 or more (was one tap per year)" "$r" "fast"
"$A" shell input swipe $MX $MY $MX $MY 4000; sleep .4
check "holding - stops at the minimum age (15)" "$(yesno "'15'")" "yes"
$U tap "Generate my checklist" >/dev/null; sleep 1.6
check "age under 18 adds the parent or guardian consent item" "$(yesno 'Parent or guardian consent')" "yes"
fresh; $U tap "Driver's License" >/dev/null; sleep 1.3; $U tap "Generate my checklist" >/dev/null; sleep 1.6
check "age 19, Student: no guardian item (default path unchanged)" "$(yesno 'Parent or guardian consent')" "no"
check "default checklist still has 5 items" "$(yesno '0 / 5')" "yes"
$U tapback; sleep 1; $U tap "Check requirements" >/dev/null; sleep 1.2; $U tap "Driver's License" >/dev/null; sleep 1.3; $U tap "Employed" >/dev/null; $U tap "Generate my checklist" >/dev/null; sleep 1.6
check "status Employed adds a company ID item" "$(yesno 'Company ID or employment certificate')" "yes"
echo "--- Saved stays in sync with ticked items"
fresh; $U tap "Passport" >/dev/null; sleep 1.3; $U tap "Generate my checklist" >/dev/null; sleep 1.6
$U tap "PSA birth certificate" >/dev/null; sleep .4; $U tap "Save checklist" >/dev/null; sleep .6
$U tap "One valid government ID" >/dev/null; sleep .5
check "ticking after saving: progress is 2 / 2" "$(yesno '2 / 2')" "yes"
check "button still says Saved" "$(yesno "'Saved'")" "yes"
$U tapback; sleep 1; $U tap "See all" >/dev/null; sleep 1.2; $U tap "Passport" >/dev/null; sleep 1.3
check "reopened from Saved: the later tick was saved too (2 / 2)" "$(yesno '2 / 2')" "yes"
echo "--- saved list survives the app being closed completely (not just process death)"
"$A" shell am force-stop $PKG; "$A" shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3
check "after full termination Home still lists the saved Passport" "$(yesno "Passport")" "yes"
$U tap "Passport" >/dev/null; sleep 1.3
check "saved checklist reopens with its ticks (2 / 2) after full termination" "$(yesno '2 / 2')" "yes"

echo "--- Back keeps the Details answers"
fresh; $U tap "Check requirements" >/dev/null; sleep 1.2; $U tap "Driver's License" >/dev/null; sleep 1.3
$U tap "Employed" >/dev/null; $U tap "No" >/dev/null
read PX PY <<<"$($U coords 'Increase age')"; for i in 1 2 3; do "$A" shell input tap $PX $PY; done; sleep .4
$U tapback >/dev/null; sleep 1; $U tap "Passport" >/dev/null; sleep 1.3
check "after Back and a new transaction, the age is still 22" "$(yesno "'22'")" "yes"
$U tap "Generate my checklist" >/dev/null; sleep 1.6
check "all three answers were kept (Renewal, Employed, age 22)" "$(yesno 'Renewal · Employed · age 22')" "yes"

echo "--- Opening a saved checklist does not make it 'In progress'"
fresh; $U tap "Check requirements" >/dev/null; sleep 1.2; $U tap "Passport" >/dev/null; sleep 1.3; $U tap "Generate my checklist" >/dev/null; sleep 1.6
$U tap "Save checklist" >/dev/null; sleep .6; $U tapback >/dev/null; sleep 1
check "a checklist you just generated shows as In progress" "$(yesno 'In progress')" "yes"
"$A" shell am force-stop $PKG; "$A" shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3
check "fresh open: no In progress card" "$(yesno 'In progress')" "no"
read SX SY <<<"$($U coords 'Passport')"; "$A" shell input tap $SX $((SY + 37)); sleep 1.4
check "tapping the saved card opens the checklist" "$(top)" "Your checklist"
$U tapback >/dev/null; sleep 1
check "after opening a saved checklist: still no In progress card" "$(yesno 'In progress')" "no"

echo "=== 8. Rapid random input does not crash the app (3 seeds x 2000 events, in batches of 100)"
# Only touches and drags are sent (no Back, Home, trackball or random key events), so the app is not
# left by the input itself. Between batches the script checks that the app is still the front activity; if it is
# not (a crash, or Android drew something over it), it relaunches the app and counts that batch as lost.
in_app() { "$A" shell "dumpsys window" | grep mFocusedApp | head -1 | grep -q "$PKG"; }
for seed in 42 7 2026; do
  "$A" logcat -c; fresh
  lost=0
  for batch in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20; do
    in_app || { lost=$((lost+1)); "$A" shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 2; }
    "$A" shell monkey -p $PKG --throttle 30 --pct-touch 100 --pct-motion 0 --pct-trackball 0 --pct-nav 0         --pct-majornav 0 --pct-syskeys 0 --pct-appswitch 0 --pct-flip 0 --pct-anyevent 0 --pct-pinchzoom 0         --pct-permission 0 --ignore-security-exceptions -s $((seed * 100 + batch)) 100 >/dev/null 2>&1
  done
  in_app || lost=$((lost+1))
  crashes=$("$A" logcat -d | grep -c "Process: $PKG")
  anrs=$("$A" logcat -d | grep -c "ANR in $PKG")
  check "monkey seed $seed: app crashes" "$crashes" "0"
  check "monkey seed $seed: ANRs" "$anrs" "0"
  check "monkey seed $seed: batches that ended outside the app (of 20)" "$lost" "0"
done
"$A" shell am force-stop $PKG
"$A" shell settings put system accelerometer_rotation 1

echo
echo "RESULT: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ]
