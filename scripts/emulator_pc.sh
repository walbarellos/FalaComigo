#!/usr/bin/env bash
set -euo pipefail

# Runs the Android Emulator in this environment.
# NOTE: The Android Emulator x86_64 requires hardware virtualization (KVM/VT-x) on Linux.
#
# This script assumes the SDK was installed to /tmp/android-sdk (writable) and stores AVD state in /tmp.

SDK_ROOT="${SDK_ROOT:-/tmp/android-sdk}"
AVD_NAME="${AVD_NAME:-falacomigo_api34}"

export ANDROID_SDK_ROOT="$SDK_ROOT"
export ANDROID_HOME="$SDK_ROOT"
export ANDROID_SDK_HOME="${ANDROID_SDK_HOME:-/tmp/android-home}"
export ANDROID_AVD_HOME="${ANDROID_AVD_HOME:-/tmp/android-avd}"

EMULATOR_BIN="$SDK_ROOT/emulator/emulator"

if [[ ! -x "$EMULATOR_BIN" ]]; then
  echo "ERROR: emulator not found at: $EMULATOR_BIN" >&2
  echo "Install it with: /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=$SDK_ROOT emulator" >&2
  exit 1
fi

if [[ ! -e /dev/kvm ]]; then
  echo "ERROR: /dev/kvm not found. The emulator requires KVM/VT-x on Linux for x86_64 images." >&2
  echo "Fix: enable Intel VT-x/AMD-V in BIOS/UEFI and load KVM kernel modules (kvm, kvm_intel/kvm_amd)." >&2
  exit 2
fi

exec "$EMULATOR_BIN" \
  -avd "$AVD_NAME" \
  -no-snapshot \
  -no-audio \
  -gpu swiftshader_indirect \
  -netdelay none \
  -netspeed full \
  -no-metrics

