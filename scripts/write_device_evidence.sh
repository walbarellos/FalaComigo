#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

PACKAGE_NAME="${PACKAGE_NAME:-br.com.falacomigo}"
OUT_DIR="${OUT_DIR:-$ROOT_DIR/build/reports/falacomigo}"
OUT_FILE="$OUT_DIR/device-evidence-$(date +%Y%m%d-%H%M%S).md"

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

device_model="$("$ADB_BIN" shell getprop ro.product.model 2>/dev/null | tr -d '\r' || echo unavailable)"
android_version="$("$ADB_BIN" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r' || echo unavailable)"
sdk_version="$("$ADB_BIN" shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r' || echo unavailable)"
package_info="$("$ADB_BIN" shell dumpsys package "$PACKAGE_NAME" 2>/dev/null | rg 'Package \[|versionCode|versionName|firstInstallTime|lastUpdateTime' || true)"

APP_OPEN_GATE="${APP_OPEN_GATE:-PENDENTE}"
BOARD_GATE="${BOARD_GATE:-PENDENTE}"
OFFLINE_GATE="${OFFLINE_GATE:-PENDENTE}"
TTS_GATE="${TTS_GATE:-PENDENTE}"
OBSERVATIONS="${OBSERVATIONS:-}"
SMOKE_REPORT="${SMOKE_REPORT:-$(find "$OUT_DIR" -maxdepth 1 -name 'device-smoke-*.txt' -type f 2>/dev/null | sort | tail -n 1 || true)}"
INSTALL_REPORT="${INSTALL_REPORT:-$(find "$OUT_DIR" -maxdepth 1 -name 'install-release-*.txt' -type f 2>/dev/null | sort | tail -n 1 || true)}"

{
  echo "# FalaComigo Device Evidence"
  echo
  echo "* Data/hora: $(date -Is)"
  echo "* Aparelho: ${device_model:-unavailable}"
  echo "* Android: ${android_version:-unavailable} (SDK ${sdk_version:-unavailable})"
  echo "* Pacote: $PACKAGE_NAME"
  echo "* Relatório de instalação: ${INSTALL_REPORT:-não informado}"
  echo "* Relatório de smoke: ${SMOKE_REPORT:-não informado}"
  echo
  echo "## Package"
  echo
  echo '```text'
  echo "$package_info"
  echo '```'
  echo
  echo "## Gates"
  echo
  echo "* App abre sem crash: $APP_OPEN_GATE"
  echo "* Prancha carrega corretamente: $BOARD_GATE"
  echo "* Offline testado: $OFFLINE_GATE"
  echo "* TTS testado/fallback sem crash: $TTS_GATE"
  echo
  echo "## Smoke"
  echo
  if [[ -n "$SMOKE_REPORT" && -f "$SMOKE_REPORT" ]]; then
    echo '```text'
    sed -n '1,180p' "$SMOKE_REPORT"
    echo '```'
  else
    echo "Sem relatório de smoke anexado."
  fi
  echo
  echo "## Observações"
  echo
  echo "${OBSERVATIONS:-Nenhuma observação registrada.}"
} > "$OUT_FILE"

echo "$OUT_FILE"
