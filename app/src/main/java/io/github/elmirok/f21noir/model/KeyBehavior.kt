package io.github.elmirok.f21noir.model

import android.view.KeyEvent

object KeyBehavior {
    fun digitForKeyCode(keyCode: Int): Int? = when (keyCode) {
        KeyEvent.KEYCODE_0 -> 0
        KeyEvent.KEYCODE_1 -> 1
        KeyEvent.KEYCODE_2 -> 2
        KeyEvent.KEYCODE_3 -> 3
        KeyEvent.KEYCODE_4 -> 4
        KeyEvent.KEYCODE_5 -> 5
        KeyEvent.KEYCODE_6 -> 6
        KeyEvent.KEYCODE_7 -> 7
        KeyEvent.KEYCODE_8 -> 8
        KeyEvent.KEYCODE_9 -> 9
        else -> null
    }

    fun dialCharacter(keyCode: Int): String? = when (keyCode) {
        KeyEvent.KEYCODE_STAR -> "*"
        KeyEvent.KEYCODE_POUND -> "#"
        else -> digitForKeyCode(keyCode)?.toString()
    }
}
