package com.example.foggoff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foggoff.auth.AuthViewModel
import com.example.foggoff.auth.SignInScreen
import com.example.foggoff.ui.MainScreen
import com.example.foggoff.ui.theme.FoggoffTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoggoffTheme {
                val authViewModel: AuthViewModel = viewModel()
                val user by authViewModel.currentUser.collectAsStateWithLifecycle()
                if (user == null) {
                    SignInScreen(
                        viewModel = authViewModel,
                        onSignedIn = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    MainScreen(
                        onSignOut = { authViewModel.signOut() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}