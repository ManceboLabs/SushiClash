package com.mancebolabs.sushiclash.ui.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.mancebolabs.sushiclash.R

/**
 * Renders the multicolor sushi asset without applying a theme tint.
 *
 * Always use this (or [androidx.compose.foundation.Image] without a color filter) for [R.drawable.ic_sushi].
 */
@Composable
fun SushiIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Icon(
        painter = painterResource(R.drawable.ic_sushi),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = Color.Unspecified,
    )
}
