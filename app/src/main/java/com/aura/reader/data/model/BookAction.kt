package com.aura.reader.data.model

/**
 * Target selector for library book mutations, unifying single-card and batch multi-select operations.
 */
sealed interface BookTarget {
    data class Single(val bookId: String) : BookTarget {
        override val bookIds: Set<String> get() = setOf(bookId)
    }

    data class Multiple(override val bookIds: Set<String>) : BookTarget

    val bookIds: Set<String>
}

/**
 * Domain actions on books in the library.
 *
 * Dispatched through [com.aura.reader.ui.screens.library.LibraryViewModel.dispatchBookAction] to
 * ensure unified undo handling, state synchronization, and repository persistence.
 */
sealed interface BookAction {
    data object MoveToTrash : BookAction
    data object RestoreFromTrash : BookAction
    data object DeletePermanently : BookAction
    data class SetProgress(val progressPercent: Int) : BookAction
    data class AddToCollection(val collectionName: String) : BookAction
    data class TogglePrivate(val makePrivate: Boolean) : BookAction
}
