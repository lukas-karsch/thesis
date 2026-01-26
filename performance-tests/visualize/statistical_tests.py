import argparse
from pathlib import Path

import pandas as pd
from scipy import stats

from visualize.aggregate import aggregate_metrics_csv


def analyze_performance(data: pd.DataFrame):
    results = []

    # Analyze each load level and metric separately
    for (metric, users), group in data.groupby(["metric", "virtual_users"]):
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
        speedup = m_crud / m_cqrs

        results.append(
            {
                "Metric": metric,
                "Users": users,
                "CRUD Mean (ms)": round(m_crud, 2),
                "CRUD CI +/-": round(get_ci(crud), 2),
                "CQRS Mean (ms)": round(m_cqrs, 2),
                "CQRS CI +/-": round(get_ci(cqrs), 2),
                "Speedup": f"{round(speedup, 1)}x",
                "p-value": f"{p_val:.4e}",
                "Significant": p_val < 0.05,
            }
        )

    return pd.DataFrame(results)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base_name")
    parser.add_argument("--path")
    parser.add_argument("--output_path")
    args = parser.parse_args()

    df = aggregate_metrics_csv(args.base_name, Path(args.path))
    stats_table = analyze_performance(df)
    print(stats_table.to_string())

    stats_table.to_csv(args.output_path, index=False)


if __name__ == "__main__":
    main()
