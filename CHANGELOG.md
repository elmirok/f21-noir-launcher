# Changelog

## 0.1.0-alpha.2 — unreleased

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
