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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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
import java.util.Locale

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
    val countries by viewModel.unlockedCountries.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.userRefreshing.collectAsStateWithLifecycle()
    val countryKmByCode by viewModel.unlockedCountryKm.collectAsStateWithLifecycle()
    val user by authViewModel.currentUser.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    var showCountries by remember { mutableStateOf(false) }

    val displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Explorer"
    val avatarLetter = displayName.first().uppercaseChar().toString()
    val email = user?.email.orEmpty()

    LaunchedEffect(showCountries) {
        if (showCountries) viewModel.refresh()
    }

    if (showSettings) {
        SettingsScreen(
            authViewModel = authViewModel,
            onBack = { showSettings = false },
            modifier = modifier,
        )
        return
    }

    if (showCountries) {
        CountriesScreen(
            countries = countries,
            countryKmByCode = countryKmByCode,
            refreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            onBack = { showCountries = false },
            modifier = modifier,
        )
        return
    }

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refresh() },
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
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
                        icon = Icons.Outlined.Language,
                        label = "Countries",
                        sublabel = "Visited map areas",
                        onClick = { showCountries = true },
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

@Composable
private fun CountriesScreen(
    countries: List<String>,
    countryKmByCode: Map<String, Double>,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swipeState = rememberSwipeRefreshState(refreshing)

    SwipeRefresh(
        state = swipeState,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Text(
                    text = "Countries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                // Keep layout balanced with an invisible icon slot.
                Box(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Visited",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (countries.isEmpty()) {
                        Text(
                            text = "Unlock a few hexes to start collecting countries.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = "${countries.distinct().size} countries unlocked",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        countries.distinct().sorted().forEach { countryRaw ->
                            val iso2 = inferIso2(countryRaw)
                            val km = iso2?.let { countryKmByCode[it] } ?: 0.0
                            val displayName = iso2?.let { code ->
                                Locale("", code).displayCountry.takeIf { it.isNotBlank() } ?: countryRaw
                            } ?: countryRaw

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.65f))
                                    .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "${formatKm(km)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        if (iso2 != null) {
                                            CountryFlag(iso2 = iso2)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryFlag(iso2: String) {
    val emoji = iso2ToEmojiFlag(iso2)
    if (emoji != null) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun iso2ToEmojiFlag(iso2Raw: String): String? {
    val iso2 = iso2Raw.trim().uppercase(Locale.ROOT)
    if (iso2.length != 2) return null
    val c1 = iso2[0]
    val c2 = iso2[1]
    if (c1 !in 'A'..'Z' || c2 !in 'A'..'Z') return null

    fun regionalIndicator(letter: Char): Int = 0x1F1E6 + (letter.code - 'A'.code)
    val first = regionalIndicator(c1)
    val second = regionalIndicator(c2)
    return String(Character.toChars(first)) + String(Character.toChars(second))
}

private fun inferIso2(countryRaw: String): String? {
    val trimmed = countryRaw.trim()
    if (trimmed.length == 2 && trimmed.all { it in 'A'..'Z' || it in 'a'..'z' }) {
        return trimmed.uppercase(Locale.ROOT)
    }

    // If we stored a country name (not a code), infer ISO alpha-2 by matching to Locale displayCountry.
    val normalizedTarget = trimmed.replace(" ", "").uppercase(Locale.ROOT)

    for (code in Locale.getISOCountries()) {
        val localeName = Locale("", code).displayCountry ?: continue
        val normalizedCandidate = localeName.replace(" ", "").uppercase(Locale.ROOT)
        if (normalizedCandidate == normalizedTarget) return code

        // Fallback: allow direct case-insensitive match.
        if (localeName.equals(trimmed, ignoreCase = true)) return code
    }
    return null
}

private fun formatKm(km: Double): String {
    // Compact formatting: 12.3 -> "12.3", 12.0 -> "12"
    val formatted = String.format(Locale.US, "%.1f", km)
    return formatted.removeSuffix(".0")
}

// NOTE: no external flag library. Flags are rendered via unicode regional-indicator emojis.
