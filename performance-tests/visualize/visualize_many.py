import argparse
from pathlib import Path

from side_by_side_box_plot import visualize_aggregated_lineplot, visualize_aggregated


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--dir", required=True)
    parser.add_argument(
        "--base_name",
        required=True,
        help="Base name of the result folders to aggregate.",
    )
    args = parser.parse_args()

    visualize_aggregated(args.base_name, Path(args.dir))


# visualize_aggregated_lineplot(args.base_name, Path(args.dir))


if __name__ == "__main__":
    main()
