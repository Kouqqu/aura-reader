package com.aura.reader.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.zIndex
import com.aura.reader.ui.components.BookCardMenu
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Refresh
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aura.reader.data.model.Book
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCard(
    book: Book,
    index: Int = 0,
    isGlass: Boolean = false,
    isDark: Boolean = isSystemInDarkTheme(),
    isReorderMode: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    wobbleAngle: Float = 0f,
    wobbleOffset: Float = 0f,
    currentlyDraggedBookId: String? = null,
    currentDragOffset: Offset = Offset.Zero,
    onDragStart: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    onAddToCollection: () -> Unit = {},
    onSetProgress: (Int) -> Unit = {},
    onCoverClick: () -> Unit = {},
    onShare: () -> Unit = {},
    onChangeCover: () -> Unit = {},
    isPrivate: Boolean = false,
    onTogglePrivate: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onRestore: () -> Unit = {},
    onDeletePermanently: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current
    val haptic = LocalHapticFeedback.current
    val hasCover = !book.coverBase64.isNullOrBlank()

    val isDraggingThis = currentlyDraggedBookId == book.id
    val rotationZ = if (isDraggingThis) 0f else if (isReorderMode) (if (index % 2 == 0) wobbleAngle else -wobbleAngle) else 0f
    val translationY = if (isDraggingThis) currentDragOffset.y else if (isReorderMode) (if (index % 2 == 0) wobbleOffset else -wobbleOffset) else 0f
    val translationX = if (isDraggingThis) currentDragOffset.x else 0f

    val cardInteractionSource = remember { MutableInteractionSource() }
    val isCardPressed by cardInteractionSource.collectIsPressedAsState()
    val bounceScale by animateFloatAsState(
        targetValue = if (isCardPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardBounce"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDraggingThis) 100f else 1f)
            .graphicsLayer {
                val currentScale = if (isDraggingThis) 1.05f else bounceScale
                this.scaleX = currentScale
                this.scaleY = currentScale
                this.rotationZ = rotationZ
                this.translationY = translationY
                this.translationX = translationX
                if (isDraggingThis) {
                    this.shadowElevation = 16f
                }
            }
            .pointerInput(book.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        onDragStart()
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                )
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = cardInteractionSource,
                indication = if (isGlass) null else LocalIndication.current
            ) {
                if (isSelectionMode) {
                    onToggleSelect()
                } else if (isReorderMode) {
                    showMenu = true
                } else {
                    onClick()
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGlass) {
                if (isDark) Color(0x281C1E26) else Color(0x65FFFFFF)
            } else if (isDraggingThis) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else if (isGlass) {
            BorderStroke(
                0.5.dp,
                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
            )
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .border(
                            1.5.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            val hasCover = !book.coverBase64.isNullOrBlank()
            BookCoverView(
                coverBase64 = book.coverBase64,
                title = book.title,
                bookId = book.id,
                modifier = Modifier
                    .size(width = 64.dp, height = 90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenProfile() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isGlass) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(end = 6.dp)
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
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (book.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = strings.colFavorites,
                                tint = if (book.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showMenu = !showMenu
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (showMenu) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.MoreVert,
                                contentDescription = strings.moreOptions,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (book.author.isNotBlank()) {
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (!book.series.isNullOrBlank()) {
                    val seriesText = if (book.seriesNumber != null) {
                        "${book.series} • #${book.seriesNumber}"
                    } else {
                        book.series.orEmpty()
                    }
                    Text(
                        text = seriesText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isGlass) {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                            ) {
                                Text(
                                    text = book.format.name.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = if (isDark) Color(0xFFD4D0DF) else Color(0xFF4A4656),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = strings.progressRead,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (book.progressPercent >= 100) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(14.dp).padding(end = 3.dp)
                                )
                            }
                            Text(
                                text = "${book.progressPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (book.progressPercent >= 100) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    LinearProgressIndicator(
                        progress = { (book.progressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.5.dp)
                            .clip(CircleShape),
                        strokeCap = StrokeCap.Round,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = if (isGlass) {
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                        } else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }
        }

        AnimatedVisibility(
                visible = showMenu,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut()
            ) {
                BookListInlineActionTray(
                    book = book,
                    isPrivate = isPrivate,
                    hasCover = hasCover,
                    isGlass = isGlass,
                    isDark = isDark,
                    onSelect = { showMenu = false; onToggleSelect() },
                    onOpenProfile = { showMenu = false; onOpenProfile() },
                    onToggleFavorite = { showMenu = false; onToggleFavorite() },
                    onAddToCollection = { showMenu = false; onAddToCollection() },
                    onTogglePrivate = { showMenu = false; onTogglePrivate() },
                    onChangeCover = { showMenu = false; onChangeCover() },
                    onShare = { showMenu = false; onShare() },
                    onSetProgress = { prog -> showMenu = false; onSetProgress(prog) },
                    onDelete = { showMenu = false; onDelete() },
                    onRestore = { showMenu = false; onRestore() },
                    onDeletePermanently = { showMenu = false; onDeletePermanently() }
                )
            }
        }
    }
}
@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun BookCoverView(
    coverBase64: String?,
    title: String,
    modifier: Modifier = Modifier,
    bookId: String? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(10.dp)
) {
    val sharedTransitionScope = com.aura.reader.ui.navigation.LocalSharedTransitionScope.current
    val animatedVisibilityScope = com.aura.reader.ui.navigation.LocalNavAnimatedVisibilityScope.current
    val sharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null && bookId != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = "book_cover_$bookId"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = 350, easing = FastOutSlowInEasing)
                },
                clipInOverlayDuringTransition = OverlayClip(shape)
            )
        }
    } else Modifier

    val finalModifier = Modifier
        .then(sharedModifier)
        .clip(shape)
        .then(modifier)

    val bitmap = remember(coverBase64) {
        if (!coverBase64.isNullOrBlank()) {
            com.aura.reader.util.CoverBitmapCache.getOrDecode(bookId ?: coverBase64, coverBase64)
        } else null
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = title,
            modifier = finalModifier,
            contentScale = ContentScale.Crop
        )
    } else {
        val strings = LocalAppStrings.current
        Box(
            modifier = finalModifier
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudDownload,
                    contentDescription = strings.downloadCover,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.downloadCover,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookGridCard(
    book: Book,
    index: Int = 0,
    isGlass: Boolean = false,
    isDark: Boolean = isSystemInDarkTheme(),
    isReorderMode: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    wobbleAngle: Float = 0f,
    wobbleOffset: Float = 0f,
    currentlyDraggedBookId: String? = null,
    currentDragOffset: Offset = Offset.Zero,
    onDragStart: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    onAddToCollection: () -> Unit = {},
    onSetProgress: (Int) -> Unit = {},
    onCoverClick: () -> Unit = {},
    onShare: () -> Unit = {},
    onChangeCover: () -> Unit = {},
    isPrivate: Boolean = false,
    onTogglePrivate: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onRestore: () -> Unit = {},
    onDeletePermanently: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current
    val haptic = LocalHapticFeedback.current
    val hasCover = !book.coverBase64.isNullOrBlank()

    val isDraggingThis = currentlyDraggedBookId == book.id
    val rotationZ = if (isDraggingThis) 0f else if (isReorderMode) (if (index % 2 == 0) wobbleAngle else -wobbleAngle) else 0f
    val translationY = if (isDraggingThis) currentDragOffset.y else if (isReorderMode) (if (index % 2 == 0) wobbleOffset else -wobbleOffset) else 0f
    val translationX = if (isDraggingThis) currentDragOffset.x else 0f

    val gridInteractionSource = remember { MutableInteractionSource() }
    val isGridPressed by gridInteractionSource.collectIsPressedAsState()
    val bounceScale by animateFloatAsState(
        targetValue = if (isGridPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "gridCardBounce"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (isDraggingThis) 100f else 1f)
            .graphicsLayer {
                val currentScale = if (isDraggingThis) 1.05f else bounceScale
                this.scaleX = currentScale
                this.scaleY = currentScale
                this.rotationZ = rotationZ
                this.translationY = translationY
                this.translationX = translationX
                if (isDraggingThis) {
                    this.shadowElevation = 16f
                }
            }
            .pointerInput(book.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        onDragStart()
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                )
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = gridInteractionSource,
                indication = if (isGlass) null else LocalIndication.current
            ) {
                if (isSelectionMode) {
                    onToggleSelect()
                } else if (isReorderMode) {
                    showMenu = true
                } else {
                    onClick()
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGlass) {
                if (isDark) Color(0x281C1E26) else Color(0x65FFFFFF)
            } else if (isDraggingThis) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else if (isGlass) {
            BorderStroke(
                0.5.dp,
                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
            )
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .animateContentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(184.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                val hasCover = !book.coverBase64.isNullOrBlank()
                BookCoverView(
                    coverBase64 = book.coverBase64,
                    title = book.title,
                    bookId = book.id,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onOpenProfile() }
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = book.format.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.55f))
                            .border(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.85f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (book.isFavorite) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Favorite,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(28.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = !showMenu
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (showMenu) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.MoreVert,
                                    contentDescription = strings.moreOptions,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                val prog = book.progressPercent.coerceIn(0, 100)
                if (prog > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            if (prog >= 100) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(13.dp).padding(end = 2.dp)
                                )
                            }
                            Text(
                                text = "$prog%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (prog >= 100) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (book.author.isNotBlank()) {
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (!book.series.isNullOrBlank()) {
                val seriesText = if (book.seriesNumber != null) {
                    "${book.series} • #${book.seriesNumber}"
                } else {
                    book.series.orEmpty()
                }
                Text(
                    text = seriesText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            AnimatedVisibility(
                visible = showMenu,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut()
            ) {
                BookGridInlineActionTray(
                    book = book,
                    isPrivate = isPrivate,
                    hasCover = hasCover,
                    isGlass = isGlass,
                    isDark = isDark,
                    onSelect = { showMenu = false; onToggleSelect() },
                    onOpenProfile = { showMenu = false; onOpenProfile() },
                    onToggleFavorite = { showMenu = false; onToggleFavorite() },
                    onAddToCollection = { showMenu = false; onAddToCollection() },
                    onTogglePrivate = { showMenu = false; onTogglePrivate() },
                    onChangeCover = { showMenu = false; onChangeCover() },
                    onShare = { showMenu = false; onShare() },
                    onSetProgress = { prog -> showMenu = false; onSetProgress(prog) },
                    onDelete = { showMenu = false; onDelete() },
                    onRestore = { showMenu = false; onRestore() },
                    onDeletePermanently = { showMenu = false; onDeletePermanently() }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookListInlineActionTray(
    book: Book,
    isPrivate: Boolean,
    hasCover: Boolean,
    isGlass: Boolean,
    isDark: Boolean,
    onSelect: () -> Unit = {},
    onOpenProfile: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToCollection: () -> Unit,
    onTogglePrivate: () -> Unit,
    onChangeCover: () -> Unit,
    onShare: () -> Unit,
    onSetProgress: (Int) -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isTrash = book.deletedAtTimestamp != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        HorizontalDivider(
            color = if (isGlass) {
                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            },
            thickness = 0.8.dp,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (!isTrash) {
                BookActionChip(
                    icon = Icons.Rounded.FolderOpen,
                    label = strings.addToCollection,
                    onClick = onAddToCollection,
                    tint = MaterialTheme.colorScheme.primary
                )

                BookActionChip(
                    icon = if (isPrivate) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                    label = if (isPrivate) strings.privateFolderRemoveBook else strings.privateFolderMoveToPrivate,
                    onClick = onTogglePrivate,
                    tint = MaterialTheme.colorScheme.secondary
                )

                if (book.progressPercent < 100) {
                    BookActionChip(
                        icon = Icons.Rounded.CheckCircle,
                        label = strings.markAsFinished,
                        onClick = { onSetProgress(100) },
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                if (book.progressPercent > 0) {
                    BookActionChip(
                        icon = Icons.Rounded.Refresh,
                        label = strings.resetProgress,
                        onClick = { onSetProgress(0) }
                    )
                }

                BookActionChip(
                    icon = Icons.Rounded.Share,
                    label = null,
                    contentDescription = strings.shareBookFile,
                    onClick = onShare
                )

                BookActionChip(
                    icon = Icons.Rounded.Delete,
                    label = null,
                    contentDescription = strings.moveToTrash,
                    onClick = onDelete,
                    tint = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                )
            } else {
                BookActionChip(
                    icon = Icons.Rounded.Restore,
                    label = strings.restoreFromTrash,
                    onClick = onRestore,
                    tint = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
                BookActionChip(
                    icon = Icons.Rounded.DeleteForever,
                    label = null,
                    contentDescription = strings.deletePermanently,
                    onClick = onDeletePermanently,
                    tint = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun BookGridInlineActionTray(
    book: Book,
    isPrivate: Boolean,
    hasCover: Boolean,
    isGlass: Boolean,
    isDark: Boolean,
    onSelect: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onAddToCollection: () -> Unit,
    onTogglePrivate: () -> Unit,
    onChangeCover: () -> Unit = {},
    onShare: () -> Unit,
    onSetProgress: (Int) -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isTrash = book.deletedAtTimestamp != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
    ) {
        HorizontalDivider(
            color = if (isGlass) {
                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            },
            thickness = 0.8.dp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        if (!isTrash) {
            GridMenuRow(
                icon = Icons.Rounded.FolderOpen,
                text = strings.addToCollection,
                onClick = onAddToCollection
            )
            GridMenuRow(
                icon = if (isPrivate) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                text = if (isPrivate) strings.privateFolderRemoveBook else strings.privateFolderMoveToPrivate,
                onClick = onTogglePrivate
            )
            if (book.progressPercent < 100) {
                GridMenuRow(
                    icon = Icons.Rounded.CheckCircle,
                    text = strings.markAsFinished,
                    onClick = { onSetProgress(100) },
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
            if (book.progressPercent > 0) {
                GridMenuRow(
                    icon = Icons.Rounded.Refresh,
                    text = strings.resetProgress,
                    onClick = { onSetProgress(0) }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onShare,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = strings.shareBookFile,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Surface(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = strings.moveToTrash,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            GridMenuRow(
                icon = Icons.Rounded.Restore,
                text = strings.restoreFromTrash,
                onClick = onRestore,
                tint = MaterialTheme.colorScheme.primary
            )
            GridMenuRow(
                icon = Icons.Rounded.DeleteForever,
                text = strings.deletePermanently,
                onClick = onDeletePermanently,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun BookActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = label,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = modifier.heightIn(min = 34.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (!label.isNullOrBlank()) 10.dp else 9.dp,
                vertical = 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            if (!label.isNullOrBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = tint,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun GridMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
