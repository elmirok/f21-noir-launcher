package io.github.elmirok.f21noir

import io.github.elmirok.f21noir.model.SwipeGesture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SwipeGestureTest {
    @Test fun detectsCardinalSwipes() {
        assertEquals(SwipeGesture.Direction.UP, SwipeGesture.detect(2f, -100f, 250, 48f))
        assertEquals(SwipeGesture.Direction.DOWN, SwipeGesture.detect(0f, 100f, 250, 48f))
        assertEquals(SwipeGesture.Direction.LEFT, SwipeGesture.detect(-100f, 3f, 250, 48f))
        assertEquals(SwipeGesture.Direction.RIGHT, SwipeGesture.detect(100f, 0f, 250, 48f))
    }

    @Test fun rejectsTapDiagonalAndSlowMovement() {
        assertNull(SwipeGesture.detect(2f, -20f, 100, 48f))
        assertNull(SwipeGesture.detect(80f, -80f, 200, 48f))
        assertNull(SwipeGesture.detect(0f, -100f, 1_100, 48f))
    }
}
