package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bridge.TextBridgeStateHolder
import com.example.engine.DictationService
import com.example.settings.KothaSettings
import com.example.ui.theme.DisplayFont
import com.example.ui.theme.LocalKothaColors
import com.example.ui.theme.MonoFont

@Composable
fun TestPadScreen(
    settings: KothaSettings,
    onBack: () -> Unit
) {
    val colors = LocalKothaColors.current
    val context = LocalContext.current
    var testText by remember { mutableStateOf("") }

    val transition = rememberInfiniteTransition(label = "testPadPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "পরীক্ষাগার • TEST PAD",
                    fontFamily = DisplayFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = "Live in-app field to verify dictation & keyboard concurrency",
                    fontFamily = MonoFont,
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Telemetry monitor box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.hairline, RoundedCornerShape(2.dp))
                .background(colors.surface)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "MONITOR: ACTIVE", fontFamily = MonoFont, fontSize = 11.sp, color = colors.signalVerdigris)
                Text(text = "LANG: ${settings.primaryLanguage}", fontFamily = MonoFont, fontSize = 11.sp, color = colors.accent)
            }

            val isRunning = DictationService.isRunning()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isRunning) "ORB STATUS: RUNNING OVERLAY" else "ORB STATUS: LOWERED / STOPPED",
                    fontFamily = MonoFont,
                    fontSize = 11.sp,
                    color = if (isRunning) colors.signalVerdigris else colors.textMuted
                )
                Text(
                    text = "MODE: ${settings.dictationMode.name}",
                    fontFamily = MonoFont,
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }

            // Keyboard and field snapshot
            val snapshot = TextBridgeStateHolder.focusedSnapshot.value
            Text(
                text = "FOCUSED: ${snapshot?.className?.split(".")?.lastOrNull() ?: "None"} • LEN: ${snapshot?.currentText?.length ?: 0}",
                fontFamily = MonoFont,
                fontSize = 11.sp,
                color = colors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main input field
        Text(
            text = "FOCUSED TEST FIELD (Try speaking or typing here):",
            fontFamily = MonoFont,
            fontSize = 11.sp,
            color = colors.textMuted
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = testText,
            onValueChange = { testText = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = {
                Text(
                    text = "ক্লিক করুন এবং কথা বলুন… Tap here to focus. Spoken Bangla or English words will be inserted here while your keyboard stays open.",
                    fontFamily = DisplayFont,
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.hairline,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { testText = "" },
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, colors.hairline, RoundedCornerShape(2.dp))
            ) {
                Text(text = "Clear Text", fontFamily = MonoFont, fontSize = 12.sp, color = colors.textPrimary)
            }

            Button(
                onClick = {
                    val service = com.example.bridge.TextBridgeService.instance
                    service?.refreshFocusedNode()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, colors.hairline, RoundedCornerShape(2.dp))
            ) {
                Text(text = "Refresh Bridge", fontFamily = MonoFont, fontSize = 12.sp, color = colors.accent)
            }
        }
    }
}
