import urllib.request
from bs4 import BeautifulSoup
from collections import Counter

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "x"},
    ),
    timeout=60,
).read()
soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

for table in soup.select("table.dextable"):
    hdr = table.find("tr")
    if not hdr or "Picture" not in hdr.get_text():
        continue
    h3 = table.find_previous("h3")
    cat = h3.get_text(strip=True) if h3 else "?"
    col_counts = Counter()
    two_img = 0
    for row in table.find_all("tr")[1:]:
        cells = row.find_all("td")
        col_counts[len(cells)] += 1
        if len(row.find_all("img")) >= 2:
            two_img += 1
            if two_img <= 2:
                print(f"\n{cat} 2-img row:")
                print(row.prettify()[:1500])
    print(f"{cat}: cols {dict(col_counts)} two_img_rows={two_img}")
