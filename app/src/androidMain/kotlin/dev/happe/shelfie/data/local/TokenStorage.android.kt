package dev.happe.shelfie.data.local

import android.content.Context
import android.content.SharedPreferences

actual object TokenStorage {
    private const val PREFS_NAME = "shelfie_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_HOUSEHOLD_ID = "household_id"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    actual fun setToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    actual fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_HOUSEHOLD_ID).apply()
    }

    actual fun getHouseholdId(): String? = prefs.getString(KEY_HOUSEHOLD_ID, null)

    actual fun setHouseholdId(id: String) {
        prefs.edit().putString(KEY_HOUSEHOLD_ID, id).apply()
    }
}
