import urllib.request
from bs4 import BeautifulSoup

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "Mozilla/5.0"},
    ),
    timeout=30,
).read()
doc = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

category_headers = {
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


def find_section_table(anchor, header):
    el = anchor
    for _ in range(30):
        el = el.find_next() if el else None
        if el is None:
            break
        if el.name == "h3" and header.lower() in el.get_text().lower():
            scan = el.find_next_sibling()
            for _ in range(10):
                if scan and scan.name == "table":
                    return scan, "h3-match"
                scan = scan.find_next_sibling() if scan else None
        if el.name == "table" and len(el.select("td")) >= 4:
            return el, "early-table"
    return None, "none"


def parse_table(table, cat, out, seen):
    rows = table.select("tr")
    for row in rows[1:]:
        cells = row.select("td")
        if len(cells) < 4:
            continue
        texts = [c.get_text(strip=True) for c in cells if c.get_text(strip=True)]
        if len(texts) >= 5:
            name, style, loc, cost = texts[1], texts[2], texts[3], texts[4]
        elif len(texts) >= 4:
            name, style, loc, cost = texts[0], texts[1], texts[2], texts[3]
        else:
            continue
        if name.lower() in ("name", "picture"):
            continue
        key = f"{cat}|{name}|{style}"
        if key in seen:
            continue
        seen.add(key)
        out.append(name)


items = []
seen = set()
for cat_id, header in category_headers.items():
    anchor = doc.select_one(f'a[name="{cat_id}"], #{cat_id}')
    if not anchor:
        print(cat_id, "NO ANCHOR")
        continue
    n_anchors = len(doc.select(f'a[name="{cat_id}"]'))
    table, how = find_section_table(anchor, header)
    if table:
        before = len(items)
        parse_table(table, cat_id, items, seen)
        hdr = table.select("tr")[0].get_text()[:40]
        print(
            cat_id,
            f"anchors={n_anchors}",
            how,
            "hdr",
            hdr.replace("\n", " "),
            "added",
            len(items) - before,
        )
    else:
        print(cat_id, "NO TABLE")

print("total", len(items))
print("blouse in tops?", sum(1 for i in items if "blouse" in i.lower()))
