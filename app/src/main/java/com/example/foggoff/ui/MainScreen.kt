package com.example.foggoff.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.List
import com.example.foggoff.feed.FeedScreen
import com.example.foggoff.map.FogMapScreen
import com.example.foggoff.profile.ProfileScreen

private val Tabs = listOf(
    "Explore" to Icons.Default.Explore,
    "Feed" to Icons.AutoMirrored.Filled.List,
    "Profile" to Icons.Default.Person,
)

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    Box(modifier = modifier.fillMaxSize()) {
        when (selectedIndex) {
            0 -> FogMapScreen(modifier = Modifier.fillMaxSize())
            1 -> FeedScreen(modifier = Modifier.fillMaxSize())
            else -> ProfileScreen(modifier = Modifier.fillMaxSize())
        }
        LiquidGlassNavBar(
            selectedIndex = selectedIndex,
            tabs = Tabs,
            onTabSelected = { selectedIndex = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        )
    }
}
