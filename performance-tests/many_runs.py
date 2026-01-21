import argparse
from pathlib import Path
from typing import List, Literal

import perf_runner


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

    apps: List[Literal["crud", "es-cqrs"]] = ["crud", "es-cqrs"]
    for app in apps:
        if args.skip == app:
            print(f"Skipping '{app}' app.")
            continue
        print(f"Testing '{app}' app ({args.runs} runs)")
        for i in range(args.runs):
            print(f"Iteration {i+1}")
            perf_runner.do_run(app, args.metric, config_file)
            print(f"Finished iteration {i+1}")


if __name__ == "__main__":
    main()
