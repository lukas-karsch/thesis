from pathlib import Path

import numpy as np
import pandas as pd
from scipy import stats


def load_csv(path: Path) -> pd.DataFrame:
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

    raise ValueError(f"'{metric_path} does not exist")


def get_median_ci(series):
    if len(series) < 20:
        print(f"Sample size is {len(series)}, returning 0 for get_median_ci")
        return 0

    d = (series.values,)

    res = stats.bootstrap(
        d,
        np.median,
        confidence_level=0.95,
        method="percentile",
        n_resamples=1000,
    )

    return (res.confidence_interval.high - res.confidence_interval.low) / 2


def get_significance(p):
    if p <= 0.001:
        return "***"
    if p <= 0.01:
        return "**"
    if p <= 0.05:
        return "*"
    return "n.s."
