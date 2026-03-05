package com.example.foggoff.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foggoff.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

private const val MIN_PASSWORD_LENGTH = 6

private val SignInDarkBg = Color(0xFF0D0D0F)
private val SignInButtonBg = Color(0xFFF5F5F5)
private val SignInButtonText = Color(0xFF1A1A1A)
private val SignInButtonShape = RoundedCornerShape(12.dp)

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var emailFormMode by remember { mutableStateOf<EmailFormMode?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val webClientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        null
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            errorMessage = "Sign in was cancelled"
            return@rememberLauncherForActivityResult
        }
        val data = result.data ?: return@rememberLauncherForActivityResult
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = try {
            task.getResult(com.google.android.gms.common.api.ApiException::class.java)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Sign in failed"
            return@rememberLauncherForActivityResult
        }
        val idToken = account?.idToken ?: run {
            errorMessage = "No ID token received"
            return@rememberLauncherForActivityResult
        }
        isLoading = true
        errorMessage = null
        scope.launch {
            viewModel.signInWithGoogleIdToken(idToken)
                .onFailure { errorMessage = it.message ?: "Sign in failed" }
            isLoading = false
        }
    }

    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    LaunchedEffect(user) {
        if (user != null) onSignedIn()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SignInDarkBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_fogoff_logo),
                contentDescription = "Fog Off",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fog Off",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(48.dp))

            when (emailFormMode) {
                null -> {
                    if (webClientId != null) {
                        Button(
                            onClick = {
                                if (isLoading) return@Button
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(webClientId)
                                    .requestEmail()
                                    .build()
                                val client = GoogleSignIn.getClient(context, gso)
                                signInLauncher.launch(client.signInIntent)
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(SignInButtonShape),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SignInButtonBg,
                                contentColor = SignInButtonText,
                            ),
                            shape = SignInButtonShape,
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_google),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Unspecified,
                                )
                                Spacer(modifier = Modifier.size(12.dp))
                                Text(
                                    text = if (isLoading) "Signing in…" else "Sign in with Google",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { emailFormMode = EmailFormMode.SignUp },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(SignInButtonShape),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SignInButtonBg,
                            contentColor = SignInButtonText,
                        ),
                        shape = SignInButtonShape,
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                Icons.Outlined.Email,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = SignInButtonText,
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Text(
                                text = "Create account with email",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            EmailFormMode.SignUp -> {
                DarkSignInTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = "Email",
                    keyboardType = KeyboardType.Email,
                )
                Spacer(modifier = Modifier.height(12.dp))
                DarkSignInTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = "Password (min $MIN_PASSWORD_LENGTH characters)",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible },
                )
                Spacer(modifier = Modifier.height(12.dp))
                DarkSignInTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = "Confirm password",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        when {
                            email.isBlank() -> errorMessage = "Enter your email"
                            password.length < MIN_PASSWORD_LENGTH -> errorMessage = "Password must be at least $MIN_PASSWORD_LENGTH characters"
                            password != confirmPassword -> errorMessage = "Passwords don't match"
                            else -> {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    viewModel.createUserWithEmail(email, password)
                                        .onFailure { e -> errorMessage = e.message ?: "Could not create account" }
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SignInButtonBg, contentColor = SignInButtonText),
                ) {
                    Text(if (isLoading) "Creating account…" else "Create account")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { emailFormMode = EmailFormMode.SignIn; errorMessage = null }) {
                    Text("Already have an account? Sign in", color = Color.White.copy(alpha = 0.9f))
                }
            }
            EmailFormMode.SignIn -> {
                DarkSignInTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = "Email",
                    keyboardType = KeyboardType.Email,
                )
                Spacer(modifier = Modifier.height(12.dp))
                DarkSignInTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = "Password",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible },
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        when {
                            email.isBlank() -> errorMessage = "Enter your email"
                            password.isBlank() -> errorMessage = "Enter your password"
                            else -> {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    viewModel.signInWithEmail(email, password)
                                        .onFailure { e -> errorMessage = e.message ?: "Sign in failed" }
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SignInButtonBg, contentColor = SignInButtonText),
                ) {
                    Text(if (isLoading) "Signing in…" else "Sign in")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { emailFormMode = EmailFormMode.SignUp; errorMessage = null }) {
                    Text("Create an account", color = Color.White.copy(alpha = 0.9f))
                }
            }
        }

        if (emailFormMode != null) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { emailFormMode = null; errorMessage = null; email = ""; password = ""; confirmPassword = "" }) {
                Text("Back", color = Color.White.copy(alpha = 0.8f))
            }
        }

        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCF6679),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    }
}

@Composable
private fun DarkSignInTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
) {
    val borderColor = Color.White.copy(alpha = 0.4f)
    val textColor = Color.White
    val labelColor = Color.White.copy(alpha = 0.7f)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = labelColor) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide" else "Show",
                        tint = labelColor,
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            cursorColor = textColor,
            focusedLabelColor = labelColor,
            unfocusedLabelColor = labelColor,
        ),
    )
}

private enum class EmailFormMode { SignUp, SignIn }
