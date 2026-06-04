import urllib.request
from bs4 import BeautifulSoup

# copy parse logic from scrape_fashion_json
URL = "https://www.serebii.net/legendsz-a/customisation.shtml"
raw = urllib.request.urlopen(
    urllib.request.Request(URL, headers={"User-Agent": "x"}), timeout=60
).read()
soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

cat = "tops"
table = None
for anchor in soup.select(f'a[name="{cat}"]'):
    el = anchor
    for _ in range(80):
        el = el.find_next()
        if not el:
            break
        if el.name == "h3" and "tops" in el.get_text().lower():
            scan = el.find_next("table", class_="dextable")
            if scan and "Picture" in scan.get_text():
                table = scan
                break

seen = set()
skipped = []
parsed = 0
for row in table.find_all("tr")[1:]:
    cells = row.find_all("td")
    if len(cells) < 4:
        skipped.append(("cols", len(cells), row.get_text()[:40]))
        continue
    texts = [c.get_text(strip=True) for c in cells if c.get_text(strip=True)]
    if len(texts) >= 5:
        name, style = texts[1], texts[2]
    elif len(texts) >= 4:
        name, style = texts[0], texts[1]
    else:
        skipped.append(("texts", len(texts), texts))
        continue
    if name.lower() in ("name", "picture"):
        continue
    key = f"{cat}|{name}|{style}"
    if key in seen:
        skipped.append(("dup", key))
        continue
    seen.add(key)
    parsed += 1

print("parsed", parsed, "skipped", len(skipped))
for s in skipped[:15]:
    print(" ", s)
