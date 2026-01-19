import argparse
from pathlib import Path
from typing import List

import pandas as pd

from visualize.helper import load_csv


def _find_matching_metrics_csv(base_name: str, directory: Path) -> List[Path]:
    if not directory.is_dir():
        raise ValueError(f"Path {directory} is not a directory.")

    matching_files: List[Path] = []

    # check current directory
    if directory.name.startswith(base_name):
        metrics_path = directory / "metrics.csv"
        if metrics_path.is_file():
            matching_files.append(metrics_path)

    # recurse into subdirectories
    for child in directory.iterdir():
        if child.is_dir():
            matching_files.extend(_find_matching_metrics_csv(base_name, child))

    return matching_files


def aggregate(base_name: str, directory: Path) -> pd.DataFrame:
    matching_metric_files = _find_matching_metrics_csv(base_name, directory)
    if len(matching_metric_files) == 0:
        raise ValueError(
            f"No matching metric files found for base_name={base_name}; directory={directory}"
        )
    return pd.concat([load_csv(f) for f in matching_metric_files], ignore_index=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dir", required=True)
    parser.add_argument(
        "--base_name",
        required=True,
        help="Base name of the result folders to aggregate.",
    )
    args = parser.parse_args()

    df = aggregate(args.base_name, Path(args.dir))
    print(df)


if __name__ == "__main__":
    main()
