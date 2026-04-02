package dev.happe.shelfie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.happe.shelfie.data.local.TokenStorage
import dev.happe.shelfie.data.remote.AuthApi
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

class AuthViewModel(
    private val tokenStorage: TokenStorage,
    private val authApi: AuthApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState(isAuthenticated = tokenStorage.getToken() != null))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = authApi.login(LoginRequest(username, password))
                tokenStorage.setToken(response.token)
                tokenStorage.setHouseholdId(response.householdId)
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
                val response = authApi.register(RegisterRequest(username, password, displayName, inviteCode))
                tokenStorage.setToken(response.token)
                tokenStorage.setHouseholdId(response.householdId)
                _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        tokenStorage.clearToken()
        _uiState.value = AuthUiState(isAuthenticated = false)
    }
}
