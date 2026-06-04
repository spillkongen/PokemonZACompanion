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

for img in soup.find_all("img"):
    iid = img.get("id")
    src = img.get("src", "")
    if iid or "custom" in src:
        if "blank" not in src and ("custom" in src or iid):
            print(iid, src[:70])
