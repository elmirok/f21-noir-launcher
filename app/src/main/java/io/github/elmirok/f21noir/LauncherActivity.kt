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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import io.github.elmirok.f21noir.data.LauncherRepository
import io.github.elmirok.f21noir.model.AppEntry
import io.github.elmirok.f21noir.model.HomeGridNavigation
import io.github.elmirok.f21noir.model.KeyBehavior
import io.github.elmirok.f21noir.model.SwipeGesture
import io.github.elmirok.f21noir.ui.AppTileView
import io.github.elmirok.f21noir.ui.NoirAppIcons
import io.github.elmirok.f21noir.ui.NoirGlyphs
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
    private var hasRendered = false
    private var touchDownX = 0f
    private var touchDownY = 0f
    private var touchDownTime = 0L
    private val homeTiles = mutableListOf<AppTileView>()
    private var homeFocusIndex = 0

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
        if (hasRendered) {
            if (mode == Mode.HOME) showHome() else showApps(page)
        } else {
            hasRendered = true
        }
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

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = event.x
                touchDownY = event.y
                touchDownTime = event.eventTime
            }
            MotionEvent.ACTION_UP -> {
                val gesture = SwipeGesture.detect(
                    deltaX = event.x - touchDownX,
                    deltaY = event.y - touchDownY,
                    durationMs = event.eventTime - touchDownTime,
                    minimumDistancePx = dp(48).toFloat(),
                )
                if (handleGesture(gesture)) return true
            }
            MotionEvent.ACTION_CANCEL -> touchDownTime = 0L
        }
        return super.dispatchTouchEvent(event)
    }

    private fun handleGesture(gesture: SwipeGesture.Direction?): Boolean = when {
        mode == Mode.HOME && gesture == SwipeGesture.Direction.UP -> {
            showApps()
            true
        }
        mode == Mode.APPS && gesture == SwipeGesture.Direction.DOWN -> {
            showHome()
            true
        }
        mode == Mode.APPS && gesture == SwipeGesture.Direction.LEFT &&
            (page + 1) * PAGE_SIZE < apps.size -> {
            showApps(page + 1)
            true
        }
        mode == Mode.APPS && gesture == SwipeGesture.Direction.RIGHT && page > 0 -> {
            showApps(page - 1)
            true
        }
        else -> false
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
        homeTiles.clear()
        val root = NoirUi.screen(this)
        val clockBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            contentDescription = "Configurações do F21 Noir"
            setOnClickListener { startActivity(Intent(this@LauncherActivity, SettingsActivity::class.java)) }
            isFocusable = false
            isFocusableInTouchMode = false
            defaultFocusHighlightEnabled = false
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
            val drawable = if (entry != null && repository.isFavoriteCustomized(slot)) {
                NoirAppIcons.forApp(packageManager, entry)
            } else {
                NoirGlyphs.forFavorite(slot)
            }
            tile.bind(entry?.label ?: repository.defaults[slot].label, drawable)
            tile.setOnClickListener { launchIntent(repository.favoriteLaunchIntent(slot)) }
            tile.setOnLongClickListener {
                startActivity(Intent(this, AppPickerActivity::class.java).putExtra(AppPickerActivity.EXTRA_FAVORITE_SLOT, slot))
                true
            }
            homeTiles += tile
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
        homeFocusIndex = homeFocusIndex.coerceIn(homeTiles.indices)
        grid.post { homeTiles.getOrNull(homeFocusIndex)?.requestFocus() }
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
                tile.bind(entry.label, NoirAppIcons.forApp(packageManager, entry))
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
                // Open immediately on key-down. Waiting for key-up made the dedicated hardware
                // button feel delayed; tracking still lets Android deliver a long press below.
                if (mode == Mode.HOME) showApps()
            }
            return true
        }
        if (mode == Mode.HOME) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER ||
                keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                val focused = homeTiles.indexOfFirst { it.hasFocus() }
                    .takeIf { it >= 0 } ?: homeFocusIndex
                homeTiles.getOrNull(focused)?.performClick()
                return true
            }
            val focused = homeTiles.indexOfFirst { it.hasFocus() }
                .takeIf { it >= 0 } ?: homeFocusIndex
            HomeGridNavigation.next(focused, keyCode)?.let { next ->
                homeFocusIndex = next
                homeTiles[next].requestFocus()
                return true
            }
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

    companion object { private const val PAGE_SIZE = 9 }
}
