package io.github.elmirok.f21noir

import android.view.KeyEvent
import io.github.elmirok.f21noir.model.HomeGridNavigation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeGridNavigationTest {
    @Test fun movesEveryDirectionDeterministically() {
        assertEquals(3, HomeGridNavigation.next(0, KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals(0, HomeGridNavigation.next(3, KeyEvent.KEYCODE_DPAD_UP))
        assertEquals(1, HomeGridNavigation.next(0, KeyEvent.KEYCODE_DPAD_RIGHT))
        assertEquals(0, HomeGridNavigation.next(1, KeyEvent.KEYCODE_DPAD_LEFT))
    }

    @Test fun wrapsAtEveryHomeEdge() {
        assertEquals(2, HomeGridNavigation.next(0, KeyEvent.KEYCODE_DPAD_LEFT))
        assertEquals(0, HomeGridNavigation.next(2, KeyEvent.KEYCODE_DPAD_RIGHT))
        assertEquals(3, HomeGridNavigation.next(0, KeyEvent.KEYCODE_DPAD_UP))
        assertEquals(0, HomeGridNavigation.next(3, KeyEvent.KEYCODE_DPAD_DOWN))
    }

    @Test fun rejectsNonDirectionalAndInvalidInput() {
        assertNull(HomeGridNavigation.next(0, KeyEvent.KEYCODE_MENU))
        assertNull(HomeGridNavigation.next(-1, KeyEvent.KEYCODE_DPAD_RIGHT))
    }
}
