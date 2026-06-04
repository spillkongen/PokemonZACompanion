import urllib.request
from bs4 import BeautifulSoup
from collections import Counter

url = "https://www.serebii.net/legendsz-a/customisation.shtml"
html = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=30
).read()
soup = BeautifulSoup(html, "html.parser")

imgs = []
rows_with_2 = 0
total_rows = 0
for table in soup.find_all("table"):
    if "roundy" not in (table.get("class") or []):
        continue
    for row in table.find_all("tr")[1:]:
        tds = row.find_all("td")
        if len(tds) < 4:
            continue
        total_rows += 1
        row_imgs = row.find_all("img")
        if len(row_imgs) >= 2:
            rows_with_2 += 1
        for img in row_imgs:
            src = img.get("src") or img.get("data-src") or ""
            imgs.append(src)

print("roundy tables rows", total_rows, "rows with 2+ imgs", rows_with_2)
print("unique img count", len(set(imgs)))
# sample paths
samples = list(set(imgs))[:15]
for s in samples:
    print(s)

# data-key on imgs?
keys = []
for img in soup.find_all("img"):
    dk = img.get("data-key")
    if dk:
        keys.append(dk)
print("data-key imgs", len(keys), keys[:10])

# onclick / data attributes on td
for row in soup.find_all("tr")[:200]:
    for td in row.find_all("td"):
        for attr in td.attrs:
            if "data" in attr or "onclick" in attr:
                print("td attr", attr, td.attrs[attr][:60] if isinstance(td.attrs[attr], str) else td.attrs[attr])
