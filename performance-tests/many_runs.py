import argparse
import json
import os
from pathlib import Path

import perf_runner
from visualize.side_by_side_box_plot import visualize_aggregated


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--metric",
        required=True,
        help="Path to the metric.json file which describes the metric.",
    )
    parser.add_argument("--runs", type=int, required=True)
    parser.add_argument("--skip", required=False, choices=["crud", "es-cqrs"])
    parser.add_argument(
        "--config",
        required=False,
        help="When trying to run on two VMs, must provide this file",
    )
    args = parser.parse_args()

    config_file = None
    if args.config is not None:
        config_file = Path(args.config)

    if args.skip == "crud":
        print("Skipping CRUD app.")
    else:
        print(f"Testing CRUD app ({args.runs} runs)")
        for i in range(args.runs):
            print(f"Iteration {i+1}")
            perf_runner.do_run("crud", args.metric, config_file)
            print(f"Finished iteration {i+1}")

    if args.skip == "es-cqrs":
        print("Skipping ES-CQRS app.")
    else:
        print(f"Testing ES-CQRS app (${args.runs} runs")
        for i in range(args.runs):
            print(f"Iteration {i+1}")
            perf_runner.do_run("es-cqrs", args.metric, config_file)
            print(f"Finished iteration {i+1}")

    metric_content = json.loads(Path(args.metric).read_text())
    k6_script = os.path.dirname(args.metric) / Path(metric_content["file"])

    visualize_aggregated(f"run-{k6_script}", Path("run-k6"))


if __name__ == "__main__":
    main()
