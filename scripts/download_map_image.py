"""Download bundled Lumiose map image (build-time only)."""
import urllib.request
from pathlib import Path

OUT = Path(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\map\lumiose.jpg")

# Official-style map from Serebii (build-time scrape source)
URLS = [
    "https://cdn.mapgenie.io/images/games/pokemon-legends-z-a/preview.jpg",
]


def main():
    OUT.parent.mkdir(parents=True, exist_ok=True)
    for url in URLS:
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "PokemonZACompanion/1.3"})
            data = urllib.request.urlopen(req, timeout=30).read()
            if len(data) > 5000:
                OUT.write_bytes(data)
                print("Saved", OUT, len(data), "bytes from", url)
                return
        except Exception as e:
            print("fail", url, e)
    raise SystemExit("Could not download map image")


if __name__ == "__main__":
    main()
