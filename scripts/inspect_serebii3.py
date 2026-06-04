import re
import urllib.request
from bs4 import BeautifulSoup

url = "https://www.serebii.net/legendsz-a/customisation.shtml"
html = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=30
).read()
soup = BeautifulSoup(html, "html.parser")

anchors = soup.find_all("a", attrs={"name": True})
print("named anchors:", [(a.get("name"), a.find_next(["h2", "h3"]).get_text()[:40] if a.find_next(["h2", "h3"]) else None) for a in anchors[:40]])

# male section structure
male = soup.find("a", {"name": "male"})
if male:
    el = male
    for i in range(15):
        el = el.find_next()
        if el and el.name in ("h2", "h3", "table"):
            print(i, el.name, (el.get("class") or []), el.get_text()[:60].replace("\n", " "))

# count tables after male vs after tops
for start_name in ["male", "tops", "all-in-one"]:
    a = soup.find("a", {"name": start_name})
    if not a:
        print("no anchor", start_name)
        continue
    count = 0
    el = a
    for _ in range(500):
        el = el.find_next("table")
        if not el:
            break
        rows = el.find_all("tr")
        if len(rows) > 3:
            count += 1
        if count >= 3:
            first_data = rows[1].find_all("td") if len(rows) > 1 else []
            print(start_name, "sample cols", len(first_data), [td.get_text()[:20] for td in first_data[:6]])
            break

# search female text
text = html.decode("utf-8", errors="ignore")
for m in re.finditer(r'name="([^"]+)"', text):
    if "fem" in m.group(1).lower() or "girl" in m.group(1).lower():
        print("anchor match", m.group(1))
