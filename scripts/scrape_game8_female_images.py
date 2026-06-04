"""Try to extract Game8 female outfit image URLs for Z-A clothing pages."""
import re
import urllib.request

URLS = [
    "https://game8.co/games/Pokemon-Legends-Z-A/archives/559432",
    "https://game8.co/games/Pokemon-Legends-Z-A/archives/556536",
]

def fetch(url):
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    return urllib.request.urlopen(req, timeout=30).read().decode("utf-8", errors="ignore")

for url in URLS:
    html = fetch(url)
    imgs = re.findall(r'https://[^"\']+\.(?:jpg|jpeg|png|webp)(?:\?[^"\']*)?', html, re.I)
    print(url, "images", len(imgs))
    for u in list(dict.fromkeys(imgs))[:8]:
        if "pokemon" in u.lower() or "legends" in u.lower() or "za" in u.lower() or "outfit" in u.lower() or "cloth" in u.lower():
            print(" ", u[:100])
