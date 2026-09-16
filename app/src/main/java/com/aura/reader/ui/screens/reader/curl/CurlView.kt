package com.aura.reader.ui.screens.reader.curl

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.animation.DecelerateInterpolator
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CurlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    val renderer = CurlRenderer()

    var onPageFlipped: ((isNext: Boolean) -> Unit)? = null
    var onRequestPages: ((isNext: Boolean) -> Unit)? = null
    var onToggleControls: (() -> Unit)? = null
    var onSettleComplete: (() -> Unit)? = null

    private var startX = 0f
    private var startY = 0f
    var isDragging = false
        private set
    var isNextTurn = true
        private set
    private var velocityTracker: VelocityTracker? = null

    private var settleAnimator: ValueAnimator? = null

    init {
        setEGLContextClientVersion(2)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        preserveEGLContextOnPause = true
    }

    fun setPages(front: Bitmap?, under: Bitmap?, back: Bitmap? = null) {
        renderer.setPages(front, under, back)
        requestRender()
    }

    fun startInteractiveCurl(downX: Float, downY: Float, isNext: Boolean) {
        isDragging = true
        isNextTurn = isNext
        startX = downX
        startY = downY
        renderer.isCurling = true
        updateCurlPose(downX, downY)
        requestRender()
    }

    fun updateCurlPose(touchX: Float, touchY: Float) {
        val w = width.toFloat().coerceAtLeast(1f)
        val h = height.toFloat().coerceAtLeast(1f)

        val u = (touchX / w).coerceIn(0f, 1f)
        val v = (touchY / h).coerceIn(0f, 1f)
        val dx = touchX - startX
        val dy = touchY - startY

        renderer.curlPosX = u
        renderer.curlPosY = v

        val angle = if (isNextTurn) {
            atan2(dy, -dx.coerceAtMost(-1f)).coerceIn(-0.55f, 0.55f) * 0.45f
        } else {
            atan2(dy, dx.coerceAtLeast(1f)).coerceIn(-0.55f, 0.55f) * 0.45f
        }

        if (isNextTurn) {
            renderer.curlNormX = cos(angle)
            renderer.curlNormY = -sin(angle)
        } else {
            renderer.curlNormX = -cos(angle)
            renderer.curlNormY = sin(angle)
        }
        renderer.curlDirX = -renderer.curlNormY
        renderer.curlDirY = renderer.curlNormX

        renderer.curlRadius = 0.16f
        renderer.coneFactor = 0.08f
        requestRender()
    }

    fun finishInteractiveCurl(touchX: Float, touchY: Float, vx: Float) {
        isDragging = false
        val currentX = renderer.curlPosX

        val shouldComplete = if (isNextTurn) {
            currentX < 0.65f || vx < -350f
        } else {
            currentX > 0.35f || vx > 350f
        }

        animateSettle(shouldComplete = shouldComplete, isNext = isNextTurn)
    }

    fun startCurlAnimation(isNext: Boolean, onDone: (() -> Unit)? = null) {
        isNextTurn = isNext
        renderer.isCurling = true
        renderer.curlPosY = 0.5f
        if (isNext) {
            renderer.curlNormX = 1.0f
            renderer.curlNormY = 0.0f
        } else {
            renderer.curlNormX = -1.0f
            renderer.curlNormY = 0.0f
        }
        renderer.curlDirX = 0f
        renderer.curlDirY = 1f
        renderer.curlRadius = 0.16f
        renderer.coneFactor = 0f

        val startPos = if (isNext) 1.0f else 0.0f
        val targetPos = if (isNext) -0.5f else 1.5f

        renderer.curlPosX = startPos
        requestRender()

        settleAnimator?.cancel()
        settleAnimator = ValueAnimator.ofFloat(startPos, targetPos).apply {
            duration = 320L
            interpolator = DecelerateInterpolator(1.4f)
            addUpdateListener { anim ->
                renderer.curlPosX = anim.animatedValue as Float
                requestRender()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    renderer.isCurling = false
                    renderer.curlPosX = if (isNext) 1.0f else 0.0f
                    requestRender()
                    onPageFlipped?.invoke(isNext)
                    onDone?.invoke()
                    onSettleComplete?.invoke()
                }
            })
            start()
        }
    }

    private fun animateSettle(shouldComplete: Boolean, isNext: Boolean) {
        val currentX = renderer.curlPosX
        val targetX = if (shouldComplete) {
            if (isNext) -0.5f else 1.5f
        } else {
            if (isNext) 1.2f else -0.2f
        }

        settleAnimator?.cancel()
        settleAnimator = ValueAnimator.ofFloat(currentX, targetX).apply {
            duration = 260L
            interpolator = DecelerateInterpolator(1.4f)
            addUpdateListener { anim ->
                renderer.curlPosX = anim.animatedValue as Float
                requestRender()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    renderer.isCurling = false
                    renderer.curlPosX = 1.0f
                    requestRender()
                    if (shouldComplete) {
                        onPageFlipped?.invoke(isNext)
                    }
                    onSettleComplete?.invoke()
                }
            })
            start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (settleAnimator?.isRunning == true) {
            return true
        }

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)

        val w = width.toFloat().coerceAtLeast(1f)
        val h = height.toFloat().coerceAtLeast(1f)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                isDragging = false
                isNextTurn = event.x > w * 0.4f
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - startX
                val dy = event.y - startY

                if (!isDragging) {
                    if (kotlin.math.abs(dx) > 18f) {
                        isDragging = true
                        isNextTurn = dx < 0
                        onRequestPages?.invoke(isNextTurn)
                        renderer.isCurling = true
                    }
                }

                if (isDragging) {
                    updateCurlPose(event.x, event.y)
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.computeCurrentVelocity(1000)
                val vx = velocityTracker?.xVelocity ?: 0f
                velocityTracker?.recycle()
                velocityTracker = null

                if (!isDragging) {
                    if (kotlin.math.abs(event.x - startX) < 15f && kotlin.math.abs(event.y - startY) < 15f) {
                        if (event.x > w * 0.8f) {
                            onRequestPages?.invoke(true)
                            startCurlAnimation(isNext = true)
                        } else if (event.x < w * 0.2f) {
                            onRequestPages?.invoke(false)
                            startCurlAnimation(isNext = false)
                        } else {
                            onToggleControls?.invoke()
                        }
                    }
                    return true
                }

                finishInteractiveCurl(event.x, event.y, vx)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        settleAnimator?.cancel()
        settleAnimator = null
    }
}
