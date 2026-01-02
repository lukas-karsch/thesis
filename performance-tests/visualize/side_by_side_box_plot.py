import sys
from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd

from aggregate import aggregate
from helper import load_csv

APP_COLORS = {
    "crud": "#1f77b4",  # blue
    "es-cqrs": "#d62728",  # red
}


def pretty_name(app: str) -> str:
    if app == "crud":
        return "CRUD"
    if app == "es-cqrs":
        return "ES-CQRS"
    raise ValueError(f"{app} is not supported.")


def _plot_grouped_metrics(df: pd.DataFrame) -> None:
    for uri in sorted(df["uri"].unique()):
        uri_df = df[df["uri"] == uri]

        metrics = sorted(uri_df["metric"].unique())
        apps = ["crud", "es-cqrs"]

        positions = []
        data = []
        colors = []
        labels = []

        pos = 1
        gap = 1.0  # space between metric groups

        for metric in metrics:
            for app in apps:
                values = uri_df[(uri_df["metric"] == metric) & (uri_df["app"] == app)][
                    "value_ms"
                ]

                if values.empty:
                    continue

                data.append(values)
                positions.append(pos)
                colors.append(APP_COLORS[app])
                labels.append(f"{metric}\n{pretty_name(app)}")

                pos += 1

            pos += gap  # add space after each metric group

        fig, ax = plt.subplots()

        boxplot = ax.boxplot(
            data,
            positions=positions,
            widths=0.6,
            patch_artist=True,
            showfliers=True,
        )

        for patch, color in zip(boxplot["boxes"], colors):
            patch.set_facecolor(color)
            patch.set_alpha(0.7)

        ax.set_xticks(positions)
        ax.set_xticklabels(labels, rotation=45, ha="right")

        ax.set_ylabel("Latency (ms)")
        ax.set_title(f"Latency comparison — {df["method"][0]} {uri}")

        ax.grid(axis="y", linestyle="--", alpha=0.6)

        # Legend (manual, clean)
        handles = [
            plt.Line2D([0], [0], color=APP_COLORS[app], lw=6) for app in APP_COLORS
        ]
        ax.legend(
            handles,
            [pretty_name(app) for app in APP_COLORS],
            title="Application",
        )

        plt.tight_layout()
        plt.show()


def visualize_one_csv_each(crud_csv: Path, es_cqrs_csv: Path):
    df = pd.concat(
        [
            load_csv(crud_csv),
            load_csv(es_cqrs_csv),
        ],
        ignore_index=True,
    )

    _plot_grouped_metrics(df)


def visualize_aggregated(base_name: str, directory: Path):
    aggregated = aggregate(base_name, directory)
    _plot_grouped_metrics(aggregated)


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("Usage: python plot_latency.py <crud.csv> <es-cqrs.csv>")

    crud_csv = Path(sys.argv[1])
    es_cqrs_csv = Path(sys.argv[2])

    visualize_one_csv_each(crud_csv, es_cqrs_csv)


if __name__ == "__main__":
    main()
