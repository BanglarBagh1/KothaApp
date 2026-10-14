package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.settings.AccentInk
import com.example.settings.AppTheme

data class KothaInkColors(
    val background: Color,
    val surface: Color,
    val hairline: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val accent: Color,
    val signalOchre: Color,
    val signalVerdigris: Color,
    val signalCrimson: Color
)

val LocalKothaColors = staticCompositionLocalOf {
    KothaInkColors(
        background = InkBackground,
        surface = InkRaised,
        hairline = InkHairline,
        textPrimary = PaperText,
        textMuted = MutedText,
        accent = SignalVermilion,
        signalOchre = SignalOchre,
        signalVerdigris = SignalVerdigris,
        signalCrimson = SignalCrimson
    )
}

@Composable
fun KothaTheme(
    appTheme: AppTheme = AppTheme.DARK,
    accentInk: AccentInk = AccentInk.VERMILION,
    content: @Composable () -> Unit
) {
    val accentColor = Color(accentInk.hexColor)

    val inkColors = when (appTheme) {
        AppTheme.DARK -> KothaInkColors(
            background = InkBackground,
            surface = InkRaised,
            hairline = InkHairline,
            textPrimary = PaperText,
            textMuted = MutedText,
            accent = accentColor,
            signalOchre = SignalOchre,
            signalVerdigris = SignalVerdigris,
            signalCrimson = SignalCrimson
        )
        AppTheme.PAPER -> KothaInkColors(
            background = PaperLightBackground,
            surface = PaperLightRaised,
            hairline = PaperLightHairline,
            textPrimary = PaperLightText,
            textMuted = PaperLightMuted,
            accent = accentColor,
            signalOchre = Color(0xFFC98E20),
            signalVerdigris = Color(0xFF2F8577),
            signalCrimson = Color(0xFFA8262E)
        )
        AppTheme.AMOLED -> KothaInkColors(
            background = AmoledBackground,
            surface = AmoledRaised,
            hairline = AmoledHairline,
            textPrimary = PaperText,
            textMuted = MutedText,
            accent = accentColor,
            signalOchre = SignalOchre,
            signalVerdigris = SignalVerdigris,
            signalCrimson = SignalCrimson
        )
    }

    val colorScheme = if (appTheme == AppTheme.PAPER) {
        lightColorScheme(
            primary = inkColors.accent,
            onPrimary = PaperLightBackground,
            background = inkColors.background,
            onBackground = inkColors.textPrimary,
            surface = inkColors.surface,
            onSurface = inkColors.textPrimary,
            outline = inkColors.hairline
        )
    } else {
        darkColorScheme(
            primary = inkColors.accent,
            onPrimary = InkBackground,
            background = inkColors.background,
            onBackground = inkColors.textPrimary,
            surface = inkColors.surface,
            onSurface = inkColors.textPrimary,
            outline = inkColors.hairline
        )
    }

    CompositionLocalProvider(LocalKothaColors provides inkColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KothaTypography,
            content = content
        )
    }
}
