import json
import urllib.request
from bs4 import BeautifulSoup

URL = "https://www.serebii.net/legendsz-a/customisation.shtml"
raw = urllib.request.urlopen(
    urllib.request.Request(URL, headers={"User-Agent": "x"}), timeout=60
).read()
soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

items = json.load(
    open(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\fashion_serebii.json", encoding="utf-8")
)["items"]
json_tops = {(i["name"], i["style"]) for i in items if i["category"] == "tops"}

h3 = next(h for h in soup.find_all("h3") if h.get_text(strip=True) == "Tops")
table = h3.find_next("table", class_="dextable")

missing = []
for row in table.find_all("tr")[1:]:
    cells = row.find_all("td")
    texts = [c.get_text(strip=True) for c in cells if c.get_text(strip=True)]
    if len(texts) >= 5:
        name, style = texts[1], texts[2]
    elif len(texts) >= 4:
        name, style = texts[0], texts[1]
    else:
        missing.append(("bad_cols", len(cells), texts))
        continue
    if (name, style) not in json_tops:
        missing.append((name, style, len(cells), texts))

print("missing from json:", len(missing))
for m in missing[:20]:
    print(m)
