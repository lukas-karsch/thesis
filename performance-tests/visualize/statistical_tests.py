import argparse
from pathlib import Path

import pandas as pd
from scipy import stats

from visualize.aggregate import aggregate_metrics_csv
from visualize.table import render_table


def analyze_performance(data: pd.DataFrame):
    results = []

    data.sort_values(["virtual_users", "metric"], inplace=True)

    # Analyze each load level and metric separately
    for (metric, users), group in data.groupby(["metric", "virtual_users"], sort=False):
        crud = group[group["app"] == "crud"]["value"]
        cqrs = group[group["app"] == "es-cqrs"]["value"]

        # Calculate Means
        m_crud, m_cqrs = crud.mean(), cqrs.mean()

        # Calculate 95% Confidence Interval
        def get_ci(series):
            if len(series) < 2:
                return 0
            return stats.sem(series) * stats.t.ppf((1 + 0.95) / 2.0, len(series) - 1)

        # Mann-Whitney U Test (Non-parametric significance)
        u_stat, p_val = stats.mannwhitneyu(crud, cqrs)

        # Speedup Ratio (How many times faster is CQRS?)
        ratio = m_crud / m_cqrs

        if ratio >= 1:
            comparison = f"{round(ratio, 1)}x Faster"
        else:
            comparison = f"{round(1/ratio, 1)}x Slower"

        def _get_significance(p) -> str:
            if p <= 0.001:
                return "***"
            elif p <= 0.01:
                return "**"
            elif p <= 0.05:
                return "*"
            return "n.s."

        results.append(
            {
                "metric": metric,
                "users": users,
                "c_mean": round(m_crud * 1000, 2),
                "c_ci": round(get_ci(crud), 2),
                "e_mean": round(m_cqrs * 1000, 2),
                "e_ci": round(get_ci(cqrs), 2),
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

    for file in ["client", "server"]:
        folders_base_name = f"{args.base_name}-"
        df = aggregate_metrics_csv(folders_base_name, Path(args.path), file)

        stats_table = analyze_performance(df)

        if omit:
            stats_table = stats_table[~stats_table["metric"].isin(omit)]

        output_base_name = f"{args.base_name}_{file}"
        output_path = (
            Path(args.output_path) / f"{output_base_name}_statistical_test.csv"
        )

        output_path.parent.mkdir(parents=True, exist_ok=True)

        stats_table.to_csv(output_path, index=False)
        render_table(
            stats_table,
            f"{args.base_name}_{file}",
            f"POST /courses ({file})",
            Path(args.output_path) / f"{output_base_name}.tex",
        )


if __name__ == "__main__":
    main()
