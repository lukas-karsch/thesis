import argparse

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
    args = parser.parse_args()

    if args.skip == "crud":
        print("Skipping CRUD app.")
    else:
        print(f"Testing CRUD app ({args.runs} runs)")
        for i in range(args.runs):
            print(f"Iteration {i+1}")
            perf_runner.do_run("crud", args.metric)
            print(f"Finished iteration {i+1}")

    if args.skip == "es-cqrs":
        print("Skipping ES-CQRS app.")
    else:
        print(f"Testing ES-CQRS app (${args.runs} runs")
        for i in range(args.runs):
            print(f"Iteration {i+1}")
            perf_runner.do_run("es-cqrs", args.metric)
            print(f"Finished iteration {i+1}")


if __name__ == "__main__":
    main()
