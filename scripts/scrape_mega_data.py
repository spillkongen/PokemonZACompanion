"""Scrape Serebii mega evolution names and enhance pokemon_za.json."""
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
    final_weak = sorted(weak - resist - immune)
    final_resist = sorted((resist - weak) - immune)
    return final_weak, final_resist


def parse_mega_names(html: str) -> dict[str, list[str]]:
    """Map base pokemon name -> list of mega form labels."""
    from bs4 import BeautifulSoup

    soup = BeautifulSoup(html, "html.parser")
    result: dict[str, list[str]] = {}
    for table in soup.select("table"):
        for row in table.select("tr"):
            cells = [c.get_text(" ", strip=True) for c in row.select("td, th")]
            if len(cells) < 2:
                continue
            for cell in cells:
                if not cell.startswith("Mega "):
                    continue
                parts = cell.replace("Mega ", "", 1).strip().split()
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


def normalize_name(name: str) -> str:
    return re.sub(r"[^a-z0-9]", "", name.lower())


def main():
    mega_map: dict[str, list[str]] = {}
    for page in ["megaevolutions.shtml", "dlc-megaevolutions.shtml"]:
        mega_map.update(parse_mega_names(fetch(BASE + page)))

    data = json.loads(POKEMON_JSON.read_text(encoding="utf-8"))
    norm_mega = {normalize_name(k): v for k, v in mega_map.items()}

    for mon in data["pokemon"]:
        types = mon.get("types", [])
        weak, resist = calc_weaknesses(types)
        mon["weaknesses"] = weak
        mon["resistances"] = resist
        forms = norm_mega.get(normalize_name(mon["name"]), [])
        mon["canMegaEvolve"] = len(forms) > 0
        mon["megaForms"] = forms
        if forms:
            mon["detail"] = (
                f"{mon['name']} can Mega Evolve into: {', '.join(forms)}. "
                f"Weak to: {', '.join(weak) if weak else '—'}."
            )
        else:
            mon["detail"] = (
                f"{mon['name']} in Pokémon Legends: Z-A. "
                f"Weak to: {', '.join(weak) if weak else '—'}."
            )

    data["megaSpeciesCount"] = len([m for m in data["pokemon"] if m.get("canMegaEvolve")])
    POKEMON_JSON.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    print("Enhanced", len(data["pokemon"]), "pokemon,", data["megaSpeciesCount"], "can mega evolve")


if __name__ == "__main__":
    main()
