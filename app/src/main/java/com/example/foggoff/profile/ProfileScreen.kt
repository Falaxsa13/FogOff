package com.example.foggoff.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foggoff.auth.AuthViewModel

private val CardShape = RoundedCornerShape(24.dp)
private val GlassBg = Color.White
private val GlassBorder = Color.Black.copy(alpha = 0.08f)
private val HeroBlack = Color(0xFFF3F4F6)
private val IconBg = Color.Black.copy(alpha = 0.06f)
private val MutedText = Color(0xFF6B7280)

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
) {
    val hexCount by viewModel.hexCount.collectAsStateWithLifecycle()
    val user by authViewModel.currentUser.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    val displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Explorer"
    val avatarLetter = displayName.first().uppercaseChar().toString()
    val email = user?.email.orEmpty()

    if (showSettings) {
        SettingsScreen(
            authViewModel = authViewModel,
            onBack = { showSettings = false },
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Hero section ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(HeroBlack),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .shadow(16.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.3f))
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = avatarLetter,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                }

                // Name row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.06f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { showSettings = true }
                            .padding(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit name",
                            tint = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                if (email.isNotBlank()) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.45f),
                    )
                }
            }
        }

        // ── Content area ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Stats strip ─────────────────────────────────────────
            GlassCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatCell(
                        value = "$hexCount",
                        label = "Hexes",
                        icon = Icons.Outlined.GridOn,
                    )
                    StatDivider()
                    StatCell(
                        value = if (hexCount >= 100) "Explorer" else "Novice",
                        label = "Rank",
                        icon = null,
                    )
                    StatDivider()
                    StatCell(
                        value = "${(hexCount * 0.07f).toInt()} km²",
                        label = "Area",
                        icon = null,
                    )
                }
            }

            // ── Level badge ──────────────────────────────────────────
            GlassCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    val badgeIcon = when {
                        hexCount >= 500 -> Icons.Outlined.Language
                        hexCount >= 200 -> Icons.Outlined.Map
                        hexCount >= 50  -> Icons.Outlined.TravelExplore
                        else            -> Icons.Outlined.Explore
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(HeroBlack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = when {
                                hexCount >= 500 -> "World Traveler"
                                hexCount >= 200 -> "Cartographer"
                                hexCount >= 50  -> "Pathfinder"
                                else            -> "Just Started"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = when {
                                hexCount >= 500 -> "You've mapped almost everything"
                                hexCount >= 200 -> "${500 - hexCount} hexes to World Traveler"
                                hexCount >= 50  -> "${200 - hexCount} hexes to Cartographer"
                                else            -> "${50 - hexCount} hexes to Pathfinder"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText,
                        )
                    }
                }
            }

            // ── Menu card ────────────────────────────────────────────
            GlassCard {
                Column {
                    MenuRow(
                        icon = Icons.Outlined.Settings,
                        label = "Settings",
                        sublabel = "Name, preferences",
                        onClick = { showSettings = true },
                    )
                    CardDivider()
                    MenuRow(
                        icon = Icons.Outlined.Info,
                        label = "About Fog Off",
                        sublabel = "Version 1.0 · Open source",
                        onClick = { },
                    )
                    CardDivider()
                    MenuRow(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        label = "Sign out",
                        sublabel = null,
                        tint = Color(0xFFE53935),
                        onClick = onSignOut,
                        showChevron = false,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Shared primitives ─────────────────────────────────────────────────────────

@Composable
private fun GlassCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = CardShape,
                spotColor = Color.Black.copy(alpha = 0.06f),
                ambientColor = Color.Black.copy(alpha = 0.03f),
            )
            .clip(CardShape)
            .background(GlassBg)
            .border(1.dp, GlassBorder, CardShape),
    ) {
        content()
    }
}

@Composable
private fun StatCell(
    value: String,
    label: String,
    icon: ImageVector?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    )
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 62.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    sublabel: String?,
    onClick: () -> Unit,
    tint: Color = Color.Black,
    showChevron: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(IconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (!showChevron) tint else MaterialTheme.colorScheme.onSurface,
            )
            if (sublabel != null) {
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
