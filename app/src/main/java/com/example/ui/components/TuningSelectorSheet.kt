package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.InstrumentType
import com.example.domain.TuningCatalog
import com.example.domain.TuningCategory
import com.example.domain.TuningPreset
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldSoft
import com.example.ui.theme.StudioConsole
import com.example.ui.theme.TextCrisp
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuningSelectorSheet(
    instrument: InstrumentType,
    activePreset: TuningPreset,
    onSelectTuning: (TuningPreset) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val grouped = TuningCatalog.presets.groupBy { it.category }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioConsole,
        contentColor = TextCrisp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.tuning_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = TextCrisp
            )
            Text(
                text = "Para ${instrument.labelPt.lowercase()}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TuningCategory.entries.forEach { category ->
                    val presets = grouped[category].orEmpty()
                    if (presets.isNotEmpty()) {
                        item(key = "header_${category.name}") {
                            Text(
                                text = category.titlePt.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
                            )
                        }
                        items(presets, key = { it.id }) { preset ->
                            TuningPresetRow(
                                preset = preset,
                                isSelected = preset.id == activePreset.id,
                                onClick = { onSelectTuning(preset) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TuningPresetRow(
    preset: TuningPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("tuning_option_${preset.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) NeonEmeraldSoft else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = preset.namePt,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) NeonEmerald else TextCrisp
                )
                Text(
                    text = preset.descriptionPt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = preset.summaryNotes,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (isSelected) NeonEmerald else TextMuted
                )

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
