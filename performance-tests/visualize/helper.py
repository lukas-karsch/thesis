from pathlib import Path

import numpy as np
import pandas as pd
from scipy import stats


def load_csv(path: Path) -> pd.DataFrame | None:
    if not path.is_file():
        print(f"load_csv: missing file at {path}")
        return None

    df = pd.read_csv(path)

    required_cols = {"app", "metric", "method", "uri", "value"}
    missing = required_cols - set(df.columns)
    if missing:
        raise ValueError(f"{path} is missing columns: {missing}")

    # Convert seconds → milliseconds
    df["value_ms"] = df["value"] * 1000.0
    return df


def pretty_name(app: str) -> str:
    if app == "crud":
        return "CRUD"
    if app == "es-cqrs":
        return "ES-CQRS"
    raise ValueError(f"{app} is not supported.")


def get_metric_json(base_path: Path) -> Path:
    metric_path = base_path / "metric.json"
    if metric_path.is_file():
        return metric_path

    raise ValueError(f"'You have not placed the metric.json file at {metric_path}.")


def get_median_ci(series):
    return _get_ci(series, np.median)


def get_mean_ci(series):
    return _get_ci(series, np.mean)


def _get_ci(series, fn):
    d = (series.values,)

    try:
        res = stats.bootstrap(
            d,
            fn,
            confidence_level=0.95,
            method="percentile",
            n_resamples=1000,
        )
    except Exception as e:
        print(e)
        print("-> Returning NaN from _get_ci")
        return float("NaN")

    return (res.confidence_interval.high - res.confidence_interval.low) / 2


def get_significance(p):
    if p <= 0.001:
        return "***"
    if p <= 0.01:
        return "**"
    if p <= 0.05:
        return "*"
    return "n.s."


def get_ratio_representation(med_crud, med_cqrs) -> str:
    ratio = med_crud / med_cqrs if med_cqrs != 0 else 0
    if round(ratio, 1) == 1:
        comparison = "1.0x"
    elif ratio != 0:
        comparison = (
            f"{round(ratio, 1)}x Lower"
            if ratio >= 1
            else f"{round(1 / ratio, 1)}x Higher"
        )
    else:
        comparison = "NaN"

    return comparison
