#!/bin/bash
set -e

VUS_VALUES=(25 50 100 200 500 1000)

METRIC_PATH="k6/reads/read-lectures/metric.json"

for vus in "${VUS_VALUES[@]}"; do
  echo "Running with VUs=$vus"

  jq --argjson vus "$vus" '.VUs = $vus' $METRIC_PATH > metric.tmp.json

  python many_runs.py \
    --metric "metric.tmp.json" \
    --config "vm/.config" \
    --runs 30
done

rm metric.tmp.json
