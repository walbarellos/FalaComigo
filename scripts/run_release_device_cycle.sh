#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

APK_PATH="${APK_PATH:-$ROOT_DIR/app/build/outputs/apk/release/app-release.apk}"
ALLOW_CLEAN_REINSTALL="${ALLOW_CLEAN_REINSTALL:-1}"
OUT_DIR="${OUT_DIR:-$ROOT_DIR/build/reports/falacomigo}"
mkdir -p "$OUT_DIR"

echo "== Build/test/release =="
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon

echo "== Release safety =="
scripts/check_release_safety.sh

echo "== APK integrity =="
scripts/verify_apk_integrity.sh "$APK_PATH" --write-sha256

echo "== Install on device =="
install_args=("$APK_PATH")
if [[ "$ALLOW_CLEAN_REINSTALL" == "1" ]]; then
  install_args+=("--clean-on-signature-mismatch")
fi
scripts/install_release_apk.sh "${install_args[@]}"

echo "== Installed package =="
scripts/check_installed_package.sh

echo "== Device smoke =="
scripts/device_smoke_check.sh

echo "== Build evidence =="
BUILD_GATE=PASS \
APP_OPEN_GATE=PASS \
RELEASE_SAFETY_GATE=PASS \
APK_HASH_GATE=PASS \
  scripts/write_build_evidence.sh release "$APK_PATH"

echo "== Device evidence =="
APP_OPEN_GATE=PASS \
BOARD_GATE="${BOARD_GATE:-PENDENTE_MANUAL}" \
OFFLINE_GATE="${OFFLINE_GATE:-PENDENTE_MANUAL}" \
TTS_GATE="${TTS_GATE:-PENDENTE_MANUAL}" \
OBSERVATIONS="${OBSERVATIONS:-Ciclo release/device executado por scripts/run_release_device_cycle.sh.}" \
  scripts/write_device_evidence.sh

echo "== Internal package =="
scripts/package_internal_release.sh "$APK_PATH"
