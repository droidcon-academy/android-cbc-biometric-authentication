package com.droidcon.biometricauthentication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.droidcon.biometricauthentication.ui.composables.AlertDialog
import com.droidcon.biometricauthentication.ui.theme.BiometricAuthenticationTheme

class MainActivity : FragmentActivity() {

    private val errorDialogViewModel: ErrorDialogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricAuthenticationTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthenticateButton(
                        onAuthenticateClicked = {

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