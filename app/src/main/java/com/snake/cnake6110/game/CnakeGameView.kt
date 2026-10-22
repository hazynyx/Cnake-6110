package com.snake.cnake6110.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

class CnakeGameView(context: Context) : View(context) {

    private val engine = CnakeEngine(cols = 10, rows = 20)

    // Paints
    private val bgPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val gridDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(38, 38, 38) // Dim grey background dot
        style = Paint.Style.FILL
    }

    private val activeBlue = Color.rgb(53, 102, 239) // Vivid electric blue from reference
    private val deadGreyHead = Color.rgb(82, 82, 82)
    private val deadGreyBody = Color.rgb(58, 58, 58)

    private val headPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeBlue
        style = Paint.Style.FILL
    }

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeBlue
        style = Paint.Style.FILL
    }

    private val foodPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeBlue
        style = Paint.Style.FILL
    }

    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val eyeXPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // Integrated Play Button Paints
    private val playButtonBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeBlue
        style = Paint.Style.STROKE
    }

    private val playButtonFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(12, 20, 48) // Very dark blue integrated pill background
        style = Paint.Style.FILL
    }

    private val playTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeBlue
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.25f
    }

    // Geometry caches
    private var cellSpacing = 0f
    private var gridOriginX = 0f
    private var gridOriginY = 0f

    private var headRadius = 0f
    private var maxBodyRadius = 0f
    private var minTailRadius = 0f
    private var gridDotRadius = 0f
    private var foodRadius = 0f

    // Play button rect
    private val playButtonRect = RectF()

    // Game loop timing
    private val tickIntervalMs = 175L
    private var isRunning = false
    private var animStartTime = SystemClock.uptimeMillis()

    // Touch gesture tracking
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var touchStartTime = 0L
    private var isSwiping = false
    private val swipeThresholdDp = 24f
    private var swipeThresholdPx = 0f

    private val loopRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            val now = SystemClock.uptimeMillis()

            if (engine.state == GameState.PLAYING) {
                val updated = engine.tick()
                if (updated) {
                    if (engine.state == GameState.DEAD) {
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                    invalidate()
                }
                postDelayed(this, tickIntervalMs)
            } else if (engine.state == GameState.READY) {
                // Animate play button pulse
                invalidate()
                postDelayed(this, 33L) // ~30 fps pulse when waiting on start
            } else {
                // DEAD state: idle
            }
        }
    }

    init {
        val density = context.resources.displayMetrics.density
        swipeThresholdPx = swipeThresholdDp * density
    }

    fun resume() {
        if (!isRunning) {
            isRunning = true
            removeCallbacks(loopRunnable)
            post(loopRunnable)
        }
    }

    fun pause() {
        isRunning = false
        removeCallbacks(loopRunnable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resume()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pause()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        computeLayout(w.toFloat(), h.toFloat())
    }

    private fun computeLayout(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return

        val cols = engine.cols
        val rows = engine.rows

        // Leave comfortable symmetric padding around grid
        val horizontalSpacing = width / (cols + 1f)
        val verticalSpacing = height / (rows + 1.6f)
        cellSpacing = min(horizontalSpacing, verticalSpacing)

        val totalGridW = (cols - 1) * cellSpacing
        val totalGridH = (rows - 1) * cellSpacing

        gridOriginX = (width - totalGridW) / 2f
        gridOriginY = (height - totalGridH) / 2f

        // Radii based on reference images
        headRadius = cellSpacing * 0.42f
        foodRadius = cellSpacing * 0.42f
        maxBodyRadius = cellSpacing * 0.36f
        minTailRadius = cellSpacing * 0.16f
        gridDotRadius = cellSpacing * 0.055f

        eyeXPaint.strokeWidth = cellSpacing * 0.045f

        // Integrated Play button position in READY state: centered between rows 13 and 15
        val btnCenterX = width / 2f
        val btnCenterY = gridOriginY + 14.5f * cellSpacing
        val btnW = cellSpacing * 5.2f
        val btnH = cellSpacing * 1.35f
        playButtonRect.set(
            btnCenterX - btnW / 2f,
            btnCenterY - btnH / 2f,
            btnCenterX + btnW / 2f,
            btnCenterY + btnH / 2f
        )
        playButtonBorderPaint.strokeWidth = cellSpacing * 0.04f
        playTextPaint.textSize = btnH * 0.42f
    }

    private fun getGridPixelX(gx: Int): Float = gridOriginX + gx * cellSpacing
    private fun getGridPixelY(gy: Int): Float = gridOriginY + gy * cellSpacing

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Black Background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val cols = engine.cols
        val rows = engine.rows
        val isDead = (engine.state == GameState.DEAD)
        val isReady = (engine.state == GameState.READY)

        // 2. Draw 10x20 Matrix Background Dots
        for (r in 0 until rows) {
            val py = getGridPixelY(r)
            for (c in 0 until cols) {
                val px = getGridPixelX(c)
                canvas.drawCircle(px, py, gridDotRadius, gridDotPaint)
            }
        }

        // 3. Draw Food (Blue when alive, Grey when dead)
        val foodX = getGridPixelX(engine.food.x)
        val foodY = getGridPixelY(engine.food.y)
        foodPaint.color = if (isDead) deadGreyBody else activeBlue
        canvas.drawCircle(foodX, foodY, foodRadius, foodPaint)

        // 4. Draw Snake Body Segments (from tail to neck, so head draws on top)
        val snake = engine.snake
        val snakeSize = snake.size

        if (snakeSize > 1) {
            for (i in (snakeSize - 1) downTo 1) {
                val seg = snake[i]
                val px = getGridPixelX(seg.x)
                val py = getGridPixelY(seg.y)

                // Fraction from 1 (near head) down to 0 (tail)
                val t = if (snakeSize <= 2) 1f else (i - 1).toFloat() / (snakeSize - 2).toFloat()
                // Radius tapers from maxBodyRadius down to minTailRadius
                val segRadius = minTailRadius + (1f - t) * (maxBodyRadius - minTailRadius)

                if (isDead) {
                    bodyPaint.color = deadGreyBody
                    bodyPaint.alpha = 200
                    canvas.drawCircle(px, py, segRadius, bodyPaint)
                    // In dead snake refrence, center grid dot remains visible
                    canvas.drawCircle(px, py, gridDotRadius, gridDotPaint)
                } else {
                    bodyPaint.color = activeBlue
                    // Subtle alpha tapering towards tail
                    val alpha = (140 + ((1f - t) * 115)).toInt().coerceIn(120, 255)
                    bodyPaint.alpha = alpha
                    canvas.drawCircle(px, py, segRadius, bodyPaint)
                }
            }
        }

        // 5. Draw Snake Head
        if (snakeSize > 0) {
            val head = snake[0]
            val hx = getGridPixelX(head.x)
            val hy = getGridPixelY(head.y)

            headPaint.color = if (isDead) deadGreyHead else activeBlue
            canvas.drawCircle(hx, hy, headRadius, headPaint)

            // Eyes rendering
            val dir = engine.currentDirection
            val eyeDist = cellSpacing * 0.16f
            val eyeOffset = cellSpacing * 0.13f
            val eyeRadius = cellSpacing * 0.08f

            var e1x = hx
            var e1y = hy
            var e2x = hx
            var e2y = hy

            when (dir) {
                Direction.UP -> {
                    e1x = hx - eyeOffset; e1y = hy - eyeDist
                    e2x = hx + eyeOffset; e2y = hy - eyeDist
                }
                Direction.DOWN -> {
                    e1x = hx - eyeOffset; e1y = hy + eyeDist
                    e2x = hx + eyeOffset; e2y = hy + eyeDist
                }
                Direction.LEFT -> {
                    e1x = hx - eyeDist; e1y = hy - eyeOffset
                    e2x = hx - eyeDist; e2y = hy + eyeOffset
                }
                Direction.RIGHT -> {
                    e1x = hx + eyeDist; e1y = hy - eyeOffset
                    e2x = hx + eyeDist; e2y = hy + eyeOffset
                }
            }

            if (isDead) {
                // Two black 'X' marks for dead snake eyes (matching reference)
                drawEyeX(canvas, e1x, e1y, cellSpacing * 0.08f)
                drawEyeX(canvas, e2x, e2y, cellSpacing * 0.08f)
            } else {
                // Two black circular dots facing movement direction
                canvas.drawCircle(e1x, e1y, eyeRadius, eyePaint)
                canvas.drawCircle(e2x, e2y, eyeRadius, eyePaint)
            }
        }

        // 6. Draw Integrated Play Button (When in READY state)
        if (isReady) {
            drawIntegratedPlayButton(canvas)
        }
    }

    private fun drawEyeX(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        canvas.drawLine(cx - size, cy - size, cx + size, cy + size, eyeXPaint)
        canvas.drawLine(cx - size, cy + size, cx + size, cy - size, eyeXPaint)
    }

    private fun drawIntegratedPlayButton(canvas: Canvas) {
        val elapsed = SystemClock.uptimeMillis() - animStartTime
        // Gentle breathing pulse between 0.75 and 1.0
        val pulse = 0.85f + 0.15f * sin(elapsed / 320.0).toFloat()

        val cornerRadius = playButtonRect.height() / 2f

        // Button background
        playButtonFillPaint.alpha = (230 * pulse).toInt()
        canvas.drawRoundRect(playButtonRect, cornerRadius, cornerRadius, playButtonFillPaint)

        // Button outline
        playButtonBorderPaint.color = activeBlue
        playButtonBorderPaint.alpha = (255 * pulse).toInt()
        canvas.drawRoundRect(playButtonRect, cornerRadius, cornerRadius, playButtonBorderPaint)

        // Text "PLAY" with subtle play arrow ▶
        playTextPaint.color = activeBlue
        playTextPaint.alpha = (255 * pulse).toInt()
        val textY = playButtonRect.centerY() - (playTextPaint.descent() + playTextPaint.ascent()) / 2f
        canvas.drawText("▶ PLAY", playButtonRect.centerX(), textY, playTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
                touchStartTime = SystemClock.uptimeMillis()
                isSwiping = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - touchStartX
                val dy = event.y - touchStartY
                val dist = hypot(dx, dy)

                if (dist >= swipeThresholdPx) {
                    isSwiping = true
                    handleSwipe(dx, dy)
                    // Reset anchor to enable chaining continuous turns while dragging
                    touchStartX = event.x
                    touchStartY = event.y
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                val tapDuration = SystemClock.uptimeMillis() - touchStartTime
                val tapDist = hypot(event.x - touchStartX, event.y - touchStartY)

                if (!isSwiping && tapDist < swipeThresholdPx && tapDuration < 450) {
                    handleTap(event.x, event.y)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleSwipe(dx: Float, dy: Float) {
        val newDirection = if (abs(dx) > abs(dy)) {
            if (dx > 0) Direction.RIGHT else Direction.LEFT
        } else {
            if (dy > 0) Direction.DOWN else Direction.UP
        }

        if (engine.state == GameState.READY) {
            startGame()
        }

        engine.setDirection(newDirection)
    }

    private fun handleTap(x: Float, y: Float) {
        when (engine.state) {
            GameState.READY -> {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                startGame()
            }

            GameState.DEAD -> {
                // Clicking on the dead snake (or dead snake area) restarts the game!
                val touchGx = (x - gridOriginX) / cellSpacing
                val touchGy = (y - gridOriginY) / cellSpacing
                val touchedSnake = engine.isTouchNearDeadSnake(touchGx, touchGy)

                // Always allow restart if tapped on dead snake or near it (or tap screen)
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                engine.restart()
                resume()
                invalidate()
            }

            GameState.PLAYING -> {
                // No button clicks during gameplay; movements are done by swiping
            }
        }
    }

    private fun startGame() {
        engine.startPlaying()
        resume()
        invalidate()
    }
}
