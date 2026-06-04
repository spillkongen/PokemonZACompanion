import urllib.request
from bs4 import BeautifulSoup
from collections import Counter

url = "https://www.serebii.net/legendsz-a/customisation.shtml"
html = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=30
).read()
soup = BeautifulSoup(html, "html.parser")

classes = Counter()
img_tables = 0
for table in soup.find_all("table"):
    cls = " ".join(table.get("class") or [])
    classes[cls] += 1
    imgs = table.find_all("img")
    rows = table.find_all("tr")
    if len(imgs) > 5 and len(rows) > 5:
        img_tables += 1
        if img_tables <= 2:
            print("TABLE class", cls, "rows", len(rows), "imgs", len(imgs))
            row = rows[1]
            print("  row1 tds", len(row.find_all("td")), [td.get_text()[:15] for td in row.find_all("td")])
            for img in row.find_all("img")[:3]:
                print("   img", img.attrs)

print("table class counts", classes.most_common(10))
print("tables with 5+ imgs", img_tables)
