"""
When using this script, make sure to adjust statistical_tests.py
1. Change "value" to "value_ms"
2. Change multiplier below from 1000 to 1

When trying to show dropped iterations, uncomment the filter in this file'S main function.
"""

from pathlib import Path

import pandas as pd

from visualize.aggregate import find_matching_folders
from visualize.statistical_tests import analyze_performance
from visualize.table import render_table
from visualize.visualize_time_to_consistency import parse_k6_output


def main() -> None:
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument("--dir", required=True)
    parser.add_argument(
        "--base_name",
        required=True,
        help="Base name of the result folders to aggregate.",
    )
    parser.add_argument("--output_path", required=True)
    args = parser.parse_args()

    base_path = Path(args.dir)
    base_name = args.base_name

    all_folders = find_matching_folders(base_name, base_path)
    print(f"Found {len(all_folders)} folders.")

    df = pd.concat(
        [parse_k6_output(f) for f in all_folders],
        ignore_index=True,
    )

    # Toggle to only filter by 'dropped_iterations_rate'
    # df = df[df["metric"] == "dropped_iterations_rate"]

    # df = df[df["metric"] == "read_visible_rate"]

    stats_table = analyze_performance(df)

    output_base_name = f"{args.base_name}_client_read_visible_rate_statistical_test"
    output_path = Path(args.output_path) / f"{output_base_name}.csv"

    output_path.parent.mkdir(parents=True, exist_ok=True)

    stats_table.to_csv(output_path, index=False)
    render_table(
        stats_table,
        f"{args.base_name}_client",
        f"GET /lectures/\\{{lectureId\\}}",
        Path(args.output_path) / f"{output_base_name}.tex",
    )


if __name__ == "__main__":
    main()
