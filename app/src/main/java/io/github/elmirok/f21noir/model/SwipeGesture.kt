package io.github.elmirok.f21noir.model

import kotlin.math.abs

object SwipeGesture {
    enum class Direction { UP, DOWN, LEFT, RIGHT }

    fun detect(
        deltaX: Float,
        deltaY: Float,
        durationMs: Long,
        minimumDistancePx: Float,
    ): Direction? {
        if (durationMs !in 1..MAX_DURATION_MS) return null
        val horizontal = abs(deltaX)
        val vertical = abs(deltaY)
        if (maxOf(horizontal, vertical) < minimumDistancePx) return null

        return if (vertical > horizontal * AXIS_DOMINANCE) {
            if (deltaY < 0) Direction.UP else Direction.DOWN
        } else if (horizontal > vertical * AXIS_DOMINANCE) {
            if (deltaX < 0) Direction.LEFT else Direction.RIGHT
        } else {
            null
        }
    }

    private const val MAX_DURATION_MS = 900L
    private const val AXIS_DOMINANCE = 1.2f
}
