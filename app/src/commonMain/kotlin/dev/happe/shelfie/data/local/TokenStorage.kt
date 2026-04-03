package dev.happe.shelfie.data.local

expect class TokenStorage {
    fun getToken(): String?
    fun setToken(token: String)
    fun clearToken()
    fun getHouseholdId(): String?
    fun setHouseholdId(id: String)
}
