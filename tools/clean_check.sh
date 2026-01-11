#!/usr/bin/env bash
set -euo pipefail

ROOT="$(pwd)"
echo "[clean] project root: $ROOT"

echo
echo "== 1) Find duplicate class/object names that often collide =="
targets=(
  "class VirtualAccountMt5"
  "data class PendingOrderMt5"
  "data class PositionMt5"
  "object ChartMarkerJson"
  "class ExpertSessionMt5"
  "class AutoTradingOrchestrator"
  "class VisualModeSessionMt5"
  "class BacktestRunnerMt5"
  "sealed class OrderCommand"
  "class OrderCommandBus"
)

for t in "${targets[@]}"; do
  echo
  echo "-- Searching: $t"
  grep -RIn --exclude-dir=build --exclude-dir=.gradle --exclude-dir=.idea --include="*.kt" "$t" app/src/main/java || true
done

echo
echo "== 2) List suspicious legacy files (common duplicates) =="
suspects=(
  "VirtualAccountMt5_Pending.kt"
  "VirtualAccountMt5_PendingPatch.kt"
  "StrategyTesterExportSnippet.kt"
  "activity_strategy_tester_export_stub.xml"
)
for s in "${suspects[@]}"; do
  echo "-- $s"
  find app/src -name "$s" -print || true
done

echo
echo "== 3) Kotlin compile sanity commands (you run) =="
echo "Run: ./gradlew :app:assembleDebug"
echo "Run: ./gradlew :app:test"
