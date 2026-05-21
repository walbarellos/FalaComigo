#!/usr/bin/env bash
set -euo pipefail

APK_PATH="${1:-}"
if [[ -z "$APK_PATH" ]]; then
  APK_PATH="$(cd "$(dirname "$0")/.." && pwd)/app/build/outputs/apk/debug/app-debug.apk"
fi

PACKAGE_NAME="${PACKAGE_NAME:-br.com.falacomigo}"
ALLOW_CLEAN_REINSTALL="${ALLOW_CLEAN_REINSTALL:-0}"

if [[ "${2:-}" == "--clean-on-signature-mismatch" ]]; then
  ALLOW_CLEAN_REINSTALL=1
fi

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

set +e
install_output="$("$ADB_BIN" install -r "$APK_PATH" 2>&1)"
install_code=$?
set -e

if [[ "$install_code" -ne 0 ]]; then
  if [[ "$install_output" == *"INSTALL_FAILED_UPDATE_INCOMPATIBLE"* && "$ALLOW_CLEAN_REINSTALL" == "1" ]]; then
    "$ADB_BIN" uninstall "$PACKAGE_NAME" >/dev/null
    "$ADB_BIN" install -r "$APK_PATH"
  else
    echo "$install_output" >&2
    exit "$install_code"
  fi
else
  echo "$install_output"
fi
