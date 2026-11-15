package com.example.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.InstrumentType
import com.example.ui.components.PermissionPromptCard
import com.example.ui.components.StringPegConsole
import com.example.ui.components.TunerGaugeCard
import com.example.ui.components.TuningSelectorSheet
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.StudioConsole
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioHairline
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.TextCrisp
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted

@Composable
fun TunerScreen(
    viewModel: TunerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasAskedPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAskedPermission = true
        viewModel.onPermissionUpdated(granted)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionUpdated(granted)
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    viewModel.onPermissionUpdated(granted)
                    viewModel.onForegroundChanged(true)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.onForegroundChanged(false)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = StudioObsidian,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val isExpandedWidth = maxWidth >= 640.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1000.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MinimalTopBar(
                    uiState = uiState,
                    onSelectInstrument = viewModel::selectInstrument,
                    onOpenTuningSheet = viewModel::openTuningSheet
                )

                if (isExpandedWidth) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                        ) {
                            if (uiState.hasMicPermission) {
                                TunerGaugeCard(
                                    uiState = uiState,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                PermissionPromptCard(
                                    onRequestPermission = {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    },
                                    onOpenSettings = {
                                        val intent = Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null)
                                        )
                                        context.startActivity(intent)
                                    },
                                    showSettingsFallback = hasAskedPermission,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.9f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            StringPegConsole(
                                uiState = uiState,
                                onStringTap = viewModel::onStringTapped,
                                onToggleAutoMode = viewModel::toggleAutoMode,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    if (uiState.hasMicPermission) {
                        TunerGaugeCard(
                            uiState = uiState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    } else {
                        PermissionPromptCard(
                            onRequestPermission = {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                            onOpenSettings = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                                context.startActivity(intent)
                            },
                            showSettingsFallback = hasAskedPermission,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }

                    StringPegConsole(
                        uiState = uiState,
                        onStringTap = viewModel::onStringTapped,
                        onToggleAutoMode = viewModel::toggleAutoMode,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (uiState.isTuningSheetOpen) {
        TuningSelectorSheet(
            instrument = uiState.instrument,
            activePreset = uiState.activePreset,
            onSelectTuning = viewModel::selectTuning,
            onDismiss = viewModel::dismissTuningSheet
        )
    }
}

@Composable
private fun MinimalTopBar(
    uiState: TunerUiState,
    onSelectInstrument: (InstrumentType) -> Unit,
    onOpenTuningSheet: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Tuning Preset Dropdown Pill with live mic dot
        Surface(
            modifier = Modifier
                .weight(1f, fill = false)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(50))
                .clickable(
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.cd_select_tuning),
                    onClick = onOpenTuningSheet
                )
                .testTag("open_tuning_sheet_button"),
            shape = RoundedCornerShape(50),
            color = StudioConsole,
            border = BorderStroke(1.dp, StudioHairline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isListening) NeonEmerald else TextDim)
                )
                Text(
                    text = uiState.activePreset.namePt,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextCrisp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.cd_select_tuning),
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Right: Clean Violão / Guitarra Segmented Switch
        Surface(
            shape = RoundedCornerShape(50),
            color = StudioConsole,
            border = BorderStroke(1.dp, StudioHairline)
        ) {
            Row(
                modifier = Modifier.padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                InstrumentType.entries.forEach { item ->
                    val selected = uiState.instrument == item
                    Surface(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(50))
                            .clickable(role = Role.RadioButton) { onSelectInstrument(item) }
                            .testTag("instrument_${item.id}"),
                        shape = RoundedCornerShape(50),
                        color = if (selected) StudioElevated else Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.labelPt,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) NeonEmerald else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
