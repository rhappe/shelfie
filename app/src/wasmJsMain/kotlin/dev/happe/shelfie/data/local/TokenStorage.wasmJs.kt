package dev.happe.shelfie.data.local

import kotlinx.browser.localStorage

actual class TokenStorage {
    actual fun getToken(): String? = localStorage.getItem(KEY_TOKEN)

    actual fun setToken(token: String) {
        localStorage.setItem(KEY_TOKEN, token)
    }

    actual fun clearToken() {
        localStorage.removeItem(KEY_TOKEN)
        localStorage.removeItem(KEY_HOUSEHOLD_ID)
    }

    actual fun getHouseholdId(): String? = localStorage.getItem(KEY_HOUSEHOLD_ID)

    actual fun setHouseholdId(id: String) {
        localStorage.setItem(KEY_HOUSEHOLD_ID, id)
    }

    private companion object {
        const val KEY_TOKEN = "shelfie_auth_token"
        const val KEY_HOUSEHOLD_ID = "shelfie_household_id"
    }
}
