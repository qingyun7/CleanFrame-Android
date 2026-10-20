# CleanFrame Android

This is an independent Android TV source port of https://github.com/nthnclrk/CleanFrame at fa537d1892b71d2de2bf94c24cbff3812e9f70d5. Preserve the upstream MIT copyright, third-party notices and asset source records.

Canonical UI: app/src/main/assets/cleanframe/. Native host: app/src/main/java/com/qingyun/cleanframe/MainActivity.java. Keep TV runtime JavaScript ES5-compatible; preserve remote focus, Back/Home behavior, preference migration, app launch IDs and cached material rendering. Do not add live blur or image analysis to animation frames.

Run npm run check and npm run check:browser for UI changes. Before publication run npm run check:public and npm run export:public. Use public fonts only. The browser fixtures are synthetic and do not establish physical device behavior; record actual validation in docs/release.md.

Do not commit signing keys, tokens, addresses, personal app inventories, preferences, SDKs, build caches or device backups. Keep .local ignored. Preserve the existing package identity and local signing key for updates. Build independently with python3 build.py; new contributors get a different local signature.

Code/build tasks do not authorize TV access, installing apps, changing HOME, disabling system packages or restarting devices. Those operations require an explicit device task. Inspect an exact recovery baseline before a requested HOME change. No automatic debloat or boot-ad removal scripts belong in this project.
