import re
import urllib.request
from bs4 import BeautifulSoup

def fetch(url):
    return urllib.request.urlopen(
        urllib.request.Request(url, headers={"User-Agent": "x"}), timeout=25
    ).read().decode("utf-8", "replace")

for path in ["megaevolutions.shtml", "dlc-megaevolutions.shtml", "wildpokemon.shtml", "megadimension.shtml"]:
    url = f"https://www.serebii.net/legendsz-a/{path}"
    soup = BeautifulSoup(fetch(url), "html.parser")
    print("\n===", path, "===")
    for t in soup.select("table"):
        rows = t.select("tr")
        if len(rows) < 2:
            continue
        hdr = rows[0].get_text(" | ", strip=True)[:120]
        if len(hdr) < 5:
            continue
        print("hdr:", hdr, "rows:", len(rows) - 1)
        for r in rows[1:3]:
            print(" ", r.get_text(" | ", strip=True)[:140])
