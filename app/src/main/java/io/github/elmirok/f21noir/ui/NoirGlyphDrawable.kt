package io.github.elmirok.f21noir.ui

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import io.github.elmirok.f21noir.model.AppEntry
import java.util.Locale

enum class NoirGlyph {
    PHONE, MESSAGE, CONTACT, CAMERA, FOLDER, SETTINGS,
    BROWSER, CALENDAR, CLOCK, IMAGE, CALCULATOR, MUSIC, GENERIC,
}

/** Original, dependency-free outline icon set drawn for the Noir Minimal UI. */
class NoirGlyphDrawable(private val glyph: NoirGlyph) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = NoirUi.WARM_WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    override fun draw(canvas: Canvas) {
        val scale = minOf(bounds.width(), bounds.height()) / 48f
        val left = bounds.left + (bounds.width() - 48f * scale) / 2f
        val top = bounds.top + (bounds.height() - 48f * scale) / 2f
        fun x(value: Float) = left + value * scale
        fun y(value: Float) = top + value * scale
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
            canvas.drawLine(x(x1), y(y1), x(x2), y(y2), paint)
        fun rect(l: Float, t: Float, r: Float, b: Float, radius: Float = 2f) =
            canvas.drawRoundRect(RectF(x(l), y(t), x(r), y(b)), radius * scale, radius * scale, paint)
        fun circle(cx: Float, cy: Float, radius: Float) = canvas.drawCircle(x(cx), y(cy), radius * scale, paint)

        paint.strokeWidth = 2.35f * scale
        when (glyph) {
            NoirGlyph.PHONE -> {
                val path = Path().apply {
                    moveTo(x(14f), y(8f)); cubicTo(x(10f), y(9f), x(8f), y(13f), x(9f), y(17f))
                    cubicTo(x(12f), y(29f), x(20f), y(37f), x(32f), y(40f))
                    cubicTo(x(36f), y(41f), x(40f), y(38f), x(41f), y(34f))
                    lineTo(x(33f), y(28f)); lineTo(x(28f), y(33f))
                    cubicTo(x(21f), y(30f), x(18f), y(27f), x(15f), y(20f))
                    lineTo(x(20f), y(15f)); close()
                }
                canvas.drawPath(path, paint)
            }
            NoirGlyph.MESSAGE -> {
                rect(7f, 9f, 41f, 35f, 8f)
                val tail = Path().apply { moveTo(x(15f), y(34f)); lineTo(x(11f), y(41f)); lineTo(x(22f), y(35f)) }
                canvas.drawPath(tail, paint)
                line(16f, 18f, 33f, 18f); line(16f, 25f, 29f, 25f)
            }
            NoirGlyph.CONTACT -> {
                circle(24f, 15f, 7f)
                val shoulders = Path().apply {
                    moveTo(x(9f), y(40f)); cubicTo(x(10f), y(30f), x(15f), y(26f), x(24f), y(26f))
                    cubicTo(x(33f), y(26f), x(38f), y(30f), x(39f), y(40f)); close()
                }
                canvas.drawPath(shoulders, paint)
            }
            NoirGlyph.CAMERA -> {
                rect(6f, 14f, 42f, 39f, 3f); circle(24f, 27f, 8f)
                val topPath = Path().apply { moveTo(x(14f), y(14f)); lineTo(x(18f), y(9f)); lineTo(x(30f), y(9f)); lineTo(x(34f), y(14f)) }
                canvas.drawPath(topPath, paint)
            }
            NoirGlyph.FOLDER -> {
                val path = Path().apply {
                    moveTo(x(6f), y(12f)); lineTo(x(19f), y(12f)); lineTo(x(23f), y(16f))
                    lineTo(x(42f), y(16f)); lineTo(x(42f), y(38f)); lineTo(x(6f), y(38f)); close()
                }
                canvas.drawPath(path, paint)
            }
            NoirGlyph.SETTINGS -> {
                circle(24f, 24f, 7f); circle(24f, 24f, 15f)
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    line(
                        24f + (15f * kotlin.math.cos(angle)).toFloat(),
                        24f + (15f * kotlin.math.sin(angle)).toFloat(),
                        24f + (20f * kotlin.math.cos(angle)).toFloat(),
                        24f + (20f * kotlin.math.sin(angle)).toFloat(),
                    )
                }
            }
            NoirGlyph.BROWSER -> {
                circle(24f, 24f, 18f); line(6f, 24f, 42f, 24f)
                canvas.drawOval(RectF(x(15f), y(6f), x(33f), y(42f)), paint)
            }
            NoirGlyph.CALENDAR -> {
                rect(7f, 10f, 41f, 41f, 3f); line(7f, 19f, 41f, 19f)
                line(16f, 6f, 16f, 14f); line(32f, 6f, 32f, 14f)
                line(15f, 27f, 19f, 27f); line(27f, 27f, 31f, 27f); line(15f, 34f, 19f, 34f)
            }
            NoirGlyph.CLOCK -> {
                circle(24f, 24f, 18f); line(24f, 13f, 24f, 25f); line(24f, 25f, 32f, 30f)
            }
            NoirGlyph.IMAGE -> {
                rect(6f, 8f, 42f, 40f, 3f); circle(33f, 17f, 3f)
                val mountains = Path().apply { moveTo(x(9f), y(36f)); lineTo(x(19f), y(25f)); lineTo(x(25f), y(31f)); lineTo(x(30f), y(26f)); lineTo(x(39f), y(36f)) }
                canvas.drawPath(mountains, paint)
            }
            NoirGlyph.CALCULATOR -> {
                rect(9f, 5f, 39f, 43f, 3f); rect(14f, 10f, 34f, 18f, 1f)
                for (row in 0..1) for (column in 0..2) circle(16f + column * 8f, 27f + row * 8f, 1.2f)
            }
            NoirGlyph.MUSIC -> {
                line(20f, 11f, 20f, 35f); line(20f, 11f, 36f, 8f); line(36f, 8f, 36f, 31f)
                circle(14f, 36f, 6f); circle(30f, 32f, 6f)
            }
            NoirGlyph.GENERIC -> {
                rect(7f, 7f, 21f, 21f, 3f); rect(27f, 7f, 41f, 21f, 3f)
                rect(7f, 27f, 21f, 41f, 3f); rect(27f, 27f, 41f, 41f, 3f)
            }
        }
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
    @Deprecated("Deprecated in Android")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

