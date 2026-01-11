#!/usr/bin/env bash
set -euo pipefail

CANDIDATES=(
  "/root/.gradle/caches"
  "${HOME}/.gradle/caches"
)

REAL=""
for base in "${CANDIDATES[@]}"; do
  if [[ -d "$base" ]]; then
    REAL="$(find "$base" -type f -path '*aapt2-*-linux/aapt2' 2>/dev/null | head -n 1 || true)"
    [[ -n "$REAL" ]] && break
  fi
done

if [[ -z "${REAL}" || ! -f "${REAL}" ]]; then
  echo "ERROR: aapt2 not found. Searched in: ${CANDIDATES[*]}" >&2
  echo "Hint: run ./gradlew once so it downloads aapt2 into the Gradle cache." >&2
  exit 1
fi

exec qemu-x86_64 -L /usr/x86_64-linux-gnu "$REAL" "$@"
