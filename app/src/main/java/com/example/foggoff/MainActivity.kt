package com.example.foggoff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.foggoff.ui.MainScreen
import com.example.foggoff.ui.theme.FoggoffTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoggoffTheme {
                MainScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}