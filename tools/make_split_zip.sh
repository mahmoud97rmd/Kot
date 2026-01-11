#!/usr/bin/env bash
set -euo pipefail

# You run from project root.
OUT_DIR="dist_zips"
mkdir -p "$OUT_DIR"

BASENAME="project_bundle"
ZIPFILE="$OUT_DIR/${BASENAME}.zip"

echo "[zip] creating $ZIPFILE ..."
# include only relevant project content (exclude build outputs)
zip -r "$ZIPFILE" \
  app \
  gradle \
  build.gradle* \
  settings.gradle* \
  gradlew gradlew.bat \
  gradle.properties \
  local.properties \
  -x "*/build/*" ".gradle/*" ".idea/*" || true

echo "[zip] size:"
ls -lh "$ZIPFILE"

# split by size (e.g., 20MB parts). change to 10m if you want smaller.
PART_SIZE="20m"
echo "[zip] splitting into parts of $PART_SIZE ..."
split -b "$PART_SIZE" -d -a 2 "$ZIPFILE" "$OUT_DIR/${BASENAME}.part"

echo "[zip] done. parts:"
ls -lh "$OUT_DIR"/${BASENAME}.part*

echo
echo "To re-join:"
echo "cat $OUT_DIR/${BASENAME}.part* > $OUT_DIR/${BASENAME}.zip"
echo "unzip -o $OUT_DIR/${BASENAME}.zip -d restored_project"
