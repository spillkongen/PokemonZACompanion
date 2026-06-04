import urllib.request
from bs4 import BeautifulSoup

url = "https://www.serebii.net/legendsz-a/megastones.shtml"
html = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "x"}), timeout=20
).read().decode("utf-8", "replace")
soup = BeautifulSoup(html, "html.parser")
for i, t in enumerate(soup.select("table")[:8]):
    rows = t.select("tr")
    print("table", i, "rows", len(rows))
    if rows:
        print(" hdr", rows[0].get_text(" | ", strip=True)[:120])
    for r in rows[1:4]:
        print("  ", [c.get_text(" ", strip=True)[:40] for c in r.select("td")])
