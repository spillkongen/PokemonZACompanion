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

for cat in ["tops", "bottoms", "all-in-one"]:
    anchors = soup.find_all("a", attrs={"name": cat})
    print(f"\n=== {cat}: {len(anchors)} anchors ===")
    for i, anchor in enumerate(anchors):
        el = anchor
        h3_text = None
        table_rows = None
        first_item = None
        for _ in range(40):
            el = el.find_next()
            if not el:
                break
            if el.name == "h3" and not h3_text:
                h3_text = el.get_text(strip=True)
            if el.name == "table" and "dextable" in (el.get("class") or []):
                trs = el.find_all("tr")
                if len(trs) > 1 and "Picture" in trs[0].get_text():
                    table_rows = len(trs) - 1
                    tds = trs[1].find_all("td")
                    if len(tds) >= 2:
                        first_item = tds[1].get_text(strip=True)
                    break
        prev_h2 = anchor.find_previous("h2")
        prev_h2_text = prev_h2.get_text(strip=True) if prev_h2 else None
        print(f"  [{i}] h2 before: {prev_h2_text!r} h3: {h3_text!r} rows: {table_rows} first: {first_item!r}")
