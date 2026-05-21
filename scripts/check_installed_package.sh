#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

PACKAGE_NAME="${PACKAGE_NAME:-br.com.falacomigo}"
EXPECTED_VERSION_CODE="${EXPECTED_VERSION_CODE:-$(rg -o 'versionCode = [0-9]+' app/build.gradle.kts | head -n 1 | awk '{print $3}')}"
EXPECTED_VERSION_NAME="${EXPECTED_VERSION_NAME:-$(rg -o 'versionName = "[^"]+"' app/build.gradle.kts | head -n 1 | sed 's/versionName = "//; s/"//')}"

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

package_info="$("$ADB_BIN" shell dumpsys package "$PACKAGE_NAME")"
installed_version_code="$(echo "$package_info" | rg -o 'versionCode=[0-9]+' | head -n 1 | sed 's/versionCode=//')"
installed_version_name="$(echo "$package_info" | rg -o 'versionName=[^ ]+' | head -n 1 | sed 's/versionName=//')"

echo "Package: $PACKAGE_NAME"
echo "Expected versionCode: $EXPECTED_VERSION_CODE"
echo "Installed versionCode: ${installed_version_code:-missing}"
echo "Expected versionName: $EXPECTED_VERSION_NAME"
echo "Installed versionName: ${installed_version_name:-missing}"

if [[ "$installed_version_code" != "$EXPECTED_VERSION_CODE" ]]; then
  echo "INSTALLED PACKAGE: FAIL (versionCode mismatch)" >&2
  exit 4
fi

if [[ "$installed_version_name" != "$EXPECTED_VERSION_NAME" ]]; then
  echo "INSTALLED PACKAGE: FAIL (versionName mismatch)" >&2
  exit 5
fi

echo "INSTALLED PACKAGE: PASS"
