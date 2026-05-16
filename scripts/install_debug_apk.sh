#!/usr/bin/env bash
set -euo pipefail

APK_PATH="${1:-}"
if [[ -z "$APK_PATH" ]]; then
  APK_PATH="$(cd "$(dirname "$0")/.." && pwd)/app/build/outputs/apk/debug/app-debug.apk"
fi

SDK_ROOT="${SDK_ROOT:-/tmp/android-sdk}"
ADB_BIN="$SDK_ROOT/platform-tools/adb"

if [[ ! -x "$ADB_BIN" ]]; then
  echo "ERROR: adb not found at: $ADB_BIN" >&2
  echo "Install it with: /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=$SDK_ROOT platform-tools" >&2
  exit 1
fi

if [[ ! -f "$APK_PATH" ]]; then
  echo "ERROR: APK not found at: $APK_PATH" >&2
  exit 1
fi

set +e
DEVICES="$("$ADB_BIN" devices | tail -n +2 | rg -n \"\\tdevice$\" || true)"
set -e

if [[ -z "$DEVICES" ]]; then
  echo "ERROR: no device/emulator connected (adb devices is empty)." >&2
  exit 2
fi

"$ADB_BIN" install -r "$APK_PATH"

