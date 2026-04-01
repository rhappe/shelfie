package dev.happe.shelfie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.happe.shelfie.data.local.TokenStorage
import dev.happe.shelfie.data.remote.ApiClient
import dev.happe.shelfie.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState(isAuthenticated = TokenStorage.getToken() != null))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = ApiClient.login(LoginRequest(username, password))
                TokenStorage.setToken(response.token)
                TokenStorage.setHouseholdId(response.householdId)
                _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Login failed")
            }
        }
    }

    fun register(username: String, password: String, displayName: String, inviteCode: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = ApiClient.register(RegisterRequest(username, password, displayName, inviteCode))
                TokenStorage.setToken(response.token)
                TokenStorage.setHouseholdId(response.householdId)
                _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        TokenStorage.clearToken()
        _uiState.value = AuthUiState(isAuthenticated = false)
    }
}