object NoirGlyphs {
    fun forFavorite(slot: Int) = NoirGlyphDrawable(
        listOf(NoirGlyph.PHONE, NoirGlyph.MESSAGE, NoirGlyph.CONTACT, NoirGlyph.CAMERA, NoirGlyph.FOLDER, NoirGlyph.SETTINGS)[slot],
    )

    fun forApp(entry: AppEntry): NoirGlyphDrawable {
        val value = (entry.label + " " + entry.component.flattenToShortString()).lowercase(Locale.ROOT)
        val glyph = when {
            listOf("phone", "telefone", "dial", "call history").any(value::contains) -> NoirGlyph.PHONE
            listOf("message", "mensag", "sms", "mms").any(value::contains) -> NoirGlyph.MESSAGE
            listOf("contact", "contato", "people").any(value::contains) -> NoirGlyph.CONTACT
            listOf("camera", "câmera").any(value::contains) -> NoirGlyph.CAMERA
            listOf("file", "arquivo", "document").any(value::contains) -> NoirGlyph.FOLDER
            listOf("setting", "config", "ajuste").any(value::contains) -> NoirGlyph.SETTINGS
            listOf("music", "música", "audio", "radio").any(value::contains) -> NoirGlyph.MUSIC
            listOf("browser", "chrome", "web").any(value::contains) -> NoirGlyph.BROWSER
            listOf("calendar", "calend").any(value::contains) -> NoirGlyph.CALENDAR
            listOf("clock", "relógio", "alarme").any(value::contains) -> NoirGlyph.CLOCK
            listOf("gallery", "galeria", "photo", "image").any(value::contains) -> NoirGlyph.IMAGE
            listOf("calculator", "calculadora").any(value::contains) -> NoirGlyph.CALCULATOR
            else -> NoirGlyph.GENERIC
        }
        return NoirGlyphDrawable(glyph)
    }
}
