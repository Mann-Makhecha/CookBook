package com.example.cookbook.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cookbook.data.model.User
import com.example.cookbook.data.repository.AuthRepository
import com.example.cookbook.util.Constants
import com.example.cookbook.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling authentication operations.
 * Manages login, registration, password reset, and form validation.
 */
class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    // Auth state
    private val _authState = MutableStateFlow<Result<User>>(Result.Loading)
    val authState: StateFlow<Result<User>> = _authState.asStateFlow()

    // Password reset state
    private val _resetPasswordState = MutableStateFlow<Result<Unit>?>(null)
    val resetPasswordState: StateFlow<Result<Unit>?> = _resetPasswordState.asStateFlow()

    // Form states
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    // Validation states
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    init {
        // Check if user is already signed in
        if (authRepository.isUserSignedIn()) {
            _authState.value = Result.Loading
            // We'll handle this in the splash screen
        }
    }

    /**
     * Update email field.
     */
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        _emailError.value = null
    }

    /**
     * Update password field.
     */
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = null
    }

    /**
     * Update name field.
     */
    fun updateName(newName: String) {
        _name.value = newName
        _nameError.value = null
    }

    /**
     * Update confirm password field.
     */
    fun updateConfirmPassword(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        _confirmPasswordError.value = null
    }

    /**
     * Sign in with email and password.
     */
    fun signIn() {
        // Validate inputs
        if (!validateEmail() || !validatePassword()) {
            return
        }

        viewModelScope.launch {
            authRepository.signIn(_email.value, _password.value)
                .collect { result ->
                    _authState.value = result
                }
        }
    }

    /**
     * Sign up with email, password, and name.
     */
    fun signUp() {
        // Validate inputs
        if (!validateEmail() || !validatePassword() || !validateName() || !validateConfirmPassword()) {
            return
        }

        viewModelScope.launch {
            authRepository.signUp(_email.value, _password.value, _name.value)
                .collect { result ->
                    _authState.value = result
                }
        }
    }

    /**
     * Send password reset email.
     */
    fun resetPassword(email: String) {
        if (email.isEmpty()) {
            _resetPasswordState.value = Result.Error(Exception("Please enter your email"))
            return
        }

        if (!isValidEmail(email)) {
            _resetPasswordState.value = Result.Error(Exception("Please enter a valid email"))
            return
        }

        viewModelScope.launch {
            authRepository.resetPassword(email)
                .collect { result ->
                    _resetPasswordState.value = result
                }
        }
    }

    /**
     * Sign out the current user.
     */
    fun signOut() {
        authRepository.signOut()
        clearForm()
        _authState.value = Result.Loading
    }

    /**
     * Clear reset password state.
     */
    fun clearResetPasswordState() {
        _resetPasswordState.value = null
    }

    /**
     * Clear auth state (used when navigating away from error state).
     */
    fun clearAuthState() {
        _authState.value = Result.Loading
    }

    /**
     * Clear the form fields.
     */
    fun clearForm() {
        _email.value = ""
        _password.value = ""
        _name.value = ""
        _confirmPassword.value = ""
        _emailError.value = null
        _passwordError.value = null
        _nameError.value = null
        _confirmPasswordError.value = null
    }

    // Validation methods

    private fun validateEmail(): Boolean {
        return when {
            _email.value.isEmpty() -> {
                _emailError.value = "Email is required"
                false
            }
            !isValidEmail(_email.value) -> {
                _emailError.value = "Please enter a valid email"
                false
            }
            else -> {
                _emailError.value = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        return when {
            _password.value.isEmpty() -> {
                _passwordError.value = "Password is required"
                false
            }
            _password.value.length < Constants.MIN_PASSWORD_LENGTH -> {
                _passwordError.value = "Password must be at least ${Constants.MIN_PASSWORD_LENGTH} characters"
                false
            }
            else -> {
                _passwordError.value = null
                true
            }
        }
    }

    private fun validateName(): Boolean {
        return when {
            _name.value.isEmpty() -> {
                _nameError.value = "Name is required"
                false
            }
            _name.value.length < 2 -> {
                _nameError.value = "Name must be at least 2 characters"
                false
            }
            else -> {
                _nameError.value = null
                true
            }
        }
    }

    private fun validateConfirmPassword(): Boolean {
        return when {
            _confirmPassword.value.isEmpty() -> {
                _confirmPasswordError.value = "Please confirm your password"
                false
            }
            _confirmPassword.value != _password.value -> {
                _confirmPasswordError.value = "Passwords do not match"
                false
            }
            else -> {
                _confirmPasswordError.value = null
                true
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
