import argparse
import json
from pathlib import Path

import pandas as pd
import seaborn as sns
from matplotlib import pyplot as plt

from visualize.aggregate import aggregate_single_val_prometheus_metrics
from visualize.helper import get_metric_json
from visualize.style import APP_COLORS


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--path", required=True)
    parser.add_argument("--base_name", required=True)
    args = parser.parse_args()

    base_path = Path(args.path)
    base_name = args.base_name

    rps_list = [25, 50, 100, 200, 500, 1000]
    records = []

    for rps in rps_list:
        postgres_size = aggregate_single_val_prometheus_metrics(
            metric_json_name="postgres_size_bytes.json",
            directory=base_path,
            base_name=f"{base_name}-{rps}-",
            metric_name="database_size_bytes",
        )

        # load Axon once per RPS (used only for es-cqrs)
        axon_snapshot_size = aggregate_single_val_prometheus_metrics(
            metric_json_name="axon_storage_size_bytes.json",
            directory=base_path,
            base_name=f"{base_name}-{rps}-",
            metric_name="axon_storage_size",
            job="axonserver",
            type="SNAPSHOT",
        )
        axon_event_size = aggregate_single_val_prometheus_metrics(
            metric_json_name="axon_storage_size_bytes.json",
            directory=base_path,
            base_name=f"{base_name}-{rps}-",
            metric_name="axon_storage_size",
            job="axonserver",
            type="EVENT",
        )

        for app in ["crud", "es-cqrs"]:
            pg_vals = postgres_size.loc[postgres_size["app"] == app, "value"]

            if app == "es-cqrs":
                sn_vals = axon_snapshot_size.loc[
                    axon_snapshot_size["app"] == app, "value"
                ]
                ev_vals = axon_event_size.loc[axon_event_size["app"] == app, "value"]

                # align by index; adjust if your data needs a different join key
                total_vals = (
                    pg_vals.reset_index(drop=True)
                    + sn_vals.reset_index(drop=True)
                    + ev_vals.reset_index(drop=True)
                )
            else:
                total_vals = pg_vals.reset_index(drop=True)

            for v in total_vals:
                records.append(
                    {
                        "RPS": rps,
                        "Data Store Size (MB)": v / 1024 / 1024,
                        "App": app.upper(),
                    }
                )

    plot_df = pd.DataFrame(records)

    plt.figure(figsize=(9, 6))
    sns.set_style("whitegrid")

    sns.lineplot(
        data=plot_df,
        x="RPS",
        y="Data Store Size (MB)",
        hue="App",
        palette=APP_COLORS,
        linewidth=2.5,
        estimator="median",
        errorbar=("pi", 50),
        marker="o",
    )

    metric_path = get_metric_json(base_path)
    metric_json = json.loads(metric_path.read_text())

    plt.suptitle(
        f"{metric_json['metric']['method']} {metric_json['metric']['uri']}: Data Store Size vs. Load",
    )
    plt.title(metric_json["metadata"]["title"], fontsize=10)
    plt.xlabel("RPS (Requests Per Second)", fontsize=12)
    plt.xticks(rps_list)
    plt.ylabel("Median Data Store Size (MB)", fontsize=12)
    plt.ylim(0)
    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    main()
