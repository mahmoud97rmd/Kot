#!/usr/bin/env bash
set -euo pipefail

echo "[clean-delete] removing known transitional/suspect files if they exist..."

rm -f app/src/main/java/com/tradingapp/metatrader/app/core/trading/mt5sim/VirtualAccountMt5_Pending.kt || true
rm -f app/src/main/java/com/tradingapp/metatrader/app/core/trading/mt5sim/VirtualAccountMt5_PendingPatch.kt || true
rm -f app/src/main/java/com/tradingapp/metatrader/app/features/tester/export/StrategyTesterExportSnippet.kt || true
rm -f app/src/main/res/layout/activity_strategy_tester_export_stub.xml || true

echo "[clean-delete] done."
