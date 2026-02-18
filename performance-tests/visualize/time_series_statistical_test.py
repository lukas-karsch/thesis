import argparse
from pathlib import Path

import pandas as pd
from scipy import stats

from visualize.aggregate import aggregate_timeseries_prometheus_metrics
from visualize.table import render_table


def _get_significance(p: float) -> str:
    if p <= 0.001:
        return "***"
    if p <= 0.01:
        return "**"
    if p <= 0.05:
        return "*"
    return "n.s."


def analyze_performance_by_time(data: pd.DataFrame):
    results = []

    group_cols = ["time_index"]
    if "metric" in data.columns:
        group_cols.append("metric")
    if "virtual_users" in data.columns:
        group_cols.append("virtual_users")

    for keys, group in data.groupby(group_cols, sort=True):
        if isinstance(keys, tuple):
            time_idx = keys[0]
        else:
            time_idx = keys

        crud = group[group["app"] == "crud"]["value"]
        cqrs = group[group["app"] == "es-cqrs"]["value"]

        m_crud, m_cqrs = crud.mean(), cqrs.mean()

        def get_ci(series):
            if len(series) < 2:
                return 0
            return stats.sem(series) * stats.t.ppf(0.975, len(series) - 1)

        try:
            u_stat, p_val = stats.mannwhitneyu(crud, cqrs)
        except ValueError:
            p_val = 1.0

        # Ratio
        ratio = m_crud / m_cqrs if m_cqrs != 0 else 0
        comparison = (
            f"{round(ratio, 1)}x Higher"
            if ratio >= 1
            else f"{round(1/ratio, 1)}x Lower"
        )

        results.append(
            {
                "time_index": time_idx,
                "crud_mean": round(m_crud, 4),
                "crud_ci": round(get_ci(crud), 4),
                "cqrs_mean": round(m_cqrs, 4),
                "cqrs_ci": round(get_ci(cqrs), 4),
                "speedup": comparison,
                "sig": _get_significance(p_val),
            }
        )

    return pd.DataFrame(results)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base_name", required=True)
    parser.add_argument("--path", required=True)
    parser.add_argument("--output_path", required=True)
    parser.add_argument("--omit")
    args = parser.parse_args()

    if args.omit:
        omit = [m.strip() for m in args.omit.split(",")]
        print(f"Metrics to omit: {omit}")
    else:
        omit = []

    folders_base_name = f"{args.base_name}-"
    df = aggregate_timeseries_prometheus_metrics(
        "cpu_usage.json", Path(args.path), folders_base_name, "process_cpu_usage"
    )

    # Generate the full stats table
    stats_table = analyze_performance_by_time(df)

    if omit:
        stats_table = stats_table[~stats_table["metric"].isin(omit)]

    output_base_name = f"{args.base_name}"
    output_path = (
        Path(args.output_path) / f"{output_base_name}_timeseries_statistical_test.csv"
    )

    output_path.parent.mkdir(parents=True, exist_ok=True)

    stats_table.to_csv(output_path, index=False)
    render_table(
        stats_table,
        f"{args.base_name}",
        f"POST /courses with prerequisites",
        Path(args.output_path) / f"{output_base_name}_timeseries_statistical_test.tex",
        type="timeseries",
    )


if __name__ == "__main__":
    main()
