package com.fairydoo.game.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Die Design-Tokens aus dem Handoff „Fairydoku – Feen-Logikpuzzle".
 *
 * Alle Farben des Spiels stehen hier; wird eine geändert, zieht die ganze
 * Oberfläche nach. Die Werte sind bewusst 1:1 aus der Vorlage übernommen.
 */

// Grundflächen — tiefblau-violetter Nachtwald
val NightBase = Color(0xFF0A0E21)
val NightTop = Color(0xFF0D1330)
val NightMiddle = Color(0xFF141A3E)
val NightBottom = Color(0xFF1B1440)
val NightHalo = Color(0xFF1C2650)

// Der rosa und blaue Schimmer am unteren Bildrand
val GlowPink = Color(0x2EFF6BB4)
val GlowBlue = Color(0x295BC8FF)

// Panels und Karten
val PanelTop = Color(0xFF2A2F5E)
val PanelBottom = Color(0xFF1C2148)
val CardTop = Color(0xFF232A5C)
val CardBottom = Color(0xFF171C42)

// Text
val TextPrimary = Color(0xFFEAE6FF)
val TextOnGold = Color(0xFF3A2604)
val StatusPurple = Color(0xFFC9C2FF)

// Gold — das Leitmotiv
val GoldLight = Color(0xFFFFE9A8)
val Gold = Color(0xFFFFD76B)
val GoldDark = Color(0xFFE9A53F)
val GoldPale = Color(0xFFFFF3C8)

// Weitere Akzente
val LeafGreen = Color(0xFF7DFF9E)
val BlossomPink = Color(0xFFFF9ECF)
val DangerPink = Color(0xFFFF8095)
val DangerRose = Color(0xFFFF9AAC)
val ConflictRed = Color(0xCCFF3C5A)
val FireflyYellow = Color(0xFFFFE9A8)

// Moosige Steinfelder — zwei Varianten im Schachbrettwechsel
val MossLightA = Color(0xFF4A5D3F)
val MossDarkA = Color(0xFF35452E)
val MossLightB = Color(0xFF55684A)
val MossDarkB = Color(0xFF3B4C33)

/**
 * Farben der Waldzonen, in der Reihenfolge der Vorlage.
 *
 * Acht Stück, weil das Gitter auf 8×8 wächst und jede Zone unterscheidbar
 * bleiben muss.
 */
val RegionColors: List<Color> = listOf(
    Color(0xFFFF6B8A),
    Color(0xFF5BC8FF),
    Color(0xFF7DFF9E),
    Color(0xFFFFD76B),
    Color(0xFFC58BFF),
    Color(0xFFFF9A5B),
    Color(0xFF6BFFF2),
    Color(0xFFFF9ECF),
)

/** Zellgrenzen innerhalb derselben Zone. */
val FaintBorder = Color(0x1AFFFFFF)
