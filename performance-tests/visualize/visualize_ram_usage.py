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

    plt.figure()
    plt.plot(crud_df["time_index"] * 5, crud_df["median_value"], label="CRUD")
    plt.plot(es_df["time_index"] * 5, es_df["median_value"], label="ES CQRS")
    plt.xlabel("Time (seconds)")
    plt.ylabel("RAM Usage")
    plt.title("RAM Usage Over Time")
    plt.grid(True)
    plt.show()


if __name__ == "__main__":
    main()
