package com.example.foggoff.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.foggoff.R
import com.example.foggoff.friends.FriendsScreen
import com.example.foggoff.map.FogMapScreen
import com.example.foggoff.profile.ProfileScreen

private val Tabs = listOf(
    "Explore" to Icons.Default.Explore,
    "Friends" to Icons.Default.People,
    "Profile" to Icons.Default.Person,
)

@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_fogoff_logo),
                contentDescription = "Fog Off",
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = "Fog Off",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 12.dp),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp) // Pushes content up so it's not hidden
            ) {
                when (selectedIndex) {
                    0 -> FogMapScreen(modifier = Modifier.fillMaxSize())
                    1 -> FriendsScreen(modifier = Modifier.fillMaxSize())
                    else -> ProfileScreen(onSignOut = onSignOut, modifier = Modifier.fillMaxSize())
                }
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
}
