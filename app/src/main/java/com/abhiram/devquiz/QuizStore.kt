package com.abhiram.devquiz

import android.content.Context

class QuizStore(context: Context) {

    private val prefs = context.getSharedPreferences("drill", Context.MODE_PRIVATE)

    fun missed(): Set<Int> = prefs.getStringSet(KEY_MISSED, emptySet())
        .orEmpty()
        .mapNotNull { it.toIntOrNull() }
        .toSet()

    fun markMissed(id: Int) = writeMissed(missed() + id)

    fun markMastered(id: Int) = writeMissed(missed() - id)

    var lifetimeRight: Int
        get() = prefs.getInt(KEY_RIGHT, 0)
        set(value) = prefs.edit().putInt(KEY_RIGHT, value).apply()

    var lifetimeAsked: Int
        get() = prefs.getInt(KEY_ASKED, 0)
        set(value) = prefs.edit().putInt(KEY_ASKED, value).apply()

    private fun writeMissed(ids: Set<Int>) {
        prefs.edit().putStringSet(KEY_MISSED, ids.map(Int::toString).toSet()).apply()
    }

    private companion object {
        const val KEY_MISSED = "missed_ids"
        const val KEY_RIGHT = "lifetime_right"
        const val KEY_ASKED = "lifetime_asked"
    }
}
