package com.droidcon.biometricauthentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ErrorDialogViewModel : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?>
        get() = _errorMessage

    fun showErrorDialog(message: String) {
        viewModelScope.launch {
            _errorMessage.value = message
        }
    }

    fun hideErrorDialog() {
        viewModelScope.launch {
            _errorMessage.value = null
        }
    }
}
