package com.fairydoo.game.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ── DESIGN-ANSCHLUSS ──────────────────────────────────────────────────────────
 * Alle Farben des Spiels stehen hier. Wenn das finale Design kommt, werden nur
 * diese Werte ersetzt — kein anderer Code muss angefasst werden.
 * Aktuell: Platzhalter-Palette (dunkel, märchenhaft).
 */

// Marken-/Akzentfarben
val FairyPink = Color(0xFFFFB9F2)
val FairyPinkDark = Color(0xFFC77BB8)
val FairyCyan = Color(0xFF8FE3FF)
val FairyCyanDark = Color(0xFF4FA8C9)
val FairyGold = Color(0xFFFFD98F)

// Flächen (Dark)
val NightDeep = Color(0xFF0D0A18)
val NightBase = Color(0xFF141024)
val NightSurface = Color(0xFF1E1834)
val NightSurfaceHigh = Color(0xFF2A2247)

// Flächen (Light)
val DayBase = Color(0xFFFDF7FF)
val DaySurface = Color(0xFFFFFFFF)
val DaySurfaceHigh = Color(0xFFF2E9FA)

// Text
val TextOnDark = Color(0xFFF4EEFF)
val TextOnDarkMuted = Color(0xFFB4A9CC)
val TextOnLight = Color(0xFF1F1A2E)
val TextOnLightMuted = Color(0xFF5D5473)

// Status
val SuccessGreen = Color(0xFF7BE0A5)
val WarningAmber = Color(0xFFFFC46B)
val ErrorRed = Color(0xFFFF8A8A)

/**
 * Farben der Waldzonen — Mondlicht-Lichtung, Pilzkreis, Flussbett und so fort.
 *
 * Neun Stück, weil das Gitter auf 9×9 wächst und jede Zone unterscheidbar
 * bleiben muss. Bewusst in Farbton *und* Helligkeit gestreut, damit sie sich
 * auch bei Farbsehschwäche auseinanderhalten lassen.
 */
val RegionColors: List<Color> = listOf(
    Color(0xFFE87BA8), // Rosenblüte
    Color(0xFF6FC5E8), // Flussbett
    Color(0xFF8BD86F), // Moos
    Color(0xFFF2C14E), // Mondlicht
    Color(0xFFB08BE8), // Dämmerung
    Color(0xFFE89A6F), // Pilzkreis
    Color(0xFF5FD3B6), // Farnschatten
    Color(0xFFE8E27B), // Glühwürmchen
    Color(0xFF9AA8E8), // Nebelhain
)
