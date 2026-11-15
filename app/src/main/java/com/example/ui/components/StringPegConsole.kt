package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.StringNote
import com.example.ui.PitchZone
import com.example.ui.TunerUiState
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldSoft
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioConsole
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.TextCrisp
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import java.util.Locale

@Composable
fun StringPegConsole(
    uiState: TunerUiState,
    onStringTap: (StringNote) -> Unit,
    onToggleAutoMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("string_peg_console"),
        shape = RoundedCornerShape(24.dp),
        color = StudioConsole,
        border = BorderStroke(1.dp, StudioBorder),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Auto / Manual toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.activePreset.isChromatic) {
                            "Cordas de referência"
                        } else {
                            "Cordas • ${uiState.activePreset.namePt}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextCrisp
                    )
                    Text(
                        text = stringResource(R.string.reference_tone_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                // Auto/Manual Toggle Chip
                val isAuto = uiState.isAutoMode
                Surface(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            role = Role.Button,
                            onClickLabel = stringResource(R.string.cd_toggle_auto),
                            onClick = onToggleAutoMode
                        )
                        .testTag("mode_toggle_button"),
                    shape = RoundedCornerShape(50),
                    color = if (isAuto) NeonEmeraldSoft else StudioElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isAuto) NeonEmerald.copy(alpha = 0.55f) else StudioBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isAuto) Icons.Rounded.AutoAwesome else Icons.Rounded.Lock,
                            contentDescription = stringResource(R.string.cd_toggle_auto),
                            tint = if (isAuto) NeonEmerald else TextCrisp,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAuto) {
                                stringResource(R.string.mode_auto)
                            } else {
                                stringResource(R.string.mode_manual)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isAuto) NeonEmerald else TextCrisp
                        )
                    }
                }
            }

            // 6 Strings in 2 rows of 3 (6ª, 5ª, 4ª / 3ª, 2ª, 1ª)
            val strings = uiState.activePreset.strings
            val topRow = strings.take(3)
            val bottomRow = strings.drop(3)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                topRow.forEach { stringNote ->
                    StringPegCard(
                        stringNote = stringNote,
                        isSelected = uiState.lockedString?.stringNumber == stringNote.stringNumber,
                        isDetected = uiState.detectedString?.stringNumber == stringNote.stringNumber,
                        isInTune = uiState.pitchZone == PitchZone.IN_TUNE &&
                            uiState.detectedString?.stringNumber == stringNote.stringNumber,
                        isPlayingTone = uiState.playingStringNumber == stringNote.stringNumber,
                        onClick = { onStringTap(stringNote) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                bottomRow.forEach { stringNote ->
                    StringPegCard(
                        stringNote = stringNote,
                        isSelected = uiState.lockedString?.stringNumber == stringNote.stringNumber,
                        isDetected = uiState.detectedString?.stringNumber == stringNote.stringNumber,
                        isInTune = uiState.pitchZone == PitchZone.IN_TUNE &&
                            uiState.detectedString?.stringNumber == stringNote.stringNumber,
                        isPlayingTone = uiState.playingStringNumber == stringNote.stringNumber,
                        onClick = { onStringTap(stringNote) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StringPegCard(
    stringNote: StringNote,
    isSelected: Boolean,
    isDetected: Boolean,
    isInTune: Boolean,
    isPlayingTone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val active = isSelected || isDetected || isPlayingTone
    val borderColor by animateColorAsState(
        targetValue = when {
            isInTune || isPlayingTone -> NeonEmerald
            isSelected -> NeonEmerald.copy(alpha = 0.75f)
            isDetected -> NeonEmerald.copy(alpha = 0.45f)
            else -> StudioBorder
        },
        animationSpec = tween(160),
        label = "peg_border"
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            isInTune || isPlayingTone -> NeonEmeraldSoft
            isSelected -> StudioElevated
            isDetected -> StudioElevated
            else -> StudioObsidian.copy(alpha = 0.72f)
        },
        animationSpec = tween(160),
        label = "peg_bg"
    )

    // String thickness bar proportional to string gauge (6th thickest -> 1st thinnest)
    val gaugeHeight = when (stringNote.stringNumber) {
        6 -> 4.dp
        5 -> 3.4.dp
        4 -> 2.8.dp
        3 -> 2.2.dp
        2 -> 1.7.dp
        else -> 1.2.dp
    }

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.cd_play_tone, stringNote.fullLabel),
                onClick = onClick
            )
            .testTag("string_peg_${stringNote.stringNumber}"),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(if (active) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual string gauge line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(gaugeHeight)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (active) NeonEmerald else TextDim.copy(alpha = 0.7f)
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) NeonEmerald.copy(alpha = 0.2f) else StudioElevated
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${stringNote.stringNumber}ª",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (active) NeonEmerald else TextMuted
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = stringResource(R.string.cd_play_tone, stringNote.fullLabel),
                    tint = if (isPlayingTone) NeonEmerald else TextDim,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringNote.shortLabel,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = if (active) TextCrisp else TextCrisp.copy(alpha = 0.9f)
                )
                Text(
                    text = stringNote.octave.toString(),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (active) NeonEmerald else TextMuted,
                    modifier = Modifier.padding(bottom = 2.dp, start = 1.dp)
                )
            }

            Text(
                text = String.format(Locale.US, "%.1f Hz", stringNote.frequencyHz),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}
