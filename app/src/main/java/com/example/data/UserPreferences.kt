package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("creative_paint_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_RECENT_COLORS = "recent_colors"
        private const val KEY_FAVORITE_COLORS = "favorite_colors"
        private const val KEY_DEFAULT_CANVAS_COLOR = "default_canvas_color"
        private const val KEY_DEFAULT_BRUSH_WIDTH = "default_brush_width"
        private const val KEY_THEME_MODE = "theme_mode"

        val DEFAULT_FAVORITES = listOf(
            Color.parseColor("#000000"), // Black
            Color.parseColor("#FFFFFF"), // White
            Color.parseColor("#EF4444"), // Red
            Color.parseColor("#F97316"), // Orange
            Color.parseColor("#FBBF24"), // Yellow
            Color.parseColor("#10B981"), // Green
            Color.parseColor("#06B6D4"), // Cyan
            Color.parseColor("#3B82F6"), // Blue
            Color.parseColor("#8B5CF6"), // Purple
            Color.parseColor("#EC4899"), // Pink
            Color.parseColor("#854D0E"), // Brown
            Color.parseColor("#64748B")  // Grey
        )
    }

    private val _recentColors = MutableStateFlow<List<Int>>(loadIntList(KEY_RECENT_COLORS, DEFAULT_FAVORITES.take(6)))
    val recentColors: StateFlow<List<Int>> = _recentColors.asStateFlow()

    private val _favoriteColors = MutableStateFlow<List<Int>>(loadIntList(KEY_FAVORITE_COLORS, DEFAULT_FAVORITES))
    val favoriteColors: StateFlow<List<Int>> = _favoriteColors.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun addRecentColor(color: Int) {
        val current = _recentColors.value.filter { it != color }.toMutableList()
        current.add(0, color)
        val trimmed = current.take(12)
        _recentColors.value = trimmed
        saveIntList(KEY_RECENT_COLORS, trimmed)
    }

    fun toggleFavoriteColor(color: Int) {
        val current = _favoriteColors.value.toMutableList()
        if (current.contains(color)) {
            current.remove(color)
        } else {
            current.add(0, color)
        }
        _favoriteColors.value = current
        saveIntList(KEY_FAVORITE_COLORS, current)
    }

    fun isFavorite(color: Int): Boolean {
        return _favoriteColors.value.contains(color)
    }

    var defaultCanvasColor: Int
        get() = prefs.getInt(KEY_DEFAULT_CANVAS_COLOR, Color.WHITE)
        set(value) = prefs.edit().putInt(KEY_DEFAULT_CANVAS_COLOR, value).apply()

    var defaultBrushWidth: Float
        get() = prefs.getFloat(KEY_DEFAULT_BRUSH_WIDTH, 12f)
        set(value) = prefs.edit().putFloat(KEY_DEFAULT_BRUSH_WIDTH, value).apply()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    private fun loadIntList(key: String, default: List<Int>): List<Int> {
        val json = prefs.getString(key, null) ?: return default
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<Int>()
            for (i in 0 until arr.length()) {
                list.add(arr.getInt(i))
            }
            if (list.isEmpty()) default else list
        } catch (e: Exception) {
            default
        }
    }

    private fun saveIntList(key: String, list: List<Int>) {
        val arr = JSONArray()
        for (item in list) {
            arr.put(item)
        }
        prefs.edit().putString(key, arr.toString()).apply()
    }
}
