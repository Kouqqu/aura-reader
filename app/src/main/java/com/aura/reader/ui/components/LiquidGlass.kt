package com.aura.reader.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.ui.theme.LocalAppStrings

/**
 * Floating Navigation Island dock matching Apple / Mornye Liquid Glass design
 * and Google Material 3 Expressive styling.
 */
@Composable
fun FloatingNavigationIsland(
    designStyle: AppDesignStyle,
    onMyBooksClick: () -> Unit,
    onAddBookClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isDark = isSystemInDarkTheme()
    val isGlass = designStyle == AppDesignStyle.LIQUID_GLASS

    // Liquid Glass vs Material 3 container colors
    val containerColor = if (isGlass) {
        if (isDark) Color(0xB81C1E26) else Color(0xD6FFFFFF)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f)
    }

    // Specular highlight rim border for Liquid Glass
    val borderBrush = if (isGlass) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.38f else 0.82f),
                Color.White.copy(alpha = if (isDark) 0.08f else 0.22f)
            )
        )
    } else {
        SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    val shadowElevation = if (isGlass) 16.dp else 8.dp

    Surface(
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(if (isGlass) 1.2.dp else 1.dp, borderBrush),
        shadowElevation = shadowElevation,
        modifier = modifier
            .height(58.dp)
            .padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tab 1: Книги (Library)
            Surface(
                onClick = onMyBooksClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isGlass) 0.55f else 0.85f),
                modifier = Modifier
                    .height(44.dp)
                    .expressiveBounce(remember { MutableInteractionSource() })
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = strings.navMyBooks,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.navMyBooks,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Tab 2: Добавить (+) (Hero Action Button)
            Surface(
                onClick = onAddBookClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = if (isGlass) 6.dp else 4.dp,
                modifier = Modifier
                    .size(44.dp)
                    .expressiveBounce(remember { MutableInteractionSource() })
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = strings.addBooks,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Tab 3: Настройки (Settings)
            Surface(
                onClick = onSettingsClick,
                shape = CircleShape,
                color = if (isGlass) {
                    Color.White.copy(alpha = if (isDark) 0.08f else 0.35f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier
                    .height(44.dp)
                    .expressiveBounce(remember { MutableInteractionSource() })
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = strings.settingsTitle,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.settingsTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
