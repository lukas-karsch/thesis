import argparse
from pathlib import Path

import pandas as pd
from scipy import stats

from visualize.aggregate import aggregate_timeseries_prometheus_metrics
from visualize.helper import get_significance, get_median_ci
from visualize.metrics import OPTIONS
from visualize.table import render_table


def analyze_performance_per_rps(data: pd.DataFrame):
    results = []

    # Group by RPS (virtual_users)
    for rps, group in data.groupby("rps", sort=True):
        # Extract series for both apps at this RPS level
        crud_values = group[group["app"] == "crud"]["value"]
        cqrs_values = group[group["app"] == "es-cqrs"]["value"]

        med_crud, med_cqrs = crud_values.median(), cqrs_values.median()

        try:
            _, p_val = stats.mannwhitneyu(crud_values, cqrs_values)
        except ValueError:
            p_val = 1.0

        ratio = med_crud / med_cqrs if med_cqrs != 0 else 0
        if ratio != 0:
            comparison = (
                f"{round(ratio, 1)}x Lower"
                if ratio >= 1
                else f"{round(1/ratio, 1)}x Higher"
            )
        else:
            comparison = "NaN"

        results.append(
            {
                "rps": rps,
                "crud_median": round(med_crud, 4),
                "crud_ci": round(get_median_ci(crud_values), 4),
                "cqrs_median": round(med_cqrs, 4),
                "cqrs_ci": round(get_median_ci(cqrs_values), 4),
                "ratio": comparison,
                "sig": get_significance(p_val),
            }
        )

    return pd.DataFrame(results)


def do_analysis(
    path: Path, base_name: str, output_path: Path, metric: dict, endpoint: str
):
    steady_state_start = metric.get("steady_state_start", 0)

    summary = []

    rps_list = [25, 50, 100, 200, 500, 1000]
    # Generate the full stats table
    for rps in rps_list:
        df_per_rps = aggregate_timeseries_prometheus_metrics(
            metric["metric_json_name"],
            path,
            f"{base_name}-{rps}-",
            metric["metric_name"],
        )

        df_per_rps["rps"] = rps

        steady_df = df_per_rps[df_per_rps["time_index"] >= steady_state_start]
        summary.append(steady_df)

    df = pd.concat(summary)

    stats_table = analyze_performance_per_rps(df)

    output_base_name = f"{base_name}"
    name = metric["df_metric_name"].replace(" ", "_")
    csv_path = output_path / f"{output_base_name}_{name}.csv"

    csv_path.parent.mkdir(parents=True, exist_ok=True)

    stats_table.to_csv(csv_path, index=False)
    render_table(
        stats_table,
        f"{base_name}-aggregated-{name}",
        f"{metric["metric_output_name"]} for {endpoint}",
        output_path / f"{output_base_name}_{name}.tex",
        type="aggregated_timeseries",
    )


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base_name", required=True)
    parser.add_argument("--path", required=True)
    parser.add_argument("--output_path", required=True)
    args = parser.parse_args()

    for _, option in OPTIONS.items():
        do_analysis(
            Path(args.path),
            args.base_name,
            Path(args.output_path),
            option,
            "GET /lectures",
        )

    print("Done. Make sure 'rps_list' is correct!")


if __name__ == "__main__":
    main()
