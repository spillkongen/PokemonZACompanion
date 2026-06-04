import re
import urllib.request
from bs4 import BeautifulSoup

BASE = "https://www.serebii.net/legendsz-a/"

def fetch(url):
    return urllib.request.urlopen(
        urllib.request.Request(url, headers={"User-Agent": "x"}), timeout=30
    ).read().decode("utf-8", "replace")

for page in ["megaevolutions.shtml", "dlc-megaevolutions.shtml"]:
    soup = BeautifulSoup(fetch(BASE + page), "html.parser")
    print("\n===", page, "===")
    names = []
    for table in soup.select("table"):
        for row in table.select("tr")[1:]:
            # links with pokemon name
            for a in row.select("a"):
                t = a.get_text(strip=True)
                if t and len(t) > 2 and not t.isdigit():
                    names.append(t)
            cells = [c.get_text(" ", strip=True) for c in row.select("td")]
            for c in cells:
                if c.startswith("Mega "):
                    names.append(c)
    blast = [n for n in names if "blast" in n.lower() or "char" in n.lower() or "venus" in n.lower()]
    print("sample names", blast[:20])
    print("total extracted", len(names))
    # show row structure for blastoise search
    for row in soup.select("tr"):
        if "blastoise" in row.get_text().lower():
            print("ROW HTML:", str(row)[:400])
