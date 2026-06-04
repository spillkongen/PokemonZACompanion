import re
import urllib.request

url = "https://www.serebii.net/legendsz-a/customisation.shtml"
html = urllib.request.urlopen(
    urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=30
).read().decode("utf-8", errors="ignore")

# Find script references
for m in re.finditer(r'src="([^"]+\.js)"', html):
    if "custom" in m.group(1).lower() or "legends" in m.group(1).lower():
        print("js:", m.group(1))

# Search for mf male female in page source
for pat in ["male", "female", "Male", "Female", "gender", "_m.", "_f.", "/m/", "/f/"]:
    if pat.lower() in html.lower():
        idx = html.lower().find(pat.lower())
        print(pat, "found at", idx, html[max(0, idx - 50) : idx + 80].replace("\n", " ")[:120])

# Check custom.js if exists
try:
    js = urllib.request.urlopen(
        urllib.request.Request(
            "https://www.serebii.net/legendsz-a/custom.js",
            headers={"User-Agent": "Mozilla/5.0"},
        ),
        timeout=15,
    ).read().decode("utf-8", errors="ignore")
    print("custom.js len", len(js))
    for pat in ["male", "female", "gender"]:
        print(pat, js.lower().count(pat.lower()))
    print(js[:800])
except Exception as e:
    print("no custom.js", e)
