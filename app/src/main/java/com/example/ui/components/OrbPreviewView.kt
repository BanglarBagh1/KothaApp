package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settings.KothaSettings
import com.example.settings.OrbShape
import com.example.settings.RingStyle
import com.example.ui.theme.DisplayFont
import com.example.ui.theme.LocalKothaColors
import com.example.ui.theme.MonoFont
import kotlin.math.cos
import kotlin.math.sin

enum class PreviewOrbState {
    IDLE,
    LISTENING,
    WAITING,
    ERROR
}

@Composable
fun OrbPreviewView(
    settings: KothaSettings,
    modifier: Modifier = Modifier
) {
    val colors = LocalKothaColors.current
    var previewState by remember { mutableStateOf(PreviewOrbState.IDLE) }

    val transition = rememberInfiniteTransition(label = "orbPreviewAnim")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.hairline, RoundedCornerShape(2.dp))
            .background(colors.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top label with specimen metadata
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "STAGE • SPECIMEN",
                fontFamily = MonoFont,
                fontSize = 11.sp,
                color = colors.textMuted
            )
            Text(
                text = "${settings.orbSizeDp} DP • ${settings.orbShape.name}",
                fontFamily = MonoFont,
                fontSize = 11.sp,
                color = colors.accent
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid canvas with Orb
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val gridSpacing = 20.dp.toPx()
                // Draw faint ruled grid lines
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = colors.hairline.copy(alpha = 0.4f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 0.8f
                    )
                    x += gridSpacing
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = colors.hairline.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.8f
                    )
                    y += gridSpacing
                }

                // Draw central crosshairs
                val cx = size.width / 2f
                val cy = size.height / 2f
                drawLine(
                    color = colors.hairline,
                    start = Offset(cx - 30f, cy),
                    end = Offset(cx + 30f, cy),
                    strokeWidth = 1f
                )
                drawLine(
                    color = colors.hairline,
                    start = Offset(cx, cy - 30f),
                    end = Offset(cx, cy + 30f),
                    strokeWidth = 1f
                )

                // Render Orb preview
                val baseRadius = (settings.orbSizeDp.dp.toPx() / 2f) * 0.8f
                val activeRadius = if (previewState == PreviewOrbState.IDLE) {
                    baseRadius + (pulse * 2.5f)
                } else {
                    baseRadius
                }

                val orbColor = when (previewState) {
                    PreviewOrbState.IDLE -> Color(settings.idleColor)
                    PreviewOrbState.LISTENING -> Color(settings.listeningColor)
                    PreviewOrbState.WAITING -> Color(settings.waitingColor)
                    PreviewOrbState.ERROR -> Color(settings.errorColor)
                }

                // Base Body
                when (settings.orbShape) {
                    OrbShape.CIRCLE -> {
                        drawCircle(color = orbColor, radius = activeRadius, center = Offset(cx, cy))
                    }
                    OrbShape.SQUIRCLE -> {
                        drawRoundRect(
                            color = orbColor,
                            topLeft = Offset(cx - activeRadius, cy - activeRadius),
                            size = Size(activeRadius * 2, activeRadius * 2),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(activeRadius * 0.6f)
                        )
                    }
                    OrbShape.ROUNDED_SQUARE -> {
                        drawRoundRect(
                            color = orbColor,
                            topLeft = Offset(cx - activeRadius, cy - activeRadius),
                            size = Size(activeRadius * 2, activeRadius * 2),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(activeRadius * 0.28f)
                        )
                    }
                    OrbShape.SEAL -> {
                        val path = Path()
                        val points = 12
                        val innerR = activeRadius * 0.88f
                        for (i in 0 until points * 2) {
                            val r = if (i % 2 == 0) activeRadius else innerR
                            val angle = Math.toRadians((i * 360.0 / (points * 2)))
                            val px = cx + (r * cos(angle)).toFloat()
                            val py = cy + (r * sin(angle)).toFloat()
                            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        path.close()
                        drawPath(path, orbColor)
                    }
                }

                // Ring
                val ringRadius = activeRadius + 6f
                val ringColor = Color(settings.ringColor)
                val strokeW = settings.outlineThicknessDp * 2f

                if (previewState == PreviewOrbState.LISTENING) {
                    val tickCount = 20
                    val spread = 360f / tickCount
                    for (i in 0 until tickCount) {
                        val deg = i * spread
                        if (deg in 285f..320f) continue // 1 o'clock gap
                        val rad = Math.toRadians(deg.toDouble())
                        val tickLen = 4f + (pulse * 8f)
                        val x1 = cx + (ringRadius * cos(rad)).toFloat()
                        val y1 = cy + (ringRadius * sin(rad)).toFloat()
                        val x2 = cx + ((ringRadius + tickLen) * cos(rad)).toFloat()
                        val y2 = cy + ((ringRadius + tickLen) * sin(rad)).toFloat()
                        drawLine(
                            color = ringColor,
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )
                    }
                } else if (previewState == PreviewOrbState.WAITING) {
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 270f * pulse,
                        useCenter = false,
                        topLeft = Offset(cx - ringRadius, cy - ringRadius),
                        size = Size(ringRadius * 2, ringRadius * 2),
                        style = Stroke(width = strokeW)
                    )
                } else if (previewState == PreviewOrbState.ERROR) {
                    drawArc(
                        color = ringColor,
                        startAngle = 30f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = Offset(cx - ringRadius, cy - ringRadius),
                        size = Size(ringRadius * 2, ringRadius * 2),
                        style = Stroke(width = strokeW)
                    )
                } else if (settings.ringStyle != RingStyle.NONE) {
                    drawCircle(
                        color = ringColor,
                        radius = ringRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = strokeW)
                    )
                }

                // Center Glyph
                val iconColor = Color(settings.iconColor)
                if (previewState == PreviewOrbState.LISTENING) {
                    drawCircle(color = iconColor, radius = activeRadius * 0.25f, center = Offset(cx, cy))
                } else {
                    drawCircle(color = iconColor, radius = activeRadius * 0.18f, center = Offset(cx, cy))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // State switcher buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PreviewOrbState.values().forEach { state ->
                val isSelected = state == previewState
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.accent else colors.hairline,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .background(if (isSelected) colors.accent else colors.surface)
                        .clickable { previewState = state }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = state.name.lowercase(),
                        fontFamily = MonoFont,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) colors.background else colors.textPrimary
                    )
                }
            }
        }
    }
}
