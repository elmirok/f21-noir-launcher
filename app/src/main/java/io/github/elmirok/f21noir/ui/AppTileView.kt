package io.github.elmirok.f21noir.ui

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.github.elmirok.f21noir.R
import io.github.elmirok.f21noir.ui.NoirUi.dp

class AppTileView(context: Context) : LinearLayout(context) {
    private val icon = ImageView(context)
    private val label = NoirUi.text(context, "", 13f, NoirUi.WARM_WHITE, Gravity.CENTER)
    private var animationsEnabled = true
    private var focusLevel = 1

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        isFocusable = true
        // The F21 has both a touchscreen and a hardware D-pad. Keeping tiles focusable while
        // Android is in touch mode prevents the first D-pad press after a tap from being spent
        // only on restoring focus.
        isFocusableInTouchMode = true
        isClickable = true
        background = context.getDrawable(R.drawable.focus_frame)
        setPadding(context.dp(5), context.dp(7), context.dp(5), context.dp(5))
        addView(icon, LayoutParams(context.dp(48), context.dp(48)))
        addView(label, LayoutParams(LayoutParams.MATCH_PARENT, context.dp(28)).apply {
            topMargin = context.dp(4)
        })
        setOnFocusChangeListener { view, focused ->
            val tint = if (focused) NoirUi.AMBER else NoirUi.WARM_WHITE
            icon.setColorFilter(tint, PorterDuff.Mode.SRC_IN)
            label.setTextColor(tint)
            background.alpha = if (focused) listOf(150, 210, 255)[focusLevel] else 255
            view.animate().cancel()
            if (animationsEnabled) {
                view.animate().scaleX(if (focused) 1.025f else 1f).scaleY(if (focused) 1.025f else 1f).setDuration(120).start()
            } else {
                view.scaleX = 1f
                view.scaleY = 1f
            }
        }
    }

    fun configure(animations: Boolean, focusBrightness: Int) {
        animationsEnabled = animations
        focusLevel = focusBrightness.coerceIn(0, 2)
    }

    fun bind(text: String, drawable: Drawable?) {
        label.text = text
        icon.setImageDrawable(drawable)
        icon.setColorFilter(if (isFocused) NoirUi.AMBER else NoirUi.WARM_WHITE, PorterDuff.Mode.SRC_IN)
        contentDescription = text
    }

    fun labelView(): TextView = label
}
