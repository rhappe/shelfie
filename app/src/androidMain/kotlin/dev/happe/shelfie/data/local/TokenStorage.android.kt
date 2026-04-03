package dev.happe.shelfie.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

actual class TokenStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    actual fun setToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    actual fun clearToken() {
        prefs.edit {
            remove(KEY_TOKEN)
            remove(KEY_HOUSEHOLD_ID)
        }
    }

    actual fun getHouseholdId(): String? = prefs.getString(KEY_HOUSEHOLD_ID, null)

    actual fun setHouseholdId(id: String) {
        prefs.edit { putString(KEY_HOUSEHOLD_ID, id) }
    }

    private companion object {
        const val PREFS_NAME = "shelfie_prefs"
        const val KEY_TOKEN = "auth_token"
        const val KEY_HOUSEHOLD_ID = "household_id"
    }
}
