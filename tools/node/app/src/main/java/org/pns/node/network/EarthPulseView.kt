package org.pns.node

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.graphics.Color
import android.graphics.RadialGradient
import android.graphics.Shader
import android.animation.ValueAnimator

class EarthPulseView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val earthPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val pulsePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var earthRadius = 0f
    private var pulseRadius = 0f
    private var pulseAmplitude = 0f
    private var lightValue = 0f
    private var pressureValue = 0f

    init {
        // Set default values
        pulseAmplitude = 20f
    }

    fun updatePulse(light: Float, pressure: Float) {
        lightValue = light
        pressureValue = pressure
        invalidate() // Force the view to redraw itself
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Calculate Earth radius based on the view's width
        earthRadius = (w / 2).toFloat() - 10f

        // Set up pulse animation
        val pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000L // Duration in milliseconds
            repeatMode = ValueAnimator.REVERSE // Reverse animation
            repeatCount = ValueAnimator.INFINITE // Repeat indefinitely

            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                pulseRadius = earthRadius + pulseAmplitude * fraction * pressureValue / 1000 // Adjust pulse radius
                invalidate()
            }
        }

        // Start the pulse animation
        pulseAnimator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the Earth
        canvas.drawCircle(width / 2f, height / 2f, earthRadius, earthPaint)

        // Draw the pulsating circle
        canvas.drawCircle(width / 2f, height / 2f, pulseRadius, pulsePaint)

        // Add some visual feedback based on light and pressure
        pulsePaint.shader = RadialGradient(
            width / 2f, height / 2f, pulseRadius,
            Color.GREEN, Color.YELLOW, Shader.TileMode.CLAMP
        )

        // Update pulse size based on pressure
        pulseAmplitude = (pressureValue / 1000f) * 20f // Scale pulse based on pressure
    }
}