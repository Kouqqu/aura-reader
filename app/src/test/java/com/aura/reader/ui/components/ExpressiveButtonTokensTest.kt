package com.aura.reader.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins [ExpressiveButtonSize] to the values published in the M3 Expressive design token
 * tables, so a future "polish" pass cannot quietly drift away from the spec again.
 *
 * Token sources (m3.material.io, `designSystems/20543ce18892f7d9`):
 * - `md.comp.split-button.small.*`
 * - `md.comp.button.small.*`
 * - `md.comp.icon-button.small.*`
 * - `md.comp.button-group.connected.small.*`
 */
class ExpressiveButtonTokensTest {

    private val small = ExpressiveButtonSize.Small

    @Test
    fun splitButtonMatchesSmallTokenSet() {
        // md.comp.split-button.small.container.height
        assertEquals(40.dp, small.height)
        // md.comp.split-button.small.inner-corner.corner-size / .pressed|hovered
        assertEquals(4.dp, small.splitInnerCorner)
        assertEquals(12.dp, small.splitInnerCornerActive)
        // md.comp.split-button.small.leading-button.leading-space / .trailing-space
        assertEquals(16.dp, small.splitLeadingStart)
        assertEquals(12.dp, small.splitLeadingEnd)
        // 13 dp + 22 dp icon + 13 dp = 48 dp, and never below the 48 dp target area
        assertEquals(48.dp, small.splitTrailingWidth)
        assertEquals(22.dp, small.splitTrailingIcon)
        // md.comp.split-button.small.trailing-button.*-space
        assertEquals(small.splitTrailingWidth, small.splitTrailingIcon + 13.dp + 13.dp)
        // Menu icon optical offset when unselected: S = -1 dp
        assertEquals((-1).dp, small.splitIconOffset)
    }

    @Test
    fun labelButtonMatchesSmallTokenSet() {
        // md.comp.button.small.container.height / leading / trailing / icon-label-space
        assertEquals(40.dp, small.height)
        assertEquals(16.dp, small.containerSpace)
        assertEquals(8.dp, small.iconLabelSpace)
        // md.comp.button.small.icon.size
        assertEquals(20.dp, small.iconSize)
        // md.comp.button.small.container.shape.square / .pressed.container.shape
        assertEquals(12.dp, small.squareCorner)
        assertEquals(8.dp, small.pressedCorner)
    }

    @Test
    fun iconButtonMatchesSmallTokenSet() {
        // md.comp.icon-button.small.container.height / .default.*-space
        assertEquals(40.dp, small.height)
        assertEquals(24.dp, small.iconButtonIconSize)
        assertEquals(8.dp, small.iconButtonSpace)
        // icon + padding must fill the container exactly
        assertEquals(small.height, small.iconButtonIconSize + small.iconButtonSpace * 2)
    }

    @Test
    fun connectedGroupMatchesSmallTokenSet() {
        // md.comp.button-group.connected.small.container.height
        assertEquals(40.dp, small.height)
        // md.comp.button-group.connected.small.inner-corner / .pressed.inner-corner
        assertEquals(8.dp, small.groupInnerCorner)
        assertEquals(4.dp, small.groupInnerCornerPressed)
        // The outer shape of a round connected group is fully round.
        assertTrue(small.groupInnerCorner < small.height / 2)
        // Selected segments use a 50% inner corner, i.e. half the container height.
        assertEquals(20.dp, small.height / 2)
    }

    @Test
    fun iconButtonPaddingFillsEverySize() {
        for (size in ExpressiveButtonSize.entries) {
            assertEquals(
                "icon button ${size.name} must be icon + 2 * padding",
                size.height,
                size.iconButtonIconSize + size.iconButtonSpace * 2
            )
            assertTrue(
                "inner corners of ${size.name} must be smaller than the pill radius",
                size.groupInnerCorner < size.height / 2
            )
        }
    }
}
