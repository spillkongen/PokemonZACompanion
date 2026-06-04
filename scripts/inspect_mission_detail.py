import urllib.request
from bs4 import BeautifulSoup

paths = [
    "sidemissions/abigol'bunnelby.shtml",
    "mainmissions/getyourtravelbagback.shtml",
]
base = "https://www.serebii.net/legendsz-a/"

for path in paths:
    u = base + path
    try:
        html = urllib.request.urlopen(
            urllib.request.Request(u, headers={"User-Agent": "Mozilla/5.0"}), timeout=15
        ).read().decode("utf-8", "replace")
        soup = BeautifulSoup(html, "html.parser")
        print("OK", path)
        for h in soup.select("h2, h3")[:8]:
            print(" ", h.name, h.get_text(strip=True)[:80])
        for p in soup.select("p"):
            t = p.get_text(" ", strip=True)
            if len(t) > 40:
                print(" p:", t[:220])
    except Exception as e:
        print("fail", path, e)
