import argparse
import json
from collections import defaultdict
from pathlib import Path
from typing import List, Literal

import pandas as pd

from visualize.helper import load_csv


def _find_matching_metrics_csv(
    base_name: str, directory: Path, file: Literal["server", "client"]
) -> List[Path]:
    folders = find_matching_folders(base_name, directory)

    filename = "server_metrics.csv" if file == "server" else "client_metrics.csv"

    return [(Path(folder) / filename) for folder in folders]


def find_matching_folders(base_name: str, directory: Path) -> List[Path]:
    if not directory.is_dir():
        raise ValueError(f"Path '{directory}' is not a directory.")

    matching_folders: List[Path] = []

    # check current directory
    if directory.name.startswith(base_name):
        matching_folders.append(directory)

    # recurse into subdirectories
    for child in directory.iterdir():
        if child.is_dir():
            matching_folders.extend(find_matching_folders(base_name, child))

    return matching_folders


def aggregate_metrics_csv(
    base_name: str, directory: Path, file: Literal["server", "client"]
) -> pd.DataFrame:
    matching_metric_files = _find_matching_metrics_csv(base_name, directory, file)
    if len(matching_metric_files) == 0:
        raise ValueError(
            f"No matching metric files found for base_name={base_name}; directory={directory}"
        )
    print(f"Found {len(matching_metric_files)} matching CSV files")
    return pd.concat([load_csv(f) for f in matching_metric_files], ignore_index=True)


def aggregate_prometheus_metrics(
    metric_json_name: str,
    directory: Path,
    base_name,
    metric_name: str | None = None,
    metric_for: str = "spring",
):
    """
    Aggregates and averages all collected metric JSONs based on app (by folder name).

    For each app (e.g. crud, es-cqrs), all Prometheus metric JSONs are loaded.
    Values are aligned by index (not timestamp!) and aggregated using the median.

    :return: dict[str, pd.DataFrame]
    """

    folders = find_matching_folders(base_name, directory)
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

        results = values = metric_json["data"]["result"]
        if metric_name is None:
            for r in results:
                if r["metric"]["job"] == metric_for:
                    values = r["values"]
                    break
        else:
            for r in results:
                if (
                    r["metric"]["__name__"] == metric_name
                    and r["metric"]["job"] == metric_for
                ):
                    values = r["values"]
                    break

        if values is None:
            raise ValueError(
                f"No values for the metric could be found. metric_name={metric_name}, metric_for={metric_for}"
            )

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


def aggregate_timeseries_prometheus_metrics(
    metric_json_name: str,
    directory: Path,
    base_name: str,
    metric_name: str | None = None,
    metric_for: str = "spring",
):
    folders = find_matching_folders(base_name, directory)
    print(f"Info: Found {len(folders)} matching folders")
    if len(folders) == 0:
        raise ValueError(
            f"Found no matching folders for base_name={base_name}, directory{directory}"
        )

    # app -> list of runs (each run is a list of floats)
    raw_data: dict[str, list[list[float]]] = defaultdict(list)

    for folder in folders:
        prometheus_dir = folder / "prometheus"
        metric_path = prometheus_dir / metric_json_name

        if not metric_path.is_file():
            print(f"Warn: {metric_path} is not a file.")
            continue

        app = "crud" if "crud" in folder.name else "es-cqrs"

        with open(metric_path) as f:
            metric_json = json.load(f)

        results = metric_json["data"]["result"]
        values = None

        # Logic to find correct metric series
        for r in results:
            if metric_name:
                if (
                    r["metric"]["__name__"] == metric_name
                    and r["metric"]["job"] == metric_for
                ):
                    values = r["values"]
                    break
            job = r["metric"].get("job")
            if job == metric_for:
                values = r["values"]
                break
            else:
                values = results[0]["values"]

        if values:
            series = [float(v[1]) for v in values]
            raw_data[app].append(series)

    # Align and Clean
    all_records = []
    for app, runs in raw_data.items():
        if not runs:
            continue

        # Find the minimum length among all runs for this app to ensure alignment
        min_len = min(len(r) for r in runs)

        for run_idx, series in enumerate(runs):
            # Trim the series to min_len
            for i in range(min_len):
                all_records.append(
                    {
                        "time_index": i,
                        "value": series[i],
                        "app": app,
                        "run_id": f"{app}_run_{run_idx}",
                    }
                )

    return pd.DataFrame(all_records)


def aggregate_single_val_prometheus_metrics(
    metric_json_name: str,
    directory: Path,
    base_name: str,
    metric_name: str | None = None,
    job: str = "spring",
    type: str | None = None,
):
    folders = find_matching_folders(base_name, directory)
    print(f"Info: Found {len(folders)} matching folders")
    if len(folders) == 0:
        raise ValueError(
            f"Found no matching folders for base_name={base_name}, directory{directory}"
        )

    raw_data: dict[str, list[float]] = defaultdict(list)

    for folder in folders:
        prometheus_dir = folder / "prometheus"
        metric_path = prometheus_dir / metric_json_name

        if not metric_path.is_file():
            print(f"Warn: {metric_path} is not a file.")
            continue

        app = "crud" if "crud" in folder.name else "es-cqrs"

        with open(metric_path) as f:
            metric_json = json.load(f)

        results = metric_json["data"]["result"]
        value = None

        # Logic to find correct metric series
        for r in results:
            if metric_name:
                if r["metric"]["__name__"] == metric_name and r["metric"]["job"] == job:
                    value = r["value"][1]
                    break
            metric_job = r["metric"].get("job")
            metric_type = r["metric"].get("type")
            if (
                metric_job == job
                and (type is not None and metric_type == type)
                or type is None
            ):
                value = r["value"][1]
                break
            else:
                value = results[0]["value"][1]

        if value:
            raw_data[app].append(float(value))

    # Align and Clean
    all_records = []
    for app, runs in raw_data.items():
        if not runs:
            continue

        for run_idx, val in enumerate(runs):
            all_records.append(
                {
                    "value": val,
                    "app": app,
                    "run_id": f"{app}_run_{run_idx}",
                }
            )
    return pd.DataFrame(all_records)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dir", required=True)
    parser.add_argument(
        "--base_name",
        required=True,
        help="Base name of the result folders to aggregate.",
    )
    parser.add_argument("--file", choices=["client", "server"], required=True)
    args = parser.parse_args()

    df = aggregate_metrics_csv(args.base_name, Path(args.dir), args.file)
    print(df)


if __name__ == "__main__":
    main()
