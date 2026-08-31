package com.mancebolabs.sushiclash.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.di.AppContainer
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.repository.OnboardingRepository
import com.mancebolabs.sushiclash.feature.achievements.AchievementNotificationHost
import com.mancebolabs.sushiclash.feature.achievements.AchievementsScreen
import com.mancebolabs.sushiclash.feature.achievements.AchievementsViewModel
import com.mancebolabs.sushiclash.feature.counter.CounterScreen
import com.mancebolabs.sushiclash.feature.counter.CounterViewModel
import com.mancebolabs.sushiclash.feature.history.HistoryScreen
import com.mancebolabs.sushiclash.feature.history.HistoryViewModel
import com.mancebolabs.sushiclash.feature.onboarding.OnboardingScreen
import com.mancebolabs.sushiclash.feature.onboarding.defaultOnboardingSteps
import com.mancebolabs.sushiclash.feature.settings.SettingsScreen
import com.mancebolabs.sushiclash.feature.settings.SettingsViewModel
import com.mancebolabs.sushiclash.feature.wheel.WheelScreen
import com.mancebolabs.sushiclash.feature.wheel.WheelViewModel
import com.mancebolabs.sushiclash.ui.components.ItamaeFloatingNavBar
import com.mancebolabs.sushiclash.ui.components.ItamaeNavItem
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing
import com.mancebolabs.sushiclash.ui.theme.LocalFloatingNavBarHeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class SushiDestination(
    val route: String,
) {
    data object Counter : SushiDestination("counter")

    data object Wheel : SushiDestination("wheel")

    data object History : SushiDestination("history")

    data object Settings : SushiDestination("settings")

    data object Achievements : SushiDestination("achievements")

    data object Onboarding : SushiDestination("onboarding/{source}") {
        const val SOURCE_ARG = "source"

        fun route(source: OnboardingSource): String = "onboarding/${source.name}"
    }
}

private val mainTabRoutes = setOf(
    SushiDestination.Counter.route,
    SushiDestination.Wheel.route,
    SushiDestination.History.route,
    SushiDestination.Settings.route,
)

internal fun resolveOnboardingCompleted(
    previous: Boolean?,
    state: PersistenceReadState<Boolean>,
): Boolean? {
    return when (state) {
        is PersistenceReadState.Data -> state.value
        // Missing means the flag was never persisted; only treat as incomplete on first resolution.
        PersistenceReadState.Missing -> previous ?: false
        // Keep loading or the last known value while onboarding persistence is unreadable.
        PersistenceReadState.Corrupted,
        PersistenceReadState.Unavailable -> previous
    }
}

@Composable
fun SushiCounterApp(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val onboardingRepository = remember { AppContainer.onboardingRepository(context) }
    var hasCompletedOnboarding by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(onboardingRepository) {
        var lastKnown = hasCompletedOnboarding
        onboardingRepository.hasCompletedOnboardingState.collect { state ->
            lastKnown = resolveOnboardingCompleted(lastKnown, state)
            hasCompletedOnboarding = lastKnown
        }
    }

    when (hasCompletedOnboarding) {
        null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        else -> {
            val startDestination = if (hasCompletedOnboarding == false) {
                SushiDestination.Onboarding.route(OnboardingSource.FIRST_LAUNCH)
            } else {
                SushiDestination.Counter.route
            }
            key(hasCompletedOnboarding) {
                SushiCounterNavHost(
                    modifier = modifier,
                    startDestination = startDestination,
                    onboardingRepository = onboardingRepository,
                )
            }
        }
    }
}

