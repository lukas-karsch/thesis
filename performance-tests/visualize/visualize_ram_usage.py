import argparse
from pathlib import Path

import matplotlib.pyplot as plt

from visualize.aggregate import aggregate_prometheus_metrics


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--path", required=True)
    args = parser.parse_args()

    aggregated = aggregate_prometheus_metrics(
        "ram_usage_total_bytes.json", Path(args.path)
    )

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
    plt.title("RAM Usage Over Time")
    plt.grid(True)
    plt.show()


if __name__ == "__main__":
    main()
