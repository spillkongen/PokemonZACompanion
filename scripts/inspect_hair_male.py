import urllib.request
from bs4 import BeautifulSoup

url = "https://www.serebii.net/legendsz-a/customisation.shtml"
html = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=30
).read()
soup = BeautifulSoup(html, "html.parser")

def tables_after(anchor_name, stop_names):
    a = soup.find("a", {"name": anchor_name})
    if not a:
        return []
    el = a
    tables = []
    for _ in range(2000):
        el = el.find_next()
        if not el:
            break
        if el.name == "a" and el.get("name") in stop_names and el.get("name") != anchor_name:
            break
        if el.name == "table" and "anctab" in (el.get("class") or []):
            rows = el.find_all("tr")
            if len(rows) > 2:
                data_rows = []
                for row in rows[1:4]:
                    tds = row.find_all("td")
                    if len(tds) >= 4:
                        img = row.find("img")
                        src = img.get("src", "") if img else ""
                        texts = [td.get_text(strip=True) for td in tds]
                        data_rows.append((texts, src[-30:]))
                tables.append((len(rows) - 1, data_rows))
    return tables

# hair section until male
hair_tables = tables_after("hair", {"male", "tops"})
male_tables = tables_after("male", {"tops"})

print("hair section tables", len(hair_tables))
if hair_tables:
    print("  first table rows", hair_tables[0][0], "samples", hair_tables[0][1])

print("male section tables", len(male_tables))
if male_tables:
    print("  first table rows", male_tables[0][0], "samples", male_tables[0][1][:2])

# Check h2/h3 between hair and male
hair = soup.find("a", {"name": "hair"})
male = soup.find("a", {"name": "male"})
el = hair
while el and el != male:
    el = el.find_next()
    if el.name in ("h2", "h3", "p"):
        t = el.get_text(strip=True)[:80]
        if t:
            print("between hair-male:", el.name, t)

# Full page text search
raw = html.decode("utf-8", errors="ignore")
for phrase in ["Female", "female", "Woman", "girl", "Hair Options", "Clothing Options"]:
    print(phrase, raw.count(phrase))

# img pattern - two imgs per row?
male_anchor = soup.find("a", {"name": "male"})
t = male_anchor.find_next("table", class_="anctab")
for row in t.find_all("tr")[1:6]:
    imgs = row.find_all("img")
    print("imgs", len(imgs), [i.get("src", "")[-25:] for i in imgs])
