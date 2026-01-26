import argparse
from pathlib import Path

from side_by_side_box_plot import visualize_aggregated
from visualize.aggregate import aggregate_metrics_csv


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--dir", required=True)
    parser.add_argument(
        "--base_name",
        required=True,
        help="Base name of the result folders to aggregate.",
    )
    args = parser.parse_args()

    aggregated_server = aggregate_metrics_csv(args.base_name, Path(args.dir), "server")
    visualize_aggregated(aggregated_server)
    # visualize_aggregated_lineplot(aggregated_server)

    aggregated_client = aggregate_metrics_csv(args.base_name, Path(args.dir), "client")
    visualize_aggregated(aggregated_client)
    # visualize_aggregated_lineplot(aggregated_client)


if __name__ == "__main__":
    main()
