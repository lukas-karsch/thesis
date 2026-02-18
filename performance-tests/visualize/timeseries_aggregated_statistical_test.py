import argparse
from pathlib import Path

import pandas as pd
from scipy import stats

from visualize.aggregate import aggregate_timeseries_prometheus_metrics
from visualize.table import render_table


def analyze_performance_per_rps(data: pd.DataFrame):
    results = []

    # Group by RPS (virtual_users)
    for rps, group in data.groupby("rps", sort=True):
        # Extract series for both apps at this RPS level
        crud_values = group[group["app"] == "crud"]["value"]
        cqrs_values = group[group["app"] == "es-cqrs"]["value"]

        # Calculate Medians (requested) and Means (for Ratio)
        med_crud, med_cqrs = crud_values.median(), cqrs_values.median()
        m_crud, m_cqrs = crud_values.mean(), cqrs_values.mean()

        def get_ci(series):
            if len(series) < 2:
                return 0
            return stats.sem(series) * stats.t.ppf(0.975, len(series) - 1)

        # Statistical significance across all samples at this RPS
        try:
            _, p_val = stats.mannwhitneyu(crud_values, cqrs_values)
        except ValueError:
            p_val = 1.0

        def _get_significance(p):
            if p <= 0.001:
                return "***"
            if p <= 0.01:
                return "**"
            if p <= 0.05:
                return "*"
            return "n.s."

        ratio = m_crud / m_cqrs if m_cqrs != 0 else 0
        comparison = (
            f"{round(ratio, 1)}x Lower"
            if ratio >= 1
            else f"{round(1/ratio, 1)}x Higher"
        )

        results.append(
            {
                "rps": rps,
                "crud_median": round(med_crud, 4),
                "crud_ci": round(get_ci(crud_values), 4),
                "cqrs_median": round(med_cqrs, 4),
                "cqrs_ci": round(get_ci(cqrs_values), 4),
                "ratio": comparison,
                "sig": _get_significance(p_val),
            }
        )

    return pd.DataFrame(results)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base_name", required=True)
    parser.add_argument("--path", required=True)
    parser.add_argument("--output_path", required=True)
    args = parser.parse_args()

    steady_state_start = 8

    summary = []

    rps_list = [25, 50, 100, 200, 500, 1000]
    # Generate the full stats table
    for rps in rps_list:
        df_per_rps = aggregate_timeseries_prometheus_metrics(
            "cpu_usage.json",
            Path(args.path),
            f"{args.base_name}-{rps}-",
            "process_cpu_usage",
        )

        df_per_rps["rps"] = rps

        steady_df = df_per_rps[df_per_rps["time_index"] >= steady_state_start]
        summary.append(steady_df)

    df = pd.concat(summary)

    stats_table = analyze_performance_per_rps(df)

    output_base_name = f"{args.base_name}"
    output_path = (
        Path(args.output_path)
        / f"{output_base_name}_timeseries_aggregated_statistical_test.csv"
    )

    output_path.parent.mkdir(parents=True, exist_ok=True)

    stats_table.to_csv(output_path, index=False)
    render_table(
        stats_table,
        f"{args.base_name}-aggregated-cpu-usage",
        f"$cpu\_usage$ for POST /courses with prerequisites",
        Path(args.output_path)
        / f"{output_base_name}_timeseries_aggregated_statistical_test.tex",
        type="aggregated_timeseries",
    )


if __name__ == "__main__":
    main()
