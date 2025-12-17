package com.bahy.newsly.ui.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahy.newsly.R
import com.bahy.newsly.di.AppModule
import com.bahy.newsly.ui.theme.Midnight
import com.bahy.newsly.ui.theme.NewslyTheme
import com.bahy.newsly.ui.theme.SplashBackground
import com.bahy.newsly.ui.viewmodel.AuthViewModel
import android.widget.Toast

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = remember { AuthViewModel(AppModule.provideAuthRepository()) },
    onBackToSignIn: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Show Toast when password reset email is sent successfully
    LaunchedEffect(uiState.passwordResetEmailSent) {
        if (uiState.passwordResetEmailSent) {
            Toast.makeText(
                context,
                context.getString(R.string.password_reset_success_message),
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearPasswordResetState()
        }
    }
    
    // Show error Toast if there's an error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            if (!uiState.passwordResetEmailSent) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.forgotten_password),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Midnight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.forgot_password_message),
                style = MaterialTheme.typography.bodyMedium,
                color = Midnight.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.email_address),
                        color = Midnight.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Midnight.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Midnight,
                    unfocusedTextColor = Midnight
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        if (email.isNotBlank()) {
                            viewModel.sendPasswordResetEmail(email)
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                color = Midnight
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.send_reset_link),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.back_to_sign_in),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Midnight,
                modifier = Modifier.clickable { onBackToSignIn() }
            )
        }
    }
}

@Preview(
    name = "Forgot Password Screen",
    showBackground = true,
    backgroundColor = 0xFF6BB5B8
)
@Composable
private fun ForgotPasswordScreenPreview() {
    NewslyTheme {
        ForgotPasswordScreen()
    }
}


