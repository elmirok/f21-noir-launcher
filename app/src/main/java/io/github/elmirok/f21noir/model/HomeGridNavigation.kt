package io.github.elmirok.f21noir.model

import android.view.KeyEvent

object HomeGridNavigation {
    fun next(current: Int, keyCode: Int, columns: Int = 3, itemCount: Int = 6): Int? {
        if (current !in 0 until itemCount || columns <= 0 || itemCount <= 0) return null
        val rowStart = (current / columns) * columns
        val rowSize = minOf(columns, itemCount - rowStart)
        val column = current - rowStart

        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> rowStart + (column - 1 + rowSize) % rowSize
            KeyEvent.KEYCODE_DPAD_RIGHT -> rowStart + (column + 1) % rowSize
            KeyEvent.KEYCODE_DPAD_UP -> wrappedVertical(current, -columns, itemCount)
            KeyEvent.KEYCODE_DPAD_DOWN -> wrappedVertical(current, columns, itemCount)
            else -> null
        }
    }

    private fun wrappedVertical(current: Int, delta: Int, itemCount: Int): Int {
        var result = (current + delta) % itemCount
        if (result < 0) result += itemCount
        return result
    }
}
