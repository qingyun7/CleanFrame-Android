# Asset preparation

All current runtime photos, thumbnails, registered glass material images, fonts and icon bounds are bundled under `app/src/main/assets/cleanframe/assets/`. Building the APK does not need to regenerate them.

The Android runtime uses pre-rendered materials rather than live blur or pixel analysis. Keep wallpaper crops and material fields aligned when changing images. The upstream material and icon-metric tools and recipes are available in the [original source](https://github.com/nthnclrk/CleanFrame/blob/fa537d1892b71d2de2bf94c24cbff3812e9f70d5/docs/assets.md); their paths and outputs need adapting to this repository layout. Those generation tools are not part of this Android build.

Android service icons are transparent SVG paths mapped by package in `assets/android-marks.js`. Offline ink bounds are stored in `assets/icons/metrics.js`; update bounds when replacing artwork and inspect every affected theme and panel. Keep the [source register](asset-sources.md), adjacent licences and [third-party notices](../THIRD_PARTY_NOTICES.md) with redistributions.
