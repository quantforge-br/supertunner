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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.PitchZone
import com.example.ui.TunerUiState
import com.example.ui.theme.AmberDrift
import com.example.ui.theme.AmberDriftSoft
import com.example.ui.theme.CrimsonSharp
import com.example.ui.theme.CrimsonSharpSoft
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldSoft
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioConsole
import com.example.ui.theme.StudioElevated
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
            PitchZone.IDLE -> if (uiState.lockedString != null) NeonEmerald.copy(alpha = 0.65f) else TextDim
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
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_glow"
    )

    val borderColor = if (uiState.pitchZone == PitchZone.IN_TUNE) {
        NeonEmerald.copy(alpha = 0.55f)
    } else {
        StudioBorder
    }

    Surface(
        modifier = modifier.testTag("tuner_gauge_card"),
        shape = RoundedCornerShape(28.dp),
        color = StudioConsole,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            activeAccent.copy(
                                alpha = when (uiState.pitchZone) {
                                    PitchZone.IN_TUNE -> 0.18f
                                    PitchZone.IDLE -> 0.05f
                                    else -> 0.11f
                                }
                            ),
                            Color.Transparent
                        ),
                        radius = 720f
                    )
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top telemetry row: Mode badge + Target string info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isManual = !uiState.isAutoMode && uiState.lockedString != null
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = StudioElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isManual) NeonEmerald.copy(alpha = 0.4f) else StudioBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isManual) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (uiState.isListening) NeonEmerald.copy(alpha = idleGlowAlpha)
                                            else TextDim
                                        )
                                )
                            }
                            Text(
                                text = if (isManual && uiState.lockedString != null) {
                                    "Manual • ${uiState.lockedString.stringNumber}ª Corda"
                                } else if (uiState.activePreset.isChromatic) {
                                    "Cromático"
                                } else {
                                    "Auto • ${uiState.activePreset.namePt}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isManual) NeonEmerald else TextMuted
                            )
                        }
                    }

                    val targetHzText = uiState.targetFrequencyHz?.let {
                        String.format(Locale.US, "Alvo %.1f Hz", it)
                    } ?: "A4 = 440 Hz"
                    Text(
                        text = targetHzText,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                // Center Gauge Canvas + Note Readout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    contentAlignment = Alignment.Center
                ) {
                    GaugeArcCanvas(
                        cents = animatedCents,
                        isSignalActive = uiState.pitchZone != PitchZone.IDLE,
                        isInTune = uiState.pitchZone == PitchZone.IN_TUNE,
                        accentColor = activeAccent,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 6.dp, bottom = 4.dp)
                    )

                    // Center Note + Frequency Overlay
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 28.dp)
                    ) {
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
                                    fontSize = 72.sp,
                                    lineHeight = 72.sp,
                                    color = if (uiState.pitchZone == PitchZone.IN_TUNE) NeonEmerald else TextCrisp
                                )
                                Column(
                                    modifier = Modifier.padding(top = 8.dp, start = 2.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    if (uiState.detectedAccidental.isNotEmpty()) {
                                        Text(
                                            text = uiState.detectedAccidental,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 26.sp,
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
                                fontSize = 68.sp,
                                color = TextDim.copy(alpha = idleGlowAlpha + 0.25f)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Cents & Measured Hz telemetry row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val centsLabel = if (uiState.pitchZone != PitchZone.IDLE) {
                                val rounded = animatedCents.roundToInt()
                                if (rounded > 0) "+$rounded c" else "$rounded c"
                            } else {
                                "0 c"
                            }
                            Text(
                                text = centsLabel,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (uiState.pitchZone == PitchZone.IDLE) TextDim else activeAccent
                            )

                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(StudioBorder)
                            )

                            val measuredHzLabel = uiState.detectedFrequencyHz?.let {
                                String.format(Locale.US, "%.1f Hz", it)
                            } ?: "--.- Hz"
                            Text(
                                text = measuredHzLabel,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Guidance Status Pill
                PitchGuidancePill(
                    uiState = uiState,
                    accentColor = activeAccent
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Live Audio Waveform Strip
                StudioWaveformStrip(
                    bars = uiState.waveformBars,
                    accentColor = activeAccent,
                    isActive = uiState.audioLevel > 0.04f
                )
            }
        }
    }
}

@Composable
private fun GaugeArcCanvas(
    cents: Float,
    isSignalActive: Boolean,
    isInTune: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 10.dp.toPx()
        val radius = (minOf(size.width * 0.44f, size.height * 0.62f)).coerceAtLeast(60f)
        val center = Offset(size.width / 2f, size.height * 0.64f)
        val arcTopLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2f, radius * 2f)

        // Arc spans from 170° (left, -50 cents) through 270° (top, 0 cents) to 370° (right, +50 cents)
        val startAngle = 170f
        val totalSweep = 200f
        val halfSweep = totalSweep / 2f

        // Base track arc
        drawArc(
            color = StudioElevated,
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Subtle sweet-spot highlight around 0 cents (±3.5 cents -> ~7° each side)
        drawArc(
            color = if (isInTune) NeonEmerald else NeonEmerald.copy(alpha = 0.25f),
            startAngle = 270f - 6.5f,
            sweepAngle = 13f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth + 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Active deviation sweep from center (270°) to current cents angle
        if (isSignalActive) {
            val normalized = (cents / 50f).coerceIn(-1f, 1f)
            val sweepFromCenter = normalized * halfSweep
            drawArc(
                color = accentColor.copy(alpha = 0.9f),
                startAngle = 270f,
                sweepAngle = sweepFromCenter,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Precision tick marks along outer rim
        val tickOuter = radius + 16.dp.toPx()
        for (c in -50..50 step 5) {
            val angleDeg = 270f + (c / 50f) * halfSweep
            val rad = angleDeg * (PI / 180.0)
            val isMajor = c % 25 == 0
            val tickLen = if (c == 0) 14.dp.toPx() else if (isMajor) 10.dp.toPx() else 5.dp.toPx()
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
                c == 0 && isInTune -> NeonEmerald
                c == 0 -> NeonEmerald.copy(alpha = 0.85f)
                isMajor -> TextMuted.copy(alpha = 0.7f)
                else -> StudioBorder
            }
            drawLine(
                color = tickColor,
                start = p1,
                end = p2,
                strokeWidth = if (c == 0) 3.dp.toPx() else if (isMajor) 2.dp.toPx() else 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Needle indicator dot & short crisp pointer on the arc
        val pointerCents = if (isSignalActive) cents else 0f
        val pointerAngleDeg = 270f + (pointerCents / 50f).coerceIn(-1f, 1f) * halfSweep
        val pointerRad = pointerAngleDeg * (PI / 180.0)
        val dotCenter = Offset(
            center.x + (radius * cos(pointerRad)).toFloat(),
            center.y + (radius * sin(pointerRad)).toFloat()
        )
        val pointerInner = Offset(
            center.x + ((radius - 22.dp.toPx()) * cos(pointerRad)).toFloat(),
            center.y + ((radius - 22.dp.toPx()) * sin(pointerRad)).toFloat()
        )

        if (isSignalActive) {
            drawLine(
                color = accentColor,
                start = pointerInner,
                end = dotCenter,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.30f),
                radius = 12.dp.toPx(),
                center = dotCenter
            )
            drawCircle(
                color = TextCrisp,
                radius = 5.5.dp.toPx(),
                center = dotCenter
            )
        } else {
            // Resting center diamond/dot
            drawCircle(
                color = TextDim,
                radius = 4.dp.toPx(),
                center = dotCenter
            )
        }
    }
}

@Composable
private fun PitchGuidancePill(
    uiState: TunerUiState,
    accentColor: Color
) {
    val bgColor: Color
    val textColor: Color
    val label: String

    when (uiState.pitchZone) {
        PitchZone.IN_TUNE -> {
            bgColor = NeonEmeraldSoft
            textColor = NeonEmerald
            label = stringResource(R.string.status_in_tune)
        }
        PitchZone.FLAT_FAR, PitchZone.FLAT_CLOSE -> {
            bgColor = if (uiState.pitchZone == PitchZone.FLAT_CLOSE) AmberDriftSoft else CrimsonSharpSoft
            textColor = accentColor
            label = stringResource(R.string.status_flat_hint)
        }
        PitchZone.SHARP_FAR, PitchZone.SHARP_CLOSE -> {
            bgColor = if (uiState.pitchZone == PitchZone.SHARP_CLOSE) AmberDriftSoft else CrimsonSharpSoft
            textColor = accentColor
            label = stringResource(R.string.status_sharp_hint)
        }
        PitchZone.IDLE -> {
            bgColor = StudioElevated
            textColor = TextMuted
            label = if (!uiState.isAutoMode && uiState.lockedString != null) {
                stringResource(
                    R.string.status_listening_manual,
                    "${uiState.lockedString.stringNumber}ª",
                    uiState.lockedString.fullLabel
                )
            } else {
                stringResource(R.string.status_listening)
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(
            1.dp,
            if (uiState.pitchZone == PitchZone.IDLE) StudioBorder else textColor.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            when (uiState.pitchZone) {
                PitchZone.IN_TUNE -> Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                PitchZone.FLAT_FAR, PitchZone.FLAT_CLOSE -> Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                PitchZone.SHARP_FAR, PitchZone.SHARP_CLOSE -> Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                PitchZone.IDLE -> Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StudioWaveformStrip(
    bars: List<Float>,
    accentColor: Color,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bars.forEachIndexed { idx, level ->
            val centerFactor = 1f - (kotlin.math.abs(idx - bars.size / 2f) / (bars.size / 2f)) * 0.45f
            val h = (level * centerFactor * 20f).coerceIn(3f, 20f)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isActive) accentColor.copy(alpha = 0.72f)
                        else StudioBorder.copy(alpha = 0.8f)
                    )
            )
        }
    }
}
