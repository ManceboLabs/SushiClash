package com.mancebolabs.sushiclash.data.repository

import com.mancebolabs.sushiclash.domain.model.AppThemeMode

/**
 * In-process cache of the user's theme preference.
 *
 * Survives Activity recreation (e.g. locale changes) so Compose can start with the correct
 * theme instead of defaulting to light until DataStore emits.
 */
object ThemeModeHolder {
    @Volatile
    var current: AppThemeMode? = null
}
