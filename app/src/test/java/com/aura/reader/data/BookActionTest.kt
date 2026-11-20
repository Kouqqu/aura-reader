package com.aura.reader.data

import com.aura.reader.data.model.BookAction
import com.aura.reader.data.model.BookTarget
import org.junit.Assert.*
import org.junit.Test

class BookActionTest {

    @Test
    fun singleTargetExposesSingleId() {
        val target = BookTarget.Single("book-123")
        assertEquals(setOf("book-123"), target.bookIds)
    }

    @Test
    fun multipleTargetExposesAllIds() {
        val ids = setOf("id-1", "id-2", "id-3")
        val target = BookTarget.Multiple(ids)
        assertEquals(ids, target.bookIds)
    }

    @Test
    fun actionEqualityAndTypes() {
        assertEquals(BookAction.MoveToTrash, BookAction.MoveToTrash)
        assertEquals(BookAction.RestoreFromTrash, BookAction.RestoreFromTrash)
        assertEquals(BookAction.DeletePermanently, BookAction.DeletePermanently)
        assertEquals(BookAction.SetProgress(75), BookAction.SetProgress(75))
        assertNotEquals(BookAction.SetProgress(50), BookAction.SetProgress(75))
        assertEquals(BookAction.AddToCollection("Фантастика"), BookAction.AddToCollection("Фантастика"))
        assertEquals(BookAction.TogglePrivate(true), BookAction.TogglePrivate(true))
        assertNotEquals(BookAction.TogglePrivate(true), BookAction.TogglePrivate(false))
    }
}
