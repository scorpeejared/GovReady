#!/bin/bash
# Final walkthrough (Phase 4): the full flow from the entry screen to the final screen, back step by
# step, every transaction end to end, then five rapid-tap loops.
# Needs: a running emulator/device with the debug app installed. Run: bash testing/walkthrough.sh
DIR="$(cd "$(dirname "$0")" && pwd)"
U="python $DIR/ui.py"
A="$($U adbpath)"
PKG=com.example.govready
P=0; F=0

# wait (up to 6 s) until a label is on screen, then let the screen transition finish so taps are accepted
waitfor() { for _ in $(seq 1 20); do $U texts | grep -q "$1" && break; sleep 0.3; done; sleep 0.6; }
fresh()  { "$A" shell pm clear $PKG >/dev/null; "$A" shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; for _ in $(seq 1 24); do sleep 0.5; $U texts | grep -q "Check requirements" && break; done; sleep 0.5; }
top()   { $U texts | grep -vE "^'Back'" | head -1 | sed "s/ *([0-9,]*)$//; s/^['\"]//; s/['\"]$//"; }
ck()    { if [ "$2" = "$3" ]; then echo "PASS  $1 -> $2"; P=$((P+1)); else echo "FAIL  $1 (expected '$3', got '$2')"; F=$((F+1)); fi; }

echo "=== A: slow walkthrough, then step-by-step back"
fresh; ck "1 entry screen" "$(top)" "GovReady"
$U tap "Check requirements" >/dev/null; sleep 1.2; ck "2 purpose" "$(top)" "What do you need?"
$U tap "Driver's License" >/dev/null; sleep 1.2; ck "3 details" "$(top)" "Driver's License"
$U tap "Generate my checklist" >/dev/null; sleep 1.5; ck "4 checklist (final screen)" "$(top)" "Your checklist"
$U tap "Valid government-issued ID" >/dev/null; $U tap "Medical certificate" >/dev/null; sleep .4
$U tap "Save checklist" >/dev/null; sleep .6; ck "5 button flips to Saved" "$($U texts | grep -c "'Saved'")" "1"
$U tapback; sleep 1; ck "back 1 -> Home" "$(top)" "GovReady"
$U tap "See all" >/dev/null; sleep 1.2; ck "Home -> Saved list" "$(top)" "Saved checklists"
$U tap "Driver's License" >/dev/null; sleep 1.2; ck "Saved -> reopened checklist keeps progress" "$($U texts | grep -c "2 / 5")" "1"
$U tapback; sleep 1; ck "back -> Saved list" "$(top)" "Saved checklists"
$U tapback; sleep 1; ck "back -> Home" "$(top)" "GovReady"

echo "=== B: every transaction end to end"
for t in "Passport" "NBI Clearance" "Postal ID" "Driver's License"; do
  fresh; $U tap "Check requirements" >/dev/null; sleep 1.2; $U tap "$t" >/dev/null; sleep 1.2
  $U tap "Generate my checklist" >/dev/null; sleep 1.5
  ck "$t -> checklist" "$(top)" "Your checklist"; $U tapback; sleep 1; ck "$t back -> Home" "$(top)" "GovReady"
done

echo "=== C: rapid, 5 full loops (every tap is repeated 3 times, as fast as adb allows)"
fresh
for i in 1 2 3 4 5; do
  $U rapid "Check requirements" 3 >/dev/null; waitfor "What do you need"; $U rapid "Passport" 3 >/dev/null; waitfor "Generate my checklist"
  $U rapid "Generate my checklist" 3 >/dev/null; waitfor "Save checklist"
  c=$(top); $U rapid "Valid government-issued ID" 1 >/dev/null; $U rapid "Save checklist" 2 >/dev/null; sleep .3
  $U rapidtapback 4; sleep 1.0
  ck "loop $i: checklist reached, then 4 rapid Backs -> Home" "$c|$(top)" "Your checklist|GovReady"
done
"$A" shell dumpsys activity activities | grep -c "topResumedActivity.*govready" | sed 's/^/app still in the foreground (1 = yes): /'
echo "crashes in the app process: $("$A" logcat -d | grep -c "Process: $PKG")"
echo
echo "RESULT: $P passed, $F failed"
[ "$F" -eq 0 ]
