"""Update gender flags in bundled fashion JSON without re-downloading images."""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from fashion_gender_rules import is_feminine_cut, is_female_wardrobe, is_masculine_cut

JSON_PATH = Path(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\fashion_serebii.json")
data = json.loads(JSON_PATH.read_text(encoding="utf-8"))
for item in data["items"]:
    name = item["name"]
    item["feminineCut"] = is_feminine_cut(name)
    item["masculineCut"] = is_masculine_cut(name)
    item["femaleWardrobe"] = is_female_wardrobe(name)

data["note"] = (
    "femaleWardrobe = all non-men's-cut items (gloves, earrings, etc.). Previews from Serebii large images."
)
JSON_PATH.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
fem = sum(1 for i in data["items"] if i["feminineCut"])
ward = sum(1 for i in data["items"] if i["femaleWardrobe"])
masc = sum(1 for i in data["items"] if i["masculineCut"])
print("patched", len(data["items"]), "| femaleWardrobe", ward, "| feminineCut", fem, "| masculineCut", masc)
