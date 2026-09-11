package io.github.elmirok.f21noir

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import io.github.elmirok.f21noir.data.LauncherRepository
import io.github.elmirok.f21noir.ui.NoirUi
import io.github.elmirok.f21noir.ui.NoirUi.dp
import java.util.Locale

class SettingsActivity : Activity() {
    private lateinit var repository: LauncherRepository
    private var page = Page.MAIN
    private var hasRendered = false

    private enum class Page { MAIN, FAVORITES, SHORTCUTS, HIDDEN, APPEARANCE, ABOUT }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = LauncherRepository(this)
        window.statusBarColor = NoirUi.BLACK
        window.navigationBarColor = NoirUi.BLACK
        page = savedInstanceState?.getString(KEY_PAGE)?.let { runCatching { Page.valueOf(it) }.getOrNull() } ?: Page.MAIN
        render()
    }

    override fun onResume() {
        super.onResume()
        if (hasRendered) render() else hasRendered = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_PAGE, page.name)
        super.onSaveInstanceState(outState)
    }

    private fun render() {
        when (page) {
            Page.MAIN -> renderMain()
            Page.FAVORITES -> renderFavorites()
            Page.SHORTCUTS -> renderShortcuts()
            Page.HIDDEN -> renderHidden()
            Page.APPEARANCE -> renderAppearance()
            Page.ABOUT -> renderAbout()
        }
    }

    private fun renderMain() {
        val items = listOf(
            "Favoritos" to Page.FAVORITES,
            "Atalhos numéricos" to Page.SHORTCUTS,
            "Apps ocultos" to Page.HIDDEN,
            "Aparência" to Page.APPEARANCE,
        )
        val body = verticalBody("F21 NOIR")
        items.forEach { (label, destination) -> body.addView(row(label) { page = destination; render() }) }
        body.addView(row("Configurações do Android", "SISTEMA") { openAndroidSettings() })
        body.addView(row("Launcher padrão") { openHomeSettings() })
        body.addView(row("Sobre e privacidade") { page = Page.ABOUT; render() })
        body.addView(NoirUi.footer(this, "MENU LONGO PARA ABRIR"))
        show(body)
    }

    private fun renderFavorites() {
        val body = verticalBody("FAVORITOS")
        repeat(6) { slot ->
            val number = "%02d".format(Locale.US, slot + 1)
            body.addView(row("$number   ${repository.favoriteLabel(slot)}") {
                startActivity(Intent(this, AppPickerActivity::class.java).putExtra(AppPickerActivity.EXTRA_FAVORITE_SLOT, slot))
            })
        }
        body.addView(NoirUi.footer(this, "CENTRO: ALTERAR   •   VOLTAR: SALVAR"))
        show(body)
    }

    private fun renderShortcuts() {
        val body = verticalBody("ATALHOS NUMÉRICOS")
        body.addView(NoirUi.footer(this, "SEGURE UM NÚMERO NA HOME"))
        val order = (1..9).toList() + 0
        order.forEach { digit ->
            val label = repository.shortcut(digit)?.label ?: "Não definido"
            body.addView(row("$digit   $label") {
                startActivity(Intent(this, AppPickerActivity::class.java).putExtra(AppPickerActivity.EXTRA_SHORTCUT_DIGIT, digit))
            })
        }
        body.addView(NoirUi.footer(this, "CENTRO: ALTERAR"))
        show(body)
    }

    private fun renderHidden() {
        val body = verticalBody("APPS OCULTOS")
        val hidden = repository.hiddenComponents()
        repository.allApps(includeHidden = true).forEach { app ->
            val isHidden = app.component.flattenToString() in hidden
            body.addView(row(app.label, if (isHidden) "✓" else "□") {
                repository.setHidden(app.component, !isHidden)
                renderHidden()
            })
        }
        body.addView(NoirUi.footer(this, "CENTRO: MOSTRAR / OCULTAR"))
        show(body)
    }

    private fun renderAppearance() {
        val body = verticalBody("APARÊNCIA")
        body.addView(row("Formato da data", prettyDatePattern()) {
            val next = when (repository.datePattern()) {
                "EEE, dd MMM" -> "dd/MM/yyyy"
                "dd/MM/yyyy" -> "EEE, MMM dd"
                else -> "EEE, dd MMM"
            }
            repository.setDatePattern(next)
            renderAppearance()
        })
        val levels = listOf("Baixo", "Médio", "Alto")
        body.addView(row("Brilho do foco", levels[repository.focusLevel()]) {
            repository.setFocusLevel((repository.focusLevel() + 1) % levels.size)
            renderAppearance()
        })
        body.addView(row("Animações", if (repository.animationsEnabled()) "Rápidas" else "Desligadas") {
            repository.setAnimationsEnabled(!repository.animationsEnabled())
            renderAppearance()
        })
        body.addView(row("Wallpaper Noir", "APLICAR") { applyNoirLockWallpaper() })
        body.addView(row("Wallpaper da ROM", "RESTAURAR") { restoreRomLockWallpaper() })
        body.addView(row("Escolher launcher padrão") { openHomeSettings() })
        body.addView(row("Voltar ao Launcher3") { openHomeSettings() })
        body.addView(row("Sobre e privacidade") { page = Page.ABOUT; render() })
        body.addView(NoirUi.footer(this, "SEM INTERNET  •  SEM TELEMETRIA"))
        show(body)
    }

    private fun renderAbout() {
        val body = verticalBody("SOBRE E PRIVACIDADE")
        body.addView(NoirUi.text(this, "F21 Noir\n\nLauncher independente para o Qin F21 Pro.\n\nSem Internet. Sem telemetria. Sem anúncios. Sem serviços Google.\n\nO Launcher3 original permanece instalado e pode ser restaurado em Launcher padrão.\n\nApache License 2.0", 18f, NoirUi.WARM_WHITE, Gravity.START).apply {
            setPadding(dp(14), dp(22), dp(14), dp(22))
        })
        body.addView(row("Abrir launcher padrão") { openHomeSettings() })
        show(body)
    }

    private fun prettyDatePattern(): String = when (repository.datePattern()) {
        "dd/MM/yyyy" -> "11/09/2026"
        "EEE, MMM dd" -> "QUI, SET 11"
        else -> "QUI, 11 SET"
    }

    private fun openHomeSettings() {
        try {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        } catch (_: Exception) {
            try {
                startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
            } catch (_: Exception) {
                Toast.makeText(this, "Abra Configurações › Apps padrão › Início", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openAndroidSettings() {
        try {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        } catch (_: Exception) {
            Toast.makeText(this, "Configurações do Android indisponíveis", Toast.LENGTH_LONG).show()
        }
    }

    private fun applyNoirLockWallpaper() {
        try {
            WallpaperManager.getInstance(this).setResource(
                R.raw.f21_noir_amber_rain,
                WallpaperManager.FLAG_LOCK,
            )
            Toast.makeText(this, "Wallpaper Noir aplicado à tela bloqueada", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Não foi possível aplicar o wallpaper", Toast.LENGTH_LONG).show()
        }
    }

    private fun restoreRomLockWallpaper() {
        try {
            WallpaperManager.getInstance(this).clear(WallpaperManager.FLAG_LOCK)
            Toast.makeText(this, "Wallpaper da ROM restaurado", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Não foi possível restaurar o wallpaper", Toast.LENGTH_LONG).show()
        }
    }

    private fun verticalBody(title: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(NoirUi.BLACK)
        addView(NoirUi.header(this@SettingsActivity, title))
    }

    private fun row(label: String, trailing: String = "›", action: () -> Unit): LinearLayout =
        NoirUi.listRow(this, label, trailing).apply { setOnClickListener { action() } }

    private fun show(body: LinearLayout) {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(NoirUi.BLACK)
            addView(body, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        setContentView(scroll)
        body.post {
            (0 until body.childCount).asSequence().map { body.getChildAt(it) }.firstOrNull { it.isFocusable }?.requestFocus()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU && page == Page.MAIN) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (page == Page.MAIN) finish() else { page = Page.MAIN; render() }
    }

    companion object { private const val KEY_PAGE = "page" }
}
