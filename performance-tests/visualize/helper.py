from pathlib import Path

import pandas as pd


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
