package dev.happe.shelfie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.happe.shelfie.data.local.TokenStorage
import dev.happe.shelfie.data.remote.AuthApi
import dev.happe.shelfie.di.AppGraph
import dev.happe.shelfie.shared.LoginRequest
import dev.happe.shelfie.shared.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthViewState {
    data object Loading : AuthViewState
    data class Error(val message: String) : AuthViewState
    data class Content(val isAuthenticated: Boolean) : AuthViewState
}

sealed interface AuthViewEvent {
    data class Login(val username: String, val password: String) : AuthViewEvent
    data class Register(
        val username: String,
        val password: String,
        val displayName: String,
        val inviteCode: String?,
    ) : AuthViewEvent
    data object Logout : AuthViewEvent
}

private data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
)

class AuthViewModel(
    private val tokenStorage: TokenStorage,
    private val authApi: AuthApi,
) : ViewModel() {
    constructor(graph: AppGraph) : this(graph.tokenStorage, graph.authApi)
    private val _uiState = MutableStateFlow(AuthUiState(isAuthenticated = tokenStorage.getToken() != null))

    val viewState: StateFlow<AuthViewState> = _uiState.map { state ->
        when {
            state.isLoading -> AuthViewState.Loading
            state.error != null -> AuthViewState.Error(state.error)
            else -> AuthViewState.Content(isAuthenticated = state.isAuthenticated)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthViewState.Content(isAuthenticated = _uiState.value.isAuthenticated))

    fun handleEvent(event: AuthViewEvent) {
        when (event) {
            is AuthViewEvent.Login -> login(event.username, event.password)
            is AuthViewEvent.Register -> register(event.username, event.password, event.displayName, event.inviteCode)
            is AuthViewEvent.Logout -> logout()
        }
    }

    private fun login(username: String, password: String) {
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

    private fun register(username: String, password: String, displayName: String, inviteCode: String?) {
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

    private fun logout() {
        tokenStorage.clearToken()
        _uiState.value = AuthUiState(isAuthenticated = false)
    }
}
