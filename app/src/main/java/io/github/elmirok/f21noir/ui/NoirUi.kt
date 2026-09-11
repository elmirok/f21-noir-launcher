package io.github.elmirok.f21noir.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import io.github.elmirok.f21noir.R
import io.github.elmirok.f21noir.data.LauncherRepository

object NoirUi {
    val BLACK = Color.rgb(0, 0, 0)
    val AMBER = Color.rgb(255, 176, 0)
    val RED = Color.rgb(198, 40, 40)
    val WARM_WHITE = Color.rgb(232, 224, 210)
    val DIM = Color.rgb(120, 111, 99)

    fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    fun text(
        context: Context,
        value: String,
        sizeSp: Float,
        color: Int = WARM_WHITE,
        gravity: Int = Gravity.CENTER_VERTICAL,
    ) = TextView(context).apply {
        text = value
        textSize = sizeSp
        setTextColor(color)
        this.gravity = gravity
        typeface = context.resources.getFont(R.font.rajdhani_regular)
        includeFontPadding = false
    }

    fun screen(context: Context) = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(BLACK)
        setPadding(context.dp(12), context.dp(8), context.dp(12), context.dp(8))
        isFocusable = false
    }

    fun header(context: Context, value: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(text(context, value, 22f, WARM_WHITE, Gravity.CENTER).apply {
                letterSpacing = 0.24f
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.dp(52)))
            addView(accentDivider(context))
        }
    }

    fun accentDivider(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            addView(View(context).apply { setBackgroundColor(DIM) }, LinearLayout.LayoutParams(0, context.dp(1), 1f))
            addView(View(context).apply { setBackgroundColor(RED) }, LinearLayout.LayoutParams(context.dp(26), context.dp(3)).apply {
                setMargins(context.dp(10), 0, context.dp(10), 0)
            })
            addView(View(context).apply { setBackgroundColor(DIM) }, LinearLayout.LayoutParams(0, context.dp(1), 1f))
        }
    }

    fun footer(context: Context, value: String) = text(context, value, 11f, DIM, Gravity.CENTER).apply {
        letterSpacing = 0.2f
        setPadding(0, context.dp(8), 0, 0)
    }

    fun listRow(context: Context, label: String, trailing: String = "›"): LinearLayout {
        val animations = LauncherRepository(context).animationsEnabled()
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isFocusable = true
            isClickable = true
            background = context.getDrawable(R.drawable.list_focus)
            setPadding(context.dp(14), 0, context.dp(12), 0)
            addView(text(context, label, 20f), LinearLayout.LayoutParams(0, context.dp(58), 1f))
            addView(text(context, trailing, 26f, DIM, Gravity.CENTER), LinearLayout.LayoutParams(context.dp(28), context.dp(58)))
            setOnFocusChangeListener { view, focused ->
                if (focused) {
                    (getChildAt(0) as TextView).setTextColor(AMBER)
                    if (animations) view.animate().scaleX(1.01f).scaleY(1.01f).setDuration(120).start()
                } else {
                    (getChildAt(0) as TextView).setTextColor(WARM_WHITE)
                    view.animate().cancel()
                    view.scaleX = 1f
                    view.scaleY = 1f
                }
            }
        }
    }
}
