import urllib.request
from bs4 import BeautifulSoup

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "x"},
    ),
    timeout=60,
).read()
soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

for row in soup.select("table.dextable tr"):
    tds = row.find_all("td")
    if not tds:
        continue
    text = row.get_text()
    if "Blouse & Skort" in text:
        print(row.prettify()[:2500])
        break

# clothing anchor (not hair)
for a in soup.select("a[data-key]"):
    if a.get("href") == "#tops" or (a.parent and "dextable" in str(a.parent.parent.get("class"))):
        pass
for a in soup.select("table.dextable a[data-key]"):
    if a.get("data-key") == "544":
        print("\n--- anchor 544 ---")
        print(a.prettify())
        break
