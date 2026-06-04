import re
import urllib.request
from bs4 import BeautifulSoup

url = "https://www.serebii.net/legendsz-a/customisation.shtml"
html = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=30
).read()
soup = BeautifulSoup(html, "html.parser")

for anchor in ["all-in-one", "tops", "bottoms"]:
    a = soup.find("a", {"name": anchor})
    if not a:
        continue
    el = a
    for _ in range(25):
        el = el.find_next()
        if el and el.name == "table":
            max_imgs = 0
            two_links = 0
            for tr in el.find_all("tr")[1:21]:
                imgs = tr.find_all("img")
                links = tr.find_all("a", class_=re.compile("select"))
                max_imgs = max(max_imgs, len(imgs))
                if len(links) >= 2:
                    two_links += 1
            sample_cols = len(el.find_all("tr")[1].find_all("td"))
            print(anchor, "max_imgs", max_imgs, "rows_2links", two_links, "cols", sample_cols)
            break

# Check if thumbnail URLs have male/female pairs - compare data-key usage
anchor = soup.find("a", {"name": "tops"})
el = anchor
for _ in range(25):
    el = el.find_next()
    if el and el.name == "table":
        keys = {}
        for tr in el.find_all("tr")[1:]:
            tds = tr.find_all("td")
            if len(tds) < 4:
                continue
            key = None
            link = tr.find("a", class_=re.compile("select"))
            if link and link.get("data-key"):
                key = link["data-key"]
            name = tds[1].get_text(strip=True)
            style = tds[2].get_text(strip=True)
            img = tr.find("img")
            src = img["src"] if img else None
            if key:
                keys.setdefault(key, []).append((name, style, src))
        # keys with multiple rows might be male/female same item
        multi = {k: v for k, v in keys.items() if len(v) > 1}
        print("keys with multiple entries", len(multi))
        for k, v in list(multi.items())[:3]:
            print("  key", k, v)
        break

# Fetch a thumbnail - check dimensions/naming
print("done")
