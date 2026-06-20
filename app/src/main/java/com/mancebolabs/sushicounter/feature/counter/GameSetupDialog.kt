package com.mancebolabs.sushicounter.feature.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mancebolabs.sushicounter.R
import com.mancebolabs.sushicounter.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushicounter.domain.model.GameSetupRules
import com.mancebolabs.sushicounter.domain.model.GameMode
import com.mancebolabs.sushicounter.domain.model.GameSetupConfig
import com.mancebolabs.sushicounter.domain.model.GameState
import com.mancebolabs.sushicounter.domain.model.RandomRouletteTriggerType
import com.mancebolabs.sushicounter.ui.components.ItamaePrimaryButton
import com.mancebolabs.sushicounter.ui.components.ItamaeTextField
import com.mancebolabs.sushicounter.ui.theme.ItamaeShapes
import com.mancebolabs.sushicounter.ui.theme.ItamaeSpacing

@Composable
fun GameSetupDialog(
    onConfirm: (GameSetupConfig) -> Unit,
) {
    var selectedMode by remember { mutableStateOf<GameMode?>(null) }
    var inputName by remember { mutableStateOf("") }
    var groupPlayers by remember { mutableStateOf(emptyList<String>()) }
    var randomRouletteEnabled by remember { mutableStateOf(false) }
    var randomRouletteTriggerType by remember {
        mutableStateOf(RandomRouletteTriggerType.FIXED)
    }
    var randomRouletteFixedThreshold by remember {
        mutableIntStateOf(GameState.DEFAULT_RANDOM_ROULETTE_THRESHOLD)
    }
    val scrollState = rememberScrollState()

    val canConfirm = GameSetupRules.canConfirmSetup(selectedMode, groupPlayers.size)

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 560.dp)
                .heightIn(max = 640.dp),
            shape = ItamaeShapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            Column(
                modifier = Modifier.padding(ItamaeSpacing.lg),
            ) {
                Text(
                    text = stringResource(R.string.setup_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(ItamaeSpacing.md))

                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
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
                                        groupPlayers.size < GameSetupRules.MAX_GROUP_PLAYERS &&
                                        groupPlayers.none { it.equals(trimmedName, ignoreCase = true) }
                                    ) {
                                        groupPlayers = groupPlayers + trimmedName
                                        inputName = ""
                                    }
                                },
                                enabled = inputName.isNotBlank() &&
                                    groupPlayers.size < GameSetupRules.MAX_GROUP_PLAYERS,
                            )
                        }

                        if (groupPlayers.size < GameSetupRules.MIN_GROUP_PLAYERS) {
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

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    RandomRouletteSetupSection(
                        enabled = randomRouletteEnabled,
                        triggerType = randomRouletteTriggerType,
                        fixedThreshold = randomRouletteFixedThreshold,
                        onEnabledChanged = { randomRouletteEnabled = it },
                        onTriggerTypeChanged = { randomRouletteTriggerType = it },
                        onFixedThresholdChanged = { randomRouletteFixedThreshold = it },
                    )
                }

                Spacer(modifier = Modifier.height(ItamaeSpacing.lg))

                ItamaePrimaryButton(
                    text = stringResource(R.string.setup_start),
                    onClick = {
                        val mode = selectedMode ?: return@ItamaePrimaryButton
                        onConfirm(
                            GameSetupConfig(
                                gameMode = mode,
                                playerNames = groupPlayers,
                                randomRouletteEnabled = randomRouletteEnabled,
                                randomRouletteTriggerType = randomRouletteTriggerType,
                                randomRouletteFixedThreshold = randomRouletteFixedThreshold.coerceIn(
                                    GameState.MIN_RANDOM_ROULETTE_THRESHOLD,
                                    GameState.MAX_RANDOM_ROULETTE_THRESHOLD,
                                ),
                            ),
                        )
                    },
                    enabled = canConfirm,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun RandomRouletteSetupSection(
    enabled: Boolean,
    triggerType: RandomRouletteTriggerType,
    fixedThreshold: Int,
    onEnabledChanged: (Boolean) -> Unit,
    onTriggerTypeChanged: (RandomRouletteTriggerType) -> Unit,
    onFixedThresholdChanged: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = ItamaeShapes.small,
            )
            .padding(ItamaeSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.setup_random_roulette_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.setup_random_roulette_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChanged,
            )
        }

        if (enabled) {
            Text(
                text = stringResource(R.string.setup_roulette_frequency_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            TriggerTypeOption(
                selected = triggerType == RandomRouletteTriggerType.FIXED,
                title = stringResource(R.string.setup_roulette_trigger_fixed),
                onClick = { onTriggerTypeChanged(RandomRouletteTriggerType.FIXED) },
            )

            TriggerTypeOption(
                selected = triggerType == RandomRouletteTriggerType.RANDOM,
                title = stringResource(R.string.setup_roulette_trigger_random),
                onClick = { onTriggerTypeChanged(RandomRouletteTriggerType.RANDOM) },
            )

            when (triggerType) {
                RandomRouletteTriggerType.FIXED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.setup_roulette_threshold_label),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )

                        ThresholdStepper(
                            threshold = fixedThreshold,
                            onThresholdChanged = onFixedThresholdChanged,
                        )
                    }
                }
                RandomRouletteTriggerType.RANDOM -> {
                    Text(
                        text = stringResource(R.string.setup_roulette_random_helper),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TriggerTypeOption(
    selected: Boolean,
    title: String,
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
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                },
                shape = ItamaeShapes.small,
            )
            .padding(horizontal = ItamaeSpacing.sm, vertical = ItamaeSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ThresholdStepper(
    threshold: Int,
    onThresholdChanged: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.xs),
    ) {
        IconButton(
            onClick = {
                onThresholdChanged(
                    (threshold - 1).coerceAtLeast(
                        GameState.MIN_RANDOM_ROULETTE_THRESHOLD,
                    ),
                )
            },
            enabled = threshold > GameState.MIN_RANDOM_ROULETTE_THRESHOLD,
        ) {
            Icon(
                imageVector = Icons.Outlined.Remove,
                contentDescription = stringResource(R.string.setup_decrease_threshold),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Text(
            text = stringResource(R.string.setup_roulette_threshold_value, threshold),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        IconButton(
            onClick = {
                onThresholdChanged(
                    (threshold + 1).coerceAtMost(
                        GameState.MAX_RANDOM_ROULETTE_THRESHOLD,
                    ),
                )
            },
            enabled = threshold < GameState.MAX_RANDOM_ROULETTE_THRESHOLD,
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.setup_increase_threshold),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
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
