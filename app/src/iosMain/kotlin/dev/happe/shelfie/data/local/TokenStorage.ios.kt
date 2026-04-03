package dev.happe.shelfie.data.local

import platform.Foundation.NSUserDefaults

actual class TokenStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getToken(): String? = defaults.stringForKey(KEY_TOKEN)

    actual fun setToken(token: String) {
        defaults.setObject(token, KEY_TOKEN)
    }

    actual fun clearToken() {
        defaults.removeObjectForKey(KEY_TOKEN)
        defaults.removeObjectForKey(KEY_HOUSEHOLD_ID)
    }

    actual fun getHouseholdId(): String? = defaults.stringForKey(KEY_HOUSEHOLD_ID)

    actual fun setHouseholdId(id: String) {
        defaults.setObject(id, KEY_HOUSEHOLD_ID)
    }

    private companion object {
        const val KEY_TOKEN = "shelfie_auth_token"
        const val KEY_HOUSEHOLD_ID = "shelfie_household_id"
    }
}
