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

for anchor in soup.find_all("a", attrs={"name": True}):
    name = anchor.get("name")
    if name in ("male", "female") or "female" in name or "women" in name:
        print("anchor name=", name)
        # next h3 / tables
        el = anchor
        for _ in range(30):
            el = el.find_next()
            if not el:
                break
            if el.name in ("h2", "h3", "a") and el.get("name"):
                print("  next named", el.name, el.get("name"))
            if el.name == "h3":
                print("  h3:", el.get_text(strip=True))
            if el.name == "table" and "dextable" in (el.get("class") or []):
                rows = len(el.find_all("tr")) - 1
                first = el.find_all("tr")[1].get_text(" ", strip=True)[:80] if rows else ""
                print(f"  table rows={rows} first={first}")
                break

# count tables between male and female
male = soup.find("a", attrs={"name": "male"})
female = soup.find("a", attrs={"name": "female"})
print("\nmale found", male is not None, "female found", female is not None)

if male and female:
    tables_between = []
    el = male
    while el and el != female:
        el = el.find_next()
        if el and el.name == "table" and "dextable" in (el.get("class") or []):
            hdr = el.find("tr")
            h = hdr.get_text() if hdr else ""
            if "Picture" in h and "Name" in h:
                tables_between.append((el.find_previous("h3"), len(el.find_all("tr")) - 1))
    print("tables from male to female:", len(tables_between))
    for h3, n in tables_between:
        print(" ", h3.get_text(strip=True) if h3 else "?", n, "rows")

if female:
    tables_after = []
    el = female
    for _ in range(500):
        el = el.find_next()
        if not el:
            break
        if el.name == "table" and "dextable" in (el.get("class") or []):
            hdr = el.find("tr")
            h = hdr.get_text() if hdr else ""
            if "Picture" in h and "Name" in h:
                h3 = el.find_previous("h3")
                tables_after.append((h3.get_text(strip=True) if h3 else "?", len(el.find_all("tr")) - 1))
    print("tables after female:", len(tables_after))
    for t in tables_after[:15]:
        print(" ", t)
