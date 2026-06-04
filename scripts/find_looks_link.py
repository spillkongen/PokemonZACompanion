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

for a in soup.find_all("a", href=True):
    t = a.get_text(strip=True).lower()
    h = a["href"]
    if "look" in t or "trainer" in t or "custom" in h.lower():
        if "look" in t or "detail" in t:
            print(a.get_text(strip=True), "->", h)
