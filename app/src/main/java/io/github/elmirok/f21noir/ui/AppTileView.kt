package io.github.elmirok.f21noir.ui

import android.content.Context
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
    private var tileDrawable: Drawable? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        isFocusable = true
        isClickable = true
        background = context.getDrawable(R.drawable.focus_frame)
        setPadding(context.dp(5), context.dp(7), context.dp(5), context.dp(5))
        addView(icon, LayoutParams(context.dp(48), context.dp(48)))
        addView(label, LayoutParams(LayoutParams.MATCH_PARENT, context.dp(28)).apply {
            topMargin = context.dp(4)
        })
        setOnFocusChangeListener { view, focused ->
            val tint = if (focused) NoirUi.AMBER else NoirUi.WARM_WHITE
            applyTint(tint)
            label.setTextColor(tint)
            background.alpha = if (focused) listOf(150, 210, 255)[focusLevel] else 255
            view.animate().cancel()
            if (animationsEnabled) {
                view.animate().scaleX(if (focused) 1.025f else 1f).scaleY(if (focused) 1.025f else 1f).setDuration(70).start()
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
        tileDrawable = drawable
        icon.setImageDrawable(drawable)
        applyTint(if (isFocused) NoirUi.AMBER else NoirUi.WARM_WHITE)
        contentDescription = text
    }

    private fun applyTint(color: Int) {
        icon.clearColorFilter()
        val drawable = tileDrawable
        if (drawable is NoirTintableDrawable) {
            drawable.setNoirTint(color)
        } else {
            drawable?.setTint(color)
        }
        icon.invalidate()
    }

    fun labelView(): TextView = label
}
