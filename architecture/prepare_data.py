from pathlib import Path

import pandas as pd


def get_dataframe(
    path: Path,
    application: str | None = None,
    decimal: str = ",",
    fillna: bool = True,
    skiprows: int = 1,
) -> pd.DataFrame:
    if not path.exists():
        raise ValueError(f"{path} doesn't exist.")

    df = pd.read_csv(path, decimal=decimal, skiprows=skiprows)
    if fillna:
        df.fillna(0, inplace=True)

    if application is not None:
        df["Application"] = application

    return df
