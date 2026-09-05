from pathlib import Path
import hashlib
import sys

p = Path('baseline/Schetchiki-v0.5-FINAL.apk')
expected = '3b9ef13a6225948aa88336740577fa8cd3b11f853d4c4746c9817aa773ce139d'

if not p.exists():
    print('ERROR: baseline APK missing')
    sys.exit(2)

actual = hashlib.sha256(p.read_bytes()).hexdigest()
print('baseline:', actual)

if actual != expected:
    print('ERROR: baseline APK was changed')
    sys.exit(3)

print('OK: baseline is intact')
