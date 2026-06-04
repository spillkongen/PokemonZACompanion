import urllib.request
from bs4 import BeautifulSoup

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "x"},
    ),
    timeout=60,
).read()
soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

for img_id in ["tops-regular", "all-in-one-regular"]:
    img = soup.find("img", id=img_id)
    if img:
        parent = img.parent
        for _ in range(5):
            if parent:
                print(f"\n=== around {img_id} ===")
                print(parent.prettify()[:2000])
                break
            parent = parent.parent
