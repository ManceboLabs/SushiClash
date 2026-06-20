package com.mancebolabs.sushicounter.ui.theme

import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.mancebolabs.sushicounter.R

val provider = androidx.compose.ui.text.googlefonts.GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val plusJakartaSans = GoogleFont("Plus Jakarta Sans")
private val inter = GoogleFont("Inter")

val PlusJakartaSansFamily = androidx.compose.ui.text.font.FontFamily(
    Font(googleFont = plusJakartaSans, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.SemiBold),
    Font(googleFont = plusJakartaSans, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.Bold),
)

val InterFamily = androidx.compose.ui.text.font.FontFamily(
    Font(googleFont = inter, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.Normal),
    Font(googleFont = inter, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.Medium),
)
