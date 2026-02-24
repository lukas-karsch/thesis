#!/bin/bash
set -e

VUS_VALUES=(25 50 100 200 500 1000)

METRIC_PATH="k6/reads/read-all-lectures/metric.json"
TMP_METRIC_PATH="k6/reads/read-all-lectures/metric.tmp.json"

for vus in "${VUS_VALUES[@]}"; do
  echo "Running with VUs=$vus"

  jq --argjson vus "$vus" '.VUs = $vus' $METRIC_PATH > $TMP_METRIC_PATH

  python many_runs.py \
    --metric $TMP_METRIC_PATH \
    --config "vm/.config" \
    --runs 25
done
