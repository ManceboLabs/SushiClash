#!/usr/bin/env bash
# Generate store/README screenshots via StoreScreenshotTest and pull them into docs/screenshots/.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

DEVICE_SCREENSHOTS_ROOT="/data/local/tmp/sushiclash-screenshots"
HOST_SCREENSHOTS_DIR="${ROOT_DIR}/docs/screenshots"
LOCALES=(en es de fr zh-CN ja)
EXPECTED_FILES=(
  01_onboarding.png
  02_solo.png
  03_group.png
  04_roulette.png
  05_achievements.png
  06_history.png
  07_settings.png
)

resolve_adb() {
  if [[ -n "${ANDROID_HOME:-}" && -x "${ANDROID_HOME}/platform-tools/adb" ]]; then
    echo "${ANDROID_HOME}/platform-tools/adb"
    return
  fi
  if [[ -x "${HOME}/Library/Android/sdk/platform-tools/adb" ]]; then
    echo "${HOME}/Library/Android/sdk/platform-tools/adb"
    return
  fi
  if command -v adb >/dev/null 2>&1; then
    command -v adb
    return
  fi
  echo "error: adb not found. Set ANDROID_HOME or install platform-tools." >&2
  exit 1
}

ADB="$(resolve_adb)"
echo "Using adb: ${ADB}"

# Best-effort: reduce system UI chrome that can appear in captures.
"${ADB}" shell settings put secure accessibility_button_mode 0 >/dev/null 2>&1 || true
"${ADB}" shell settings put secure accessibility_display_magnification_enabled 0 >/dev/null 2>&1 || true
"${ADB}" shell settings put system pointer_location 0 >/dev/null 2>&1 || true
"${ADB}" shell settings put system show_touches 0 >/dev/null 2>&1 || true

echo "Running StoreScreenshotTest on connected device/emulator..."
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.mancebolabs.sushiclash.screenshots.StoreScreenshotTest

echo "Pulling screenshots from ${DEVICE_SCREENSHOTS_ROOT} into ${HOST_SCREENSHOTS_DIR}..."
mkdir -p "${HOST_SCREENSHOTS_DIR}"

# Merge locale dirs from device tmp into docs/screenshots/ (durable path survives clearPackageData).
PULL_STAGING="$(mktemp -d "${TMPDIR:-/tmp}/sushiclash-screenshots.XXXXXX")"
cleanup() {
  rm -rf "${PULL_STAGING}"
}
trap cleanup EXIT

if ! "${ADB}" pull "${DEVICE_SCREENSHOTS_ROOT}/." "${PULL_STAGING}/" >/dev/null; then
  echo "error: adb pull failed for ${DEVICE_SCREENSHOTS_ROOT}" >&2
  echo "Ensure StoreScreenshotTest wrote durable copies and a device is connected." >&2
  exit 1
fi

for locale in "${LOCALES[@]}"; do
  src="${PULL_STAGING}/${locale}"
  dest="${HOST_SCREENSHOTS_DIR}/${locale}"
  if [[ -d "${src}" ]]; then
    mkdir -p "${dest}"
    # Copy PNGs into locale dirs without wiping unrelated host files.
    find "${src}" -maxdepth 1 -type f -name '*.png' -exec cp -f {} "${dest}/" \;
  else
    echo "warning: missing locale directory on device: ${locale}" >&2
  fi
done

echo
echo "Screenshot summary (PNG counts under docs/screenshots/):"
total=0
for locale in "${LOCALES[@]}"; do
  dir="${HOST_SCREENSHOTS_DIR}/${locale}"
  if [[ -d "${dir}" ]]; then
    count="$(find "${dir}" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')"
  else
    count=0
  fi
  printf "  %-6s %s\n" "${locale}" "${count}"
  total=$((total + count))
done
echo "  total  ${total}"

missing=0
for locale in "${LOCALES[@]}"; do
  for name in "${EXPECTED_FILES[@]}"; do
    path="${HOST_SCREENSHOTS_DIR}/${locale}/${name}"
    if [[ ! -f "${path}" ]] || [[ ! -s "${path}" ]]; then
      echo "warning: missing or empty: docs/screenshots/${locale}/${name}" >&2
      missing=$((missing + 1))
    fi
  done
done

if [[ "${missing}" -gt 0 ]]; then
  echo "warning: ${missing} expected screenshot(s) missing or empty." >&2
else
  echo "All expected screenshots present (${#LOCALES[@]} locales x ${#EXPECTED_FILES[@]} files)."
fi
