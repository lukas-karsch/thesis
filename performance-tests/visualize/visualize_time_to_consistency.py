import json
import re
from pathlib import Path

import pandas as pd

from visualize.aggregate import find_matching_folders
from visualize.side_by_side_box_plot import (
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


def main():
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument("--dir", required=True)
    parser.add_argument(
        "--base_name",
        required=True,
        help="Base name of the result folders to aggregate.",
    )
    args = parser.parse_args()

    base_path = Path(args.dir)
    base_name = args.base_name

    all_folders = find_matching_folders(base_name, base_path)
    print(f"Found {len(all_folders)} folders.")

    dfs = pd.concat(
        [parse_k6_output(f) for f in all_folders if "-20-" not in f.stem],
        ignore_index=True,
    )

    lineplot_read_visible_rate_vs_users(dfs, log_x=False)


if __name__ == "__main__":
    main()
