package com.aura.reader.data

import com.aura.reader.data.model.AndroidColorPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidColorPresetTest {

    @Test
    fun `fromId returns correct preset case-insensitively`() {
        assertEquals(AndroidColorPreset.PURPLE, AndroidColorPreset.fromId("PURPLE"))
        assertEquals(AndroidColorPreset.PURPLE, AndroidColorPreset.fromId("purple"))
        assertEquals(AndroidColorPreset.BLUE, AndroidColorPreset.fromId("Blue"))
        assertEquals(AndroidColorPreset.TEAL, AndroidColorPreset.fromId("teal"))
        assertEquals(AndroidColorPreset.GREEN, AndroidColorPreset.fromId("green"))
        assertEquals(AndroidColorPreset.ORANGE, AndroidColorPreset.fromId("orange"))
        assertEquals(AndroidColorPreset.ROSE, AndroidColorPreset.fromId("rose"))
        assertEquals(AndroidColorPreset.INDIGO, AndroidColorPreset.fromId("indigo"))
        assertEquals(AndroidColorPreset.NEUTRAL, AndroidColorPreset.fromId("neutral"))
    }

    @Test
    fun `fromId fallback to PURPLE on null, blank or invalid input`() {
        assertEquals(AndroidColorPreset.PURPLE, AndroidColorPreset.fromId(null))
        assertEquals(AndroidColorPreset.PURPLE, AndroidColorPreset.fromId(""))
        assertEquals(AndroidColorPreset.PURPLE, AndroidColorPreset.fromId("   "))
        assertEquals(AndroidColorPreset.PURPLE, AndroidColorPreset.fromId("UNKNOWN_COLOR"))
    }

    @Test
    fun `all presets have distinct IDs and swatch colors`() {
        val allPresets = AndroidColorPreset.entries
        val uniqueIds = allPresets.map { it.id }.toSet()
        assertEquals(allPresets.size, uniqueIds.size)

        allPresets.forEach { preset ->
            assertNotNull(preset.swatchColor)
            assertNotNull(preset.lightPrimary)
            assertNotNull(preset.lightPrimaryContainer)
            assertNotNull(preset.darkPrimary)
            assertNotNull(preset.darkPrimaryContainer)
        }
    }
}
