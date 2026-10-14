package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bridge.TextBridgeStateHolder
import com.example.ui.theme.DisplayFont
import com.example.ui.theme.LocalKothaColors
import com.example.ui.theme.MonoFont

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val colors = LocalKothaColors.current
    val context = LocalContext.current
    val isBridgeConnected by TextBridgeStateHolder.isConnected.collectAsState()

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    val hasMic = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Wordmark lockup
        Text(
            text = "কথা • KOTHA",
            fontFamily = DisplayFont,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = colors.accent
        )
        Text(
            text = "SPECIMEN SETUP • 4-STEP PERMISSION LEDGER",
            fontFamily = MonoFont,
            fontSize = 11.sp,
            color = colors.textMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                StepCard(
                    number = "01",
                    title = "Microphone Access",
                    bangla = "কথা শোনার জন্য মাইক্রোফোন অনুমতি প্রয়োজন।",
                    english = "Required by Android's speech recognizer to capture audio.",
                    isComplete = hasMic,
                    buttonLabel = "Grant Microphone"
                ) {
                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            item {
                StepCard(
                    number = "02",
                    title = "Floating Button Overlay",
                    bangla = "কীবোর্ডের উপরে ভেসে থাকার জন্য অনুমতি।",
                    english = "Enables the Orb to float over all applications.",
                    isComplete = hasOverlay,
                    buttonLabel = "Allow Overlay"
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }
            }

            item {
                StepCard(
                    number = "03",
                    title = "Text Bridge Service",
                    bangla = "অন্য অ্যাপে টেক্সট প্রবেশ করানোর জন্য অ্যাক্সেসিবিলিটি ব্রিজ।",
                    english = "Streams dictated text directly into your focused field without altering your keyboard.",
                    isComplete = isBridgeConnected,
                    buttonLabel = "Enable Kotha Bridge"
                ) {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            }

            item {
                StepCard(
                    number = "04",
                    title = "Unrestricted Battery & Auto-Repair",
                    bangla = "পটভূমিতে বন্ধ হওয়া রোধ করতে ব্যাটারি অপটিমাইজেশন বন্ধ রাখুন।",
                    english = "Exempt Kotha from aggressive background killer routines.",
                    isComplete = true,
                    buttonLabel = "Battery Settings"
                ) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFinished,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, colors.accent, RoundedCornerShape(2.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
            shape = RoundedCornerShape(2.dp)
        ) {
            Text(
                text = "ENTER STAGE • অ্যাপে প্রবেশ করুন",
                fontFamily = MonoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.background
            )
        }
    }
}

@Composable
private fun StepCard(
    number: String,
    title: String,
    bangla: String,
    english: String,
    isComplete: Boolean,
    buttonLabel: String,
    onAction: () -> Unit
) {
    val colors = LocalKothaColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isComplete) colors.hairline else colors.accent, RoundedCornerShape(2.dp))
            .background(colors.surface)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$number • $title",
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = colors.textPrimary
            )
            Text(
                text = if (isComplete) "[ READY ]" else "[ PENDING ]",
                fontFamily = MonoFont,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isComplete) colors.signalVerdigris else colors.accent
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(text = bangla, fontFamily = DisplayFont, fontSize = 12.sp, color = colors.textPrimary)
        Text(text = english, fontFamily = MonoFont, fontSize = 11.sp, color = colors.textMuted)

        if (!isComplete) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = colors.background),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.hairline, RoundedCornerShape(2.dp))
            ) {
                Text(text = buttonLabel, fontFamily = MonoFont, fontSize = 11.sp, color = colors.accent)
            }
        }
    }
}
