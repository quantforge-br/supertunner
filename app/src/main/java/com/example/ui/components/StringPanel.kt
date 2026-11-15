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
import com.example.ui.theme.StudioConsole
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioHairline
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.TextCrisp
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted

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
        border = BorderStroke(1.dp, StudioHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Compact single-line header: subtle hint + Auto/Manual toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = if (uiState.playingStringNumber != null) NeonEmerald else TextDim,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (uiState.playingStringNumber != null) {
                            "Reproduzindo ${uiState.playingStringNumber}ª corda"
                        } else {
                            "Toque na corda para ouvir"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.playingStringNumber != null) NeonEmerald else TextMuted
                    )
                }

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
                    color = if (isAuto) NeonEmeraldSoft else StudioElevated
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (isAuto) Icons.Rounded.AutoAwesome else Icons.Rounded.Lock,
                            contentDescription = stringResource(R.string.cd_toggle_auto),
                            tint = if (isAuto) NeonEmerald else TextCrisp,
                            modifier = Modifier.size(14.dp)
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

            // Sleek single horizontal row of 6 string pegs (6ª -> 1ª)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                uiState.activePreset.strings.forEach { stringNote ->
                    MinimalStringPeg(
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
private fun MinimalStringPeg(
    stringNote: StringNote,
    isSelected: Boolean,
    isDetected: Boolean,
    isInTune: Boolean,
    isPlayingTone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighlighted = isSelected || isDetected || isPlayingTone
    val bgColor by animateColorAsState(
        targetValue = when {
            isInTune || isPlayingTone -> NeonEmeraldSoft
            isSelected -> StudioElevated
            isDetected -> StudioElevated.copy(alpha = 0.85f)
            else -> StudioObsidian.copy(alpha = 0.65f)
        },
        animationSpec = tween(150),
        label = "peg_bg"
    )
    val accentColor by animateColorAsState(
        targetValue = when {
            isInTune || isPlayingTone || isSelected -> NeonEmerald
            isDetected -> TextCrisp
            else -> TextDim
        },
        animationSpec = tween(150),
        label = "peg_accent"
    )

    // Subtle string thickness indicator
    val gaugeHeight = when (stringNote.stringNumber) {
        6 -> 3.5.dp
        5 -> 3.dp
        4 -> 2.5.dp
        3 -> 2.dp
        2 -> 1.5.dp
        else -> 1.dp
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
        border = if (isHighlighted) {
            BorderStroke(1.2.dp, accentColor.copy(alpha = 0.65f))
        } else {
            BorderStroke(1.dp, StudioHairline.copy(alpha = 0.7f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Minimal gauge bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(gaugeHeight)
                    .clip(RoundedCornerShape(50))
                    .background(if (isHighlighted) accentColor else TextDim.copy(alpha = 0.55f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Primary note letter
            Text(
                text = stringNote.shortLabel,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = if (isHighlighted) TextCrisp else TextCrisp.copy(alpha = 0.86f)
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Subtle string number or active pulse dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (isPlayingTone) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald)
                    )
                }
                Text(
                    text = "${stringNote.stringNumber}ª",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = if (isHighlighted) accentColor else TextMuted
                )
            }
        }
    }
}
