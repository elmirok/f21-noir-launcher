package io.github.elmirok.f21noir

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import io.github.elmirok.f21noir.data.LauncherRepository
import io.github.elmirok.f21noir.ui.NoirUi

class AppPickerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = NoirUi.BLACK
        window.navigationBarColor = NoirUi.BLACK
        val repository = LauncherRepository(this)
        val favorite = intent.getIntExtra(EXTRA_FAVORITE_SLOT, -1)
        val shortcut = intent.getIntExtra(EXTRA_SHORTCUT_DIGIT, -1)
        if (favorite !in 0..5 && shortcut !in 0..9) {
            finish()
            return
        }

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(NoirUi.BLACK)
            addView(NoirUi.header(this@AppPickerActivity, "ESCOLHER APLICATIVO"))
            if (shortcut in 0..9) {
                addView(NoirUi.listRow(this@AppPickerActivity, "Não definido").apply {
                    setOnClickListener {
                        repository.clearShortcut(shortcut)
                        finish()
                    }
                })
            }
            repository.allApps(includeHidden = true).forEach { app ->
                addView(NoirUi.listRow(this@AppPickerActivity, app.label).apply {
                    setOnClickListener {
                        if (favorite in 0..5) repository.setFavorite(favorite, app.component)
                        if (shortcut in 0..9) repository.setShortcut(shortcut, app.component)
                        finish()
                    }
                })
            }
            addView(NoirUi.footer(this@AppPickerActivity, "CENTRO: ESCOLHER  •  VOLTAR: CANCELAR"))
        }
        setContentView(ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(NoirUi.BLACK)
            addView(body, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        })
        body.post {
            (0 until body.childCount).asSequence().map { body.getChildAt(it) }.firstOrNull { it.isFocusable }?.requestFocus()
        }
    }

    companion object {
        const val EXTRA_FAVORITE_SLOT = "favorite_slot"
        const val EXTRA_SHORTCUT_DIGIT = "shortcut_digit"
    }
}
