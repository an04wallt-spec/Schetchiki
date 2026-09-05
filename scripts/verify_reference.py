from pathlib import Path
import hashlib
import sys

EXPECTED = "3b9ef13a6225948aa88336740577fa8cd3b11f853d4c4746c9817aa773ce139d"
path = Path("reference/Schetchiki-v0.5-FINAL.apk")
if not path.exists():
    print("ERROR: reference APK is missing")
    sys.exit(1)
actual = hashlib.sha256(path.read_bytes()).hexdigest()
print("Reference APK SHA-256:", actual)
if actual != EXPECTED:
    print("ERROR: immutable v0.5 reference has been modified")
    sys.exit(2)
print("OK: immutable v0.5 reference is intact")
