import json
import re
import urllib.request
from urllib.parse import quote
from bs4 import BeautifulSoup

page = "List_of_Pokémon_in_Pokémon_Legends:_Z-A"
url = "https://bulbapedia.bulbagarden.net/w/api.php?action=parse&format=json&prop=text&page=" + quote(page, safe="_:")
req = urllib.request.Request(url, headers={"User-Agent": "x"})
html = json.loads(urllib.request.urlopen(req, timeout=60).read())["parse"]["text"]["*"]
soup = BeautifulSoup(html, "html.parser")
tables = soup.select("table.roundy, table.sortable, table.wikitable")
print("tables", len(tables))
for ti, table in enumerate(tables[:5]):
    rows = table.select("tr")
    print("table", ti, "rows", len(rows))
    if rows:
        print("  hdr", rows[0].get_text(" ", strip=True)[:100])
    count = 0
    for row in rows[1:6]:
        cells = row.select("td, th")
        links = row.select("a[href]")
        for a in links:
            if "/wiki/" in a.get("href", "") and "Category:" not in a["href"]:
                print("  row ndex", cells[0].get_text(strip=True) if cells else "", "name", repr(a.get_text(strip=True)), "title", a.get("title"))
                break
        if len(cells) >= 4 and link:
            count += 1
    print("  sample ok", count)
