package com.fairydoo.game.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Die Design-Tokens aus dem Handoff „Fairydoku – Feen-Logikpuzzle".
 *
 * Alle Farben des Spiels stehen hier; wird eine geändert, zieht die ganze
 * Oberfläche nach. Die Werte sind bewusst 1:1 aus der Vorlage übernommen.
 */

// Grundflächen — der „gemalte" blaugrün-violette Nachtwald
val NightBase = Color(0xFF0A0E21)
val NightTop = Color(0xFF0A2032)
val NightMiddle = Color(0xFF12283E)
val NightDeep = Color(0xFF1C1F4A)
val NightBottom = Color(0xFF241A4A)
val NightHalo = Color(0xFF0E2E42)

// Die farbigen Schimmer, die den Wald von den Rändern her erhellen:
// rosa und violett unten, türkis und blau seitlich.
val GlowPink = Color(0x47FF78BE)
val GlowViolet = Color(0x42A06EFF)
val GlowTeal = Color(0x2450C8BE)
val GlowBlue = Color(0x245AA0FF)

// Panels und Karten
val PanelTop = Color(0xFF252B52)
val PanelBottom = Color(0xFF151A38)
val PanelBorder = Color(0x7396A5DC)
val PanelGoldBorder = Color(0x66FFD76B)
val PanelText = Color(0xFFF2ECFF)
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
val GoldCream = Color(0xFFFFF2C9)

// Der Verlauf des Titels — Creme oben, warmes Gold unten
val TitleTop = Color(0xFFFFFBE8)
val TitleMiddle = Color(0xFFFFEDB0)
val TitleBottom = Color(0xFFF2C060)

// Weitere Akzente
val LeafGreen = Color(0xFF7DFF9E)
val BlossomPink = Color(0xFFFF9ECF)
val DangerPink = Color(0xFFFF8095)
val DangerRose = Color(0xFFFF9AAC)
val ConflictRed = Color(0xCCFF3C5A)
val FireflyYellow = Color(0xFFFFE9A8)

// Die Steinplatten des Bretts. Beide Töne werden je Feld mit der Zonenfarbe
// leicht eingefärbt — daher „Grundton" und nicht „Farbe".
val StoneLight = Color(0xFF6A7561)
val StoneDark = Color(0xFF48523F)

// Die Moos-Matte, auf der das Gitter liegt
val MossMatTop = Color(0xFF42562F)
val MossMatMiddle = Color(0xFF2C3D20)
val MossMatBottom = Color(0xFF22301A)
val MossMatBorder = Color(0x668CBE5A)

// Die Moosflecken auf den Steinen
val MossPatchA = Color(0x8C3C5A2D)
val MossPatchB = Color(0x80466932)

// Die Kacheln der Fähigkeiten — Bonbon-Relief wie in der Vorlage
val PowerTileTop = Color(0xFF4C5CA6)
val PowerTileMiddle = Color(0xFF2B3670)
val PowerTileBottom = Color(0xFF222C5E)
val PowerTileBorder = Color(0xB3CDDCFF)
val PowerTileShieldTop = Color(0xFF3D7A52)
val PowerTileShieldMiddle = Color(0xFF245536)
val PowerTileShieldBottom = Color(0xFF1C452C)
val PowerTileShieldBorder = Color(0xD9A0FFBE)

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

/**
 * Die Fuge zwischen zwei Feldern derselben Zone.
 *
 * Dunkel statt hell: Neben einem leuchtenden Zonenrand würde eine helle Linie
 * mitleuchten, und man müsste zweimal hinsehen, um Zonengrenze und Feldgrenze
 * auseinanderzuhalten.
 */
val CellSeam = Color(0x59000000)
