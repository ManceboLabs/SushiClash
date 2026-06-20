package com.mancebolabs.sushiclash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mancebolabs.sushiclash.di.AppContainer
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.navigation.SushiCounterApp
import com.mancebolabs.sushiclash.ui.theme.SushiCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themeRepository = AppContainer.themeRepository(context)
            val themeMode by themeRepository.themeMode.collectAsStateWithLifecycle(
                initialValue = AppThemeMode.LIGHT,
            )

            SushiCounterTheme(
                darkTheme = themeMode == AppThemeMode.DARK,
            ) {
                SushiCounterApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
