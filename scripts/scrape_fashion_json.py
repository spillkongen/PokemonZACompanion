"""Regenerate app/src/main/assets/fashion_serebii.json from Serebii."""
import json
import re
import sys
import urllib.request
from pathlib import Path

from bs4 import BeautifulSoup

sys.path.insert(0, str(Path(__file__).resolve().parent))
from fashion_gender_rules import is_feminine_cut, is_female_wardrobe, is_masculine_cut

URL = "https://www.serebii.net/legendsz-a/customisation.shtml"
OUT = r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\fashion_serebii.json"

CATEGORIES = {
    "all-in-one": "All-in-One",
    "tops": "Tops",
    "bottoms": "Bottoms",
    "headwear": "Headwear",
    "eyewear": "Eyewear",
    "gloves": "Gloves",
    "legwear": "Legwear",
    "footwear": "Footwear",
    "satchels": "Satchels",
    "earrings": "Earrings",
}
HEADER_TO_ID = {v: k for k, v in CATEGORIES.items()}


def resolve_url(src: str) -> str:
    if src.startswith("//"):
        return "https:" + src
    if src.startswith("/"):
        return "https://www.serebii.net" + src
    if src.startswith("http"):
        return src
    return "https://www.serebii.net/" + src.lstrip("/")


def format_location(raw: str) -> str:
    s = raw
    for token in (
        "Passage", "Galerie", "Hotel", "Vernal", "During", "Complete", "Fresh",
        "Boutique", "Kickspin", "SUBATOMIC", "Mode ", "Glammor", "NIGHTSIDE",
        "Wisp", "Masterpiece", "Porte", "Midnight", "DENSOKU", "Les ", "Bundle",
        "FILMFAN", "Kikonashi", "South",
    ):
        s = re.sub(f"([A-Za-z])({token})", r"\1 · \2", s)
    return " ".join(s.split())


def format_cost(raw: str) -> str:
    t = raw.strip()
    if not t:
        return "Free / Mission reward"
    if t.isdigit():
        return f"₽{t}"
    return t


def parse_row_fields(cells):
    if len(cells) >= 5:
        name = cells[1].get_text(strip=True)
        style = cells[2].get_text(strip=True)
        location = cells[3].get_text(strip=True)
        cost = cells[4].get_text(strip=True)
    elif len(cells) >= 4:
        name = cells[0].get_text(strip=True)
        style = cells[1].get_text(strip=True)
        location = cells[2].get_text(strip=True)
        cost = cells[3].get_text(strip=True)
    else:
        return None
    if not name or name.lower() in ("name", "picture"):
        return None
    return name, style, location, cost


def parse_table(table, category: str, out: list, seen: set):
    for row in table.find_all("tr")[1:]:
        cells = row.find_all("td")
        parsed = parse_row_fields(cells)
        if parsed is None:
            continue
        name, style, location, cost = parsed
        img_el = row.find("img")
        img_src = resolve_url(img_el["src"]) if img_el and img_el.get("src") else None
        anchor = row.find("a", attrs={"data-key": True})
        preview_key = anchor.get("data-key") if anchor else None

        key = f"{category}|{name}|{style}"
        if key in seen:
            continue
        seen.add(key)

        out.append(
            {
                "category": category,
                "name": name,
                "style": style,
                "location": format_location(location),
                "cost": format_cost(cost),
                "imageUrl": img_src,
                "previewKey": preview_key,
                "feminineCut": is_feminine_cut(name),
                "masculineCut": is_masculine_cut(name),
                "femaleWardrobe": is_female_wardrobe(name),
            }
        )


def main():
    raw = urllib.request.urlopen(
        urllib.request.Request(URL, headers={"User-Agent": "PokemonZACompanion/1.4"}),
        timeout=60,
    ).read()
    soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

    items = []
    seen = set()
    for h3 in soup.find_all("h3"):
        header = h3.get_text(strip=True)
        cat_id = HEADER_TO_ID.get(header)
        if not cat_id:
            continue
        table = h3.find_next("table", class_="dextable")
        if not table:
            print("WARN: no table for", header)
            continue
        before = len(items)
        parse_table(table, cat_id, items, seen)
        print(header, len(items) - before, "items")

    payload = {
        "source": URL,
        "note": "femaleWardrobe = all non-men's-cut items (gloves, earrings, etc. included). Previews from Serebii large images.",
        "items": items,
    }
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)

    fem = sum(1 for i in items if i["feminineCut"])
    ward = sum(1 for i in items if i["femaleWardrobe"])
    masc = sum(1 for i in items if i["masculineCut"])
    print("wrote", len(items), "| femaleWardrobe", ward, "| feminineCut", fem, "| masculineCut", masc)


if __name__ == "__main__":
    main()
