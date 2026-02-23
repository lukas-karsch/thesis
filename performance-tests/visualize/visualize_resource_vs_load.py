import argparse
import json
from pathlib import Path

import pandas as pd
import seaborn as sns
from matplotlib import pyplot as plt

from visualize.aggregate import aggregate_timeseries_prometheus_metrics
from visualize.helper import get_metric_json
from visualize.metrics import OPTIONS
from visualize.style import APP_COLORS

THREADPOOL_LIMIT = 200

rps_list = [10, 20, 50, 100, 200, 300, 400, 500]


def visualize_metric(METRIC: dict, base_path: Path, base_name: str) -> None:
    metric_path = get_metric_json(base_path)
    metric_json = json.loads(metric_path.read_text())

    steady_state_start = METRIC["steady_state_start"]
    records = []

    for rps in rps_list:
        df_rps = aggregate_timeseries_prometheus_metrics(
            metric_json_name=METRIC["metric_json_name"],
            directory=base_path,
            base_name=f"{base_name}-{rps}-",
            metric_name=METRIC["metric_name"],
        )

        steady_df = df_rps[df_rps["time_index"] >= steady_state_start]

        for app in ["crud", "es-cqrs"]:
            app_vals = steady_df.loc[steady_df["app"] == app, "value"]

            for v in app_vals:
                records.append(
                    {
                        "RPS": rps,
                        METRIC["df_metric_name"]: v * METRIC.get("y_multiply", 1),
                        "App": app.upper(),
                    }
                )

    plot_df = pd.DataFrame(records)

    plt.figure(figsize=(9, 6))
    sns.set_style("whitegrid")

    sns.lineplot(
        data=plot_df,
        x="RPS",
        y=METRIC["df_metric_name"],
        hue="App",
        palette=APP_COLORS,
        linewidth=2.5,
        estimator="median",
        errorbar=("pi", 50),
        marker="o",
    )

    plt.suptitle(
        f"{metric_json['metric']['method']} {metric_json['metric']['uri']}: {METRIC["title"]}",
    )
    plt.title(metric_json["metadata"]["title"], fontsize=10)
    plt.xlabel("RPS (Requests Per Second)", fontsize=12)
    plt.xticks(rps_list)
    # plt.xticks([25, 200, 500, 1000, 2000, 3000, 4000])
    plt.ylabel(METRIC["ylabel"], fontsize=12)
    plt.tight_layout()
    plt.show()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--path", required=True)
    parser.add_argument("--base_name", required=True)
    parser.add_argument(
        "--metric", choices=["CPU_USAGE", "THREADPOOL_USAGE", "DB_CONNECTIONS"]
    )
    args = parser.parse_args()

    base_path = Path(args.path)
    base_name = args.base_name
    metric = args.metric

    if metric is not None:
        visualize_metric(OPTIONS[metric], base_path, base_name)
    else:
        for _n, option in OPTIONS.items():
            visualize_metric(option, base_path, base_name)

    print("!! Make sure 'rps_list' is set correctly!!")


if __name__ == "__main__":
    main()
