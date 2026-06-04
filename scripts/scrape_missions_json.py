"""Regenerate app/src/main/assets/missions_serebii.json from Serebii."""
import json
import re
import urllib.request
from bs4 import BeautifulSoup

BASE = "https://www.serebii.net/legendsz-a/"
OUT = r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\missions_serebii.json"

PAGES = [
    ("MAIN", "mainmissions.shtml"),
    ("SIDE", "sidemissions.shtml"),
    ("HYPERSPACE", "hyperspacemissions.shtml"),
]


def fetch(url: str) -> str:
    req = urllib.request.Request(url, headers={"User-Agent": "PokemonZACompanion/1.3"})
    return urllib.request.urlopen(req, timeout=30).read().decode("utf-8", "replace")


def abs_url(href: str) -> str:
    if href.startswith("http"):
        return href
    if href.startswith("/"):
        return "https://www.serebii.net" + href
    return BASE + href.lstrip("/")


def parse_list(html: str, mission_type: str) -> list[dict]:
    soup = BeautifulSoup(html, "html.parser")
    missions = []
    for table in soup.select("table"):
        rows = table.select("tr")
        if len(rows) < 2:
            continue
        header = " | ".join(c.get_text(" ", strip=True) for c in rows[0].select("th, td")).lower()
        if "name" not in header or "description" not in header:
            continue
        for row in rows[1:]:
            cells = row.select("td")
            if len(cells) < 3:
                continue
            number = cells[0].get_text(strip=True)
            link = row.select("a[href]")
            title = link[0].get_text(strip=True) if link else cells[1].get_text(strip=True)
            if not title or len(title) < 2:
                continue
            description = cells[2].get_text(" ", strip=True)
            detail = abs_url(link[0]["href"]) if link else ""
            missions.append(
                {
                    "number": number,
                    "title": title,
                    "type": mission_type,
                    "description": description,
                    "detailUrl": detail,
                }
            )
        if missions:
            break
    return missions


def main():
    all_missions = []
    for mtype, page in PAGES:
        url = BASE + page
        print("Fetching", url)
        html = fetch(url)
        parsed = parse_list(html, mtype)
        print(f"  {mtype}: {len(parsed)}")
        all_missions.extend(parsed)

    payload = {
        "source": BASE,
        "updated": __import__("datetime").date.today().isoformat(),
        "count": len(all_missions),
        "missions": all_missions,
    }
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)
    print("Wrote", OUT, "total", len(all_missions))


if __name__ == "__main__":
    main()
