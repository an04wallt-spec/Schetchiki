# Permanent signing line for Schetchiki

Package: `ru.schetchiki.app`

All releases starting with **v0.9** must use the same permanent signing key.

- Keystore type: `PKCS12`
- Alias: `schetchiki-release`
- Certificate SHA-256: `14:FC:A4:39:D0:E0:94:14:3D:2C:69:B3:61:1C:F2:F1:7B:7C:B4:25:2E:49:35:3D:0F:85:4A:03:C9:9F:B0:FB`
- Certificate valid until: `2054-01-21`

## Rules

1. Never commit the private keystore or its password to this public repository.
2. Every future APK must be signed with the exact same keystore and alias.
3. Every future release must have a `versionCode` greater than the currently installed release.
4. Release APK filenames must be meaningful, for example: `Schetchiki-v0.10-<CHANGE>.apk`.
5. The workflow `.github/workflows/build-signed-release.yml` expects these GitHub Secrets:
   - `SCHETCHIKI_KEYSTORE_B64`
   - `SCHETCHIKI_KEYSTORE_PASSWORD`

The user must keep an offline backup of `Schetchiki-release-keystore.p12` and the signing information file.
