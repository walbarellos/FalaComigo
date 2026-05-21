#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APK_PATH="${1:-$ROOT_DIR/app/build/outputs/apk/release/app-release.apk}"
PACKAGE_NAME="${PACKAGE_NAME:-br.com.falacomigo}"
ALLOW_CLEAN_REINSTALL="${ALLOW_CLEAN_REINSTALL:-0}"
OUT_DIR="${OUT_DIR:-$ROOT_DIR/build/reports/falacomigo}"
OUT_FILE="$OUT_DIR/install-release-$(date +%Y%m%d-%H%M%S).txt"

mkdir -p "$OUT_DIR"

if [[ "${2:-}" == "--clean-on-signature-mismatch" ]]; then
  ALLOW_CLEAN_REINSTALL=1
fi

if [[ ! -f "$APK_PATH" ]]; then
  echo "ERROR: APK not found: $APK_PATH" >&2
  exit 1
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
  exit 2
fi

connected_devices="$("$ADB_BIN" devices | tail -n +2 | rg '\tdevice$' || true)"
if [[ -z "$connected_devices" ]]; then
  echo "ERROR: no Android device connected via adb." >&2
  exit 3
fi

set +e
install_output="$("$ADB_BIN" install -r "$APK_PATH" 2>&1)"
install_code=$?
set -e
install_action="install -r"

if [[ "$install_code" -ne 0 ]]; then
  if [[ "$install_output" == *"INSTALL_FAILED_UPDATE_INCOMPATIBLE"* ]]; then
    if [[ "$ALLOW_CLEAN_REINSTALL" != "1" ]]; then
      echo "$install_output" >&2
      echo "ERROR: installed package was signed with another certificate." >&2
      echo "Run with --clean-on-signature-mismatch to uninstall $PACKAGE_NAME and install this release APK." >&2
      exit 4
    fi

    "$ADB_BIN" uninstall "$PACKAGE_NAME" >/dev/null
    install_action="uninstall + install -r"
    install_output="$("$ADB_BIN" install -r "$APK_PATH" 2>&1)"
  else
    echo "$install_output" >&2
    exit "$install_code"
  fi
else
  echo "$install_output"
fi

package_output="$("$ADB_BIN" shell dumpsys package "$PACKAGE_NAME" | rg 'Package \[|versionCode|versionName|firstInstallTime|lastUpdateTime' || true)"

{
  echo "FalaComigo release install"
  echo "Date: $(date -Is)"
  echo "APK: $APK_PATH"
  echo "Package: $PACKAGE_NAME"
  echo "Action: $install_action"
  echo
  echo "== adb install =="
  echo "$install_output"
  echo
  echo "== package =="
  echo "$package_output"
} > "$OUT_FILE"

echo "$package_output"
echo "INSTALL REPORT: $OUT_FILE"
