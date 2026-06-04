import urllib.request

def exists(url):
    try:
        req = urllib.request.Request(url, method="HEAD", headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=10) as r:
            return r.status
    except Exception as e:
        return str(e)[:40]

base = "https://www.serebii.net/legendsz-a/custom/th/1088"
patterns = [
    f"{base}.jpg",
    f"{base}_f.jpg",
    f"{base}_m.jpg",
    f"{base}f.jpg",
    "https://www.serebii.net/legendsz-a/custom/th/f/1088.jpg",
    "https://www.serebii.net/legendsz-a/custom/th/m/1088.jpg",
    "https://www.serebii.net/legendsz-a/custom/th/1088_f.jpg",
    "https://www.serebii.net/legendsz-a/custom/th/1088-2.jpg",
    "https://www.serebii.net/legendsz-a/custom/th/2088.jpg",
]
for p in patterns:
    print(p, exists(p))
