package com.fairydoo.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairydoo.game.game.DailyReward
import com.fairydoo.game.game.DailyScoreState
import com.fairydoo.game.game.DailyScoring
import com.fairydoo.game.game.DailySettlement
import com.fairydoo.game.ui.GameCopy
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.GoldCream
import com.fairydoo.game.ui.theme.LeafGreen
import com.fairydoo.game.ui.theme.PanelBottom
import com.fairydoo.game.ui.theme.PanelGoldBorder
import com.fairydoo.game.ui.theme.PanelTop
import com.fairydoo.game.ui.theme.StatusPurple
import com.fairydoo.game.ui.theme.TextPrimary

/**
 * Die Tageswertung, aufgeklappt.
 *
 * Erste Ausbaustufe: Es gibt noch keine Mitspielerinnen, deshalb stehen hier
 * zwei Zeilen — heute und der beste Tag bisher. Die Fläche ist bewusst schon
 * die einer Rangliste. Kommen später echte Namen dazu, wächst die Liste nach
 * unten, ohne dass sich der Aufbau ändert.
 */
@Composable
fun DailyScoreOverlay(daily: DailyScoreState, onClose: () -> Unit) {
    OverlayScaffold(
        borderColor = Gold.copy(alpha = 0.5f),
        entrance = OverlayEntrance.RiseUp,
        scrimAlpha = 0.8f,
    ) {
        Text(
            text = "Der heutige Tag",
            style = MaterialTheme.typography.headlineMedium,
            color = GoldCream,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Punkte zählen bis zum Tageswechsel. Danach gibt es die Belohnung, und der Wald beginnt von vorn.",
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary.copy(alpha = 0.78f),
            fontSize = 11.5.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(14.dp))

        ScoreRow(icon = "🌅", label = "Heute", value = daily.points, highlighted = true)

        Spacer(Modifier.height(6.dp))

        // Erst zeigen, wenn es überhaupt einen abgeschlossenen Tag gab — sonst
        // stünde am ersten Tag eine Null als „Bestleistung" daneben.
        if (daily.bestPoints > 0) {
            ScoreRow(icon = "🏆", label = "Bester Tag", value = daily.bestPoints, highlighted = false)
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(8.dp))

        RewardHint(points = daily.points)

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Neuer Tag in ${GameCopy.formatWaitTime(daily.remainingSeconds)}",
            style = MaterialTheme.typography.labelSmall,
            color = StatusPurple,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        GoldButton(label = "Weiterrätseln", onClick = onClose)
    }
}

/**
 * Der Tagesabschluss — der eigentliche Grund, am nächsten Tag wiederzukommen.
 *
 * Erscheint beim ersten Start nach dem Stichtag, und nur dann, wenn am
 * abgerechneten Tag überhaupt gespielt wurde. Ein Overlay über null Punkte wäre
 * keine Belohnung, sondern eine Mahnung.
 */
@Composable
fun DailySettlementOverlay(settlement: DailySettlement, onContinue: () -> Unit) {
    OverlayScaffold(
        borderColor = Gold.copy(alpha = 0.6f),
        entrance = OverlayEntrance.PopIn,
        scrimAlpha = 0.82f,
    ) {
        Text(text = "🧚‍♀️✨", fontSize = 40.sp)

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Die Nacht ist vorüber",
            style = MaterialTheme.typography.headlineMedium,
            color = Gold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Du hast ${settlement.points} Punkte gesammelt.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        if (settlement.wasBest) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Dein bester Tag bisher!",
                style = MaterialTheme.typography.bodyLarge,
                color = LeafGreen,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(16.dp))

        if (settlement.reward.isEmpty) {
            Text(
                text = "Für eine Belohnung hat es diesmal nicht gereicht — heute ist ein neuer Tag.",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary.copy(alpha = 0.8f),
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = "Der Wald schenkt dir",
                style = MaterialTheme.typography.labelSmall,
                color = StatusPurple,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            RewardChips(settlement.reward)
        }

        Spacer(Modifier.height(18.dp))

        GoldButton(label = "Weiter", onClick = onContinue)
    }
}

/** Eine Zeile der Wertung — dieselbe Form, die später eine Ranglistenzeile trägt. */
@Composable
private fun ScoreRow(icon: String, label: String, value: Int, highlighted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (highlighted) Gold.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
            )
            .border(
                width = if (highlighted) 1.dp else 0.dp,
                color = if (highlighted) PanelGoldBorder else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$icon  $label",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleLarge,
            color = if (highlighted) GoldCream else TextPrimary,
        )
    }
}

/**
 * Was der aktuelle Stand einbringt — und was der nächste Schritt brächte.
 *
 * Das erreichbare Ziel steht vor dem erreichten Stand: „noch 480 Punkte" gibt
 * einen Grund weiterzuspielen, „du hast 1 Feenstaub sicher" nicht.
 */
@Composable
private fun RewardHint(points: Int) {
    val next = DailyScoring.nextTier(points)
    val current = DailyScoring.rewardFor(points)

    if (next != null) {
        val (missing, reward) = next
        Text(
            text = "Noch $missing Punkte bis ${rewardLabel(reward)}",
            style = MaterialTheme.typography.labelSmall,
            color = GoldCream,
            textAlign = TextAlign.Center,
        )
    } else {
        Text(
            text = "Höchste Stufe erreicht — mehr geht heute nicht.",
            style = MaterialTheme.typography.labelSmall,
            color = LeafGreen,
            textAlign = TextAlign.Center,
        )
    }

    if (!current.isEmpty) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Sicher ist dir schon:",
            style = MaterialTheme.typography.labelSmall,
            color = StatusPurple,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        RewardChips(current)
    }
}

/** Die Belohnung als Plaketten — dieselbe Panel-Form wie die Badges auf der Karte. */
@Composable
private fun RewardChips(reward: DailyReward) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (reward.fairyDust > 0) RewardChip("✨ ${reward.fairyDust} Feenstaub")
        if (reward.irrlicht > 0) RewardChip("🔮 ${reward.irrlicht} Irrlicht")
    }
}

@Composable
private fun RewardChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(listOf(PanelTop, PanelBottom)),
                shape = RoundedCornerShape(12.dp),
            )
            .border(1.dp, PanelGoldBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = GoldCream,
        )
    }
}

/** „✨ 1 Feenstaub und 🔮 1 Irrlicht" — für Fließtext, nicht für Plaketten. */
private fun rewardLabel(reward: DailyReward): String = buildList {
    if (reward.fairyDust > 0) add("✨ ${reward.fairyDust} Feenstaub")
    if (reward.irrlicht > 0) add("🔮 ${reward.irrlicht} Irrlicht")
}.joinToString(" und ")
