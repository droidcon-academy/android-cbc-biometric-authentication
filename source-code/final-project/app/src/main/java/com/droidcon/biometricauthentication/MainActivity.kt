package com.droidcon.biometricauthentication

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.droidcon.biometricauthentication.ui.composables.AlertDialog
import com.droidcon.biometricauthentication.ui.theme.BiometricAuthenticationTheme

class MainActivity : FragmentActivity() {

    private val errorDialogViewModel: ErrorDialogViewModel by viewModels()

    private val activityResultHandler =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_CANCELED) {
                errorDialogViewModel.showErrorDialog(getString(R.string.error_biometric_enrollment_cancelled))
            } else {
                showLoginPrompt()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricAuthenticationTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthenticateButton(
                        onAuthenticateClicked = {
                            checkBiometricAvailability()
                        }
                    )

                    // Observe the errorMessage state from the ViewModel
                    val errorMessage by errorDialogViewModel.errorMessage.collectAsState()

                    // Show the error dialog when errorMessage is not null
                    errorMessage?.let { message ->
                        ShowErrorDialog(
                            message = message,
                            onClose = {
                                errorDialogViewModel.hideErrorDialog()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showLoginPrompt()
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                errorDialogViewModel.showErrorDialog(getString(R.string.error_biometric_not_available))
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                errorDialogViewModel.showErrorDialog(getString(R.string.error_biometric_not_available))
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                launchBiometricEnrollment()
            }
        }
    }

    private fun showLoginPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    errorDialogViewModel.showErrorDialog(getString(R.string.biometric_authentication_successful))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    val cancelled = errorCode in arrayListOf(
                        BiometricPrompt.ERROR_CANCELED,
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    )
                    if (cancelled) {
                        errorDialogViewModel.showErrorDialog(getString(R.string.error_biometric_prompt_cancelled))
                    } else { // Too many attempts (ERROR_LOCKOUT)
                        errorDialogViewModel.showErrorDialog(getString(R.string.error_biometric_authentication_too_many_attempts))
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // This method will be invoked on unsuccessful attempts
                    // (e.g. unrecognized fingerprint)
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    private fun launchBiometricEnrollment() {
        val intent: Intent = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                Intent(Settings.ACTION_BIOMETRIC_ENROLL).putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
            }

            else -> Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        activityResultHandler.launch(intent)
    }

    @Composable
    fun AuthenticateButton(onAuthenticateClicked: () -> Unit) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        onAuthenticateClicked()
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = "Authenticate")
                }
            }
        }
    }

    @Composable
    fun ShowErrorDialog(message: String, onClose: () -> Unit) {
        AlertDialog(
            message = message,
            onDismiss = onClose
        )
    }
}