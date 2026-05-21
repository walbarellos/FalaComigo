#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

PACKAGE_NAME="${PACKAGE_NAME:-br.com.falacomigo}"
MAIN_ACTIVITY="${MAIN_ACTIVITY:-br.com.falacomigo/.MainActivity}"
OUT_DIR="${OUT_DIR:-$ROOT_DIR/build/reports/falacomigo}"
OUT_FILE="$OUT_DIR/device-smoke-$(date +%Y%m%d-%H%M%S).txt"

mkdir -p "$OUT_DIR"

ADB_BIN="${ADB_BIN:-}"
if [[ -z "$ADB_BIN" ]]; then
  if command -v adb >/dev/null 2>&1; then
    ADB_BIN="$(command -v adb)"
  elif [[ -x "${ANDROID_HOME:-/opt/android-sdk}/platform-tools/adb" ]]; then
    ADB_BIN="${ANDROID_HOME:-/opt/android-sdk}/platform-tools/adb"
  else
    ADB_BIN="/opt/android-sdk/platform-tools/adb"
  fi
fi

if [[ ! -x "$ADB_BIN" ]]; then
  echo "ERROR: adb not found. Set ADB_BIN or install Android platform-tools." >&2
  exit 2
fi

if ! "$ADB_BIN" shell pm path "$PACKAGE_NAME" >/dev/null 2>&1; then
  echo "ERROR: package not installed: $PACKAGE_NAME" >&2
  exit 3
fi

"$ADB_BIN" logcat -c || true
start_output="$("$ADB_BIN" shell am start -W -n "$MAIN_ACTIVITY" 2>&1)"
sleep "${SMOKE_WAIT_SECONDS:-3}"
pid_output="$("$ADB_BIN" shell pidof "$PACKAGE_NAME" 2>/dev/null || true)"
package_output="$("$ADB_BIN" shell dumpsys package "$PACKAGE_NAME" | rg 'Package \[|versionCode|versionName|firstInstallTime|lastUpdateTime' || true)"
activity_output="$("$ADB_BIN" shell dumpsys activity activities | rg "topResumedActivity|mResumedActivity|mFocusedWindow|$PACKAGE_NAME" || true)"
log_output="$("$ADB_BIN" logcat -d -v time -t "${LOGCAT_LINES:-1200}" | rg "$PACKAGE_NAME|AndroidRuntime|FATAL EXCEPTION|ANR|ActivityTaskManager|ActivityManager|SecurityException|ClassNotFoundException|RuntimeException" || true)"

{
  echo "FalaComigo device smoke check"
  echo "Date: $(date -Is)"
  echo "Package: $PACKAGE_NAME"
  echo "Activity: $MAIN_ACTIVITY"
  echo
  echo "== am start =="
  echo "$start_output"
  echo
  echo "== pidof =="
  echo "${pid_output:-not-running}"
  echo
  echo "== package =="
  echo "$package_output"
  echo
  echo "== activity =="
  echo "$activity_output"
  echo
  echo "== filtered logcat =="
  echo "$log_output"
} > "$OUT_FILE"

if [[ "$start_output" == *"Error"* || "$start_output" == *"Exception"* || "$start_output" == *"Status: failed"* ]]; then
  echo "SMOKE: FAIL (activity start error). Report: $OUT_FILE" >&2
  exit 4
fi

if [[ -z "$pid_output" ]]; then
  echo "SMOKE: FAIL (app process not running after start). Report: $OUT_FILE" >&2
  exit 5
fi

if [[ "$activity_output" != *"$PACKAGE_NAME"* ]]; then
  echo "SMOKE: FAIL (app activity is not resumed/focused). Report: $OUT_FILE" >&2
  exit 6
fi

if [[ "$log_output" == *"FATAL EXCEPTION"* || "$log_output" == *"ANR"* ]]; then
  echo "SMOKE: FAIL (crash/ANR found). Report: $OUT_FILE" >&2
  exit 7
fi

echo "SMOKE: PASS. Report: $OUT_FILE"
