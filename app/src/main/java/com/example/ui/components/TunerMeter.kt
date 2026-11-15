package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.PitchZone
import com.example.ui.TunerUiState
import com.example.ui.theme.AmberDrift
import com.example.ui.theme.CrimsonSharp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldSoft
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioHairline
import com.example.ui.theme.TextCrisp
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TunerGaugeCard(
    uiState: TunerUiState,
    modifier: Modifier = Modifier
) {
    val activeAccent by animateColorAsState(
        targetValue = when (uiState.pitchZone) {
            PitchZone.IN_TUNE -> NeonEmerald
            PitchZone.FLAT_CLOSE, PitchZone.SHARP_CLOSE -> AmberDrift
            PitchZone.FLAT_FAR, PitchZone.SHARP_FAR -> CrimsonSharp
            PitchZone.IDLE -> if (uiState.lockedString != null) NeonEmerald.copy(alpha = 0.7f) else TextDim
        },
        animationSpec = tween(durationMillis = 180),
        label = "gauge_accent"
    )

    val animatedCents by animateFloatAsState(
        targetValue = if (uiState.pitchZone == PitchZone.IDLE) 0f else uiState.centsDeviation.coerceIn(-50f, 50f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "needle_cents"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "idle_pulse")
    val idleGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_glow"
    )

    Box(
        modifier = modifier
            .testTag("tuner_gauge_card")
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        activeAccent.copy(
                            alpha = when (uiState.pitchZone) {
                                PitchZone.IN_TUNE -> 0.16f
                                PitchZone.IDLE -> 0.04f
                                else -> 0.09f
                            }
                        ),
                        Color.Transparent
                    ),
                    radius = 700f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Precision Arc + Center Note Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                MinimalGaugeArc(
                    cents = animatedCents,
                    isSignalActive = uiState.pitchZone != PitchZone.IDLE,
                    isInTune = uiState.pitchZone == PitchZone.IN_TUNE,
                    accentColor = activeAccent,
                    modifier = Modifier.fillMaxSize()
                )

                // Center Focal Readout
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 34.dp)
                ) {
                    val contextLabel = when {
                        !uiState.isAutoMode && uiState.lockedString != null ->
                            "${uiState.lockedString.stringNumber}ª CORDA"
                        uiState.detectedString != null ->
                            "${uiState.detectedString.stringNumber}ª CORDA"
                        uiState.activePreset.isChromatic -> "CROMÁTICO"
                        else -> "AUTOMÁTICO"
                    }

                    Text(
                        text = contextLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = 1.6.sp,
                        color = if (!uiState.isAutoMode) NeonEmerald.copy(alpha = 0.85f) else TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val note = uiState.detectedNoteName
                    if (note != null) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = note,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 86.sp,
                                lineHeight = 86.sp,
                                color = if (uiState.pitchZone == PitchZone.IN_TUNE) NeonEmerald else TextCrisp
                            )
                            Column(
                                modifier = Modifier.padding(top = 10.dp, start = 3.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                if (uiState.detectedAccidental.isNotEmpty()) {
                                    Text(
                                        text = uiState.detectedAccidental,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = activeAccent
                                    )
                                }
                                uiState.detectedOctave?.let { oct ->
                                    Text(
                                        text = oct.toString(),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 18.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "—",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 78.sp,
                            lineHeight = 86.sp,
                            color = TextDim.copy(alpha = idleGlowAlpha + 0.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Minimalist Guidance Status
                    MinimalGuidanceBadge(
                        uiState = uiState,
                        accentColor = activeAccent
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Clean Tabular Telemetry
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val centsLabel = if (uiState.pitchZone != PitchZone.IDLE) {
                            val rounded = animatedCents.roundToInt()
                            if (rounded > 0) "+$rounded cents" else "$rounded cents"
                        } else {
                            "0 cents"
                        }
                        Text(
                            text = centsLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (uiState.pitchZone == PitchZone.IDLE) TextDim else activeAccent
                        )

                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(StudioHairline)
                        )

                        val hzLabel = uiState.detectedFrequencyHz?.let {
                            String.format(Locale.US, "%.1f Hz", it)
                        } ?: uiState.targetFrequencyHz?.let {
                            String.format(Locale.US, "Alvo %.1f Hz", it)
                        } ?: "A4 440 Hz"

                        Text(
                            text = hzLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalGaugeArc(
    cents: Float,
    isSignalActive: Boolean,
    isInTune: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val trackStroke = 4.5.dp.toPx()
        val activeStroke = 5.5.dp.toPx()
        val radius = minOf(size.width * 0.43f, size.height * 0.62f).coerceAtLeast(64f)
        val center = Offset(size.width / 2f, size.height * 0.62f)
        val arcTopLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2f, radius * 2f)

        val startAngle = 175f
        val totalSweep = 190f
        val halfSweep = totalSweep / 2f

        // Hairline background track
        drawArc(
            color = StudioElevated,
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = trackStroke, cap = StrokeCap.Round)
        )

        // Subtle center sweet-spot target
        drawArc(
            color = if (isInTune) NeonEmerald else NeonEmerald.copy(alpha = 0.24f),
            startAngle = 270f - 5.5f,
            sweepAngle = 11f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = activeStroke + 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Active sweep arc
        if (isSignalActive) {
            val normalized = (cents / 50f).coerceIn(-1f, 1f)
            drawArc(
                color = accentColor.copy(alpha = 0.92f),
                startAngle = 270f,
                sweepAngle = normalized * halfSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = activeStroke, cap = StrokeCap.Round)
            )
        }

        // Minimalist outer tick marks (only 11 clean marks across -50..+50)
        val tickOuter = radius + 15.dp.toPx()
        for (c in -50..50 step 10) {
            val angleDeg = 270f + (c / 50f) * halfSweep
            val rad = angleDeg * (PI / 180.0)
            val isCenter = c == 0
            val isQuarter = c == -25 || c == 25 || c == -50 || c == 50
            val tickLen = if (isCenter) 11.dp.toPx() else if (isQuarter) 7.dp.toPx() else 4.dp.toPx()
            val innerR = tickOuter - tickLen
            val p1 = Offset(
                center.x + (innerR * cos(rad)).toFloat(),
                center.y + (innerR * sin(rad)).toFloat()
            )
            val p2 = Offset(
                center.x + (tickOuter * cos(rad)).toFloat(),
                center.y + (tickOuter * sin(rad)).toFloat()
            )
            val tickColor = when {
                isCenter -> if (isInTune) NeonEmerald else NeonEmerald.copy(alpha = 0.7f)
                isQuarter -> TextMuted.copy(alpha = 0.5f)
                else -> StudioHairline
            }
            drawLine(
                color = tickColor,
                start = p1,
                end = p2,
                strokeWidth = if (isCenter) 2.5.dp.toPx() else 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Clean glowing dot indicator
        val pointerCents = if (isSignalActive) cents else 0f
        val pointerAngleDeg = 270f + (pointerCents / 50f).coerceIn(-1f, 1f) * halfSweep
        val pointerRad = pointerAngleDeg * (PI / 180.0)
        val dotCenter = Offset(
            center.x + (radius * cos(pointerRad)).toFloat(),
            center.y + (radius * sin(pointerRad)).toFloat()
        )

        if (isSignalActive) {
            drawCircle(
                color = accentColor.copy(alpha = 0.25f),
                radius = 13.dp.toPx(),
                center = dotCenter
            )
            drawCircle(
                color = accentColor,
                radius = 6.dp.toPx(),
                center = dotCenter
            )
            drawCircle(
                color = TextCrisp,
                radius = 2.5.dp.toPx(),
                center = dotCenter
            )
        } else {
            drawCircle(
                color = TextDim.copy(alpha = 0.6f),
                radius = 3.5.dp.toPx(),
                center = dotCenter
            )
        }
    }
}

@Composable
private fun MinimalGuidanceBadge(
    uiState: TunerUiState,
    accentColor: Color
) {
    when (uiState.pitchZone) {
        PitchZone.IN_TUNE -> {
            Surface(
                shape = RoundedCornerShape(50),
                color = NeonEmeraldSoft
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = stringResource(R.string.status_in_tune),
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonEmerald
                    )
                }
            }
        }
        PitchZone.FLAT_FAR, PitchZone.FLAT_CLOSE -> {
            Text(
                text = "◄  Aperte a tarraxa",
                style = MaterialTheme.typography.labelLarge,
                color = accentColor
            )
        }
        PitchZone.SHARP_FAR, PitchZone.SHARP_CLOSE -> {
            Text(
                text = "Afrouxe a tarraxa  ►",
                style = MaterialTheme.typography.labelLarge,
                color = accentColor
            )
        }
        PitchZone.IDLE -> {
            val text = if (!uiState.isAutoMode && uiState.lockedString != null) {
                "Toque a ${uiState.lockedString.stringNumber}ª corda (${uiState.lockedString.fullLabel})"
            } else {
                stringResource(R.string.status_listening)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
    }
}
