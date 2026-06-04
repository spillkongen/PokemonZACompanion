"""Regenerate app/src/main/assets/fashion_serebii.json from Serebii."""
import json
import re
import urllib.request
from bs4 import BeautifulSoup

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


def resolve_url(src: str) -> str:
    if src.startswith("//"):
        return "https:" + src
    if src.startswith("/"):
        return "https://www.serebii.net" + src
    if src.startswith("http"):
        return src
    return "https://www.serebii.net/" + src.lstrip("/")


def female_preview_url(male_url: str | None) -> str | None:
    if not male_url:
        return None
    # Serebii does not publish separate female thumbs; use full-size preview when available.
    m = re.search(r"/custom/th/(\d+)\.jpg", male_url)
    if m:
        return f"https://www.serebii.net/legendsz-a/custom/{m.group(1)}.jpg"
    return male_url


def format_location(raw: str) -> str:
    s = raw
    for token in (
        "Passage",
        "Galerie",
        "Hotel",
        "Vernal",
        "During",
        "Complete",
        "Fresh",
        "Boutique",
        "Kickspin",
        "SUBATOMIC",
        "Mode ",
        "Glammor",
        "NIGHTSIDE",
        "Wisp",
        "Masterpiece",
        "Porte",
        "Midnight",
        "DENSOKU",
        "Les ",
        "Bundle",
        "FILMFAN",
        "Kikonashi",
        "South",
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


def is_feminine_cut(name: str) -> bool:
    n = name.lower()
    if "blouse" in n or "skort" in n or "dress" in n or "skirt" in n:
        return True
    if "romper" in n or "ribbon blouse" in n or "off shoulder" in n:
        return True
    return False


def is_masculine_cut(name: str) -> bool:
    n = name.lower()
    if "blouse" in n or "skort" in n or "dress" in n or "skirt" in n:
        return False
    if "shirt" in n or "cargo pants" in n or "biker jacket" in n:
        return True
    return False


def find_category_table(soup, cat_id: str, header: str):
    anchors = soup.select(f'a[name="{cat_id}"]')
    for anchor in anchors:
        el = anchor
        for _ in range(80):
            el = el.find_next()
            if not el:
                break
            if el.name == "h3" and header.lower() in el.get_text().lower():
                scan = el.find_next("table", class_="dextable")
                if scan and "Picture" in scan.get_text() and "Name" in scan.get_text():
                    return scan
    for h3 in soup.find_all("h3"):
        if h3.get_text(strip=True).lower() == header.lower():
            scan = h3.find_next("table", class_="dextable")
            if scan:
                return scan
    return None


def parse_table(table, category: str, out: list, seen: set):
    for row in table.find_all("tr")[1:]:
        cells = row.find_all("td")
        if len(cells) < 4:
            continue
        img_el = row.find("img")
        img_src = resolve_url(img_el["src"]) if img_el and img_el.get("src") else None
        anchor = row.find("a", attrs={"data-key": True})
        preview_key = anchor.get("data-key") if anchor else None

        texts = [c.get_text(strip=True) for c in cells if c.get_text(strip=True)]
        if len(texts) >= 5:
            name, style, location, cost = texts[1], texts[2], texts[3], texts[4]
        elif len(texts) >= 4:
            name, style, location, cost = texts[0], texts[1], texts[2], texts[3]
        else:
            continue
        if name.lower() in ("name", "picture"):
            continue

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
                "femaleImageUrl": female_preview_url(img_src),
                "previewKey": preview_key,
                "feminineCut": is_feminine_cut(name),
                "masculineCut": is_masculine_cut(name),
            }
        )


def main():
    raw = urllib.request.urlopen(
        urllib.request.Request(URL, headers={"User-Agent": "PokemonZACompanion/1.2"}),
        timeout=60,
    ).read()
    soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

    items = []
    seen = set()
    for cat_id, header in CATEGORIES.items():
        table = find_category_table(soup, cat_id, header)
        if table:
            parse_table(table, cat_id, items, seen)
        else:
            print("WARN: no table for", cat_id)

    payload = {
        "source": URL,
        "note": "All outfits are wearable on any character gender in Z-A. Serebii uses one model per preview.",
        "items": items,
    }
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)

    fem = sum(1 for i in items if i["feminineCut"])
    print("wrote", len(items), "items,", fem, "feminine-cut names,", sum(1 for i in items if i["imageUrl"]), "with images")


if __name__ == "__main__":
    main()
