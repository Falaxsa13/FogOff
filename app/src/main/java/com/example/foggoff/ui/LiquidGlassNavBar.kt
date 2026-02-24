package com.example.foggoff.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val PillShape = RoundedCornerShape(28.dp)
private val IndicatorShape = RoundedCornerShape(18.dp)

// Light-glass palette: frosted bar on light maps
private val BarBg = Color.White.copy(alpha = 0.82f)
private val BarBorder = Color.Black.copy(alpha = 0.06f)
private val IndicatorBg = Color.Black.copy(alpha = 0.08f)
private val SelectedColor = Color(0xFF1A1A1A)
private val UnselectedColor = Color(0xFF5C5C5C)

@Composable
fun LiquidGlassNavBar(
    selectedIndex: Int,
    tabs: List<Pair<String, ImageVector>>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animate the indicator position as a fraction of tab count
    val indicatorFraction by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "indicatorSlide",
    )

    BoxWithConstraints(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = PillShape,
                spotColor = Color.Black.copy(alpha = 0.12f),
                ambientColor = Color.Black.copy(alpha = 0.08f),
            )
            .clip(PillShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.Transparent,
                    ),
                )
            )
            .background(BarBg)
            .border(1.dp, BarBorder, PillShape),
    ) {
        val tabWidth: Dp = maxWidth / tabs.size
        val indicatorHPad = 10.dp
        val indicatorVPad = 6.dp

        // Sliding pill indicator
        Box(
            modifier = Modifier
                .offset(x = tabWidth * indicatorFraction + indicatorHPad)
                .padding(vertical = indicatorVPad)
                .width(tabWidth - indicatorHPad * 2)
                .height(52.dp)
                .clip(IndicatorShape)
                .background(IndicatorBg),
        )

        // Tab items
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, (label, icon) ->
                val selected = selectedIndex == index

                val iconColor by animateColorAsState(
                    targetValue = if (selected) SelectedColor else UnselectedColor,
                    animationSpec = tween(220),
                    label = "iconColor$index",
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) SelectedColor else UnselectedColor,
                    animationSpec = tween(220),
                    label = "textColor$index",
                )
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.12f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "iconScale$index",
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onTabSelected(index) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier
                            .scale(iconScale)
                            .size(22.dp),
                        tint = iconColor,
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                        color = textColor,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
