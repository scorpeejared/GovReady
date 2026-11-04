"""Tiny adb UI driver used by the GovReady test scripts.

Usage: python ui.py <command> [args]
  adbpath               print the adb executable that is being used
  texts                 list visible text / content-desc nodes with their centre coordinates
  coords "Text"         print "x y" of the node whose text/desc matches
  tap "Text"            tap the node whose text/desc matches (exact match preferred)
  rapid "Text" N        tap that node N times as fast as possible (parallel input processes)
  back                  system Back key
  rapidback N           N system Back keys fired back to back
  tapback               tap the in-app top-bar back arrow (content-desc "Back")
  rapidtapback N        tap the in-app back arrow N times quickly
  shot path.png         save a screenshot

adb is found from the ADB environment variable, then ANDROID_HOME / ANDROID_SDK_ROOT,
then the default Android Studio location, then PATH.
"""
import os
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET


def find_adb():
    if os.environ.get("ADB"):
        return os.environ["ADB"]
    exe = "adb.exe" if os.name == "nt" else "adb"
    for root in (os.environ.get("ANDROID_HOME"), os.environ.get("ANDROID_SDK_ROOT"),
                 os.path.join(os.environ.get("LOCALAPPDATA", ""), "Android", "Sdk"),
                 os.path.expanduser("~/Library/Android/sdk"), os.path.expanduser("~/Android/Sdk")):
        if root:
            path = os.path.join(root, "platform-tools", exe)
            if os.path.exists(path):
                return path
    return shutil.which("adb") or "adb"


ADB = find_adb()


def adb(*args):
    return subprocess.run([ADB, *args], capture_output=True, text=True).stdout


def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    xml = adb("shell", "cat", "/sdcard/ui.xml")
    xml = xml[xml.find("<?xml"):] if "<?xml" in xml else xml
    try:
        root = ET.fromstring(xml)
    except ET.ParseError:
        return []
    out = []
    for n in root.iter("node"):
        label = n.get("text") or n.get("content-desc") or ""
        m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", n.get("bounds") or "")
        if not m or not label:
            continue
        x1, y1, x2, y2 = map(int, m.groups())
        out.append((label, (x1 + x2) // 2, (y1 + y2) // 2))
    return out


def find(text):
    found = nodes()
    for label, x, y in found:
        if label == text:
            return x, y
    for label, x, y in found:
        if text.lower() in label.lower():
            return x, y
    return None


def need(text):
    p = find(text)
    if not p:
        print("NOT FOUND:", text)
        sys.exit(1)
    return p


cmd = sys.argv[1]
if cmd == "adbpath":
    print(ADB)
elif cmd == "texts":
    for label, x, y in nodes():
        print(f"{label!r:70} ({x},{y})")
elif cmd == "coords":
    x, y = need(sys.argv[2])
    print(x, y)
elif cmd == "tap":
    x, y = need(sys.argv[2])
    adb("shell", "input", "tap", str(x), str(y))
    print("tapped", sys.argv[2], (x, y))
elif cmd == "rapid":
    x, y = need(sys.argv[2])
    n = int(sys.argv[3])
    adb("shell", " & ".join([f"input tap {x} {y}"] * n) + " & wait")
    print(f"rapid x{n} on", sys.argv[2], (x, y))
elif cmd == "back":
    adb("shell", "input", "keyevent", "4")
elif cmd == "rapidback":
    adb("shell", " & ".join(["input keyevent 4"] * int(sys.argv[2])) + " & wait")
elif cmd == "tapback":
    p = find("Back")
    if p:
        adb("shell", "input", "tap", str(p[0]), str(p[1]))
    else:
        print("no back arrow")
elif cmd == "rapidtapback":
    p = find("Back")
    if p:
        adb("shell", " & ".join([f"input tap {p[0]} {p[1]}"] * int(sys.argv[2])) + " & wait")
    else:
        print("no back arrow")
elif cmd == "shot":
    with open(sys.argv[2], "wb") as f:
        f.write(subprocess.run([ADB, "exec-out", "screencap", "-p"], capture_output=True).stdout)
    print(sys.argv[2])
else:
    print(__doc__)
    sys.exit(2)
