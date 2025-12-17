package com.bahy.newsly.ui.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import java.util.Locale

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = remember { AuthViewModel(AppModule.provideAuthRepository()) },
    onSignUpSuccess: () -> Unit = {},
    onNavigateToSignIn: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }
    
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Validation functions
    fun validateUsername(usernameValue: String): String? {
        return when {
            usernameValue.isEmpty() -> "Username is required"
            usernameValue.length < 3 -> "Username must be at least 3 characters"
            usernameValue.length > 20 -> "Username must be less than 20 characters"
            !usernameValue.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Username can only contain letters, numbers, and underscores"
            else -> null
        }
    }
    
    fun validatePassword(passwordValue: String): String? {
        return when {
            passwordValue.isEmpty() -> "Password is required"
            passwordValue.length < 6 -> "Password must be at least 6 characters"
            passwordValue.length > 50 -> "Password must be less than 50 characters"
            else -> null
        }
    }
    
    fun validateConfirmPassword(passwordValue: String, confirmValue: String): String? {
        return when {
            confirmValue.isEmpty() -> "Please confirm your password"
            passwordValue != confirmValue -> "Passwords do not match"
            else -> null
        }
    }
    
    fun validateEmail(emailValue: String): String? {
        return when {
            emailValue.isEmpty() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() -> "Please enter a valid email address"
            else -> null
        }
    }
    
    fun isFormValid(): Boolean {
        return usernameError == null && passwordError == null && 
               confirmPasswordError == null && emailError == null &&
               username.isNotEmpty() && password.isNotEmpty() && 
               confirmPassword.isNotEmpty() && emailOrPhone.isNotEmpty() &&
               agreeToTerms
    }
    
    // Navigate on successful sign up
    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) {
            onSignUpSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.newsly_logo),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Title
            Text(
                text = stringResource(id = R.string.register),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Username Field
            Column(modifier = Modifier.fillMaxWidth()) {
                CustomTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        usernameError = validateUsername(it)
                        viewModel.clearError()
                    },
                    placeholder = stringResource(id = R.string.username),
                    leadingIcon = Icons.Default.Person,
                    isError = usernameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (usernameError != null) {
                    Text(
                        text = usernameError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            Column(modifier = Modifier.fillMaxWidth()) {
                CustomTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        passwordError = validatePassword(it)
                        confirmPasswordError = validateConfirmPassword(it, confirmPassword)
                        viewModel.clearError()
                    },
                    placeholder = stringResource(id = R.string.password),
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    isError = passwordError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (passwordError != null) {
                    Text(
                        text = passwordError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            Column(modifier = Modifier.fillMaxWidth()) {
                CustomTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        confirmPasswordError = validateConfirmPassword(password, it)
                        viewModel.clearError()
                    },
                    placeholder = stringResource(id = R.string.confirm_password),
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    isError = confirmPasswordError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (confirmPasswordError != null) {
                    Text(
                        text = confirmPasswordError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            Column(modifier = Modifier.fillMaxWidth()) {
                CustomTextField(
                    value = emailOrPhone,
                    onValueChange = { 
                        emailOrPhone = it
                        emailError = validateEmail(it)
                        viewModel.clearError()
                    },
                    placeholder = stringResource(id = R.string.email_or_phone),
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    isError = emailError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError != null) {
                    Text(
                        text = emailError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Terms Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreeToTerms,
                    onCheckedChange = { agreeToTerms = it },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.agree_terms),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Midnight.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { agreeToTerms = !agreeToTerms }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Register Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable(enabled = !uiState.isLoading && isFormValid()) {
                        usernameError = validateUsername(username)
                        passwordError = validatePassword(password)
                        confirmPasswordError = validateConfirmPassword(password, confirmPassword)
                        emailError = validateEmail(emailOrPhone)
                        
                        if (usernameError == null && passwordError == null && 
                            confirmPasswordError == null && emailError == null) {
                            viewModel.signUp(username, emailOrPhone, password)
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.isLoading || !isFormValid()) Color(0xFFE0E0E0).copy(alpha = 0.6f) else Color(0xFFE0E0E0)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Midnight
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.register),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Midnight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false
) {
    val visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Midnight.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (isError) Color.Red.copy(alpha = 0.6f) else Midnight.copy(alpha = 0.6f)
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedBorderColor = if (isError) Color.Red else Color.Transparent,
            unfocusedBorderColor = if (isError) Color.Red else Color.Transparent,
            focusedTextColor = Midnight,
            unfocusedTextColor = Midnight,
            errorBorderColor = Color.Red,
            errorContainerColor = Color(0xFFFFEBEE)
        ),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        isError = isError
    )
}

@Preview(
    name = "Sign Up Screen",
    showBackground = true,
    backgroundColor = 0xFF6BB5B8
)
@Composable
private fun SignUpScreenPreview() {
    NewslyTheme {
        SignUpScreen()
    }
}

