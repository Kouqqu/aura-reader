package com.aura.reader.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

// The bevel-normal / Snell refraction model is adapted from liquid_glass_easy
// 5.0.0 by Ahmed Gamil (MIT), used by Mornye. Attribution and license are shipped
// in assets/licenses/liquid_glass_easy.txt. No lighting/tint is added here.
internal const val DOCK_REFRACTION_AGSL = """
    uniform shader composable;
    uniform float2 uCenter;
    uniform float2 uHalfSize;
    uniform float uRadius;
    uniform float uRefractionWidth;
    uniform float uRefractiveIndex;
    uniform float uRayTravel;
    uniform float uIntensity;

    half4 main(float2 fragCoord) {
        if (uIntensity <= 0.005) return composable.eval(fragCoord);

        // Exact capsule gradient: the same outline that clips the selection.
        float2 p = fragCoord - uCenter;
        float spineLimit = max(0.0, uHalfSize.x - uRadius);
        float2 delta = p - float2(clamp(p.x, -spineLimit, spineLimit), 0.0);
        float radialDistance = length(delta);
        float inside = uRadius - radialDistance;
        float band = max(1.0, min(uRefractionWidth, uRadius));
        if (inside <= 0.0 || inside >= band) return composable.eval(fragCoord);

        // A curved bevel faces the viewer at its inner end and turns outward
        // at the silhouette. Keep both axes of the true surface normal.
        float h = clamp(inside / band, 0.0, 1.0);
        float2 normal2D = delta / max(radialDistance, 0.001);
        float bulge = sqrt(max(1.0 - h * h, 0.0));
        float3 surfaceNormal = normalize(float3(normal2D * bulge, h));
        float3 ray = refract(float3(0.0, 0.0, -1.0), surfaceNormal,
                             1.0 / max(uRefractiveIndex, 1.0));
        float travel = uRayTravel * (1.0 - h) * clamp(uIntensity, 0.0, 1.0);

        // Sample the uncut icon row; the caller clips the result AFTER optics.
        // Preserve premultiplied color/alpha together, without artificial glints
        // or separate RGB offsets that can leave fringes on transparent glyphs.
        return composable.eval(fragCoord + ray.xy * travel);
    }
"""

@Composable
fun Modifier.liquidRefraction(
    centerPx: Offset,
    sizePx: Size,
    radiusPx: Float,
    refractiveIndex: Float = 1.018f,
    refractionWidthPx: Float = 24f,
    intensity: Float = 1f,
    blurRadiusPx: Float = 0f
): Modifier {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return this
    val rayTravelPx = with(LocalDensity.current) { 70.dp.toPx() }
    return dockRefractionLayer(centerPx, sizePx, radiusPx, refractiveIndex,
        refractionWidthPx, rayTravelPx, intensity, blurRadiusPx)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Modifier.dockRefractionLayer(
    centerPx: Offset,
    sizePx: Size,
    radiusPx: Float,
    refractiveIndex: Float,
    refractionWidthPx: Float,
    rayTravelPx: Float,
    intensity: Float,
    blurRadiusPx: Float
): Modifier {
    // Compile once per mounted dock, including while resting. Animation frames
    // rebind uniform snapshots; a new gesture does not compile a fresh shader.
    val shader = remember {
        try { RuntimeShader(DOCK_REFRACTION_AGSL) }
        catch (error: Exception) {
            android.util.Log.e("LiquidGlass", "Dock refraction unavailable", error)
            null
        }
    }
    return graphicsLayer {
        if (shader != null && intensity > 0.005f) {
            shader.setFloatUniform("uCenter", centerPx.x, centerPx.y)
            shader.setFloatUniform("uHalfSize", sizePx.width * 0.5f, sizePx.height * 0.5f)
            shader.setFloatUniform("uRadius", radiusPx.coerceAtLeast(1f))
            shader.setFloatUniform("uRefractionWidth", refractionWidthPx.coerceAtLeast(1f))
            shader.setFloatUniform("uRefractiveIndex", refractiveIndex.coerceIn(1f, 2f))
            shader.setFloatUniform("uRayTravel", rayTravelPx)
            shader.setFloatUniform("uIntensity", intensity.coerceIn(0f, 1f))
            val shaderEffect = RenderEffect.createRuntimeShaderEffect(shader, "composable")
            renderEffect = if (blurRadiusPx > 0.5f) {
                val blur = (blurRadiusPx * intensity).coerceAtLeast(1f)
                val blurEffect = RenderEffect.createBlurEffect(blur, blur, android.graphics.Shader.TileMode.CLAMP)
                RenderEffect.createChainEffect(shaderEffect, blurEffect).asComposeRenderEffect()
            } else {
                shaderEffect.asComposeRenderEffect()
            }
        } else renderEffect = null
    }
}
