package com.mancebolabs.sushicounter.feature.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mancebolabs.sushicounter.R
import com.mancebolabs.sushicounter.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushicounter.domain.model.GameMode
import com.mancebolabs.sushicounter.ui.components.ItamaePrimaryButton
import com.mancebolabs.sushicounter.ui.components.ItamaeTextField
import com.mancebolabs.sushicounter.ui.theme.ItamaeShapes
import com.mancebolabs.sushicounter.ui.theme.ItamaeSpacing

@Composable
fun GameSetupDialog(
    onConfirm: (GameMode, List<String>) -> Unit,
) {
    var selectedMode by remember { mutableStateOf<GameMode?>(null) }
    var inputName by remember { mutableStateOf("") }
    var groupPlayers by remember { mutableStateOf(emptyList<String>()) }
    val scrollState = rememberScrollState()

    val canConfirm = when (selectedMode) {
        GameMode.SOLO -> true
        GameMode.GROUP -> groupPlayers.size >= AppPreferencesDataStore.MIN_GROUP_PLAYERS
        null -> false
    }

    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        shape = ItamaeShapes.large,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
            ) {
                GameModeOption(
                    selected = selectedMode == GameMode.SOLO,
                    title = stringResource(R.string.setup_solo_title),
                    description = stringResource(R.string.setup_solo_description),
                    onClick = { selectedMode = GameMode.SOLO },
                )

                GameModeOption(
                    selected = selectedMode == GameMode.GROUP,
                    title = stringResource(R.string.setup_group_title),
                    description = stringResource(R.string.setup_group_description),
                    onClick = { selectedMode = GameMode.GROUP },
                )

                if (selectedMode == GameMode.GROUP) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        ItamaeTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = stringResource(R.string.setup_player_name_hint),
                            modifier = Modifier.weight(1f),
                        )
                        ItamaePrimaryButton(
                            text = stringResource(R.string.wheel_add),
                            onClick = {
                                val trimmedName = inputName.trim()
                                if (
                                    trimmedName.isNotEmpty() &&
                                    groupPlayers.size < AppPreferencesDataStore.MAX_GROUP_PLAYERS &&
                                    groupPlayers.none { it.equals(trimmedName, ignoreCase = true) }
                                ) {
                                    groupPlayers = groupPlayers + trimmedName
                                    inputName = ""
                                }
                            },
                            enabled = inputName.isNotBlank() &&
                                groupPlayers.size < AppPreferencesDataStore.MAX_GROUP_PLAYERS,
                        )
                    }

                    if (groupPlayers.size < AppPreferencesDataStore.MIN_GROUP_PLAYERS) {
                        Text(
                            text = stringResource(R.string.setup_min_players_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    groupPlayers.forEach { playerName ->
                        SetupPlayerRow(
                            name = playerName,
                            onDelete = {
                                groupPlayers = groupPlayers.filterNot { it == playerName }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            ItamaePrimaryButton(
                text = stringResource(R.string.setup_start),
                onClick = {
                    val mode = selectedMode ?: return@ItamaePrimaryButton
                    onConfirm(mode, groupPlayers)
                },
                enabled = canConfirm,
            )
        },
    )
}

@Composable
private fun GameModeOption(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
                shape = ItamaeShapes.small,
            )
            .padding(ItamaeSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SetupPlayerRow(
    name: String,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = ItamaeShapes.small,
            )
            .padding(horizontal = ItamaeSpacing.md, vertical = ItamaeSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(
                    R.string.wheel_delete_content_description,
                    name,
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
