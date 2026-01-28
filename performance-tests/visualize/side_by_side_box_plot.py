import sys
from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
from matplotlib.ticker import ScalarFormatter

from visualize.helper import load_csv, pretty_name

APP_COLORS = {
    "crud": "#1f77b4",  # blue
    "es-cqrs": "#d62728",  # red
}


def _boxplot_grouped_metrics(df: pd.DataFrame, log_y: bool = False) -> None:
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

        if log_y:
            ax.set_yscale("log")
            ax.get_yaxis().set_major_formatter(ScalarFormatter())

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
    additional_title: str = "",
) -> None:
    percentiles = {
        "latency_p50": "p50",
        "latency_p95": "p95",
    }

    for uri in sorted(df["uri"].unique()):
        uri_df = df[df["uri"] == uri]

        fig, ax = plt.subplots()

        for app in ["crud", "es-cqrs"]:
            app_df = uri_df[uri_df["app"] == app]

            if ax is None:
                continue

            for metric, label in percentiles.items():
                metric_df = app_df[app_df["metric"] == metric]

                if metric_df.empty:
                    continue

                metric_df = metric_df.sort_values("virtual_users")

                sns.lineplot(
                    data=metric_df,
                    x="virtual_users",
                    y="value_ms",
                    markers=True,
                    linestyle="--" if metric == "latency_p95" else "-",
                    color=APP_COLORS[app],
                    label=f"{pretty_name(app)} {label}",
                    errorbar="sd",
                    ax=ax,
                )

        # ---- axis config ----

        ax.set_xlabel("Requests per second (RPS)")

        if log_x:
            ax.set_xscale("log")
            ax.get_xaxis().set_major_formatter(ScalarFormatter())

        ax.set_xticks(sorted(df["virtual_users"].unique()))

        if log_y:
            ax.set_yscale("log")
            ax.get_yaxis().set_major_formatter(ScalarFormatter())

        ax.set_ylabel("Latency (ms)")

        ax.set_title(
            f"Latency vs load — {df['method'].iloc[0]} {uri} {additional_title}"
        )

        ax.grid(True, linestyle="--", alpha=0.6)

        # ---- legend handling ----
        handles = []
        labels = []

        h, l = ax.get_legend_handles_labels()
        handles.extend(h)
        labels.extend(l)

        ax.legend(handles, labels, loc="upper left")

        ax.axhline(100, ls="--", color="gray", alpha=0.5)
        ax.text(
            0.01,
            100,
            "SLO: 100ms latency",
            color="gray",
            ha="left",
            va="bottom",
            fontsize="x-small",
            transform=ax.get_yaxis_transform(),
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

    _boxplot_grouped_metrics(df)


def visualize_aggregated(aggregated: pd.DataFrame, log_y: bool = False):
    _boxplot_grouped_metrics(aggregated, log_y=log_y)


def visualize_aggregated_lineplot(
    aggregated: pd.DataFrame,
    log_x: bool = True,
    log_y: bool = True,
    additional_title: str = "",
):
    _lineplot_latency_vs_users(
        aggregated, log_x=log_x, log_y=log_y, additional_title=additional_title
    )


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("Usage: python plot_latency.py <crud.csv> <es-cqrs.csv>")

    crud_csv = Path(sys.argv[1])
    es_cqrs_csv = Path(sys.argv[2])

    visualize_one_csv_each(crud_csv, es_cqrs_csv)


if __name__ == "__main__":
    main()
