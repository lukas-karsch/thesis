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
