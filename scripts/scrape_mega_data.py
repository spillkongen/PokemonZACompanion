"""Scrape mega-capable Pokémon from Serebii mega stones + mega evolution pages."""
import json
import re
import urllib.request
from pathlib import Path

POKEMON_JSON = Path(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\pokemon_za.json")
BASE = "https://www.serebii.net/legendsz-a/"

TYPE_CHART = {
    "Normal": {"weak": ["Fighting"], "resist": [], "immune": ["Ghost"]},
    "Fire": {"weak": ["Water", "Ground", "Rock"], "resist": ["Fire", "Grass", "Ice", "Bug", "Steel", "Fairy"], "immune": []},
    "Water": {"weak": ["Electric", "Grass"], "resist": ["Fire", "Water", "Ice", "Steel"], "immune": []},
    "Electric": {"weak": ["Ground"], "resist": ["Electric", "Flying", "Steel"], "immune": []},
    "Grass": {"weak": ["Fire", "Ice", "Poison", "Flying", "Bug"], "resist": ["Water", "Electric", "Grass", "Ground"], "immune": []},
    "Ice": {"weak": ["Fire", "Fighting", "Rock", "Steel"], "resist": ["Ice"], "immune": []},
    "Fighting": {"weak": ["Flying", "Psychic", "Fairy"], "resist": ["Bug", "Rock", "Dark"], "immune": []},
    "Poison": {"weak": ["Ground", "Psychic"], "resist": ["Grass", "Fighting", "Poison", "Bug", "Fairy"], "immune": []},
    "Ground": {"weak": ["Water", "Grass", "Ice"], "resist": ["Poison", "Rock"], "immune": ["Electric"]},
    "Flying": {"weak": ["Electric", "Ice", "Rock"], "resist": ["Grass", "Fighting", "Bug"], "immune": ["Ground"]},
    "Psychic": {"weak": ["Bug", "Ghost", "Dark"], "resist": ["Fighting", "Psychic"], "immune": []},
    "Bug": {"weak": ["Fire", "Flying", "Rock"], "resist": ["Grass", "Fighting", "Ground"], "immune": []},
    "Rock": {"weak": ["Water", "Grass", "Fighting", "Ground", "Steel"], "resist": ["Normal", "Fire", "Poison", "Flying"], "immune": []},
    "Ghost": {"weak": ["Ghost", "Dark"], "resist": ["Poison", "Bug"], "immune": ["Normal", "Fighting"]},
    "Dragon": {"weak": ["Ice", "Dragon", "Fairy"], "resist": ["Fire", "Water", "Electric", "Grass"], "immune": []},
    "Dark": {"weak": ["Fighting", "Bug", "Fairy"], "resist": ["Ghost", "Dark"], "immune": ["Psychic"]},
    "Steel": {"weak": ["Fire", "Fighting", "Ground"], "resist": ["Normal", "Grass", "Ice", "Flying", "Psychic", "Bug", "Rock", "Dragon", "Steel", "Fairy"], "immune": ["Poison"]},
    "Fairy": {"weak": ["Poison", "Steel"], "resist": ["Fighting", "Bug", "Dark"], "immune": ["Dragon"]},
}


def fetch(url: str) -> str:
    return urllib.request.urlopen(
        urllib.request.Request(url, headers={"User-Agent": "PokemonZACompanion/1.4"}), timeout=30
    ).read().decode("utf-8", "replace")


def calc_weaknesses(types: list[str]) -> tuple[list[str], list[str]]:
    weak = set()
    resist = set()
    immune = set()
    for t in types:
        info = TYPE_CHART.get(t)
        if not info:
            continue
        weak.update(info["weak"])
        resist.update(info["resist"])
        immune.update(info["immune"])
    for t in types:
        weak -= {t}
        resist -= {t}
    for t in immune:
        weak.discard(t)
        resist.discard(t)
    return sorted(weak - resist - immune), sorted((resist - weak) - immune)


def normalize_name(name: str) -> str:
    return re.sub(r"[^a-z0-9]", "", name.lower())


def parse_mega_from_stats_pages(html: str) -> dict[str, list[str]]:
    from bs4 import BeautifulSoup

    soup = BeautifulSoup(html, "html.parser")
    result: dict[str, list[str]] = {}
    for table in soup.select("table"):
        for row in table.select("tr"):
            for cell in row.select("td, th"):
                text = cell.get_text(" ", strip=True)
                if not text.startswith("Mega "):
                    continue
                parts = text.replace("Mega ", "", 1).strip().split()
                if not parts:
                    continue
                suffix = ""
                if parts[-1] in ("X", "Y", "Z", "EX"):
                    suffix = parts[-1]
                    parts = parts[:-1]
                base = " ".join(parts)
                label = f"Mega {base}" + (f" {suffix}" if suffix else "")
                result.setdefault(base, [])
                if label not in result[base]:
                    result[base].append(label)
    return result


def parse_mega_from_stones(html: str) -> dict[str, list[str]]:
    """Mega stones page lists species that can mega evolve via stone effect text."""
    from bs4 import BeautifulSoup

    soup = BeautifulSoup(html, "html.parser")
    result: dict[str, list[str]] = {}
    for table in soup.select("table"):
        rows = table.select("tr")
        if len(rows) < 2:
            continue
        hdr = rows[0].get_text(" ", strip=True).lower()
        if "name" not in hdr or "location" not in hdr:
            continue
        for row in rows[1:]:
            cells = row.select("td")
            if len(cells) < 2:
                continue
            stone = cells[1].get_text(" ", strip=True)
            effect = cells[2].get_text(" ", strip=True) if len(cells) > 2 else ""
            pokemon = ""
            m = re.search(r"A ([A-Za-z][A-Za-z\s\.\-\']+?) holding", effect)
            if m:
                pokemon = m.group(1).strip()
            if not pokemon:
                pokemon = stone_to_pokemon(stone)
            if not pokemon:
                continue
            form = stone_to_form_label(stone, pokemon)
            result.setdefault(pokemon, [])
            if form not in result[pokemon]:
                result[pokemon].append(form)
    return result


def stone_to_pokemon(stone: str) -> str:
    if not stone:
        return ""
    s = stone.strip()
    if s.endswith("ite X") or s.endswith("ite Y") or s.endswith("ite Z"):
        base = s.replace("ite X", "").replace("ite Y", "").replace("ite Z", "")
        return base
    if s.endswith("ite"):
        name = s[:-3]
        specials = {
            "Blastois": "Blastoise",
            "Mewtw": "Mewtwo",
            "Ampharos": "Ampharos",
            "Heracross": "Heracross",
            "Houndoom": "Houndoom",
            "Tyranitar": "Tyranitar",
            "Gardevoir": "Gardevoir",
            "Gallade": "Gallade",
            "Lucario": "Lucario",
            "Garchomp": "Garchomp",
            "Absol": "Absol",
            "Manectric": "Manectric",
            "Gengar": "Gengar",
            "Kangaskhan": "Kangaskhan",
            "Pinsir": "Pinsir",
            "Aerodactyl": "Aerodactyl",
            "Alakazam": "Alakazam",
            "Slowbro": "Slowbro",
            "Venusaur": "Venusaur",
            "Charizard": "Charizard",
        }
        return specials.get(name, name)
    return ""


def stone_to_form_label(stone: str, pokemon: str) -> str:
    if stone.endswith("ite X"):
        return f"Mega {pokemon} X"
    if stone.endswith("ite Y"):
        return f"Mega {pokemon} Y"
    if stone.endswith("ite Z"):
        return f"Mega {pokemon} Z"
    return f"Mega {pokemon}"


def merge_mega_maps(*maps: dict[str, list[str]]) -> dict[str, list[str]]:
    merged: dict[str, list[str]] = {}
    for m in maps:
        for pokemon, forms in m.items():
            merged.setdefault(pokemon, [])
            for form in forms:
                if form not in merged[pokemon]:
                    merged[pokemon].append(form)
    return merged


def main():
    mega_map = merge_mega_maps(
        parse_mega_from_stones(fetch(BASE + "megastones.shtml")),
        parse_mega_from_stats_pages(fetch(BASE + "megaevolutions.shtml")),
        parse_mega_from_stats_pages(fetch(BASE + "dlc-megaevolutions.shtml")),
    )

    data = json.loads(POKEMON_JSON.read_text(encoding="utf-8"))
    norm_mega = {normalize_name(k): (k, v) for k, v in mega_map.items()}

    matched = 0
    for mon in data["pokemon"]:
        types = mon.get("types", [])
        weak, resist = calc_weaknesses(types)
        mon["weaknesses"] = weak
        mon["resistances"] = resist
        key = normalize_name(mon["name"])
        forms = norm_mega.get(key, (None, []))[1]
        mon["canMegaEvolve"] = len(forms) > 0
        mon["megaForms"] = forms
        if forms:
            matched += 1
            mon["detail"] = (
                f"{mon['name']} can Mega Evolve: {', '.join(forms)}. "
                f"Weak to: {', '.join(weak) if weak else '—'}."
            )
        else:
            mon["detail"] = (
                f"{mon['name']} in Pokémon Legends: Z-A. "
                f"Weak to: {', '.join(weak) if weak else '—'}."
            )

    data["megaSpeciesCount"] = matched
    POKEMON_JSON.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    print("Mega-capable species:", matched)
    for name in ["Blastoise", "Charizard", "Venusaur", "Pikachu", "Lucario"]:
        p = next((x for x in data["pokemon"] if x["name"] == name), None)
        if p:
            print(f"  {name}: {p['canMegaEvolve']} {p['megaForms']}")


if __name__ == "__main__":
    main()
