import re
import urllib.request

url = "https://www.serebii.net/legendsz-a/customisation.shtml"
req = urllib.request.Request(url, headers={"User-Agent": "PokemonZACompanion/1.1"})
html = urllib.request.urlopen(req, timeout=30).read().decode("utf-8", errors="ignore")

print("Male count:", html.count("Male"))
print("Female count:", html.count("Female"))

for anchor in ["tops", "all-in-one", "bottoms"]:
    idx = html.find(f'name="{anchor}"')
    if idx < 0:
        idx = html.find(f'id="{anchor}"')
    if idx < 0:
        continue
    chunk = html[idx : idx + 4000]
    headers = re.findall(r"<th[^>]*>(.*?)</th>", chunk, re.DOTALL | re.I)
    headers = [re.sub(r"<[^>]+>", "", h).strip() for h in headers]
    print(f"\n=== {anchor} headers ===")
    print(headers[:12])
