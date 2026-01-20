import argparse
import json
from pathlib import Path

import matplotlib.pyplot as plt

from visualize.aggregate import aggregate_prometheus_metrics
from visualize.helper import get_metric_json


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--path", required=True)
    args = parser.parse_args()

    base_path = Path(args.path)

    aggregated = aggregate_prometheus_metrics("ram_usage_total_bytes.json", base_path)

    metric_path = get_metric_json(base_path)
    metric_json = json.loads(metric_path.read_text())
    VUs = metric_json.get("VUs")
    if VUs is None:
        raise ValueError("Malformed metric.json: 'VUs' field is missing.")
    metric = {
        "method": metric_json["metric"]["method"],
        "uri": metric_json["metric"]["uri"],
    }
    crud_df = aggregated["crud"]
    es_df = aggregated["es-cqrs"]

    ram_divisor = 1024 * 1024  # bytes to megabytes

    plt.figure()
    plt.plot(
        crud_df["time_index"] * 5,
        crud_df["median_value"] / ram_divisor,
        label="CRUD",
    )
    plt.plot(
        es_df["time_index"] * 5,
        es_df["median_value"] / ram_divisor,
        label="ES CQRS",
    )
    plt.xlabel("Time (seconds)")
    plt.ylabel("RAM Usage (Megabytes)")
    plt.suptitle(f"{metric["method"]} {metric["uri"]}: RAM Usage Over Time")
    plt.title(metric_json["metadata"]["title"], fontsize=10)

    plt.legend()

    plt.grid(True)
    plt.show()


if __name__ == "__main__":
    main()
