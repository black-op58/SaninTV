package com.sanin.tv.themes

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable

/**
 * Applies a programmatic OLED window background for Glow Spots and Gradient modes.
 *
 * Modes:
 *   0 = Off         — no-op, normal theme background
 *   1 = Pure AMOLED — no-op here; handled by AppTheme_Amoled XML overlay
 *   2 = Glow Spots  — pure black + 3 intense radial glow orbs (top-center, top-right, bottom-left)
 *   3 = Gradient    — pure black + linear gradient in the theme primary color, direction-aware
 *   4 = Vignette    — colored vignette fading from edges inward, cinematic feel
 *
 * Gradient directions (gradientDir):
 *   0 = Top → Bottom
 *   1 = Bottom → Top
 *   2 = Left → Right
 *   3 = Right → Left
 */
object OledBackgroundManager {

    fun apply(activity: Activity, oledMode: Int, primaryColor: Int, gradientDir: Int = 0) {
        when (oledMode) {
            2 -> activity.window.setBackgroundDrawable(GlowSpotsDrawable(primaryColor))
            3 -> activity.window.setBackgroundDrawable(GradientBgDrawable(primaryColor, gradientDir))
            4 -> activity.window.setBackgroundDrawable(VignetteBgDrawable(primaryColor))
        }
    }

    private class GlowSpotsDrawable(private val primaryColor: Int) : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 3 spots: [cx%, cy%, radius%] — positions: top-center, top-right, bottom-left
        private val spots = listOf(
            floatArrayOf(0.50f, 0.10f, 0.45f),   // top center
            floatArrayOf(0.90f, 0.18f, 0.36f),   // top right
            floatArrayOf(0.10f, 0.82f, 0.38f),   // bottom left
        )

        override fun draw(canvas: Canvas) {
            canvas.drawColor(Color.BLACK)
            val w = bounds.width().toFloat()
            val h = bounds.height().toFloat()
            val r = Color.red(primaryColor)
            val g = Color.green(primaryColor)
            val b = Color.blue(primaryColor)
            for (spot in spots) {
                val cx     = spot[0] * w
                val cy     = spot[1] * h
                val radius = spot[2] * minOf(w, h)
                paint.shader = RadialGradient(
                    cx, cy, radius,
                    intArrayOf(
                        Color.argb(90, r, g, b),
                        Color.argb(40, r, g, b),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.40f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(cx, cy, radius, paint)
            }
        }

        override fun setAlpha(alpha: Int)              { paint.alpha = alpha }
        override fun setColorFilter(cf: ColorFilter?)  { paint.colorFilter = cf }
        override fun getOpacity() = PixelFormat.TRANSLUCENT
    }

    private class GradientBgDrawable(
        private val primaryColor: Int,
        private val direction: Int   // 0=top→bottom, 1=bottom→top, 2=left→right, 3=right→left
    ) : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun draw(canvas: Canvas) {
            canvas.drawColor(Color.BLACK)
            val w = bounds.width().toFloat()
            val h = bounds.height().toFloat()
            val r = Color.red(primaryColor)
            val g = Color.green(primaryColor)
            val b = Color.blue(primaryColor)

            val x0: Float
            val y0: Float
            val x1: Float
            val y1: Float
            when (direction) {
                1 -> { x0 = w / 2f; y0 = h;        x1 = w / 2f;        y1 = h * 0.30f } // bottom → top
                2 -> { x0 = 0f;     y0 = h / 2f;   x1 = w * 0.70f;     y1 = h / 2f   } // left → right
                3 -> { x0 = w;      y0 = h / 2f;   x1 = w * 0.30f;     y1 = h / 2f   } // right → left
                else -> { x0 = w / 2f; y0 = 0f;    x1 = w / 2f;        y1 = h * 0.70f } // top → bottom
            }

            paint.shader = LinearGradient(
                x0, y0, x1, y1,
                intArrayOf(
                    Color.argb(80, r, g, b),
                    Color.argb(35, r, g, b),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.40f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, w, h, paint)
        }

        override fun setAlpha(alpha: Int)              { paint.alpha = alpha }
        override fun setColorFilter(cf: ColorFilter?)  { paint.colorFilter = cf }
        override fun getOpacity() = PixelFormat.TRANSLUCENT
    }

    private class VignetteBgDrawable(private val primaryColor: Int) : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun draw(canvas: Canvas) {
            canvas.drawColor(Color.BLACK)
            val w = bounds.width().toFloat()
            val h = bounds.height().toFloat()
            val r = Color.red(primaryColor)
            val g = Color.green(primaryColor)
            val b = Color.blue(primaryColor)
            val cx = w / 2f
            val cy = h / 2f
            val radius = maxOf(w, h) * 0.80f
            paint.shader = RadialGradient(
                cx, cy, radius,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.argb(45, r, g, b),
                    Color.argb(110, 0, 0, 0)
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, w, h, paint)
        }

        override fun setAlpha(alpha: Int)              { paint.alpha = alpha }
        override fun setColorFilter(cf: ColorFilter?)  { paint.colorFilter = cf }
        override fun getOpacity() = PixelFormat.TRANSLUCENT
    }
}
