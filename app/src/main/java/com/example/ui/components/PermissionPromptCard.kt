package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldSoft
import com.example.ui.theme.StudioHairline
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.TextCrisp
import com.example.ui.theme.TextMuted

@Composable
fun PermissionPromptCard(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    showSettingsFallback: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .testTag("permission_prompt_card"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(NeonEmeraldSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.mic_permission_title),
            style = MaterialTheme.typography.titleLarge,
            color = TextCrisp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.mic_permission_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .height(48.dp)
                .testTag("grant_mic_button"),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonEmerald,
                contentColor = StudioObsidian
            )
        ) {
            Text(
                text = stringResource(R.string.mic_permission_cta),
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (showSettingsFallback) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("open_settings_button"),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, StudioHairline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextCrisp
                )
            ) {
                Text(
                    text = stringResource(R.string.mic_permission_settings),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
