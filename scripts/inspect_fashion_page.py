import urllib.request
from bs4 import BeautifulSoup

url = "https://www.serebii.net/legendsz-a/customisation.shtml"
raw = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "inspect"}), timeout=60
).read()
soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

print("h3 headers:")
for h3 in soup.find_all("h3"):
    print(" ", repr(h3.get_text(strip=True)))

print("\ndextable count:", len(soup.select("table.dextable")))

for cat in ["tops", "bottoms", "all-in-one"]:
    anchor = soup.select(f'a[name="{cat}"]')
    if not anchor:
        continue
    el = anchor[0]
    for _ in range(120):
        el = el.find_next()
        if not el:
            break
        if el.name == "h3" and cat.replace("-", " ").title() in el.get_text() or el.get_text().strip() in (
            "Tops",
            "Bottoms",
            "All-in-One",
        ):
            tbl = el.find_next("table", class_="dextable")
            if tbl:
                rows = tbl.find_all("tr")[1:6]
                print(f"\n=== {cat} sample rows ===")
                for row in rows:
                    imgs = [(i.get("src") or "") for i in row.find_all("img")]
                    texts = [c.get_text(strip=True) for c in row.find_all("td") if c.get_text(strip=True)]
                    print(" imgs:", len(row.find_all("img")), imgs[:3])
                    print(" texts:", texts[:5])
                break

# search page for women/female
html = str(soup)
for term in ["Women", "Female", "fem", "Male", "Men", "Blouse", "data-female", "custom/f"]:
    idx = html.lower().find(term.lower())
    print(f"{term}: {'found at '+str(idx) if idx>=0 else 'not found'}")
