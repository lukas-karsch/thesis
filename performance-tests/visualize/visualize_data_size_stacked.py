import argparse
from pathlib import Path

import seaborn as sns
from matplotlib import pyplot as plt

from visualize.aggregate import aggregate_single_val_prometheus_metrics
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

    es_stack_data = {
        "rps": rps_list,
        "postgres": [],
        "axon_event": [],
        "axon_snapshot": [],
    }

    for rps in rps_list:
        postgres_size = aggregate_single_val_prometheus_metrics(
            metric_json_name="postgres_size_bytes.json",
            directory=base_path,
            base_name=f"{base_name}-{rps}-",
            metric_name="database_size_bytes",
        )

        for app in ["crud", "es-cqrs"]:
            # Calculate median Postgres size
            pg_val = postgres_size[postgres_size["app"] == app]["value"].median() / (
                1024 * 1024
            )

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

                # Calculate Axon components (only for ES-CQRS)
                ev_val = axon_event_size[axon_event_size["app"] == app][
                    "value"
                ].median() / (1024 * 1024)
                sn_val = axon_snapshot_size[axon_snapshot_size["app"] == app][
                    "value"
                ].median() / (1024 * 1024)

                es_stack_data["postgres"].append(pg_val)
                es_stack_data["axon_event"].append(ev_val)
                es_stack_data["axon_snapshot"].append(sn_val)

                # Total size for the summary list (for general comparison)
                total_val = pg_val + ev_val + sn_val
                summary_records.append(
                    {"RPS": rps, "Size": total_val, "App": "es-cqrs"}
                )
            else:
                summary_records.append({"RPS": rps, "Size": pg_val, "App": "crud"})

    # --- PLOTTING ---
    plt.figure(figsize=(10, 6))
    sns.set_style("whitegrid")

    # 1. Plot the CRUD line (Simple)
    crud_data = [r["Size"] for r in summary_records if r["App"] == "crud"]
    plt.plot(
        rps_list,
        crud_data,
        label="CRUD (Postgres)",
        color=APP_COLORS["crud"],
        linewidth=3,
    )

    # 2. Create the STACKED area for ES-CQRS
    # Order: Postgres (bottom), then Events, then Snapshots
    plt.stackplot(
        rps_list,
        es_stack_data["postgres"],
        es_stack_data["axon_event"],
        es_stack_data["axon_snapshot"],
        labels=["ES: Postgres (Projections)", "ES: Axon Events", "ES: Axon Snapshots"],
        colors=["#ff9999", "#ff4d4d", "#b30000"],  # Shades of red
        alpha=0.7,
    )

    # ... labels, titles, and legend ...
    plt.legend(loc="upper left")
    plt.xlabel("RPS")
    plt.ylabel("Size (MB)")
    plt.show()


if __name__ == "__main__":
    main()
