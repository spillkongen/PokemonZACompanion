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

total = 0
for table in soup.select("table.dextable"):
    hdr = table.find("tr")
    if not hdr or "Picture" not in hdr.get_text():
        continue
    n = len(table.find_all("tr")) - 1
    h3 = table.find_previous("h3")
    total += n
    print(h3.get_text(strip=True) if h3 else "?", n)

print("total rows", total)

# count blouse vs biker in page
text = soup.get_text()
for term in ["Blouse & Skort", "Biker Jacket", "Blazer & Blouse", "Blazer & Shirt"]:
    print(term, text.count(term))
