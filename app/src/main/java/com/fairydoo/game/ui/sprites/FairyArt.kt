package com.fairydoo.game.ui.sprites

import androidx.annotation.DrawableRes
import com.fairydoo.game.R
import com.fairydoo.game.game.FairySpecies

/**
 * Die Bilder der zehn Feen.
 *
 * Gezeichnete Pixel-Art, aus einer gemeinsamen Vorlage freigestellt und
 * einheitlich in ein 256×256-Feld eingepasst — alle Figuren stehen auf
 * derselben Grundlinie und sind gleich hoch, damit das Brett ruhig wirkt.
 *
 * Die Zuordnung ist ein `when` über alle Enum-Werte statt einer Map: Kommt eine
 * elfte Fee dazu, bricht der Compiler an genau dieser Stelle, statt dass zur
 * Laufzeit ein leeres Feld erscheint.
 */
@get:DrawableRes
val FairySpecies.drawableRes: Int
    get() = when (this) {
        FairySpecies.Flora -> R.drawable.fairy_flora
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
