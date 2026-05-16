#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APK_PATH="${1:-$ROOT_DIR/app/build/outputs/apk/release/app-release.apk}"

if [[ ! -f "$APK_PATH" ]]; then
  echo "ERROR: APK not found: $APK_PATH" >&2
  exit 1
fi

APKSIGNER_BIN="${APKSIGNER_BIN:-}"
if [[ -z "$APKSIGNER_BIN" ]]; then
  if command -v apksigner >/dev/null 2>&1; then
    APKSIGNER_BIN="$(command -v apksigner)"
  elif [[ -x /opt/android-sdk/build-tools/34.0.0/apksigner ]]; then
    APKSIGNER_BIN="/opt/android-sdk/build-tools/34.0.0/apksigner"
  else
    APKSIGNER_BIN="$(find "${ANDROID_HOME:-/opt/android-sdk}" -path '*/apksigner' -type f 2>/dev/null | sort | tail -n 1 || true)"
  fi
fi

if [[ -z "$APKSIGNER_BIN" || ! -x "$APKSIGNER_BIN" ]]; then
  echo "ERROR: apksigner not found. Install Android build-tools or set APKSIGNER_BIN." >&2
  exit 2
fi

echo "APK: $APK_PATH"
echo "Size: $(wc -c < "$APK_PATH") bytes"
echo "SHA256: $(sha256sum "$APK_PATH" | awk '{print $1}')"
echo

if "$APKSIGNER_BIN" verify --verbose "$APK_PATH"; then
  echo
  echo "SIGNATURE: PASS"
else
  echo
  echo "SIGNATURE: FAIL"
  echo "This APK must not be distributed as a release artifact until it is signed."
  exit 3
fi
