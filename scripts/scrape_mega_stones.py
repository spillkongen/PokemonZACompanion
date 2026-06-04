"""Scrape Serebii mega stones into assets."""
import json
import urllib.request
from pathlib import Path
from bs4 import BeautifulSoup

OUT = Path(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\mega_stones_serebii.json")
URL = "https://www.serebii.net/legendsz-a/megastones.shtml"


def main():
    html = urllib.request.urlopen(
        urllib.request.Request(URL, headers={"User-Agent": "PokemonZACompanion/1.4"}), timeout=30
    ).read().decode("utf-8", "replace")
    soup = BeautifulSoup(html, "html.parser")
    stones = []
    for table in soup.select("table"):
        rows = table.select("tr")
        if len(rows) < 2:
            continue
        hdr = rows[0].get_text(" ", strip=True).lower()
        if "name" not in hdr or "location" not in hdr:
            continue
        for row in rows[1:]:
            cells = row.select("td")
            if len(cells) < 3:
                continue
            name = cells[1].get_text(" ", strip=True)
            effect = cells[2].get_text(" ", strip=True) if len(cells) > 2 else ""
            location = cells[3].get_text(" ", strip=True) if len(cells) > 3 else cells[-1].get_text(" ", strip=True)
            if name and len(name) > 2:
                stones.append({"stone": name, "effect": effect, "location": location})
        if stones:
            break
    payload = {"source": URL, "count": len(stones), "stones": stones}
    OUT.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    print("Wrote", len(stones), "mega stones")


if __name__ == "__main__":
    main()
