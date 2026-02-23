from pathlib import Path

from jinja2 import Template

from prepare_data import get_dataframe

output_path = "C:\\Users\\lukas\\Documents\\Studium\\Bachelorarbeit\\thesis\\latex\\tables\\es-cqrs-coupling-per-package.tex"


def latex_escape(value):
    if isinstance(value, str):
        return value.replace("_", r"\_").replace("%", r"\%")
    return value


df = get_dataframe(Path("../../Static Analysis/impl-es-cqrs/es-cqrs-martin.csv"))
# num_cols = df.select_dtypes(include="number").columns
# df[num_cols] = df[num_cols].astype(int)

with open("tables/coupling_table_template.tex.j2") as t:
    template = Template(t.read())
    template.globals["e"] = latex_escape

rendered_tex = template.render(
    rows=df.to_dict(orient="records"),
    label="es-cqrs-coupling",
    caption="CRUD architecture (Packages)",
)

with open(output_path, "w") as f:
    f.write(rendered_tex)
