import argparse
import json
from collections import defaultdict
from pathlib import Path
from typing import List

import pandas as pd

from visualize.helper import load_csv


def _find_matching_metrics_csv(base_name: str, directory: Path) -> List[Path]:
    folders = _find_matching_folders(base_name, directory)
    return [(Path(folder) / "metrics.csv") for folder in folders]


def _find_matching_folders(base_name: str, directory: Path) -> List[Path]:
    if not directory.is_dir():
        raise ValueError(f"Path '{directory}' is not a directory.")

    matching_folders: List[Path] = []

    # check current directory
    if directory.name.startswith(base_name):
        metrics_path = directory / "metrics.csv"
        if metrics_path.is_file():
            matching_folders.append(directory)

    # recurse into subdirectories
    for child in directory.iterdir():
        if child.is_dir():
            matching_folders.extend(_find_matching_folders(base_name, child))

    return matching_folders


def aggregate_metrics_csv(base_name: str, directory: Path) -> pd.DataFrame:
    matching_metric_files = _find_matching_metrics_csv(base_name, directory)
    if len(matching_metric_files) == 0:
        raise ValueError(
            f"No matching metric files found for base_name={base_name}; directory={directory}"
        )
    print(f"Found {len(matching_metric_files)} matching CSV files")
    return pd.concat([load_csv(f) for f in matching_metric_files], ignore_index=True)


def aggregate_prometheus_metrics(metric_json_name: str, directory: Path):
    """
    Aggregates and averages all collected metric JSONs based on app (by folder name).

    For each app (e.g. crud, es-cqrs), all Prometheus metric JSONs are loaded.
    Values are aligned by index (not timestamp!) and aggregated using the median.

    :param metric_json_name: name of the JSON files to aggregate
    :param directory: directory to traverse while searching for metrics
    :return: dict[str, pd.DataFrame]
    """

    folders = _find_matching_folders("", directory)
    print(f"Info: Found {len(folders)} matching folders")

    # app -> list of value lists
    app_series: dict[str, list[list[float]]] = defaultdict(list)

    for folder in folders:
        prometheus_dir = folder / "prometheus"
        metric_path = prometheus_dir / metric_json_name

        if not metric_path.is_file():
            print(f"Warn: '{metric_path}' is not a file")
            continue

        # infer app name from folder name
        if "crud" in folder.name:
            app = "crud"
        elif "es-cqrs" in folder.name:
            app = "es-cqrs"
        else:
            raise ValueError(f"No app was found in folder name '{folder.name}'")

        with open(metric_path) as f:
            metric_json = json.load(f)

        values = metric_json["data"]["result"][0]["values"]

        # extract only metric values, ignore timestamps
        series = [float(v[1]) for v in values]
        app_series[app].append(series)

    aggregated: dict[str, pd.DataFrame] = {}

    for app, runs in app_series.items():
        # ensure equal length
        min_len = min(len(r) for r in runs)
        trimmed = [r[:min_len] for r in runs]

        df = pd.DataFrame(trimmed).T  # index = time index

        aggregated[app] = pd.DataFrame(
            {
                "time_index": df.index,
                "median_value": df.median(axis=1),
            }
        )

    return aggregated


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dir", required=True)
    parser.add_argument(
        "--base_name",
        required=True,
        help="Base name of the result folders to aggregate.",
    )
    args = parser.parse_args()

    df = aggregate_metrics_csv(args.base_name, Path(args.dir))
    print(df)


if __name__ == "__main__":
    main()
