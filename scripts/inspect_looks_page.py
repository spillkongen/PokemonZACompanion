import re
import urllib.request
from bs4 import BeautifulSoup

url = "https://www.serebii.net/legendsz-a/looks.shtml"
raw = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "x"}), timeout=60
).read()
soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

print("title:", soup.title.get_text() if soup.title else "?")
for h2 in soup.find_all("h2")[:15]:
    print("h2:", h2.get_text(strip=True))
for h3 in soup.find_all("h3")[:20]:
    print("h3:", h3.get_text(strip=True))

for anchor in soup.find_all("a", attrs={"name": True}):
    n = anchor.get("name")
    if any(x in n for x in ("male", "female", "top", "women", "men")):
        print("named:", n)

print("dextable:", len(soup.select("table.dextable")))

raws = raw.decode("iso-8859-1", "replace")
for term in ["female", "male", "Women", "Men"]:
    print(term, raw.lower().count(term.lower()))

# sample imgs
imgs = re.findall(r"legendsz-a/[^\"']+\.jpg", raws)[:15]
print("img samples:", imgs)