@Composable
private fun SushiCounterNavHost(
    startDestination: String,
    onboardingRepository: OnboardingRepository,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var floatingNavBarHeight by remember { mutableStateOf(ItamaeSpacing.floatingNavBarDefaultHeight) }
    val rouletteNavState = remember { RandomRouletteNavState() }
    val context = LocalContext.current
    val feedbackSettingsRepository = remember { AppContainer.feedbackSettingsRepository(context) }
    val vibrationEnabled by feedbackSettingsRepository.vibrationEnabled.collectAsStateWithLifecycle(initialValue = true)

    // Onboarding is a dedicated route; bottom navigation is limited to the four main tabs.
    val showBottomNavigation = currentRoute in mainTabRoutes

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
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize(),
                ) {
                composable(
                    route = SushiDestination.Onboarding.route,
                    arguments = listOf(
                        navArgument(SushiDestination.Onboarding.SOURCE_ARG) {
                            type = NavType.StringType
                        },
                    ),
                ) { backStackEntry ->
                    val sourceName = backStackEntry.arguments?.getString(SushiDestination.Onboarding.SOURCE_ARG)
                    val source = OnboardingSource.entries.firstOrNull { it.name == sourceName }
                        ?: OnboardingSource.FIRST_LAUNCH

                    OnboardingScreen(
                        steps = defaultOnboardingSteps(),
                        onSkip = {
                            navController.exitOnboarding(
                                source = source,
                                onboardingRepository = onboardingRepository,
                                scope = scope,
                            )
                        },
                        onFinish = {
                            navController.exitOnboarding(
                                source = source,
                                onboardingRepository = onboardingRepository,
                                scope = scope,
                            )
                        },
                    )
                }
                composable(SushiDestination.Counter.route) {
                    val viewModel: CounterViewModel = viewModel(
                        factory = CounterViewModel.factory(
                            AppContainer.gameRepository(context),
                            AppContainer.onboardingRepository(context),
                            AppContainer.feedbackSettingsRepository(context),
                            AppContainer.achievementRepository(context),
                            AppContainer.frequentPlayersRepository(context),
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
                        onSetupDismissed = viewModel::onSetupDismissed,
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
                        onChefCelebrationDismissed = viewModel::onChefCelebrationDismissed,
                        onPersistenceRetry = viewModel::onPersistenceRetry,
                        onFeedbackConsumed = viewModel::onFeedbackConsumed,
                    )
                }
                composable(SushiDestination.Wheel.route) {
                    val viewModel: WheelViewModel = viewModel(
                        factory = WheelViewModel.factory(
                            AppContainer.participantsRepository(context),
                            AppContainer.achievementRepository(context),
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
                        onPersistenceRetry = viewModel::onPersistenceRetry,
                    )
                }
                composable(SushiDestination.History.route) {
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
                    val appVersion = remember(context) {
                        runCatching {
                            context.packageManager
                                .getPackageInfo(context.packageName, 0)
                                .versionName
                        }.getOrNull().orEmpty()
                    }
                    val viewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.factory(
                            AppContainer.themeRepository(context),
                            AppContainer.languageRepository(),
                            AppContainer.historyRepository(context),
                            AppContainer.feedbackSettingsRepository(context),
                            AppContainer.achievementRepository(context),
                        ),
                    )
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    SettingsScreen(
                        uiState = uiState,
                        onThemeModeSelected = viewModel::onThemeModeSelected,
                        onLanguagePickerRequested = viewModel::onLanguagePickerRequested,
                        onLanguagePickerDismissed = viewModel::onLanguagePickerDismissed,
                        onLanguageSelected = viewModel::onLanguageSelected,
                        onAppLanguageRefreshRequested = viewModel::onAppLanguageRefreshRequested,
                        onClearHistoryRequested = viewModel::onClearHistoryRequested,
                        onClearHistoryConfirmed = viewModel::onClearHistoryConfirmed,
                        onClearHistoryDismissed = viewModel::onClearHistoryDismissed,
                        onClearAchievementsRequested = viewModel::onClearAchievementsRequested,
                        onClearAchievementsConfirmed = viewModel::onClearAchievementsConfirmed,
                        onClearAchievementsDismissed = viewModel::onClearAchievementsDismissed,
                        onViewTutorialRequested = {
                            // Navigate to onboarding without mutating app state; completion
                            // and first-launch flags remain unchanged until the user finishes.
                            navController.navigate(
                                SushiDestination.Onboarding.route(OnboardingSource.SETTINGS),
                            )
                        },
                        onViewAchievementsRequested = {
                            navController.navigate(SushiDestination.Achievements.route)
                        },
                        onSoundEnabledChanged = viewModel::onSoundEnabledChanged,
                        onVibrationEnabledChanged = viewModel::onVibrationEnabledChanged,
                        onPersistenceRetry = viewModel::onPersistenceRetry,
                        appVersion = appVersion,
                    )
                }
                composable(SushiDestination.Achievements.route) {
                    val viewModel: AchievementsViewModel = viewModel(
                        factory = AchievementsViewModel.factory(
                            AppContainer.achievementRepository(context),
                        ),
                    )
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    AchievementsScreen(
                        uiState = uiState,
                        onBack = { navController.popBackStack() },
                    )
                }
            }

                AchievementNotificationHost(
                    vibrationEnabled = vibrationEnabled,
                    onNavigateToAchievements = {
                        navController.navigate(SushiDestination.Achievements.route) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }

        if (showBottomNavigation) {
            ItamaeFloatingNavBar(
                items = navItems,
                selectedRoute = currentRoute ?: SushiDestination.Counter.route,
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
}

private fun androidx.navigation.NavHostController.exitOnboarding(
    source: OnboardingSource,
    onboardingRepository: OnboardingRepository,
    scope: CoroutineScope,
) {
    when (source) {
        OnboardingSource.FIRST_LAUNCH -> {
            // App shell rebuilds with Counter as start once onboarding is persisted.
            scope.launch {
                onboardingRepository.setOnboardingCompleted()
            }
        }
        OnboardingSource.SETTINGS -> {
            popBackStack()
        }
    }
}
