package com.mancebolabs.sushicounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mancebolabs.sushicounter.navigation.SushiCounterApp
import com.mancebolabs.sushicounter.ui.theme.SushiCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SushiCounterTheme {
                SushiCounterApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
