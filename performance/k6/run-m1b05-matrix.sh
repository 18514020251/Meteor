#!/usr/bin/env bash
set -euo pipefail

: "${TOKEN:?TOKEN is required}"
: "${SCREENING_ID:?SCREENING_ID is required}"
BASE_URL="${BASE_URL:-http://127.0.0.1:8085}"
DURATION="${DURATION:-30s}"
VUS_LIST="${VUS_LIST:-50 100 150 200}"

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
mkdir -p "$ROOT/results"

vus=( $VUS_LIST )
for i in "${!vus[@]}"; do
  vu="${vus[$i]}"
  run_id="m1b05-vu${vu}-$(date +%Y%m%d-%H%M%S)"
  out="$ROOT/results/${run_id}.json"
  echo "=== $run_id ==="
  k6 run \
    --summary-export "$out" \
    -e TOKEN="$TOKEN" \
    -e SCREENING_ID="$SCREENING_ID" \
    -e BASE_URL="$BASE_URL" \
    -e VUS="$vu" \
    -e DURATION="$DURATION" \
    -e RUN_ID="$run_id" \
    "$ROOT/grab-m1b05-baseline.js"
  echo "Result: $out"
  if (( i < ${#vus[@]} - 1 )); then
    read -r -p "Prepare a fresh high-stock screening, then enter SCREENING_ID: " SCREENING_ID
  fi
done
