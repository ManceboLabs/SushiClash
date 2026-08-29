package com.mancebolabs.sushiclash.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.mancebolabs.sushiclash.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val plusJakartaSans = GoogleFont("Plus Jakarta Sans")
private val inter = GoogleFont("Inter")
private val notoSansJp = GoogleFont("Noto Sans JP")
private val notoSansSc = GoogleFont("Noto Sans SC")

val PlusJakartaSansFamily = FontFamily(
    Font(googleFont = plusJakartaSans, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = plusJakartaSans, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = notoSansJp, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = notoSansJp, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = notoSansSc, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = notoSansSc, fontProvider = provider, weight = FontWeight.Bold),
)

val InterFamily = FontFamily(
    Font(googleFont = inter, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = inter, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = notoSansJp, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = notoSansJp, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = notoSansSc, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = notoSansSc, fontProvider = provider, weight = FontWeight.Medium),
)
