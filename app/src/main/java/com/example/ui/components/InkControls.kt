package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalKothaColors
import com.example.ui.theme.MonoFont

/**
 * Bracket-style toggle: [ ●   ] <-> [   ● ]
 */
@Composable
fun InkBracketToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalKothaColors.current
    val view = LocalView.current
    val indicatorOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 400f),
        label = "bracketToggleOffset"
    )

    Box(
        modifier = modifier
            .semantics { contentDescription = if (checked) "Enabled" else "Disabled" }
            .border(1.dp, colors.hairline, RoundedCornerShape(2.dp))
            .background(colors.surface)
            .clickable {
                try { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) } catch (ignored: Exception) {}
                onCheckedChange(!checked)
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "[",
                fontFamily = MonoFont,
                color = colors.textMuted,
                fontSize = 14.sp
            )
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Sliding dot - ensure leftPad is strictly non-negative to prevent IllegalArgumentException
                val dotColor = if (checked) colors.accent else colors.textMuted
                val safeFraction = indicatorOffset.coerceIn(0f, 1f)
                val leftPad = (safeFraction * 22).dp
                Box(
                    modifier = Modifier
                        .padding(start = leftPad)
                        .width(14.dp)
                        .height(14.dp)
                        .background(dotColor, RoundedCornerShape(1.dp))
                )
            }
            Text(
                text = "]",
                fontFamily = MonoFont,
                color = colors.textMuted,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Ruler-style slider with tick marks, notched thumb and value readout.
 */
@Composable
fun InkRulerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    unitLabel: String = ""
) {
    val colors = LocalKothaColors.current
    val view = LocalView.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${value.toInt()} $unitLabel",
                fontFamily = MonoFont,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            )
            Text(
                text = "${valueRange.start.toInt()} … ${valueRange.endInclusive.toInt()}",
                fontFamily = MonoFont,
                fontSize = 11.sp,
                color = colors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(colors.surface, RoundedCornerShape(2.dp))
                .border(1.dp, colors.hairline, RoundedCornerShape(2.dp))
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val rangeSpan = valueRange.endInclusive - valueRange.start
            val fraction = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val safeWidth = widthPx.coerceAtLeast(1f)
                            val newFraction = (offset.x / safeWidth).coerceIn(0f, 1f)
                            val newValue = (valueRange.start + newFraction * rangeSpan).coerceIn(valueRange.start, valueRange.endInclusive)
                            try { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) } catch (ignored: Exception) {}
                            onValueChange(newValue)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val safeWidth = widthPx.coerceAtLeast(1f)
                            val newFraction = (change.position.x / safeWidth).coerceIn(0f, 1f)
                            val newValue = (valueRange.start + newFraction * rangeSpan).coerceIn(valueRange.start, valueRange.endInclusive)
                            onValueChange(newValue)
                        }
                    }
            ) {
                val totalTicks = 24
                val tickSpacing = size.width / totalTicks

                // Draw ruler ticks
                for (i in 0..totalTicks) {
                    val x = i * tickSpacing
                    val isMajor = i % 4 == 0
                    val tickHeight = if (isMajor) 18f else 10f
                    val tickColor = if (x <= fraction * size.width) colors.accent else colors.hairline
                    drawLine(
                        color = tickColor,
                        start = Offset(x, size.height - tickHeight),
                        end = Offset(x, size.height),
                        strokeWidth = if (isMajor) 2f else 1f
                    )
                }

                // Draw notched thumb
                val thumbX = fraction * size.width
                drawLine(
                    color = colors.accent,
                    start = Offset(thumbX, 4f),
                    end = Offset(thumbX, size.height - 4f),
                    strokeWidth = 3f
                )
                drawCircle(
                    color = colors.accent,
                    radius = 4f,
                    center = Offset(thumbX, 8f)
                )
            }
        }
    }
}

/**
 * Segmented choices as words with a moving ink underline.
 */
@Composable
fun <T> InkSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier
) {
    val colors = LocalKothaColors.current
    val view = LocalView.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.hairline, RoundedCornerShape(2.dp))
            .background(colors.surface),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        try { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) } catch (ignored: Exception) {}
                        onItemSelected(item)
                    }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = labelProvider(item),
                        fontFamily = MonoFont,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) colors.textPrimary else colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(2.dp)
                            .background(if (isSelected) colors.accent else Color.Transparent)
                    )
                }
            }
        }
    }
}

/**
 * Stepper numeral with +/- controls.
 */
@Composable
fun InkStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    label: String = ""
) {
    val colors = LocalKothaColors.current
    val view = LocalView.current

    Row(
        modifier = modifier
            .border(1.dp, colors.hairline, RoundedCornerShape(2.dp))
            .background(colors.surface)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "–",
            fontFamily = MonoFont,
            fontSize = 16.sp,
            color = if (value > range.first) colors.textPrimary else colors.textMuted,
            modifier = Modifier
                .clickable(enabled = value > range.first) {
                    try { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) } catch (ignored: Exception) {}
                    onValueChange(value - 1)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Text(
            text = "$value $label",
            fontFamily = MonoFont,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Text(
            text = "+",
            fontFamily = MonoFont,
            fontSize = 16.sp,
            color = if (value < range.last) colors.textPrimary else colors.textMuted,
            modifier = Modifier
                .clickable(enabled = value < range.last) {
                    try { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) } catch (ignored: Exception) {}
                    onValueChange(value + 1)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
