import json
from pathlib import Path

path = Path(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\missions_serebii.json")
data = json.loads(path.read_text(encoding="utf-8"))
for m in data["missions"]:
    detail = m.get("detailUrl", "")
    slug = detail.rsplit("/", 1)[-1].replace(".shtml", "") if detail else f"n{m['number']}"
    m["id"] = f"{m['type']}_{m['number']}_{slug}"
path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
print("ids added", len(data["missions"]))
