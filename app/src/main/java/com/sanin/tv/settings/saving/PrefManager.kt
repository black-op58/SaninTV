package com.sanin.tv.settings.saving

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object PrefManager {
    // TODO: Full implementation was not present in the source ZIP — only stub recovered
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getVal(prefName: PrefName, default: T? = null): T {
    val pref = prefName.data
        return when (pref.type) {
            Boolean::class -> (prefs.getBoolean(prefName.name, (pref.default as? Boolean) ?: false)) as T
            Int::class     -> (prefs.getInt(prefName.name, (pref.default as? Int) ?: 0)) as T
            Float::class   -> (prefs.getFloat(prefName.name, (pref.default as? Float) ?: 0f)) as T
            String::class  -> (prefs.getString(prefName.name, pref.default as? String) ?: "") as T
            else           -> default ?: pref.default as T
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getNullableVal(prefName: PrefName, default: T? = null): T? {
    return try { getVal(prefName, default) } catch (_: Exception) { default }
    }

    fun <T : Any> setVal(prefName: PrefName, value: T) {
        prefs.edit().apply {
    when (value) {
                is Boolean -> putBoolean(prefName.name, value)
                is Int     -> putInt(prefName.name, value)
                is Float   -> putFloat(prefName.name, value)
                is String  -> putString(prefName.name, value)
            }
        }.apply()
    }

    fun <T> getCustomVal(key: String, default: T): T = default
    fun setCustomVal(key: String, value: Any) {}
    fun removeCustomVal(key: String) {}
}
