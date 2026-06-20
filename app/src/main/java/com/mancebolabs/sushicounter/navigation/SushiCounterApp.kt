package com.mancebolabs.sushicounter.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mancebolabs.sushicounter.R
import com.mancebolabs.sushicounter.di.AppContainer
import com.mancebolabs.sushicounter.feature.counter.CounterScreen
import com.mancebolabs.sushicounter.feature.counter.CounterViewModel
import com.mancebolabs.sushicounter.feature.history.HistoryScreen
import com.mancebolabs.sushicounter.feature.history.HistoryViewModel
import com.mancebolabs.sushicounter.feature.settings.SettingsScreen
import com.mancebolabs.sushicounter.feature.settings.SettingsViewModel
import com.mancebolabs.sushicounter.feature.wheel.WheelScreen
import com.mancebolabs.sushicounter.feature.wheel.WheelViewModel
import com.mancebolabs.sushicounter.ui.components.ItamaeFloatingNavBar
import com.mancebolabs.sushicounter.ui.components.ItamaeNavItem
import com.mancebolabs.sushicounter.ui.theme.ItamaeSpacing
import com.mancebolabs.sushicounter.ui.theme.LocalFloatingNavBarHeight

sealed class SushiDestination(
    val route: String,
) {
    data object Counter : SushiDestination("counter")

    data object Wheel : SushiDestination("wheel")

    data object History : SushiDestination("history")

    data object Settings : SushiDestination("settings")
}

@Composable
fun SushiCounterApp(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: SushiDestination.Counter.route
    val density = LocalDensity.current
    var floatingNavBarHeight by remember { mutableStateOf(ItamaeSpacing.floatingNavBarDefaultHeight) }
    val rouletteNavState = remember { RandomRouletteNavState() }

    val navItems = listOf(
        ItamaeNavItem(
            route = SushiDestination.Counter.route,
            contentDescription = stringResource(R.string.nav_counter),
            iconRes = R.drawable.ic_sushi,
        ),
        ItamaeNavItem(
            route = SushiDestination.Wheel.route,
            contentDescription = stringResource(R.string.nav_wheel),
            icon = Icons.Default.Casino,
        ),
        ItamaeNavItem(
            route = SushiDestination.History.route,
            contentDescription = stringResource(R.string.nav_history),
            icon = Icons.Default.EmojiEvents,
        ),
        ItamaeNavItem(
            route = SushiDestination.Settings.route,
            contentDescription = stringResource(R.string.nav_settings),
            icon = Icons.Default.Settings,
        ),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CompositionLocalProvider(LocalFloatingNavBarHeight provides floatingNavBarHeight) {
            NavHost(
                navController = navController,
                startDestination = SushiDestination.Counter.route,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(SushiDestination.Counter.route) {
                    val context = LocalContext.current
                    val viewModel: CounterViewModel = viewModel(
                        factory = CounterViewModel.factory(
                            AppContainer.gameRepository(context),
                            AppContainer.historyRepository(context),
                        ),
                    )
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    CounterScreen(
                        uiState = uiState,
                        onSoloSushiTapped = viewModel::onSoloSushiTapped,
                        onPlayerSushiTapped = viewModel::onPlayerSushiTapped,
                        onPlayerResetRequested = viewModel::onPlayerResetRequested,
                        onPlayerResetConfirmed = viewModel::onPlayerResetConfirmed,
                        onPlayerResetDismissed = viewModel::onPlayerResetDismissed,
                        onResetSoloCountConfirmed = viewModel::onResetSoloCountConfirmed,
                        onStartGameRequested = viewModel::onStartGameRequested,
                        onFinishGameRequested = viewModel::onFinishGameRequested,
                        onFinishGameCancelled = viewModel::onFinishGameCancelled,
                        onFinishGameWithoutSaving = viewModel::onFinishGameWithoutSaving,
                        onFinishGameWithSaving = viewModel::onFinishGameWithSaving,
                        onSetupConfirmed = viewModel::onSetupConfirmed,
                        onRouletteTriggerAccepted = {
                            viewModel.onRouletteTriggerConfirmed()
                            rouletteNavState.requestAutoSpin()
                            navController.navigate(SushiDestination.Wheel.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onRouletteTriggerDismissed = viewModel::onRouletteTriggerDismissed,
                    )
                }
                composable(SushiDestination.Wheel.route) {
                    val context = LocalContext.current
                    val viewModel: WheelViewModel = viewModel(
                        factory = WheelViewModel.factory(
                            AppContainer.participantsRepository(context),
                        ),
                    )
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(rouletteNavState.pendingAutoSpin) {
                        if (rouletteNavState.pendingAutoSpin) {
                            rouletteNavState.consumeAutoSpin()
                            viewModel.onAutoSpinRequested()
                        }
                    }

                    WheelScreen(
                        uiState = uiState,
                        onInputChanged = viewModel::onInputChanged,
                        onAddParticipant = viewModel::onAddParticipant,
                        onRemoveParticipant = viewModel::onRemoveParticipant,
                        onSpin = viewModel::onSpin,
                        onWinnerDialogDismissed = viewModel::onWinnerDialogDismissed,
                        onInsufficientParticipantsDismissed = viewModel::onInsufficientParticipantsDismissed,
                    )
                }
                composable(SushiDestination.History.route) {
                    val context = LocalContext.current
                    val viewModel: HistoryViewModel = viewModel(
                        factory = HistoryViewModel.factory(
                            AppContainer.historyRepository(context),
                        ),
                    )
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    HistoryScreen(
                        uiState = uiState,
                        onSectionSelected = viewModel::onSectionSelected,
                    )
                }
                composable(SushiDestination.Settings.route) {
                    val context = LocalContext.current
                    val viewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.factory(
                            AppContainer.themeRepository(context),
                            AppContainer.historyRepository(context),
                        ),
                    )
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    SettingsScreen(
                        uiState = uiState,
                        onThemeModeSelected = viewModel::onThemeModeSelected,
                        onClearHistoryRequested = viewModel::onClearHistoryRequested,
                        onClearHistoryConfirmed = viewModel::onClearHistoryConfirmed,
                        onClearHistoryDismissed = viewModel::onClearHistoryDismissed,
                    )
                }
            }
        }

        ItamaeFloatingNavBar(
            items = navItems,
            selectedRoute = currentRoute,
            onItemSelected = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = ItamaeSpacing.navBottomMargin)
                .onSizeChanged { size ->
                    floatingNavBarHeight = with(density) { size.height.toDp() }
                },
        )
    }
}
