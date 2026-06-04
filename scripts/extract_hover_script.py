import urllib.request
from bs4 import BeautifulSoup

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "Mozilla/5.0"},
    ),
    timeout=30,
).read()
soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

# find script near tops dextable
tops = None
for h in soup.find_all("h3"):
    if h.get_text(strip=True) == "Tops":
        tops = h
        break
if tops:
    table = tops.find_next("table", class_="dextable")
    # scripts between h3 and table?
    el = tops
    for _ in range(5):
        el = el.find_next()
        if el and el.name == "script" and el.string:
            print("SCRIPT before table:", el.string[:1500])
            print("---")

# check img onmouseover
for table in soup.find_all("table", class_="dextable")[:3]:
    for img in table.find_all("img")[:3]:
        print("img attrs", {k: (v[:80] if isinstance(v, str) else v) for k, v in img.attrs.items()})

# parent td onmouseover
table = tops.find_next("table", class_="dextable") if tops else None
if table:
    row = table.find_all("tr")[2]
    for td in row.find_all("td"):
        print("td", td.attrs)
