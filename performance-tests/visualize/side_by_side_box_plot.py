import sys
from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd
from matplotlib.ticker import ScalarFormatter

from visualize.helper import load_csv, pretty_name

APP_COLORS = {
    "crud": "#1f77b4",  # blue
    "es-cqrs": "#d62728",  # red
}


def _boxplot_grouped_metrics(df: pd.DataFrame) -> None:
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


def _lineplot_latency_vs_users(
    df: pd.DataFrame,
    *,
    log_x: bool = True,
    log_y: bool = True,
    split_y_axis: bool = False,
) -> None:
    percentiles = {
        "latency_p50": "p50",
        "latency_p99": "p99",
    }

    for uri in sorted(df["uri"].unique()):
        uri_df = df[df["uri"] == uri]

        fig, ax_left = plt.subplots()
        ax_right = None

        if split_y_axis:
            ax_right = ax_left.twinx()

        axes_by_app = {
            "crud": ax_left,
            "es-cqrs": ax_right if split_y_axis else ax_left,
        }

        for app in ["crud", "es-cqrs"]:
            app_df = uri_df[uri_df["app"] == app]
            ax = axes_by_app[app]

            if ax is None:
                continue

            for metric, label in percentiles.items():
                metric_df = app_df[app_df["metric"] == metric]

                if metric_df.empty:
                    continue

                metric_df = metric_df.sort_values("virtual_users")

                ax.plot(
                    metric_df["virtual_users"],
                    metric_df["value_ms"],
                    marker="o",
                    linestyle="--" if metric == "latency_p99" else "-",
                    color=APP_COLORS[app],
                    label=f"{pretty_name(app)} {label}",
                )

        # ---- axis config ----

        ax_left.set_xlabel("Virtual users")

        if log_x:
            ax_left.set_xscale("log")
            ax_left.get_xaxis().set_major_formatter(ScalarFormatter())

        ax_left.set_xticks(sorted(df["virtual_users"].unique()))

        if log_y:
            ax_left.set_yscale("log")
            ax_left.get_yaxis().set_major_formatter(ScalarFormatter())

        if split_y_axis:
            ax_right.set_ylabel("ES-CQRS Latency (ms)")
            ax_left.set_ylabel("CRUD Latency (ms)")

            if log_y:
                ax_right.set_yscale("log")
                ax_right.get_yaxis().set_major_formatter(ScalarFormatter())
        else:
            ax_left.set_ylabel("Latency (ms)")

        ax_left.set_title(f"Latency vs load — {df['method'].iloc[0]} {uri}")

        ax_left.grid(True, linestyle="--", alpha=0.6)

        # ---- legend handling ----
        handles = []
        labels = []

        for ax in {ax_left, ax_right} - {None}:
            h, l = ax.get_legend_handles_labels()
            handles.extend(h)
            labels.extend(l)

        ax_left.legend(handles, labels)

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

    _boxplot_grouped_metrics(df)


def visualize_aggregated(aggregated: pd.DataFrame):
    _boxplot_grouped_metrics(aggregated)


def visualize_aggregated_lineplot(aggregated: pd.DataFrame):
    _lineplot_latency_vs_users(aggregated, log_x=False, log_y=False)


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("Usage: python plot_latency.py <crud.csv> <es-cqrs.csv>")

    crud_csv = Path(sys.argv[1])
    es_cqrs_csv = Path(sys.argv[2])

    visualize_one_csv_each(crud_csv, es_cqrs_csv)


if __name__ == "__main__":
    main()
