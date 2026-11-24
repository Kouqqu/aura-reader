package com.aura.reader.ui.screens.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import com.aura.reader.ui.components.SyncBottomSheetSystemBars
import com.aura.reader.ui.components.jellyOverscroll
import com.aura.reader.ui.components.sheetScrollIsolation
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.BlockType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersBottomSheet(
    chapters: List<Chapter>,
    currentChapterIndex: Int,
    onChapterSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentChapterIndex.coerceIn(0, (chapters.size - 1).coerceAtLeast(0)))
    var collapsedParents by remember { mutableStateOf(setOf<Int>()) }

    val hasChildren = remember(chapters) {
        chapters.indices.map { i ->
            i + 1 < chapters.size && chapters[i + 1].depth > chapters[i].depth
        }
    }
    val visibleIndices = remember(chapters, collapsedParents) {
        val result = mutableListOf<Int>()
        var skipUntilDepth: Int? = null
        for (i in chapters.indices) {
            val d = chapters[i].depth
            if (skipUntilDepth != null) {
                if (d > skipUntilDepth) continue else skipUntilDepth = null
            }
            result.add(i)
            if (i in collapsedParents && hasChildren.getOrElse(i) { false }) {
                skipUntilDepth = d
            }
        }
        result
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 24.dp)
    ) {
        SyncBottomSheetSystemBars()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            val isSinglePageChapters = remember(chapters) {
                chapters.isNotEmpty() && chapters.all { it.blocks.size == 1 && it.blocks[0].type == BlockType.IMAGE }
            }
            Text(
                text = if (isSinglePageChapters) "Страницы (${chapters.size})" else "Оглавление (${chapters.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .jellyOverscroll(allowTopBounce = false)
                    .weight(1f, fill = false),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                itemsIndexed(visibleIndices, key = { _, chIdx -> chapters[chIdx].id }) { _, index ->
                    val chapter = chapters[index]
                    val isSelected = index == currentChapterIndex
                    val indentDp = (chapter.depth.coerceIn(0, 4) * 16).dp
                    val isParent = hasChildren.getOrElse(index) { false }
                    val isCollapsed = index in collapsedParents

                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = indentDp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onChapterSelected(index)
                                onDismiss()
                            },
                        headlineContent = {
                            Text(
                                text = chapter.title.ifBlank { "Глава ${index + 1}" },
                                style = if (chapter.depth == 0) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else if (chapter.depth == 0) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingContent = if (isSelected) {
                            {
                                Icon(
                                    Icons.Rounded.Bookmark,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null,
                        trailingContent = if (isParent) {
                            {
                                IconButton(onClick = {
                                    collapsedParents = if (isCollapsed) collapsedParents - index else collapsedParents + index
                                }) {
                                    Icon(
                                        imageVector = if (isCollapsed) Icons.Rounded.ExpandMore else Icons.Rounded.ExpandLess,
                                        contentDescription = null
                                    )
                                }
                            }
                        } else null,
                        colors = ListItemDefaults.colors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                        )
                    )
                }
            }
        }
    }
}
