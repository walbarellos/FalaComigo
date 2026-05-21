#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

failures=0

check_fail() {
  failures=$((failures + 1))
  echo "FAIL: $1" >&2
}

check_pass() {
  echo "PASS: $1"
}

echo "Release safety gate"
echo "Root: $ROOT_DIR"
echo

if command -v git >/dev/null 2>&1 && git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  tracked_secrets="$(
    git ls-files | rg '(^|/)(key\.properties|local\.properties|google-services\.json|[^/]+\.(jks|keystore|p12|pem|pk8))$' || true
  )"
  if [[ -n "$tracked_secrets" ]]; then
    check_fail "tracked signing/secret files found"
    echo "$tracked_secrets" >&2
  else
    check_pass "no tracked signing/secret files"
  fi

  if git check-ignore -q key.properties; then
    check_pass "key.properties is ignored"
  else
    check_fail "key.properties is not ignored"
  fi

  if git check-ignore -q app/signing/falacomigo-release.jks 2>/dev/null; then
    check_pass "app/signing release keystore path is ignored"
  else
    check_fail "app/signing release keystore path is not ignored"
  fi
else
  echo "WARN: git unavailable; tracked-secret checks skipped"
fi

log_hits="$(
  rg -n 'Log\.|println\(|System\.(out|err)' app/src/main/java/br/com/falacomigo || true
)"
if [[ -n "$log_hits" ]]; then
  check_fail "runtime logs found in production Kotlin source"
  echo "$log_hits" >&2
else
  check_pass "no println/System.out/System.err/Log.* in production source"
fi

if rg -n 'HttpLoggingInterceptor\.Level\.BODY' app/src/main/java app/build.gradle.kts build.gradle.kts >/tmp/falacomigo_body_logging_hits.txt 2>/dev/null; then
  check_fail "HTTP BODY logging found"
  cat /tmp/falacomigo_body_logging_hits.txt >&2
else
  check_pass "HTTP BODY logging absent"
fi

if rg -n 'BuildConfig\.DEBUG|Level\.NONE' app/src/main/java/br/com/falacomigo/di/NetworkModule.kt >/dev/null 2>&1; then
  check_pass "network logging is build-type gated"
else
  check_fail "network logging gate not found"
fi

echo
if [[ "$failures" -eq 0 ]]; then
  echo "RELEASE SAFETY: PASS"
else
  echo "RELEASE SAFETY: FAIL ($failures issue(s))" >&2
  exit 1
fi
