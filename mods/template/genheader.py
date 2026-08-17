import os

SRC = os.path.join("dexbuild", "classes.dex")
OUT = "dex_payload.h"

with open(SRC, "rb") as f:
    data = f.read()

n = len(data)
lines = []
lines.append("#ifndef DEX_PAYLOAD_H")
lines.append("#define DEX_PAYLOAD_H")
lines.append("#define DEX_LEN %d" % n)
lines.append("static const unsigned char DEX_BYTES[] = {")
per = 16
for i in range(0, n, per):
    chunk = data[i:i + per]
    lines.append("  " + ", ".join("0x%02x" % b for b in chunk) + ",")
lines.append("};")
lines.append("#endif")

with open(OUT, "w") as f:
    f.write("\n".join(lines))

print("wrote %d bytes to %s" % (n, OUT))
