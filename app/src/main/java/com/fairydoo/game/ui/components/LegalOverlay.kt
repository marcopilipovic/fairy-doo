package com.fairydoo.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairydoo.game.ui.GameCopy
import com.fairydoo.game.ui.LegalPage
import com.fairydoo.game.ui.theme.CardBottom
import com.fairydoo.game.ui.theme.CardTop
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.GoldLight
import com.fairydoo.game.ui.theme.TextOnGold
import com.fairydoo.game.ui.theme.TextPrimary

/**
 * Eine der drei Rechtliches-Seiten (Impressum/AGB/Datenschutz).
 *
 * Eine Karte für alle drei statt drei eigener Screens: Titel und Text kommen
 * aus [GameCopy], die Darstellung bleibt gleich. Die Platzhaltertexte sind
 * absichtlich als Platzhalter erkennbar — vor Veröffentlichung durch echte,
 * juristisch geprüfte Texte ersetzen.
 */
@Composable
fun LegalOverlay(page: LegalPage, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080A1C).copy(alpha = 0.85f))
            .pointerInput(Unit) { detectTapGestures { onClose() } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 340.dp)
                .heightIn(max = 560.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(CardTop, CardBottom)),
                    shape = RoundedCornerShape(22.dp),
                )
                .border(1.5.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(horizontal = 22.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = GameCopy.legalTitle(page),
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 19.sp,
                color = Gold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = GameCopy.legalBody(page),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.5.sp,
                lineHeight = 20.sp,
                color = TextPrimary.copy(alpha = 0.9f),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            )

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(listOf(GoldLight, Gold)),
                        shape = CircleShape,
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose,
                    )
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Zurück", style = MaterialTheme.typography.labelLarge, color = TextOnGold)
            }
        }
    }
}
