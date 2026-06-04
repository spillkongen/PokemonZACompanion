import struct
import urllib.request


def dims(url):
    d = urllib.request.urlopen(
        urllib.request.Request(url, headers={"User-Agent": "x"}), timeout=15
    ).read()
    i = 2
    while i < len(d) - 8:
        if d[i] != 0xFF:
            i += 1
            continue
        m = d[i + 1]
        if m in (0xC0, 0xC1, 0xC2):
            h = struct.unpack(">H", d[i + 5 : i + 7])[0]
            w = struct.unpack(">H", d[i + 7 : i + 9])[0]
            return w, h, len(d)
        i += 2 + struct.unpack(">H", d[i + 2 : i + 4])[0]
    return None


for key, name in [("1066", "blouse"), ("1088", "biker"), ("805", "romper")]:
    for kind, path in [("th", f"th/{key}"), ("full", f"{key}")]:
        url = f"https://www.serebii.net/legendsz-a/custom/{path}.jpg"
        print(name, kind, dims(url))
