package com.mancebolabs.sushiclash

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.mancebolabs.sushiclash.data.repository.ThemeModeHolder
import com.mancebolabs.sushiclash.di.AppContainer
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.domain.repository.ThemeRepository
import com.mancebolabs.sushiclash.navigation.SushiCounterApp
import com.mancebolabs.sushiclash.ui.theme.SushiCounterTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeRepository = AppContainer.themeRepository(this)
        val cachedThemeMode = ThemeModeHolder.current

        if (cachedThemeMode != null) {
            applyWindowBackground(cachedThemeMode)
            setAppContent(themeRepository, cachedThemeMode)
        } else {
            applyWindowBackground(AppThemeMode.LIGHT)
            lifecycleScope.launch {
                val themeMode = themeRepository.themeMode.first()
                ThemeModeHolder.current = themeMode
                applyWindowBackground(themeMode)
                setAppContent(themeRepository, themeMode)
            }
        }
    }

    private fun setAppContent(
        themeRepository: ThemeRepository,
        initialThemeMode: AppThemeMode,
    ) {
        setContent {
            val themeMode by themeRepository.themeMode.collectAsStateWithLifecycle(
                initialValue = initialThemeMode,
            )

            LaunchedEffect(themeMode) {
                ThemeModeHolder.current = themeMode
            }

            SideEffect {
                applyWindowBackground(themeMode)
            }

            SushiCounterTheme(
                darkTheme = themeMode == AppThemeMode.DARK,
            ) {
                SushiCounterApp(modifier = Modifier.fillMaxSize())
            }
        }
    }

    private fun applyWindowBackground(themeMode: AppThemeMode) {
        val backgroundRes = if (themeMode == AppThemeMode.DARK) {
            R.color.itamae_dark_background
        } else {
            R.color.itamae_background
        }
        window.setBackgroundDrawableResource(backgroundRes)
    }
}
