import re
import urllib.request
from bs4 import BeautifulSoup

url = "https://www.serebii.net/legendsz-a/customisation.shtml"
html = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=30
).read()
soup = BeautifulSoup(html, "html.parser")

for gender in ["male", "female"]:
    anchor = soup.find("a", {"name": gender})
    print("anchor", gender, anchor)
    if not anchor:
        continue
    # find next h2 or tables
    el = anchor
    tables = []
    for _ in range(200):
        el = el.find_next()
        if not el:
            break
        if el.name == "a" and el.get("name") in ("male", "female") and el.get("name") != gender:
            break
        if el.name == "table" and "roundy" in (el.get("class") or []):
            rows = len(el.find_all("tr")) - 1
            if rows > 5:
                tables.append(rows)
    print(gender, "tables row counts", tables[:15], "total tables", len(tables))

# Find female anchor
f = soup.find("a", {"name": "female"})
if f:
    nxt = f.find_next("h2") or f.find_next("h3")
    print("female next heading", nxt.get_text() if nxt else None)
