import argparse
import json
from pathlib import Path
from typing import Any

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns

from visualize.aggregate import (
    aggregate_timeseries_prometheus_metrics,
)
from visualize.helper import get_metric_json
from visualize.style import APP_COLORS


def sns_plot_with_sd(df: pd.DataFrame, metric: dict, metric_json: Any):
    sns.set_theme(style="whitegrid")
    plt.figure(figsize=(10, 6))

    sns.lineplot(
        data=df,
        x=df["time_index"] * 5,
        y=df["value"] / 1024 / 1024,
        hue="app",
        palette=APP_COLORS,
        estimator=np.median,
        errorbar=("pi", 90),
    )

    plt.ylim(0)
    plt.xlabel("Time (seconds)")
    plt.ylabel("RAM Usage (MB)")
    plt.suptitle(
        f"{metric['method']} {metric['uri']}: Total RAM Usage Over Time ({metric['rps']} RPS)"
    )
    plt.title(metric_json["metadata"]["title"], fontsize=10)

    plt.show()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--path", required=True)
    parser.add_argument("--base_name", required=True)
    parser.add_argument("--VUs", required=True, type=int)
    args = parser.parse_args()

    base_path = Path(args.path)
    base_name = args.base_name
    VUs = args.VUs

    metric_folder_base_name = f"{base_name}-{VUs}-"

    aggregated = aggregate_timeseries_prometheus_metrics(
        "ram_usage_total_bytes.json",
        base_path,
        metric_folder_base_name,
    )

    metric_path = get_metric_json(base_path)
    metric_json = json.loads(metric_path.read_text())
    metric = {
        "method": metric_json["metric"]["method"],
        "uri": metric_json["metric"]["uri"],
        "rps": VUs,
    }

    sns_plot_with_sd(aggregated, metric, metric_json)


if __name__ == "__main__":
    main()
