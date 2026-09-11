# Physical keys

| Key | Home action |
|---|---|
| D-pad | Move focus |
| Center | Open focused item |
| Menu | Open all apps |
| Long Menu | Open F21 Noir settings |
| Call | Open dialer |
| Short 0–9, `*`, `#` | Open dialer with character |
| Long 0–9 | Open configured shortcut |
| Back | Return to home |
| Tap the clock | Open F21 Noir settings (touch fallback) |

## F21 Pro hardware observed

Read-only inspection on a physical `k61v1_64_bsp` running Android 11 identified:

| Linux input | Android mapping |
|---|---|
| `KEY_0`–`KEY_9` | `KEYCODE_0`–`KEYCODE_9` |
| `KEY_UP/DOWN/LEFT/RIGHT` | D-pad navigation |
| `KEY_OK` | `KEYCODE_DPAD_CENTER` |
| `KEY_SEND` | `KEYCODE_CALL` |
| `KEY_NUMERIC_POUND` | `KEYCODE_POUND` |
| Dedicated GPIO Menu key | `KEYCODE_MENU` |

## Touch gestures

- Swipe up on Home: open all apps.
- Swipe down in all apps: return Home.
- Swipe left or right in all apps: change page.

Home navigation is handled directly by the launcher and preserves its last position after touch,
so the first D-pad press moves immediately. It wraps at every grid edge, making each directional
press produce a visible move while a screen tap continues to open a tile on the first touch.

The keypad also reports the star position as `KEY_SWITCHVIDEOMODE` at the Linux
input layer. Its final Android keycode, and long-press behavior for Menu and
digits, must still be confirmed by pressing the physical keys before beta.
