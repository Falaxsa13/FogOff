package com.example.foggoff.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foggoff.data.LeaderboardEntry

private val GlassShape = RoundedCornerShape(24.dp)
private val RowShape = RoundedCornerShape(20.dp)
private val GlassBg = Color.White.copy(alpha = 0.82f)
private val GlassBorder = Color.Black.copy(alpha = 0.06f)
private val HighlightBg = Color(0xFF0F172A).copy(alpha = 0.84f)
private val HighlightBorder = Color.White.copy(alpha = 0.18f)

@Composable
fun FriendsScreen(
    modifier: Modifier = Modifier,
    viewModel: FriendsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.loading -> {
            Row(
                modifier = modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    GlassHeaderCard(modifier = Modifier.padding(top = 8.dp))
                }

                itemsIndexed(uiState.top20) { index, entry ->
                    LeaderboardRow(
                        rank = index + 1,
                        entry = entry,
                        highlight = uiState.currentUserEntry?.uid == entry.uid,
                    )
                }

                item {
                    val rank = uiState.currentUserRank
                    val entry = uiState.currentUserEntry
                    val errorMessage = uiState.errorMessage
                    if (rank != null && entry != null) {
                        Text(
                            text = "Your rank",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 12.dp, start = 4.dp),
                        )
                        LeaderboardRow(
                            rank = rank,
                            entry = entry,
                            highlight = true,
                        )
                    } else if (errorMessage != null) {
                        GlassContainer(modifier = Modifier.padding(bottom = 24.dp)) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: LeaderboardEntry,
    highlight: Boolean,
) {
    val rowBg = if (highlight) HighlightBg else GlassBg
    val rowBorder = if (highlight) HighlightBorder else GlassBorder
    val nameColor = if (highlight) Color.White else MaterialTheme.colorScheme.onSurface
    val subColor = if (highlight) Color.White.copy(alpha = 0.72f) else MaterialTheme.colorScheme.onSurfaceVariant
    val scoreBg = if (highlight) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.07f)
    val scoreColor = if (highlight) Color.White else Color(0xFF111827)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RowShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .clip(RowShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent)
                    )
                )
                .background(rowBg)
                .border(1.dp, rowBorder, RowShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RankBadge(rank = rank, highlight = highlight)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = entry.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = nameColor,
                        )
                        Text(
                            text = "ID ${entry.uid.take(8)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = subColor,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(scoreBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${entry.unlockedHexes}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun RankBadge(rank: Int, highlight: Boolean) {
    val bg = when {
        highlight -> Color.White.copy(alpha = 0.20f)
        rank == 1 -> Color(0xFFFFE082)
        rank == 2 -> Color(0xFFE5E7EB)
        rank == 3 -> Color(0xFFF0B27A)
        else -> Color.Black.copy(alpha = 0.06f)
    }
    val textColor = if (highlight) Color.White else Color(0xFF111827)
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}

@Composable
private fun GlassHeaderCard(modifier: Modifier = Modifier) {
    GlassContainer(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Global Leaderboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Top 20 explorers ranked by unlocked hexes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GlassContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = GlassShape,
                spotColor = Color.Black.copy(alpha = 0.10f),
                ambientColor = Color.Black.copy(alpha = 0.06f),
            )
            .clip(GlassShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                )
            )
            .background(GlassBg)
            .border(1.dp, GlassBorder, GlassShape),
    ) {
        content()
    }
}
