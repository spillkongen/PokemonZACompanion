import re
import urllib.request

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "x"},
    ),
    timeout=60,
).read().decode("iso-8859-1", "replace")

# Find script blocks mentioning custom image paths
for m in re.finditer(r"<script[^>]*>([\s\S]{200,8000}?)</script>", raw, re.I):
    body = m.group(1)
    if "custom" in body.lower() and ("key" in body or "select" in body or "img" in body):
        if "male" in body.lower() or "female" in body.lower() or "gender" in body.lower():
            print("=== script with gender ===")
            print(body[:3000])
            print("...")
        elif "all-in-one-select" in body or "tops-select" in body:
            print("=== clothing select script ===")
            print(body[:4000])
