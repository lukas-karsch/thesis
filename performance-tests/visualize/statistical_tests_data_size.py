import argparse
from pathlib import Path

import pandas as pd
from scipy import stats

from visualize.aggregate import aggregate_single_val_prometheus_metrics
from visualize.helper import get_significance, get_ratio_representation, get_median_ci
from visualize.table import render_table


def analyze_performance_per_rps(data: pd.DataFrame):
    results = []

    for rps, group in data.groupby("rps", sort=True):
        crud_values = group[group["app"] == "crud"]["value"]
        cqrs_values = group[group["app"] == "es-cqrs"]["value"]

        # Calculate Medians and Means
        med_crud, med_cqrs = crud_values.median(), cqrs_values.median()

        try:
            _, p_val = stats.mannwhitneyu(crud_values, cqrs_values)
        except (ValueError, TypeError):
            p_val = 1.0

        comparison = get_ratio_representation(med_crud, med_cqrs)

        results.append(
            {
                "rps": rps,
                "crud_median": round(med_crud, 2),
                "crud_ci": round(get_median_ci(crud_values), 2),
                "cqrs_median": round(med_cqrs, 2),
                "cqrs_ci": round(get_median_ci(cqrs_values), 2),
                "ratio": comparison,
                "sig": get_significance(p_val),
            }
        )

    return pd.DataFrame(results)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base_name", required=True)
    parser.add_argument("--path", required=True)
    parser.add_argument("--output_path", required=True)
    args = parser.parse_args()

    base_path = Path(args.path)
    rps_list = [10, 20, 50, 100, 200, 300, 400, 500]
    all_data_frames = []

    for rps in rps_list:
        # 1. Fetch Postgres Size
        pg_df = aggregate_single_val_prometheus_metrics(
            metric_json_name="postgres_size_bytes.json",
            directory=base_path,
            base_name=f"{args.base_name}-{rps}-",
            metric_name="database_size_bytes",
        )

        # 2. Fetch Axon Sizes
        axon_sn_df = aggregate_single_val_prometheus_metrics(
            metric_json_name="axon_storage_size_bytes.json",
            directory=base_path,
            base_name=f"{args.base_name}-{rps}-",
            metric_name="axon_storage_size",
            job="axonserver",
            type="SNAPSHOT",
        )
        axon_ev_df = aggregate_single_val_prometheus_metrics(
            metric_json_name="axon_storage_size_bytes.json",
            directory=base_path,
            base_name=f"{args.base_name}-{rps}-",
            metric_name="axon_storage_size",
            job="axonserver",
            type="EVENT",
        )

        # Process each app to calculate the total MB
        for app in ["crud", "es-cqrs"]:
            pg_vals = pg_df[pg_df["app"] == app]["value"].reset_index(drop=True)

            if app == "es-cqrs":
                sn_vals = axon_sn_df[axon_sn_df["app"] == app]["value"].reset_index(
                    drop=True
                )
                ev_vals = axon_ev_df[axon_ev_df["app"] == app]["value"].reset_index(
                    drop=True
                )

                total_mb = (pg_vals + sn_vals + ev_vals) / 1024 / 1024
            else:
                total_mb = pg_vals / 1024 / 1024

            temp_df = pd.DataFrame({"value": total_mb, "app": app, "rps": rps})
            all_data_frames.append(temp_df)

    full_df = pd.concat(all_data_frames, ignore_index=True)

    stats_table = analyze_performance_per_rps(full_df)

    output_base_name = f"{args.base_name}"
    output_dir = Path(args.output_path)
    output_dir.mkdir(parents=True, exist_ok=True)

    # Save CSV
    csv_path = output_dir / f"{output_base_name}_datastore_size.csv"
    stats_table.to_csv(csv_path, index=False)

    # Render LaTeX Table
    tex_path = output_dir / f"{output_base_name}_datastore_size.tex"
    render_table(
        stats_table,
        f"{args.base_name}-aggregated-datastore-size",
        f"Data Store Size (MB) for {args.base_name}",
        tex_path,
        type="aggregated_timeseries",
    )

    print(f"Done. Statistics and LaTeX table generated in {output_dir}")
    print(f"Is this 'rps_list' correct? {rps_list}")


if __name__ == "__main__":
    main()
