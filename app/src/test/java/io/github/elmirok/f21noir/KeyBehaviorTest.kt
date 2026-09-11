package io.github.elmirok.f21noir

import android.view.KeyEvent
import io.github.elmirok.f21noir.model.KeyBehavior
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyBehaviorTest {
    @Test fun mapsEveryDigit() {
        val keyCodes = listOf(
            KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3,
            KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7,
            KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_9,
        )
        keyCodes.forEachIndexed { digit, keyCode -> assertEquals(digit, KeyBehavior.digitForKeyCode(keyCode)) }
    }

    @Test fun mapsStarAndPound() {
        assertEquals("*", KeyBehavior.dialCharacter(KeyEvent.KEYCODE_STAR))
        assertEquals("#", KeyBehavior.dialCharacter(KeyEvent.KEYCODE_POUND))
    }

    @Test fun refusesNonDialKeys() {
        assertNull(KeyBehavior.dialCharacter(KeyEvent.KEYCODE_CALL))
        assertNull(KeyBehavior.digitForKeyCode(KeyEvent.KEYCODE_DPAD_CENTER))
    }
}
