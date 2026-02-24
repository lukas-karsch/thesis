import argparse
from pathlib import Path

import pandas as pd
from scipy import stats

from visualize.aggregate import aggregate_metrics_csv
from visualize.helper import get_median_ci
from visualize.table import render_table


def analyze_performance(data: pd.DataFrame):
    results = []

    data.sort_values(["virtual_users", "metric"], inplace=True)

    for (metric, users), group in data.groupby(["metric", "virtual_users"], sort=False):
        crud = group[group["app"] == "crud"]["value"]
        cqrs = group[group["app"] == "es-cqrs"]["value"]

        # Calculate medians
        med_crud, med_cqrs = crud.median(), cqrs.median()

        # Mann-Whitney U Test (Non-parametric significance)
        u_stat, p_val = stats.mannwhitneyu(crud, cqrs)

        # Speedup Ratio (How many times faster is CQRS?)
        if med_cqrs > 0:
            ratio = med_crud / med_cqrs
        else:
            ratio = float("inf") if med_crud > 0 else float("nan")

        if ratio >= 1:
            comparison = f"{round(ratio, 1)}x Faster"
        elif 0 < ratio < 1:
            comparison = f"{round(1/ratio, 1)}x Slower"
        else:
            comparison = "NaN"

        def _get_significance(p) -> str:
            if p <= 0.001:
                return "***"
            elif p <= 0.01:
                return "**"
            elif p <= 0.05:
                return "*"
            return "n.s."

        med_crud = (
            round(med_crud * 1000, 2)
            if metric != "dropped_iterations_rate"
            else round(med_crud, 2)
        )

        med_cqrs = (
            round(med_cqrs * 1000, 2)
            if metric != "dropped_iterations_rate"
            else round(med_cqrs, 2)
        )

        results.append(
            {
                "metric": metric,
                "users": users,
                "c_median": med_crud,
                "c_ci": round(get_median_ci(crud), 2),
                "e_median": med_cqrs,
                "e_ci": round(get_median_ci(cqrs), 2),
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
            f"POST /lectures/create, then GET /lectures/\\{{lectureId\\}} ({file})",
            Path(args.output_path) / f"{output_base_name}.tex",
        )


if __name__ == "__main__":
    main()
