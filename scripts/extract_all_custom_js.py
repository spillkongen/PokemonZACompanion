import re
import urllib.request

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "x"},
    ),
    timeout=60,
).read().decode("iso-8859-1", "replace")

for m in re.finditer(r"getElementById\('([^']+)'\)\.src\s*=\s*'([^']+)'", raw):
    print(m.group(1), "->", m.group(2))

for m in re.finditer(r"custom/[^']+", raw):
    s = m.group(0)
    if "hair" not in s and len(s) < 80:
        if s not in getattr(extract_all_custom_js, "seen", set()):
            extract_all_custom_js.seen = getattr(extract_all_custom_js, "seen", set()) | {s}

print("\nunique custom paths in js assignments:")
for line in raw.split("\n"):
    if "custom/" in line and "getElementById" in line:
        print(line.strip()[:180])
