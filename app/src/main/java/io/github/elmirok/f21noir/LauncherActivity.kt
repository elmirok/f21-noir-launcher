package io.github.elmirok.f21noir

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import io.github.elmirok.f21noir.data.LauncherRepository
import io.github.elmirok.f21noir.model.AppEntry
import io.github.elmirok.f21noir.model.KeyBehavior
import io.github.elmirok.f21noir.ui.AppTileView
import io.github.elmirok.f21noir.ui.NoirUi
import io.github.elmirok.f21noir.ui.NoirUi.dp
import java.util.Locale

class LauncherActivity : Activity() {
    private lateinit var repository: LauncherRepository
    private var mode = Mode.HOME
    private var apps: List<AppEntry> = emptyList()
    private var page = 0
    private var longPressConsumed = false
    private var receiverRegistered = false

    private enum class Mode { HOME, APPS }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (mode == Mode.APPS) showApps(page)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = LauncherRepository(this)
        window.statusBarColor = NoirUi.BLACK
        window.navigationBarColor = NoirUi.BLACK
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0
        showHome()
    }

    override fun onResume() {
        super.onResume()
        if (mode == Mode.HOME) showHome() else showApps(page)
        registerPackageReceiver()
    }

    override fun onPause() {
        if (receiverRegistered) {
            unregisterReceiver(packageReceiver)
            receiverRegistered = false
        }
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        showHome()
    }

    private fun registerPackageReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, filter)
        receiverRegistered = true
    }

    private fun showHome() {
        mode = Mode.HOME
        val root = NoirUi.screen(this)
        val clockBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(TextClock(this@LauncherActivity).apply {
                format12Hour = "HH:mm"
                format24Hour = "HH:mm"
                textSize = 70f
                setTextColor(NoirUi.WARM_WHITE)
                gravity = Gravity.CENTER
                typeface = resources.getFont(R.font.rajdhani_regular)
                includeFontPadding = false
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(TextClock(this@LauncherActivity).apply {
                format12Hour = repository.datePattern()
                format24Hour = repository.datePattern()
                textSize = 18f
                setTextColor(NoirUi.WARM_WHITE)
                gravity = Gravity.CENTER
                isAllCaps = true
                letterSpacing = 0.18f
                typeface = resources.getFont(R.font.rajdhani_regular)
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(42)))
        }
        root.addView(clockBlock, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.82f))
        root.addView(NoirUi.accentDivider(this), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(4)))

        val grid = GridLayout(this).apply {
            columnCount = 3
            rowCount = 2
            useDefaultMargins = false
        }
        repeat(6) { slot ->
            val tile = AppTileView(this)
            tile.configure(repository.animationsEnabled(), repository.focusLevel())
            val entry = repository.favorite(slot)
            val icon = entry?.let { loadIcon(it) } ?: getDrawable(R.drawable.ic_launcher)
            tile.bind(entry?.label ?: repository.defaults[slot].label, icon)
            tile.setOnClickListener { launchIntent(repository.favoriteLaunchIntent(slot)) }
            tile.setOnLongClickListener {
                startActivity(Intent(this, AppPickerActivity::class.java).putExtra(AppPickerActivity.EXTRA_FAVORITE_SLOT, slot))
                true
            }
            grid.addView(tile, GridLayout.LayoutParams(
                GridLayout.spec(slot / 3, 1f),
                GridLayout.spec(slot % 3, 1f),
            ).apply {
                width = 0
                height = 0
                setGravity(Gravity.FILL)
                setMargins(dp(2), dp(2), dp(2), dp(2))
            })
        }
        root.addView(grid, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.18f))
        setContentView(root)
        grid.post { grid.getChildAt(0)?.requestFocus() }
    }

    private fun showApps(requestedPage: Int = 0) {
        mode = Mode.APPS
        apps = repository.allApps()
        val pageCount = maxOf(1, (apps.size + PAGE_SIZE - 1) / PAGE_SIZE)
        page = requestedPage.coerceIn(0, pageCount - 1)
        val root = NoirUi.screen(this)
        root.addView(NoirUi.header(this, getString(R.string.apps)))
        val grid = GridLayout(this).apply {
            columnCount = 3
            rowCount = 3
        }
        val visible = apps.drop(page * PAGE_SIZE).take(PAGE_SIZE)
        repeat(PAGE_SIZE) { index ->
            val tile = AppTileView(this)
            tile.configure(repository.animationsEnabled(), repository.focusLevel())
            val entry = visible.getOrNull(index)
            if (entry != null) {
                tile.bind(entry.label, loadIcon(entry))
                tile.setOnClickListener { launch(entry) }
            } else {
                tile.visibility = View.INVISIBLE
                tile.isFocusable = false
            }
            tile.tag = index
            grid.addView(tile, GridLayout.LayoutParams(
                GridLayout.spec(index / 3, 1f),
                GridLayout.spec(index % 3, 1f),
            ).apply {
                width = 0
                height = 0
                setGravity(Gravity.FILL)
                setMargins(dp(2), dp(2), dp(2), dp(2))
            })
        }
        root.addView(grid, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(NoirUi.footer(this, "%02d / %02d".format(Locale.US, page + 1, pageCount)), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(38)))
        setContentView(root)
        grid.post { grid.getChildAt(0)?.requestFocus() }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val dial = KeyBehavior.dialCharacter(keyCode)
        if (dial != null) {
            if (event.repeatCount == 0) {
                longPressConsumed = false
                event.startTracking()
            }
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (event.repeatCount == 0) {
                longPressConsumed = false
                event.startTracking()
            }
            return true
        }
        if (mode == Mode.APPS && keyCode == KeyEvent.KEYCODE_DPAD_LEFT && currentFocus?.tag == 0 && page > 0) {
            showApps(page - 1)
            return true
        }
        if (mode == Mode.APPS && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && currentFocus?.tag == PAGE_SIZE - 1 && (page + 1) * PAGE_SIZE < apps.size) {
            showApps(page + 1)
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_CALL) {
            launchIntent(Intent(Intent.ACTION_DIAL))
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        val digit = KeyBehavior.digitForKeyCode(keyCode)
        if (digit != null) {
            longPressConsumed = true
            repository.shortcutLaunchIntent(digit)?.let(::launchIntent)
                ?: Toast.makeText(this, "Atalho $digit não definido", Toast.LENGTH_SHORT).show()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            longPressConsumed = true
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        val dial = KeyBehavior.dialCharacter(keyCode)
        if (dial != null) {
            if (!longPressConsumed && event.isTracking && !event.isCanceled) {
                launchIntent(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(dial)}")))
            }
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (!longPressConsumed && event.isTracking && !event.isCanceled) showApps()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (mode == Mode.APPS) showHome()
    }

    private fun launch(entry: AppEntry) = launchIntent(repository.launchIntent(entry))

    private fun launchIntent(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Aplicativo indisponível", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadIcon(entry: AppEntry) = try {
        packageManager.getActivityIcon(entry.component)
    } catch (_: Exception) {
        getDrawable(R.drawable.ic_launcher)
    }

    companion object { private const val PAGE_SIZE = 9 }
}
