# Changelog

## 0.1.0-alpha.6 — 2026-09-11

- Replaced the generic unknown-app glyph with a local high-contrast monochrome conversion of each
  app's original icon, preserving recognisable details without adding an icon pack or Internet use.
- Added focus-aware amber rendering while retaining the icon's internal luminance and transparency.
- Kept the six default Home favorites on their purpose-built Noir outline glyphs; customized
  favorites now use the same recognisable themed icons as the app drawer.
- Physically verified Cello, IronFox, and Translate You on the F21 Pro; measured 58.7 MB total PSS.

## 0.1.0-alpha.5 — unreleased

- Added the original `Amber Rain` 480×640 Noir lock-screen wallpaper.
- Added one-tap lock-screen apply and reversible ROM-wallpaper restore actions under Appearance.
- Removed touch-mode focus capture now that Home D-pad movement is deterministic, restoring true one-tap tile opening.

## 0.1.0-alpha.4 — unreleased

- Renamed the sixth default Home tile to `Ajustes Noir` and routed it consistently to the Noir settings UI.
- Added an explicit `Configurações do Android` row inside Noir settings for intentional access to the vendor Settings app.

## 0.1.0-alpha.3 — unreleased

- Replaced Android's variable Home focus search with deterministic wrap-around D-pad navigation.
- Reduced the focus transition from 120 ms to 70 ms and handled Center/Enter explicitly.

## 0.1.0-alpha.2 — 2026-09-11

- Added swipe up for all apps, swipe down for Home, and horizontal app-page gestures.
- Kept app tiles focusable in touch mode so D-pad navigation responds on the first press after a tap.
- Made the dedicated Menu key open all apps on key-down instead of waiting for key-up.
- Added unit coverage for gesture thresholds, direction, diagonals, and slow movement.

## 0.1.0-alpha.1 — unreleased

- Initial Noir Minimal home and 3×3 app drawer.
- Physical-key navigation, dial keys, and configurable long-press shortcuts.
- Editable favorites, reversible hidden apps, appearance, privacy, and rollback screens.
- Verified the Android 11, 480×640, 208 dpi baseline on a physical `k61v1_64_bsp`.
- Avoided a duplicate first-frame render; optimized device-test starts measured 429–517 ms and about 36 MB PSS on the F21 Pro.
- Replaced tinted adaptive app blocks with an original dependency-free outline icon set matching the approved Noir Minimal concept.
- Fixed D-pad focus on the tappable clock, long-header clipping, settings value columns, and Music icon classification.
- Completed an on-device functional pass for all six favorites, app drawer paging, settings pages, dial keys, picker cancellation, hidden-app restoration, and launcher rollback access.
- Final optimized starts measured 325–347 ms and about 25 MB PSS on the physical test device.
