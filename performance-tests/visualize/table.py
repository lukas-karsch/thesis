from pathlib import Path
from typing import Literal

import pandas as pd
from jinja2 import Template


def render_table(
    df: pd.DataFrame,
    label: str,
    caption: str,
    output_path: Path,
    type: Literal["standard", "timeseries", "aggregated_timeseries"] = "standard",
):
    if type == "standard":
        template_path = "visualize/table_template.tex.j2"
    elif type == "timeseries":
        template_path = "visualize/table_template_timeseries.tex.j2"
    elif type == "aggregated_timeseries":
        template_path = "visualize/table_template_aggregated_timeseries.tex.j2"
    else:
        raise ValueError(f"{type} not allowed")

    with open(template_path) as f:
        template = Template(f.read())

    def latex_escape(value):
        if isinstance(value, str):
            return value.replace("_", r"\_").replace("%", r"\%")
        return value

    template.globals["e"] = latex_escape

    rendered_tex = template.render(
        rows=df.to_dict(orient="records"), label=label, caption=caption
    )

    with open(output_path, "w") as f:
        f.write(rendered_tex)
