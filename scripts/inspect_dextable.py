import urllib.request
from bs4 import BeautifulSoup

url = "https://www.serebii.net/legendsz-a/customisation.shtml"
html = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=30
).read()
soup = BeautifulSoup(html, "html.parser")

for i, table in enumerate(soup.find_all("table", class_="dextable")):
    prev_h = table.find_previous(["h2", "h3"])
    title = prev_h.get_text(strip=True) if prev_h else "?"
    rows = table.find_all("tr")
    header = rows[0].find_all(["th", "td"]) if rows else []
    hdr = [c.get_text(strip=True)[:20] for c in header]
    print(f"\n=== dextable {i} after '{title}' rows={len(rows)} header={hdr}")
    for row in rows[1:4]:
        tds = row.find_all("td")
        imgs = row.find_all("img")
        names = [td.get_text(strip=True)[:25] for td in tds]
        srcs = [(img.get("src") or "")[-35:] for img in imgs]
        print("  cols", len(tds), "imgs", len(imgs), "texts", names[:6], "srcs", srcs[:4])
