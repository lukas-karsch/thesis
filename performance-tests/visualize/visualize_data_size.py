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
    summary_records = []

    for rps in rps_list:
        postgres_size = aggregate_single_val_prometheus_metrics(
            metric_json_name="postgres_size_bytes.json",
            directory=base_path,
            base_name=f"{base_name}-{rps}-",
            metric_name="database_size_bytes",
        )

        for app in ["crud", "es-cqrs"]:
            # Filter the dataframe for the specific app and calculate the median
            pg_value = postgres_size[postgres_size["app"] == app]["value"].median()

            if app == "es-cqrs":
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

                sn = axon_snapshot_size[axon_snapshot_size["app"] == app][
                    "value"
                ].median()
                ev = axon_event_size[axon_event_size["app"] == app]["value"].median()
                summary_records.append(
                    {
                        "RPS": rps,
                        "Median Data Store Size (MB)": pg_value + sn + ev,
                        "App": app,
                    }
                )
            else:
                summary_records.append(
                    {"RPS": rps, "Median Data Store Size (MB)": pg_value, "App": app}
                )

    summary_df = pd.DataFrame(summary_records)

    plt.figure(figsize=(9, 6))
    sns.set_style("whitegrid")

    # Using a lineplot with markers to show the trend
    sns.lineplot(
        data=summary_df,
        x="RPS",
        y=summary_df["Median Data Store Size (MB)"] / 1024 / 1024,
        hue="App",
        palette=APP_COLORS,
        linewidth=2.5,
        errorbar="sd",
    )

    metric_path = get_metric_json(base_path)
    metric_json = json.loads(metric_path.read_text())

    plt.suptitle(
        f"{metric_json["metric"]['method']} {metric_json["metric"]['uri']}: Data Store Size vs. Load",
    )
    plt.title(metric_json["metadata"]["title"], fontsize=10)
    plt.xlabel("RPS (Requests Per Second)", fontsize=12)
    plt.xticks(rps_list)
    plt.ylabel("Median Data Store Size (MB)", fontsize=12)
    plt.ylim(0, 100)
    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    main()
