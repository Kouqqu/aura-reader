package com.aura.reader

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.ui.components.FloatingNavigationIsland
import com.aura.reader.ui.components.liquidRefraction
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 33)
@SuppressLint("NewApi")
class DockRefractionTest {
    @get:Rule val compose = createComposeRule()

    @Composable private fun Pattern(legacy: Boolean, intensity: Float, plain: Boolean = false) {
        val density = LocalDensity.current.density
        val center = Offset(140f * density, 60f * density)
        val size = Size(180f * density, 70f * density)
        val base = Modifier.width(280.dp).height(120.dp)
        val optics = if (legacy) {
            val shader = remember {
                val source = InstrumentationRegistry.getInstrumentation().context.assets
                    .open("legacy_dock_refraction.agsl").bufferedReader().use { it.readText() }
                RuntimeShader(source)
            }
            base.graphicsLayer {
                shader.setFloatUniform("uCenter", center.x, center.y)
                shader.setFloatUniform("uHalfSize", size.width / 2, size.height / 2)
                shader.setFloatUniform("uRadius", 35f * density)
                shader.setFloatUniform("uRefractionDepth", 0.22f)
                shader.setFloatUniform("uRefractionWidth", 18f * density)
                shader.setFloatUniform("uDispersion", 1.15f)
                shader.setFloatUniform("uIntensity", intensity)
                renderEffect = if (intensity > 0f) RenderEffect.createRuntimeShaderEffect(shader, "composable").asComposeRenderEffect() else null
            }
        } else base.liquidRefraction(center, size, 35f * density,
            refractiveIndex = 1.018f, refractionWidthPx = 18f * density, intensity = intensity)
        // Capture the parent so the child render layer (including its effect)
        // is sampled, rather than a recording of the pre-effect Canvas.
        Box(Modifier.testTag("pattern")) {
        Canvas(optics) {
            drawRect(if (plain) Color(0xFF344C72) else Color.White)
            if (!plain) {
                val step = 6f * density
                var x = 0f
                while (x < this.size.width) {
                    drawLine(Color.Black, Offset(x, 0f), Offset(x, this.size.height), density)
                    x += step
                }
                var y = 0f
                while (y < this.size.height) {
                    drawLine(Color.Black, Offset(0f, y), Offset(this.size.width, y), density)
                    y += step
                }
            }
        }
        }
    }

    private fun snapshot(): ImageBitmap {
        compose.waitForIdle()
        return compose.onNodeWithTag("pattern").captureToImage()
    }

    private fun changed(a: ImageBitmap, b: ImageBitmap, left: Float, top: Float, right: Float, bottom: Float): Int {
        val first = a.toPixelMap()
        val second = b.toPixelMap()
        val scale = a.width / 280f
        var count = 0
        for (y in (top * scale).toInt() until (bottom * scale).toInt()) {
            for (x in (left * scale).toInt() until (right * scale).toInt()) {
                val c = first[x, y]; val d = second[x, y]
                if (abs(c.red - d.red) + abs(c.green - d.green) + abs(c.blue - d.blue) + abs(c.alpha - d.alpha) > 0.025f) count++
            }
        }
        return count
    }

    @Test fun refractionBendsHorizontalAndVerticalEdgesButKeepsCenterAndOutsideStable() {
        var strength by mutableFloatStateOf(0f)
        compose.setContent { Pattern(false, strength) }
        val baseline = snapshot()
        compose.runOnIdle { strength = 1f }
        val bent = snapshot()
        assertTrue("Horizontal bevel must refract", changed(baseline, bent, 51f, 52f, 67f, 68f) > 20)
        assertTrue("Vertical bevel must refract", changed(baseline, bent, 120f, 26f, 160f, 42f) > 20)
        assertEquals("Lens center must stay unchanged", 0, changed(baseline, bent, 110f, 53f, 170f, 67f))
        assertEquals("Outside must stay unchanged", 0, changed(baseline, bent, 0f, 0f, 280f, 20f))
    }

    @Test fun refractionAddsNoGlintOrTintToUniformContent() {
        var strength by mutableFloatStateOf(0f)
        compose.setContent { Pattern(false, strength, plain = true) }
        val baseline = snapshot()
        compose.runOnIdle { strength = 1f }
        assertEquals(0, changed(baseline, snapshot(), 0f, 0f, 280f, 120f))
    }

    @Test fun newOpticsDiffersFromLegacyStretchWhileRestingAppearanceMatches() {
        var legacy by mutableStateOf(true)
        var strength by mutableFloatStateOf(0f)
        compose.setContent { Pattern(legacy, strength) }
        val oldRest = snapshot()
        compose.runOnIdle { legacy = false }
        assertEquals(0, changed(oldRest, snapshot(), 0f, 0f, 280f, 120f))
        compose.runOnIdle { legacy = true; strength = 1f }
        val oldHeld = snapshot()
        compose.runOnIdle { legacy = false }
        val newHeld = snapshot()
        assertTrue(changed(oldHeld, newHeld, 51f, 52f, 67f, 68f) > 20)
        assertTrue(changed(oldHeld, newHeld, 120f, 26f, 160f, 42f) > 20)
    }

    @Test fun dockTapAndDragStillSelectDestinations() {
        var selected by mutableIntStateOf(0)
        compose.setContent {
            MaterialTheme {
                FloatingNavigationIsland(AppDesignStyle.LIQUID_GLASS, selected,
                    onMyBooksClick = { selected = 0 }, onSearchClick = { selected = 1 },
                    onSettingsClick = { selected = 2 }, modifier = Modifier.testTag("dock"))
            }
        }
        compose.onNodeWithTag("dock").performTouchInput { click(Offset(width * 5f / 6f, height / 2f)) }
        compose.runOnIdle { assertEquals(2, selected) }
        compose.onNodeWithTag("dock").performTouchInput {
            swipe(Offset(width * 5f / 6f, height / 2f), Offset(width / 6f, height / 2f), durationMillis = 600)
        }
        compose.runOnIdle { assertEquals(0, selected) }
    }
}
