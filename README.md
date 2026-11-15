# AUREX
**Customize. Control. Experience.**

AUREX replaces the stock Android volume panel with a sleek, fully customizable one. Press the volume
buttons and a slim pill slides in from the edge of your screen. Drag it to set the volume, tap the speaker
to mute, or long-press for a full audio mixer. Make it yours with four styles (Classic, Glass, Aurex,
Minimal), eight built-in themes plus your own, and control over size, position, colors, animation and haptics.
The app itself is a dark, atmospheric Jetpack Compose interface with an interactive 3D volume orb.

## Features
- **Edge Volume Island:** vertical pill on the screen edge with spring animations, drag-to-set, mute and gestures
- **Edge pops:** small animated bars for Volume Up and Volume Down, configured separately
- **Audio mixer:** Media, Ring, Notification, Alarm and System volumes, live-synced
- **Customization studio:** live phone preview, colors, dimensions, position, animation, haptics, landscape options
- **Theme engine:** Midnight, Obsidian, Pure Black, Frosted Glass, Aurora, Material You, Monochrome, Silver Chrome, plus custom themes you can save, import and export
- **Built-in update checker:** notifies you when a new version is published on GitHub
- **Privacy-minded:** the accessibility service only detects volume-key presses; it never reads your screen. Internet is used only for update checks

Package: `com.umang.aurex` | Android 10+ | Kotlin, Jetpack Compose, MVVM, DataStore, AccessibilityService

## Build
1. Open the folder in Android Studio (Ladybug or newer, JDK 17). Let Gradle sync; it downloads Gradle 8.9.
2. Run the `app` configuration, or `Build > Build APK(s)`.
3. Release signing: put `keystore.properties` in the project root with `storeFile`, `storePassword`,
   `keyAlias`, `keyPassword`. If it is missing, the release build is simply unsigned.
   `keystore.properties` and `*.jks` are in `.gitignore`. Never commit them.
4. On the phone: open AUREX, tap **Enable accessibility service**, switch AUREX on. On Android 13+
   sideloaded apps first need App info > menu > **Allow restricted settings**.

> This source was written without access to an Android SDK, so it has not been compiled or run yet.
> Expect a few compile errors on first sync. Paste them back and they are quick to fix.

## How it works
- `AurexAccessibilityService` asks only for key events (`flagRequestFilterKeyEvents`). It sees
  Volume Up/Down, changes the **media** volume with `AudioManager`, and shows overlays. It consumes
  the key so the system panel does not also appear. During calls it leaves the keys to the system.
  It ignores all accessibility events and never reads screen content.
- Overlays use `TYPE_ACCESSIBILITY_OVERLAY`, so no separate "draw over apps" permission is needed.
- `IslandPill` is one composable used by the real overlay and by the live previews.
- Volume sync uses a `ContentObserver` on system settings (no polling). The only timer is a
  1.5 s "is music playing" check on the Home screen while it is visible.
- Settings are one JSON document in DataStore; backup/restore and theme import/export use the same JSON.

## Android limits (documented, not faked)
- **Power button:** Android does not let apps detect it. Not implemented. Edge pops cover Volume Up/Down only.
- **Volume keys** only work while the accessibility service is enabled.
- **Ring/notification volume** can be blocked by Do Not Disturb; grant DND access for those sliders.
- **Backdrop blur** for the Glass style is not available to overlay windows, so Glass is a translucent
  gradient, not a true blur.
- **Material You** theme reads wallpaper colors on Android 12+; older versions use a fallback palette.

## Not done yet / honest gaps
- Not compiled or tested on a device. No unit or UI tests.
- Landscape has its own on/off switches and side; width/height/animation are shared with portrait.
- Swipe up/down gestures are not implemented. Tap, double tap and long press on the speaker icon, drag,
  and tap-outside-to-dismiss (mixer) are. Single tap waits for the double-tap window (~300 ms).
- Backup/theme sharing uses the clipboard, not files.
- 3D is a Canvas-projected sphere and rings (no OpenGL, no RuntimeShader); Lottie is not used.
- App UI is dark only. Material 3 Expressive APIs are not used (still alpha).
- Edge pops use fixed positions above/below the island; no separate per-button position.
- Splash is the Android 12+ splash with a fade/scale exit; no custom Lottie splash.

## Updates (GitHub)
AUREX checks `version.json` in this repo each time it opens (can be switched off in Settings, Updates) and
has a **Check for updates** button in Settings. If `versionCode` in that file is higher than the installed
app, it shows an "Update available" popup with the changelog and an **Update** button that opens the APK link.
This is the only thing that uses the internet permission.

**Setup:** the update URLs in `update/UpdateChecker.kt` and `version.json` already point to
`github.com/Umangnamdeo/AUREX` (branch `main`). Change them if you rename the repo or branch.

**To publish a new version:**
1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Build the signed release APK; create a GitHub Release tagged `v<versionName>` and upload it as `aurex-<versionName>.apk`.
3. Update `version.json` (same `versionCode`/`versionName`, `apkUrl`, `changelog`) and push it to `main`.
Users see the popup after GitHub refreshes the raw file (up to about 5 minutes). An update installs over the old
app only if both APKs are signed with the same key, so keep your keystore safe.

## License and credit
Copyright 2026 Umangnamdeo. Licensed under the [Apache License 2.0](LICENSE).
If you redistribute or modify AUREX, you must keep the `LICENSE` and `NOTICE` files and the copyright notice,
and state your changes. The AUREX name and logo are not covered by this license. If you build on AUREX,
please credit **Umangnamdeo** (https://github.com/Umangnamdeo).
