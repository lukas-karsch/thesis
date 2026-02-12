import json
import re
from pathlib import Path
from typing import List

import pandas as pd

from visualize.side_by_side_box_plot import (
    visualize_aggregated_lineplot,
    lineplot_read_visible_rate_vs_users,
)


def extract_rps(folder: Path) -> int | None:
    """
    Returns the first '-' separated token in folder.stem
    that can be parsed as an integer.
    """
    for token in folder.stem.split("-"):
        try:
            return int(token)
        except ValueError:
            continue
    return None


def parse_k6_output(folder: Path) -> pd.DataFrame:
    file_path = folder / "k6-summary.json"
    data = json.loads(file_path.read_text())

    app = "es-cqrs" if "es-cqrs" in folder.stem else "crud"
    rps = extract_rps(folder)

    rows = []

    uri = "/lectures/{lectureId}"

    for metric_name, metric_data in data.get("metrics", {}).items():
        if metric_name.startswith("http_req_duration"):
            uri_match = re.search(r"endpoint:(.+)}", metric_name)
            if uri_match is None:
                continue
            if uri_match.group(1) != uri:
                continue

            rows.extend(
                [
                    {
                        "metric": "latency_p95",
                        "method": "GET",
                        "uri": uri,
                        "value_ms": metric_data.get("p(95)"),
                        "app": app,
                        "virtual_users": rps,
                    },
                    {
                        "metric": "latency_avg",
                        "method": "GET",
                        "uri": uri,
                        "value_ms": metric_data.get("avg"),
                        "app": app,
                        "virtual_users": rps,
                    },
                    {
                        "metric": "latency_p50",
                        "method": "GET",
                        "uri": uri,
                        "value_ms": metric_data.get("med"),
                        "app": app,
                        "virtual_users": rps,
                    },
                    {
                        "metric": "latency_p99",
                        "method": "GET",
                        "uri": uri,
                        "value_ms": metric_data.get("p(99)"),
                        "app": app,
                        "virtual_users": rps,
                    },
                ]
            )

        elif metric_name == "read_visible_rate":
            rows.append(
                {
                    "metric": "read_visible_rate",
                    "method": "GET",
                    "uri": uri,
                    "value": metric_data.get("value"),
                    "app": app,
                    "virtual_users": rps,
                }
            )

    df = pd.DataFrame(rows)
    return df


def _find_matching_folders(base_name: str, directory: Path) -> List[Path]:
    if not directory.is_dir():
        raise ValueError(f"Path '{directory}' is not a directory.")

    matching_folders: List[Path] = []

    # check current directory
    if directory.name.startswith(base_name):
        matching_folders.append(directory)

    # recurse into subdirectories
    for child in directory.iterdir():
        if child.is_dir():
            matching_folders.extend(_find_matching_folders(base_name, child))

    return matching_folders


def main():
    base_path = Path(
        "C:\\Users\\lukas\\Documents\\Studium\\Bachelorarbeit\\Test Results\\time-to-consistency\\create-lecture\\run-k6\\"
    )
    base_name = "run-create-lecture"

    all_folders = _find_matching_folders(base_name, base_path)
    print(f"Found {len(all_folders)} folders.")

    dfs = pd.concat([parse_k6_output(f) for f in all_folders], ignore_index=True)

    lineplot_read_visible_rate_vs_users(dfs)


if __name__ == "__main__":
    main()
