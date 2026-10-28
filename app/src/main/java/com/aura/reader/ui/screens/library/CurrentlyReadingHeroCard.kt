package com.aura.reader.ui.screens.library

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.aura.reader.data.model.Book
import com.aura.reader.ui.components.liquidGlassMenuAppearance
import com.aura.reader.ui.theme.LocalAppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CurrentlyReadingHeroCard(
    book: Book,
    onBookClick: (Book) -> Unit,
    onDelete: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onAddToCollection: () -> Unit = {},
    onSetProgress: (Int) -> Unit = {},
    onCoverClick: (Book) -> Unit = {},
    onShare: () -> Unit = {},
    onChangeCover: () -> Unit = {},
    isGlass: Boolean = false,
    isDark: Boolean = isSystemInDarkTheme(),
    clarity: Float = 0.85f,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var showMenu by remember { mutableStateOf(false) }

    val hasCover = !book.coverBase64.isNullOrBlank()

    // Dynamic color extraction via Palette API (Offloaded entirely to Dispatchers.Default with downscaled thumbnail for 120fps smoothness)
    var dominantColor by remember { mutableStateOf<Color?>(null) }
    var accentColor by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(book.coverBase64) {
        val base64 = book.coverBase64
        if (!base64.isNullOrBlank()) {
            withContext(Dispatchers.Default) {
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = 8
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }
                    val thumb = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                    if (thumb != null) {
                        val palette = Palette.from(thumb).maximumColorCount(12).generate()
                        val swatch = palette.darkVibrantSwatch
                            ?: palette.dominantSwatch
                            ?: palette.mutedSwatch
                        val vibrant = palette.vibrantSwatch ?: palette.lightVibrantSwatch

                        if (swatch != null) dominantColor = Color(swatch.rgb)
                        if (vibrant != null) accentColor = Color(vibrant.rgb)
                    }
                } catch (e: Exception) {
                    // fallback
                }
            }
        }
    }

    val holidaySeason = com.aura.reader.data.model.LocalHolidaySeason.current
    val isFestive = holidaySeason != com.aura.reader.data.model.HolidaySeason.NONE

    val primaryAccent = if (isFestive) {
        holidaySeason.primaryAccent
    } else {
        accentColor ?: MaterialTheme.colorScheme.primary
    }
    val cardBg = if (isFestive) {
        holidaySeason.primaryAccent
    } else {
        dominantColor ?: MaterialTheme.colorScheme.primaryContainer
    }
    val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black

    val heroBorder = if (isFestive) {
        BorderStroke(
            1.2.dp,
            holidaySeason.primaryAccent.copy(alpha = if (isDark) 0.38f else 0.28f)
        )
    } else if (isGlass) {
        BorderStroke(
            0.5.dp,
            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
        )
    } else null

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGlass) {
                if (isDark) {
                    if (isAmoled) Color(0xF2121316)
                    else Color(0xEE181A20)
                } else Color(0xF6F7FA)
            } else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = heroBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isGlass) 0.dp else 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onBookClick(book) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            cardBg.copy(alpha = if (isDark) 0.32f else 0.10f),
                            cardBg.copy(alpha = if (isDark) 0.10f else 0.03f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large cover with ambient shadow, shared transition element and cover inspection click
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = cardBg.copy(alpha = 0.6f),
                            spotColor = cardBg
                        )
                ) {
                    val hasCover = !book.coverBase64.isNullOrBlank()
                    BookCoverView(
                        coverBase64 = book.coverBase64,
                        title = book.title,
                        bookId = book.id,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(width = 88.dp, height = 130.dp)
                            .clickable { if (hasCover) onCoverClick(book) else onChangeCover() }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Header Row: Badge + Favorite & 3-dots Menu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val holidaySeason = com.aura.reader.data.model.LocalHolidaySeason.current
                            val festiveBadgeColor = if (holidaySeason != com.aura.reader.data.model.HolidaySeason.NONE) {
                                holidaySeason.primaryAccent
                            } else {
                                primaryAccent
                            }
                            val festiveBadgeText = if (holidaySeason != com.aura.reader.data.model.HolidaySeason.NONE) {
                                "${holidaySeason.iconEmoji} ${strings.currentlyReadingBadge}"
                            } else {
                                strings.currentlyReadingBadge
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = festiveBadgeColor.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = festiveBadgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = festiveBadgeColor,
                                    letterSpacing = 0.08.sp,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = book.format.name.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Favorite toggle
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (book.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (book.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // 3-dots Menu
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                val heroMenuAppearance = liquidGlassMenuAppearance(isGlass, isDark)
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    shape = RoundedCornerShape(20.dp),
                                    containerColor = heroMenuAppearance.containerColor,
                                    border = heroMenuAppearance.border,
                                    shadowElevation = heroMenuAppearance.shadowElevation
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(strings.shareBookFile) },
                                        leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            showMenu = false
                                            onShare()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (hasCover) strings.changeCover else strings.downloadCover) },
                                        leadingIcon = { Icon(if (hasCover) Icons.Rounded.Image else Icons.Rounded.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            showMenu = false
                                            onChangeCover()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.addToCollection) },
                                        leadingIcon = { Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            showMenu = false
                                            onAddToCollection()
                                        }
                                    )
                                    if (book.progressPercent < 100) {
                                        DropdownMenuItem(
                                            text = { Text(strings.markAsFinished) },
                                            leadingIcon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            onClick = {
                                                showMenu = false
                                                onSetProgress(100)
                                            }
                                        )
                                    }
                                    if (book.progressPercent > 0) {
                                        DropdownMenuItem(
                                            text = { Text(strings.resetProgress) },
                                            leadingIcon = { Icon(Icons.Rounded.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                            onClick = {
                                                showMenu = false
                                                onSetProgress(0)
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text(strings.delete, color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showMenu = false
                                            onDelete()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    if (isFestive) {
                        Text(
                            text = holidaySeason.greetingText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = primaryAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar + percentage
                    val progressPercent = book.progressPercent.coerceIn(0, 100)
                    val progressFraction = progressPercent / 100f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = primaryAccent,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (progressPercent >= 100) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(15.dp).padding(end = 3.dp)
                                )
                            }
                            Text(
                                text = "$progressPercent%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (progressPercent >= 100) Color(0xFFFFB300) else primaryAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Continue reading button
                    Button(
                        onClick = { onBookClick(book) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryAccent
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 14.dp,
                            vertical = 6.dp
                        ),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.continueReadingAction,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
