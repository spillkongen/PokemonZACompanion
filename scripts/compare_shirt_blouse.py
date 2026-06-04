import urllib.request
from bs4 import BeautifulSoup

url = "https://www.serebii.net/legendsz-a/customisation.shtml"
raw = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=30
).read()
soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

for table in soup.find_all("table", class_="dextable"):
    hdr = table.find("tr")
    if not hdr or "Name" not in hdr.get_text():
        continue
    for row in table.find_all("tr")[1:]:
        tds = row.find_all("td")
        if len(tds) < 5:
            continue
        name = tds[1].get_text(strip=True)
        if "Blazer" in name or "Blouse" in name and "Set" in name:
            img = row.find("img")
            src = img.get("src", "") if img else ""
            print(name, tds[2].get_text(strip=True)[:20], src)
