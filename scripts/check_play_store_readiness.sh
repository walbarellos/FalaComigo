#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

AAB_PATH="${1:-$ROOT_DIR/app/build/outputs/bundle/release/app-release.aab}"
failures=0
warnings=0

fail() {
  failures=$((failures + 1))
  echo "FAIL: $1" >&2
}

warn() {
  warnings=$((warnings + 1))
  echo "WARN: $1"
}

pass() {
  echo "PASS: $1"
}

echo "Play Store readiness gate"
echo "AAB: $AAB_PATH"
echo

rg -n 'applicationId = "br\.com\.falacomigo"' app/build.gradle.kts >/dev/null && pass "applicationId fixed" || fail "applicationId missing"
rg -n 'versionCode = [0-9]+' app/build.gradle.kts >/dev/null && pass "versionCode present" || fail "versionCode missing"
rg -n 'versionName = "[^"]+"' app/build.gradle.kts >/dev/null && pass "versionName present" || fail "versionName missing"
rg -n 'targetSdk = 34' app/build.gradle.kts >/dev/null && pass "targetSdk declared" || fail "targetSdk missing"
rg -n 'isMinifyEnabled = true' app/build.gradle.kts >/dev/null && pass "release minify enabled" || warn "release minify not enabled"
rg -n 'isShrinkResources = true' app/build.gradle.kts >/dev/null && pass "release shrink resources enabled" || warn "release shrink resources not enabled"
rg -n 'android:label="@string/app_name"' app/src/main/AndroidManifest.xml >/dev/null && pass "app label uses string resource" || fail "app label missing"
rg -n 'android:icon="@drawable/' app/src/main/AndroidManifest.xml >/dev/null && pass "app icon declared" || fail "app icon missing"

if [[ -f "$AAB_PATH" ]]; then
  pass "AAB artifact exists"
  echo "AAB size: $(wc -c < "$AAB_PATH") bytes"
  echo "AAB SHA256: $(sha256sum "$AAB_PATH" | awk '{print $1}')"
else
  warn "AAB artifact not generated yet. Run ./gradlew :app:bundleRelease only when gate 315 is in scope."
fi

if [[ "$failures" -eq 0 ]]; then
  echo
  echo "PLAY STORE READINESS: PASS with $warnings warning(s)"
else
  echo
  echo "PLAY STORE READINESS: FAIL ($failures issue(s), $warnings warning(s))" >&2
  exit 1
fi
