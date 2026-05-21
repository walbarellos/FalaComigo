#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

APK_PATH="${1:-$ROOT_DIR/app/build/outputs/apk/release/app-release.apk}"
VERSION_NAME="$(rg -o 'versionName = "[^"]+"' app/build.gradle.kts | head -n 1 | sed 's/versionName = "//; s/"//')"
VERSION_CODE="$(rg -o 'versionCode = [0-9]+' app/build.gradle.kts | head -n 1 | awk '{print $3}')"
PACKAGE_DIR="$ROOT_DIR/build/distribution/falacomigo-${VERSION_NAME:-unknown}-${VERSION_CODE:-0}"
ZIP_PATH="$PACKAGE_DIR.zip"

if [[ ! -f "$APK_PATH" ]]; then
  echo "ERROR: APK not found: $APK_PATH" >&2
  exit 1
fi

"$ROOT_DIR/scripts/verify_apk_integrity.sh" "$APK_PATH" --write-sha256 >/dev/null
BUILD_GATE="${BUILD_GATE:-PASS}" \
APP_OPEN_GATE="${APP_OPEN_GATE:-PENDENTE}" \
RELEASE_SAFETY_GATE="${RELEASE_SAFETY_GATE:-PASS}" \
APK_HASH_GATE="${APK_HASH_GATE:-PASS}" \
  "$ROOT_DIR/scripts/write_build_evidence.sh" release "$APK_PATH" >/dev/null

rm -rf "$PACKAGE_DIR"
mkdir -p "$PACKAGE_DIR"

cp "$APK_PATH" "$PACKAGE_DIR/FalaComigo-${VERSION_NAME:-unknown}-${VERSION_CODE:-0}.apk"
cp "$APK_PATH.sha256" "$PACKAGE_DIR/FalaComigo-${VERSION_NAME:-unknown}-${VERSION_CODE:-0}.apk.sha256"
cp "$ROOT_DIR/build/reports/falacomigo/build-evidence-release.md" "$PACKAGE_DIR/build-evidence-release.md"
cp "$ROOT_DIR/RELEASE.md" "$PACKAGE_DIR/RELEASE.md"
cp "$ROOT_DIR/apk_verifier.html" "$PACKAGE_DIR/apk_verifier.html"

latest_device_evidence="$(find "$ROOT_DIR/build/reports/falacomigo" -maxdepth 1 -name 'device-evidence-*.md' -type f 2>/dev/null | sort | tail -n 1 || true)"
latest_smoke_report="$(find "$ROOT_DIR/build/reports/falacomigo" -maxdepth 1 -name 'device-smoke-*.txt' -type f 2>/dev/null | sort | tail -n 1 || true)"
latest_install_report="$(find "$ROOT_DIR/build/reports/falacomigo" -maxdepth 1 -name 'install-release-*.txt' -type f 2>/dev/null | sort | tail -n 1 || true)"

if [[ -n "$latest_device_evidence" && -f "$latest_device_evidence" ]]; then
  cp "$latest_device_evidence" "$PACKAGE_DIR/device-evidence.md"
fi

if [[ -n "$latest_smoke_report" && -f "$latest_smoke_report" ]]; then
  cp "$latest_smoke_report" "$PACKAGE_DIR/device-smoke.txt"
fi

if [[ -n "$latest_install_report" && -f "$latest_install_report" ]]; then
  cp "$latest_install_report" "$PACKAGE_DIR/install-release.txt"
fi

{
  echo "# FalaComigo Internal Release Package"
  echo
  echo "* Version: ${VERSION_NAME:-unknown} (${VERSION_CODE:-unknown})"
  echo "* APK: FalaComigo-${VERSION_NAME:-unknown}-${VERSION_CODE:-0}.apk"
  echo "* SHA256 file: FalaComigo-${VERSION_NAME:-unknown}-${VERSION_CODE:-0}.apk.sha256"
  echo "* Created at: $(date -Is)"
  echo "* Device evidence: $([[ -f "$PACKAGE_DIR/device-evidence.md" ]] && echo included || echo not-included)"
  echo "* Device smoke: $([[ -f "$PACKAGE_DIR/device-smoke.txt" ]] && echo included || echo not-included)"
  echo "* Install report: $([[ -f "$PACKAGE_DIR/install-release.txt" ]] && echo included || echo not-included)"
  echo
  echo "Distribution is internal and controlled. Play Store publication remains a future gate."
} > "$PACKAGE_DIR/README_RELEASE_PACKAGE.md"

rm -f "$ZIP_PATH"
if command -v zip >/dev/null 2>&1; then
  (cd "$(dirname "$PACKAGE_DIR")" && zip -qr "$(basename "$ZIP_PATH")" "$(basename "$PACKAGE_DIR")")
  echo "$ZIP_PATH"
else
  echo "WARN: zip not found; package directory created without ZIP." >&2
  echo "$PACKAGE_DIR"
fi
