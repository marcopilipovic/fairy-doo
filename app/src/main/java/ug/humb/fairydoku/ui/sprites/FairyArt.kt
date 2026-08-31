package ug.humb.fairydoku.ui.sprites

import androidx.annotation.DrawableRes
import ug.humb.fairydoku.R
import ug.humb.fairydoku.game.FairySpecies

/**
 * Die Bilder der zehn Feen.
 *
 * Vektorzeichnungen aus dem Handoff „Feen schlicht": flache Farbflächen, eine
 * Kontur, ein Gesicht aus zwei Punkten und einem Bogen. Alle im selben Raster
 * (120 × 164), damit die Figuren auf dem Brett gleich hoch stehen und auf
 * derselben Grundlinie sitzen.
 *
 * Jede Fee trägt ihre eigene Farbe — nicht die ihrer Zone. Das ist Absicht:
 * [ug.humb.fairydoku.game.GameState.speciesForZone] dreht die Feen über die
 * Level so durch, dass jede einmal in jeder Zone vorkommt. Wäre die Farbe die
 * der Zone, wäre die Fee als Figur nicht wiedererkennbar.
 *
 * Die Zuordnung ist ein `when` über alle Enum-Werte statt einer Map: Kommt eine
 * elfte Fee dazu, bricht der Compiler an genau dieser Stelle, statt dass zur
 * Laufzeit ein leeres Feld erscheint.
 */
@get:DrawableRes
val FairySpecies.drawableRes: Int
    get() = when (this) {
        FairySpecies.Viridis -> R.drawable.fairy_viridis
        FairySpecies.Nebula -> R.drawable.fairy_nebula
        FairySpecies.Salta -> R.drawable.fairy_salta
        FairySpecies.Aura -> R.drawable.fairy_aura
        FairySpecies.Nixie -> R.drawable.fairy_nixie
        FairySpecies.Zephyr -> R.drawable.fairy_zephyr
        FairySpecies.Ignis -> R.drawable.fairy_ignis
        FairySpecies.Terra -> R.drawable.fairy_terra
        FairySpecies.Chrono -> R.drawable.fairy_chrono
        FairySpecies.Trixie -> R.drawable.fairy_trixie
    }
