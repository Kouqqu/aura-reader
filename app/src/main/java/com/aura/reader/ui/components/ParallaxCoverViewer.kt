package com.aura.reader.ui.components

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import android.content.Context
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.OpdsBook
import com.aura.reader.ui.theme.LocalAppStrings

@Composable
fun ParallaxCoverViewer(
    book: Book,
    onDismiss: () -> Unit
) {
    ParallaxCoverViewer(
        title = book.title,
        author = book.author,
        coverBase64 = book.coverBase64,
        coverUrl = null,
        onDismiss = onDismiss
    )
}

@Composable
fun ParallaxCoverViewer(
    book: OpdsBook,
    onDismiss: () -> Unit
) {
    ParallaxCoverViewer(
        title = book.title,
        author = book.author,
        coverBase64 = null,
        coverUrl = book.coverUrl,
        onDismiss = onDismiss
    )
}

@Composable
fun ParallaxCoverViewer(
    title: String,
    author: String = "",
    coverBase64: String? = null,
    coverUrl: String? = null,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val density = LocalDensity.current

    var rawPitch by remember { mutableFloatStateOf(0f) }
    var rawRoll by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            var basePitch: Float? = null
            var baseRoll: Float? = null

            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GAME_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val p = Math.toDegrees(orientation[1].toDouble()).toFloat()
                        val r = Math.toDegrees(orientation[2].toDouble()).toFloat()
                        if (basePitch == null) {
                            basePitch = p
                            baseRoll = r
                        }
                        rawPitch = ((p - (basePitch ?: 0f))).coerceIn(-22f, 22f)
                        rawRoll = ((r - (baseRoll ?: 0f))).coerceIn(-22f, 22f)
                    }
                    Sensor.TYPE_GRAVITY, Sensor.TYPE_ACCELEROMETER -> {
                        val gx = event.values[0]
                        val gy = event.values[1]
                        rawRoll = (gx / 9.8f * 20f).coerceIn(-22f, 22f)
                        rawPitch = ((gy - 4.5f) / 9.8f * 20f).coerceIn(-22f, 22f)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensor != null) {
            sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    var touchOffsetX by remember { mutableFloatStateOf(0f) }
    var touchOffsetY by remember { mutableFloatStateOf(0f) }

    val targetTiltY = (-rawRoll + touchOffsetX).coerceIn(-25f, 25f)
    val targetTiltX = (rawPitch + touchOffsetY).coerceIn(-25f, 25f)

    val tiltY by animateFloatAsState(
        targetValue = targetTiltY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tiltY"
    )

    val tiltX by animateFloatAsState(
        targetValue = targetTiltX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tiltX"
    )

    val bitmap = remember(coverBase64) {
        if (!coverBase64.isNullOrBlank()) {
            try {
                val decoded = Base64.decode(coverBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
        // Top action bar with title & close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (author.isNotBlank()) {
                    Text(
                        text = author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = strings.closeDialog,
                    tint = Color.White
                )
            }
        }

        // Center 3D Parallax Book Cover Card
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 96.dp, bottom = 72.dp),
            contentAlignment = Alignment.Center
        ) {
            val cardAspectRatio = 0.68f
            val maxCardWidth = maxWidth.coerceAtMost(400.dp)
            val maxCardHeight = maxHeight

            val (cardWidth, cardHeight) = if (maxCardWidth / maxCardHeight < cardAspectRatio) {
                val w = maxCardWidth
                val h = w / cardAspectRatio
                w to h
            } else {
                val h = maxCardHeight
                val w = h * cardAspectRatio
                w to h
            }

            Box(
                modifier = Modifier
                    .size(width = cardWidth, height = cardHeight)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                touchOffsetX = (touchOffsetX + dragAmount.x * 0.12f).coerceIn(-20f, 20f)
                                touchOffsetY = (touchOffsetY - dragAmount.y * 0.12f).coerceIn(-20f, 20f)
                            },
                            onDragEnd = {
                                touchOffsetX = 0f
                                touchOffsetY = 0f
                            },
                            onDragCancel = {
                                touchOffsetX = 0f
                                touchOffsetY = 0f
                            }
                        )
                    }
                    .graphicsLayer {
                        rotationY = tiltY
                        rotationX = tiltX
                        cameraDistance = 16f * density.density
                        translationX = tiltY * 1.6f
                        translationY = tiltX * 1.6f
                        shadowElevation = 32.dp.toPx()
                        shape = RoundedCornerShape(22.dp)
                        clip = true
                    }
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(22.dp))
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Real hardcover book spine indentation / crease on left edge
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(18.dp)
                        .align(Alignment.CenterStart)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.38f),
                                    Color.Black.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Dynamic holographic light specular glare moving smoothly across the surface
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val glareCenterX = size.width * (0.5f - (tiltY / 25f) * 0.6f)
                    val glareCenterY = size.height * (0.5f + (tiltX / 25f) * 0.6f)
                    val glareRadius = size.maxDimension * 1.2f

                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            center = androidx.compose.ui.geometry.Offset(glareCenterX, glareCenterY),
                            radius = glareRadius
                        )
                    )
                }
            }
        }

        // Hint at the bottom
        Text(
            text = strings.parallaxCoverHint,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
    }
}
