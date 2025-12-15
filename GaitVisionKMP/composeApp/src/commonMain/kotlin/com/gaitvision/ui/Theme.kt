package com.gaitvision.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Color Palette
// Clinical Color Palette
val MedicalBlue = Color(0xFF0052CC) // Deep trustworthy blue
val MedicalBlueVariant = Color(0xFF003D99)
val TealAccent = Color(0xFF00B8D4) // Modern, clean accent
val CleanWhite = Color(0xFFFFFFFF)
val LightGrayBackground = Color(0xFFF5F7FA) // Soft clinical background
val TextPrimary = Color(0xFF172B4D) // Dark blue-gray for readability
val TextSecondary = Color(0xFF5E6C84)

private val LightColorPalette = lightColors(
    primary = MedicalBlue,
    primaryVariant = MedicalBlueVariant,
    secondary = TealAccent,
    background = LightGrayBackground,
    surface = CleanWhite,
    onPrimary = CleanWhite,
    onSecondary = CleanWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

// We focus on Light Theme for clinical apps as it feels cleaner/sterile, 
// but Dark Theme can be mapped similarly if needed.
private val DarkColorPalette = darkColors(
    primary = MedicalBlue,
    primaryVariant = MedicalBlueVariant,
    secondary = TealAccent,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = CleanWhite,
    onSecondary = Color.Black,
    onBackground = CleanWhite,
    onSurface = CleanWhite
)

val AppTypography = Typography(
    h1 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = TextPrimary
    ),
    h4 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        color = TextPrimary
    ),
    h6 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        color = TextPrimary
    ),
    body1 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = TextSecondary
    ),
    button = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 1.sp
    )
)

@Composable
fun GaitVisionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = AppTypography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
