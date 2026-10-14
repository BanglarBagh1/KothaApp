package com.example.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.graphics.ColorUtils
import com.example.settings.CaptionBubbleSize
import com.example.settings.CaptionBubbleStyle
import com.example.settings.KothaSettings
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ViewConstructor")
class CaptionBubbleOverlay(
    context: Context,
    var settings: KothaSettings
) : View(context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private val cardRect = RectF()
    private val cornerRadiusDp = 14f

    var text: String = ""
        set(value) {
            if (field != value) {
                field = value
                rebuildLayout()
                postInvalidate()
            }
        }

    var isLiveStreaming: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }

    private var staticLayout: StaticLayout? = null
    private var fadeAnimator: ValueAnimator? = null
    private val hideRunnable = Runnable { startFadeOut() }

    init {
        updateStyles()
    }

    fun updateStyles() {
        val density = resources.displayMetrics.density

        // 1. Text size based on CaptionBubbleSize
        val spSize = when (settings.captionBubbleSize) {
            CaptionBubbleSize.COMPACT -> 13f
            CaptionBubbleSize.MEDIUM -> 16f
            CaptionBubbleSize.LARGE -> 19f
            CaptionBubbleSize.EXTRA_LARGE -> 22f
        }
        textPaint.textSize = spSize * density

        // 2. Background and Border based on CaptionBubbleStyle
        val accentColor = settings.listeningColor.toInt()
        when (settings.captionBubbleStyle) {
            CaptionBubbleStyle.TRANSLUCENT_DARK -> {
                bgPaint.color = 0xDD12100E.toInt()
                borderPaint.color = 0x44ECE6DA.toInt()
                borderPaint.strokeWidth = 1.5f * density
            }
            CaptionBubbleStyle.SOLID_AMOLED -> {
                bgPaint.color = 0xFF000000.toInt()
                borderPaint.color = 0x66ECE6DA.toInt()
                borderPaint.strokeWidth = 2f * density
            }
            CaptionBubbleStyle.ACCENT_THEMED -> {
                bgPaint.color = ColorUtils.blendARGB(0xDD12100E.toInt(), accentColor, 0.20f)
                borderPaint.color = accentColor
                borderPaint.strokeWidth = 2f * density
            }
        }

        dotPaint.color = if (isLiveStreaming) accentColor else 0xFF45A596.toInt()

        rebuildLayout()
        requestLayout()
        postInvalidate()
    }

    fun setCaptionContent(newText: String, streaming: Boolean) {
        removeCallbacks(hideRunnable)
        fadeAnimator?.cancel()
        alpha = 1.0f

        val trimmed = newText.trim()
        isLiveStreaming = streaming

        if (trimmed.isEmpty()) {
            text = ""
            visibility = INVISIBLE
            return
        }

        text = trimmed
        visibility = VISIBLE

        if (!streaming) {
            // Speech committed: linger for configured duration before fade out
            postDelayed(hideRunnable, settings.captionLingerDurationMs)
        }
    }

    private fun startFadeOut() {
        fadeAnimator?.cancel()
        fadeAnimator = ValueAnimator.ofFloat(alpha, 0f).apply {
            duration = 350L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val a = it.animatedValue as Float
                alpha = a
                if (a <= 0.01f) {
                    visibility = INVISIBLE
                    text = ""
                }
            }
        }
        fadeAnimator?.start()
    }

    private fun rebuildLayout() {
        if (text.isEmpty()) {
            staticLayout = null
            return
        }

        val density = resources.displayMetrics.density
        val screenWidth = resources.displayMetrics.widthPixels
        val maxAvailableWidth = (screenWidth - (48 * density).toInt()).coerceAtLeast(180)
        val maxCardWidth = min(maxAvailableWidth, (340 * density).toInt())
        val horizontalPad = (32 * density).toInt()
        val textWidth = max(60, maxCardWidth - horizontalPad)

        val maxLines = settings.captionMaxLines.coerceIn(1, 3)

        staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setMaxLines(maxLines)
                .setEllipsize(android.text.TextUtils.TruncateAt.END)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                textPaint,
                textWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0f,
                false
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val layout = staticLayout
        if (layout == null || text.isEmpty()) {
            setMeasuredDimension(0, 0)
            return
        }

        val padX = (16 * density)
        val padY = (10 * density)
        val dotRadius = (4 * density)
        val dotGap = (8 * density)

        val contentWidth = layout.width.toFloat().coerceAtMost(maxLineWidth(layout))
        val totalWidth = contentWidth + padX * 2 + dotRadius * 2 + dotGap
        val totalHeight = layout.height + padY * 2

        setMeasuredDimension(
            totalWidth.toInt().coerceAtLeast((80 * density).toInt()),
            totalHeight.toInt().coerceAtLeast((36 * density).toInt())
        )
    }

    private fun maxLineWidth(layout: StaticLayout): Float {
        var maxW = 0f
        for (i in 0 until layout.lineCount) {
            maxW = max(maxW, layout.getLineWidth(i))
        }
        return maxW
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val layout = staticLayout ?: return
        if (text.isEmpty()) return

        val density = resources.displayMetrics.density
        val cornerRadius = cornerRadiusDp * density

        cardRect.set(1f, 1f, width.toFloat() - 1f, height.toFloat() - 1f)

        // Draw card background
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)
        // Draw card border
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint)

        // Draw dot indicator (pulsing / live accent if streaming, emerald if final)
        val padX = 14f * density
        val dotRadius = 3.5f * density
        val dotCx = padX + dotRadius
        val dotCy = height / 2f
        val accentColor = settings.listeningColor.toInt()
        dotPaint.color = if (isLiveStreaming) accentColor else 0xFF45A596.toInt()
        canvas.drawCircle(dotCx, dotCy, dotRadius, dotPaint)

        // Draw StaticLayout text
        val textStartX = dotCx + dotRadius + (8f * density)
        val textStartY = (height - layout.height) / 2f
        canvas.save()
        canvas.translate(textStartX, textStartY)
        layout.draw(canvas)
        canvas.restore()
    }
}
