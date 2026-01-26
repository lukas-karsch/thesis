#!/bin/bash
set -e

VUS_VALUES=(25 50 100 200 500 1000)

for vus in "${VUS_VALUES[@]}"; do
  echo "Running with VUs=$vus"

  jq --argjson vus "$vus" '.VUs = $vus' k6/writes/create-course-prerequisites/metric.json

  python many_runs.py \
    --metric "k6/writes/create-course-prerequisites/metric.json" \
    --config "vm/.config" \
    --runs 30
done
