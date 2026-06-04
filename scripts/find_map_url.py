import re
import urllib.request

urls = [
    "https://mapgenie.io/pokemon-legends-z-a/maps/lumiose-city",
    "https://cdn.mapgenie.io/images/games/pokemon-legends-z-a/maps/lumiose-city.png",
    "https://archives.bulbagarden.net/media/upload/thumb/4/4e/Lumiose_City_Map.png/800px-Lumiose_City_Map.png",
]
for u in urls:
    try:
        req = urllib.request.Request(u, headers={"User-Agent": "Mozilla/5.0"})
        r = urllib.request.urlopen(req, timeout=20)
        data = r.read(80000)
        print(u, r.status, len(data), r.headers.get("content-type"))
        m = re.search(br'property="og:image" content="([^"]+)"', data)
        if m:
            print("  og:", m.group(1).decode())
    except Exception as e:
        print(u, e)
