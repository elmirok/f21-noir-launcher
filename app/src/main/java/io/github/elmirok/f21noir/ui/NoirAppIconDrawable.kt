package io.github.elmirok.f21noir.ui

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import io.github.elmirok.f21noir.model.AppEntry

/** A drawable whose colour follows the Noir focus state without losing its internal detail. */
interface NoirTintableDrawable {
    fun setNoirTint(color: Int)
}

/**
 * Renders an application's own icon as a high-contrast monochrome mark.
 *
 * Keeping luminance, rather than replacing every opaque pixel with one colour, preserves logos,
 * cut-outs and lettering in both legacy and adaptive icons. The resulting greys are multiplied by
 * warm white normally and amber while focused so third-party apps remain recognisable and still
 * belong to the Noir palette.
 */
class NoirAppIconDrawable(source: Drawable, renderSize: Int = 128) : Drawable(), NoirTintableDrawable {
    private val bitmap: Bitmap = renderMonochrome(source, renderSize)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val sourceRect = Rect(0, 0, bitmap.width, bitmap.height)
    private var drawableAlpha = 255

    init {
        setNoirTint(NoirUi.WARM_WHITE)
    }

    override fun draw(canvas: Canvas) {
        if (bounds.isEmpty) return
        val inset = minOf(bounds.width(), bounds.height()) * 0.04f
        val destination = RectF(
            bounds.left + inset,
            bounds.top + inset,
            bounds.right - inset,
            bounds.bottom - inset,
        )
        canvas.drawBitmap(bitmap, sourceRect, destination, paint)
    }

    override fun setNoirTint(color: Int) {
        val red = Color.red(color) / 255f
        val green = Color.green(color) / 255f
        val blue = Color.blue(color) / 255f
        paint.colorFilter = ColorMatrixColorFilter(
            ColorMatrix(
                floatArrayOf(
                    red, 0f, 0f, 0f, 0f,
                    0f, green, 0f, 0f, 0f,
                    0f, 0f, blue, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            ),
        )
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        drawableAlpha = alpha.coerceIn(0, 255)
        paint.alpha = drawableAlpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Android")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = 48
    override fun getIntrinsicHeight(): Int = 48

    companion object {
        private fun renderMonochrome(source: Drawable, size: Int): Bitmap {
            val safeSize = size.coerceAtLeast(48)
            val rendered = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
            val drawable = source.constantState?.newDrawable()?.mutate() ?: source.mutate()
            val oldBounds = Rect(drawable.bounds)
            drawable.bounds = Rect(0, 0, safeSize, safeSize)
            drawable.draw(Canvas(rendered))
            drawable.bounds = oldBounds

            val pixels = IntArray(safeSize * safeSize)
            rendered.getPixels(pixels, 0, safeSize, 0, 0, safeSize, safeSize)
            for (index in pixels.indices) {
                val pixel = pixels[index]
                val alpha = Color.alpha(pixel)
                if (alpha == 0) continue
                val luminance = (
                    Color.red(pixel) * 0.2126f +
                        Color.green(pixel) * 0.7152f +
                        Color.blue(pixel) * 0.0722f
                    ).toInt()
                // Slightly stronger contrast keeps small 48 dp icons crisp on the pure-black UI.
                val contrasted = ((luminance - 128) * 1.18f + 142).toInt().coerceIn(18, 255)
                pixels[index] = Color.argb(alpha, contrasted, contrasted, contrasted)
            }
            rendered.setPixels(pixels, 0, safeSize, 0, 0, safeSize, safeSize)
            return rendered
        }
    }
}

object NoirAppIcons {
    fun forApp(packageManager: PackageManager, entry: AppEntry): Drawable {
        return try {
            val info = packageManager.getActivityInfo(entry.component, 0)
            NoirAppIconDrawable(info.loadIcon(packageManager))
        } catch (_: Exception) {
            NoirGlyphs.forApp(entry)
        }
    }
}
