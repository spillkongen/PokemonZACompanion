"""Build app/src/main/assets/pokemon_za.json + sprite files (build-time only)."""
import json
import re
import time
import urllib.request
from pathlib import Path

from bs4 import BeautifulSoup

API = "https://bulbapedia.bulbagarden.net/w/api.php"
OUT_JSON = Path(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\pokemon_za.json")
SPRITE_DIR = Path(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\pokemon\sprites")


def fetch_wiki_html(page: str) -> str:
    from urllib.parse import quote
    page_key = quote(page.replace(" ", "_"), safe="_:()")
    url = f"{API}?action=parse&format=json&prop=text&page={page_key}"
    req = urllib.request.Request(url, headers={"User-Agent": "PokemonZACompanion/1.3"})
    data = json.loads(urllib.request.urlopen(req, timeout=60).read().decode())
    return data["parse"]["text"]["*"]


def resolve_img(src: str) -> str:
    if src.startswith("//"):
        return "https:" + src
    if src.startswith("http"):
        return src
    if src.startswith("/"):
        return "https://archives.bulbagarden.net" + src
    return src


def parse_pokemon(html: str) -> list[dict]:
    soup = BeautifulSoup(html, "html.parser")
    results = []
    types_list = {
        "Normal", "Fire", "Water", "Electric", "Grass", "Ice", "Fighting", "Poison",
        "Ground", "Flying", "Psychic", "Bug", "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy",
    }
    for table in soup.select("table.roundy, table.sortable, table.wikitable"):
        for row in table.select("tr")[1:]:
            cells = row.select("td, th")
            if len(cells) < 4:
                continue
            name = ""
            image_url = ""
            for a in row.select("a[href]"):
                href = a.get("href", "")
                if "/wiki/" not in href or "Category:" in href:
                    continue
                candidate = (a.get("title") or a.get_text(strip=True)).strip()
                if candidate and candidate != "Pokémon":
                    name = candidate
                    break
            if not name:
                continue
            ndex = cells[0].get_text(strip=True).replace("#", "")
            if not re.fullmatch(r"\d+", ndex):
                continue
            img = row.select("img")
            if img:
                image_url = resolve_img(img[0]["src"])
            types = []
            for cell in cells:
                t = cell.get_text(strip=True)
                if t in types_list and t not in types:
                    types.append(t)
            lumiose = cells[1].get_text(strip=True).replace("#", "") if len(cells) > 1 else ""
            hyperspace = cells[2].get_text(strip=True).replace("#", "") if len(cells) > 2 else ""
            results.append(
                {
                    "nationalDex": ndex.zfill(4),
                    "lumioseDex": lumiose if re.fullmatch(r"\d+", lumiose) else None,
                    "hyperspaceDex": hyperspace if re.fullmatch(r"\d+", hyperspace) else None,
                    "name": name,
                    "types": types[:2] if types else ["Unknown"],
                    "normallyAvailable": True,
                    "detail": f"{name} appears in Pokémon Legends: Z-A.",
                    "remoteImageUrl": image_url,
                }
            )
    seen = set()
    unique = []
    for p in results:
        key = p["nationalDex"]
        if key in seen:
            continue
        seen.add(key)
        unique.append(p)
    return unique


def download_sprite(pokemon: dict) -> str:
    ndex = pokemon["nationalDex"]
    dest = SPRITE_DIR / f"{ndex}.png"
    if dest.exists() and dest.stat().st_size > 100:
        return f"pokemon/sprites/{ndex}.png"
    url = pokemon.get("remoteImageUrl") or ""
    if not url:
        return ""
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "PokemonZACompanion/1.3"})
        data = urllib.request.urlopen(req, timeout=30).read()
        if len(data) < 50:
            return ""
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(data)
        return f"pokemon/sprites/{ndex}.png"
    except Exception as e:
        print(f"  sprite {ndex}: {e}")
        return ""


def main():
    print("Fetching Bulbapedia dex...")
    html = fetch_wiki_html("List_of_Pokémon_in_Pokémon_Legends:_Z-A")
    mons = parse_pokemon(html)
    print(f"Parsed {len(mons)} Pokémon")
    SPRITE_DIR.mkdir(parents=True, exist_ok=True)
    for i, p in enumerate(mons):
        p["spriteAsset"] = download_sprite(p)
        p.pop("remoteImageUrl", None)
        if (i + 1) % 30 == 0:
            print(f"  sprites {i + 1}/{len(mons)}")
        time.sleep(0.08)
    payload = {
        "bundledOnly": True,
        "count": len(mons),
        "pokemon": mons,
    }
    OUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    OUT_JSON.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    print("Wrote", OUT_JSON)


if __name__ == "__main__":
    main()
