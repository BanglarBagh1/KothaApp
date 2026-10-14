package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalKothaColors
import com.example.ui.theme.MonoFont
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InkColorPicker(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Color"
) {
    val colors = LocalKothaColors.current
    val currentComposeColor = Color(selectedColor)
    var hexInput by remember(selectedColor) {
        mutableStateOf(String.format("%06X", (selectedColor and 0xFFFFFFL)))
    }

    val presetSwatches = listOf(
        0xFFF2542D, // Vermilion
        0xFFE3A93B, // Ochre
        0xFF45A596, // Verdigris
        0xFF4D62CD, // Indigo
        0xFFC8323C, // Crimson
        0xFFECE6DA, // Paper
        0xFF171512, // Ink Raised
        0xFF8C857A  // Muted
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.hairline, RoundedCornerShape(2.dp))
            .background(colors.surface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = MonoFont,
                fontSize = 12.sp,
                color = colors.textPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(currentComposeColor, CircleShape)
                        .border(1.dp, colors.hairline, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "#$hexInput",
                    fontFamily = MonoFont,
                    fontSize = 12.sp,
                    color = colors.accent
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset Swatches Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            presetSwatches.forEach { swatch ->
                val isSelected = swatch == selectedColor
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(swatch), RoundedCornerShape(2.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) colors.accent else colors.hairline,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .clickable { onColorSelected(swatch) }
                )
            }
        }
    }
}
