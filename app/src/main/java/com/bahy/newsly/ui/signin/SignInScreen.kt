package com.bahy.newsly.ui.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.bahy.newsly.data.repository.GoogleSignInHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahy.newsly.R
import com.bahy.newsly.di.AppModule
import com.bahy.newsly.ui.theme.Midnight
import com.bahy.newsly.ui.theme.NewslyTheme
import com.bahy.newsly.ui.theme.SplashBackground
import com.bahy.newsly.ui.viewmodel.AuthViewModel

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = remember { AuthViewModel(AppModule.provideAuthRepository()) },
    onSignInSuccess: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Validation functions
    fun validateEmail(emailValue: String): String? {
        return when {
            emailValue.isEmpty() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() -> "Please enter a valid email address"
            else -> null
        }
    }
    
    fun validatePassword(passwordValue: String): String? {
        return when {
            passwordValue.isEmpty() -> "Password is required"
            passwordValue.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
    
    fun isFormValid(): Boolean {
        return emailError == null && passwordError == null && 
               email.isNotEmpty() && password.isNotEmpty()
    }
    
    // Google Sign In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account?.let {
                // Sign in with Google
                viewModel.signInWithGoogle(it)
            } ?: run {
                // No account returned - user cancelled
            }
        } catch (e: ApiException) {
            // Handle Google Sign In error
            when (e.statusCode) {
                com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR -> {
                    // Network error
                }
                com.google.android.gms.common.api.CommonStatusCodes.CANCELED -> {
                    // User cancelled - no error needed
                }
                else -> {
                    // Other errors
                }
            }
        }
    }
    
    // Navigate on successful sign in - watch both isSignedIn and currentUser
    LaunchedEffect(uiState.isSignedIn, uiState.currentUser, uiState.isLoading) {
        if (uiState.isSignedIn && uiState.currentUser != null && !uiState.isLoading) {
            // Navigate immediately when signed in
            onSignInSuccess()
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
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Welcome Back Title with emoji
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.welcome_back),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Midnight
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "👋",
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Welcome Message
            Text(
                text = stringResource(id = R.string.welcome_message),
                style = MaterialTheme.typography.bodyMedium,
                color = Midnight.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            Column(modifier = Modifier.fillMaxWidth()) {
                CustomTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = validateEmail(it)
                        viewModel.clearError()
                    },
                    placeholder = stringResource(id = R.string.email_address),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            Column(modifier = Modifier.fillMaxWidth()) {
                CustomTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        passwordError = validatePassword(it)
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

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot Password Link
            Text(
                text = stringResource(id = R.string.forgotten_password),
                style = MaterialTheme.typography.bodyMedium,
                color = Midnight.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onForgotPasswordClick() },
                textAlign = TextAlign.End
            )

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

            // Sign In Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable(enabled = !uiState.isLoading && isFormValid()) {
                        emailError = validateEmail(email)
                        passwordError = validatePassword(password)
                        if (emailError == null && passwordError == null) {
                            viewModel.signIn(email, password)
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.isLoading || !isFormValid()) Midnight.copy(alpha = 0.6f) else Midnight
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
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.sign_in),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Or separator
            Text(
                text = stringResource(id = R.string.or),
                style = MaterialTheme.typography.bodyMedium,
                color = Midnight.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign In Button
            SocialLoginButton(
                text = stringResource(id = R.string.sign_in_with_google),
                onClick = {
                    val googleSignInClient = GoogleSignInHelper.getGoogleSignInClient(context)
                    if (googleSignInClient != null) {
                        // Sign out first to ensure account selection dialog appears
                        // This ensures user can choose which account to use
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                googleSignInClient.signOut().await()
                            } catch (e: Exception) {
                                // Ignore errors - proceed with sign in anyway
                            }
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        }
                    } else {
                        // Show error if Web Client ID not configured
                        // You can show a toast or error message here
                    }
                },
                iconColor = Color(0xFF4285F4),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Link
            val signUpText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Midnight.copy(alpha = 0.7f))) {
                    append(stringResource(id = R.string.dont_have_account).substringBefore("Sign Up"))
                }
                withStyle(style = SpanStyle(color = Midnight, fontWeight = FontWeight.Bold)) {
                    append("Sign Up")
                }
            }
            
            Text(
                text = signUpText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSignUp() }
                    .padding(vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
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

@Composable
private fun SocialLoginButton(
    text: String,
    onClick: () -> Unit,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icon placeholder (G for Google, f for Facebook)
            Box(
                modifier = Modifier
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (text.contains("Google", ignoreCase = true)) "G" else "f",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Midnight
            )
        }
    }
}

@Preview(
    name = "Sign In Screen",
    showBackground = true,
    backgroundColor = 0xFF6BB5B8
)
@Composable
private fun SignInScreenPreview() {
    NewslyTheme {
        SignInScreen()
    }
}

