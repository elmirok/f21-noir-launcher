package io.github.elmirok.f21noir.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.MediaStore
import io.github.elmirok.f21noir.SettingsActivity
import io.github.elmirok.f21noir.model.AppEntry
import java.text.Collator
import java.util.Locale

class LauncherRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val packageManager = context.packageManager

    data class DefaultFavorite(val label: String, val intent: Intent)

    val defaults: List<DefaultFavorite> = listOf(
        DefaultFavorite("Telefone", Intent(Intent.ACTION_DIAL)),
        DefaultFavorite("Mensagens", Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)),
        DefaultFavorite("Contatos", Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CONTACTS)),
        DefaultFavorite("Câmera", Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)),
        DefaultFavorite("Arquivos", Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*")),
        DefaultFavorite("Ajustes Noir", Intent(context, SettingsActivity::class.java)),
    )

    fun allApps(includeHidden: Boolean = false): List<AppEntry> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val hidden = hiddenComponents()
        val collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.PRIMARY }
        return packageManager.queryIntentActivities(intent, 0)
            .asSequence()
            .mapNotNull { info ->
                val activity = info.activityInfo ?: return@mapNotNull null
                val component = ComponentName(activity.packageName, activity.name)
                if (component.packageName == context.packageName) return@mapNotNull null
                if (!includeHidden && component.flattenToString() in hidden) return@mapNotNull null
                AppEntry(component, info.loadLabel(packageManager).toString())
            }
            .distinctBy { it.component.flattenToString() }
            .sortedWith { a, b -> collator.compare(a.label, b.label) }
            .toList()
    }

    fun favorite(slot: Int): AppEntry? {
        val stored = prefs.getString(favoriteKey(slot), null)
        if (stored != null) return entryFor(ComponentName.unflattenFromString(stored))
        val resolved = packageManager.resolveActivity(defaults[slot].intent, 0)?.activityInfo ?: return null
        return AppEntry(ComponentName(resolved.packageName, resolved.name), defaults[slot].label)
    }

    fun favoriteLabel(slot: Int): String {
        if (!prefs.contains(favoriteKey(slot))) return defaults[slot].label
        return favorite(slot)?.label ?: defaults[slot].label
    }

    fun favoriteLaunchIntent(slot: Int): Intent {
        val stored = prefs.getString(favoriteKey(slot), null)
        val entry = stored?.let { entryFor(ComponentName.unflattenFromString(it)) }
        return entry?.let(::launchIntent) ?: Intent(defaults[slot].intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun setFavorite(slot: Int, component: ComponentName) {
        prefs.edit().putString(favoriteKey(slot), component.flattenToString()).apply()
    }

    fun isFavoriteCustomized(slot: Int): Boolean = prefs.contains(favoriteKey(slot))

    fun shortcut(digit: Int): AppEntry? {
        val stored = prefs.getString(shortcutKey(digit), null)
        if (stored == NONE) return null
        if (stored != null) return entryFor(ComponentName.unflattenFromString(stored))
        return if (digit in 1..6) favorite(digit - 1) else null
    }

    fun shortcutLaunchIntent(digit: Int): Intent? {
        val stored = prefs.getString(shortcutKey(digit), null)
        if (stored == NONE) return null
        val entry = stored?.let { entryFor(ComponentName.unflattenFromString(it)) }
        if (entry != null) return launchIntent(entry)
        return if (digit in 1..6) favoriteLaunchIntent(digit - 1) else null
    }

    fun setShortcut(digit: Int, component: ComponentName) {
        prefs.edit().putString(shortcutKey(digit), component.flattenToString()).apply()
    }

    fun clearShortcut(digit: Int) {
        prefs.edit().putString(shortcutKey(digit), NONE).apply()
    }

    fun hiddenComponents(): Set<String> = prefs.getStringSet(KEY_HIDDEN, emptySet())?.toSet().orEmpty()

    fun setHidden(component: ComponentName, hidden: Boolean) {
        val values = hiddenComponents().toMutableSet()
        if (hidden) values += component.flattenToString() else values -= component.flattenToString()
        prefs.edit().putStringSet(KEY_HIDDEN, values).apply()
    }

    fun datePattern(): String = prefs.getString(KEY_DATE_PATTERN, "EEE, dd MMM") ?: "EEE, dd MMM"

    fun setDatePattern(pattern: String) {
        prefs.edit().putString(KEY_DATE_PATTERN, pattern).apply()
    }

    fun focusLevel(): Int = prefs.getInt(KEY_FOCUS_LEVEL, 1).coerceIn(0, 2)

    fun setFocusLevel(level: Int) {
        prefs.edit().putInt(KEY_FOCUS_LEVEL, level.coerceIn(0, 2)).apply()
    }

    fun animationsEnabled(): Boolean = prefs.getBoolean(KEY_ANIMATIONS, true)

    fun setAnimationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
    }

    fun launchIntent(entry: AppEntry): Intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setComponent(entry.component)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

    private fun entryFor(component: ComponentName?): AppEntry? {
        component ?: return null
        return try {
            val info = packageManager.getActivityInfo(component, 0)
            AppEntry(component, info.loadLabel(packageManager).toString())
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val PREFS = "f21_noir_preferences"
        private const val KEY_HIDDEN = "hidden_components"
        private const val KEY_DATE_PATTERN = "date_pattern"
        private const val KEY_FOCUS_LEVEL = "focus_level"
        private const val KEY_ANIMATIONS = "animations"
        private const val NONE = "__none__"
        fun favoriteKey(slot: Int) = "favorite_$slot"
        fun shortcutKey(digit: Int) = "shortcut_$digit"
    }
}
