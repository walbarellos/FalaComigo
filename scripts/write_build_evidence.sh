#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

BUILD_TYPE="${1:-release}"
APK_PATH="${2:-$ROOT_DIR/app/build/outputs/apk/$BUILD_TYPE/app-$BUILD_TYPE.apk}"
OUT_DIR="${OUT_DIR:-$ROOT_DIR/build/reports/falacomigo}"
OUT_FILE="$OUT_DIR/build-evidence-$BUILD_TYPE.md"

mkdir -p "$OUT_DIR"

version_name="$(rg -o 'versionName = "[^"]+"' app/build.gradle.kts | head -n 1 | sed 's/versionName = "//; s/"//')"
version_code="$(rg -o 'versionCode = [0-9]+' app/build.gradle.kts | head -n 1 | awk '{print $3}')"
git_ref="unavailable"
if command -v git >/dev/null 2>&1 && git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  git_ref="$(git rev-parse --short HEAD 2>/dev/null || echo unavailable)"
fi

apk_status="missing"
apk_size="-"
apk_sha256="-"
if [[ -f "$APK_PATH" ]]; then
  apk_status="present"
  apk_size="$(wc -c < "$APK_PATH") bytes"
  apk_sha256="$(sha256sum "$APK_PATH" | awk '{print $1}')"
fi

BUILD_GATE="${BUILD_GATE:-PENDENTE}"
APP_OPEN_GATE="${APP_OPEN_GATE:-PENDENTE}"
BOARD_GATE="${BOARD_GATE:-PENDENTE}"
TTS_GATE="${TTS_GATE:-PENDENTE}"
RELEASE_SAFETY_GATE="${RELEASE_SAFETY_GATE:-PENDENTE}"
APK_HASH_GATE="${APK_HASH_GATE:-PENDENTE}"

{
  echo "# FalaComigo Build Evidence"
  echo
  echo "* Data/hora: $(date -Is)"
  echo "* Build type: $BUILD_TYPE"
  echo "* Versão: ${version_name:-unknown} (${version_code:-unknown})"
  echo "* Git ref: $git_ref"
  echo "* JDK: $(java -version 2>&1 | head -n 1)"
  echo "* Gradle wrapper: ./gradlew"
  echo "* Android SDK: ${ANDROID_HOME:-/opt/android-sdk}"
  echo "* APK: $APK_PATH"
  echo "* APK status: $apk_status"
  echo "* APK size: $apk_size"
  echo "* SHA256: $apk_sha256"
  echo
  echo "## Gates"
  echo
  echo "* Build compila sem erros: $BUILD_GATE"
  echo "* App abre sem crash: $APP_OPEN_GATE"
  echo "* Prancha carrega corretamente: $BOARD_GATE"
  echo "* TTS funciona/fallback sem crash: $TTS_GATE"
  echo "* Release sem segredos/logs sensíveis: $RELEASE_SAFETY_GATE"
  echo "* APK verificável por hash: $APK_HASH_GATE"
} > "$OUT_FILE"

echo "$OUT_FILE"
