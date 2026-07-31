package com.fairydoo.game.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fairydoo.game.R

/**
 * Die beiden Schriften aus dem Handoff.
 *
 * Quicksand liegt nur als Variable Font vor; die Schnitte entstehen deshalb
 * über [FontVariation] statt über einzelne Dateien. Das setzt API 26 voraus —
 * genau unser minSdk.
 */
@OptIn(ExperimentalTextApi::class)
private fun quicksand(weight: FontWeight) = Font(
    resId = R.font.quicksand_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Titel und Overlay-Überschriften. */
val DecorativeFont = FontFamily(
    Font(R.font.cinzel_decorative_bold, FontWeight.Bold),
    Font(R.font.cinzel_decorative_black, FontWeight.Black),
)

/** Alles andere in der Oberfläche. */
val UiFont = FontFamily(
    quicksand(FontWeight.Normal),
    quicksand(FontWeight.Medium),
    quicksand(FontWeight.SemiBold),
    quicksand(FontWeight.Bold),
)

val FairyDooTypography = Typography(
    // „Fairydoku" im Kopf
    displayLarge = TextStyle(
        fontFamily = DecorativeFont,
        fontWeight = FontWeight.Black,
        fontSize = 34.sp,
        letterSpacing = 1.sp,
    ),
    // „LEVEL UP!"
    headlineLarge = TextStyle(
        fontFamily = DecorativeFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
    ),
    // Überschriften in Intro und Spielende
    headlineMedium = TextStyle(
        fontFamily = DecorativeFont,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = UiFont,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        letterSpacing = 1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = UiFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = UiFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 21.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = UiFont,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = UiFont,
        fontWeight = FontWeight.Bold,
        fontSize = 11.5.sp,
        lineHeight = 14.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = UiFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
    ),
)
