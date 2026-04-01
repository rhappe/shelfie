package dev.happe.shelfie.data.local

expect object TokenStorage {
    fun getToken(): String?
    fun setToken(token: String)
    fun clearToken()
    fun getHouseholdId(): String?
    fun setHouseholdId(id: String)
}
