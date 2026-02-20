from pathlib import Path

import perf_runner
from visualize.aggregate import find_matching_folders

directory = Path(
    "C:\\Users\\lukas\\Documents\\Studium\\Bachelorarbeit\\Test Results\\reads\\read-lectures"
)

folders = find_matching_folders("run-read-lectures-4000-", directory)
k6_summaries = [f / "k6-summary.json" for f in folders]

print(f"{len(k6_summaries)} k6-summary.json files found.")

for summary in k6_summaries:
    print(summary)

    app = "crud" if "crud" in str(summary.parent) else "es-cqrs"
    perf_runner.extract_k6_summary_to_csv(
        summary, directory / "metric.json", summary.parent / "client_metrics.csv", app
    )
