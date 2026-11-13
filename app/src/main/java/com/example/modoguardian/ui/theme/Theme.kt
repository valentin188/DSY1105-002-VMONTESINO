package com.example.modoguardian.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Navy900 = Color(0xFF0B1426)
private val Navy800 = Color(0xFF111D33)
private val Navy700 = Color(0xFF192740)

private val Blue500 = Color(0xFF2864E8)
private val Blue400 = Color(0xFF528BFF)

private val Green500 = Color(0xFF20C997)
private val Red500 = Color(0xFFF04452)
private val Yellow500 = Color(0xFFFFC857)

private val White = Color(0xFFFFFFFF)
private val TextLight = Color(0xFFEAF0FF)
private val TextMuted = Color(0xFF9BAAC3)

private val InsercodeDarkColors = darkColorScheme(
    primary = Blue400,
    onPrimary = White,
    primaryContainer = Blue500,
    onPrimaryContainer = White,

    secondary = Green500,
    onSecondary = Navy900,
    secondaryContainer = Color(0xFF123F38),
    onSecondaryContainer = Color(0xFFB7F4E2),

    tertiary = Yellow500,
    onTertiary = Navy900,

    background = Navy900,
    onBackground = TextLight,

    surface = Navy800,
    onSurface = TextLight,

    surfaceVariant = Navy700,
    onSurfaceVariant = TextMuted,

    surfaceContainer = Navy800,
    surfaceContainerHigh = Navy700,
    surfaceContainerHighest = Color(0xFF21324E),

    outline = Color(0xFF536582),
    outlineVariant = Color(0xFF304058),

    error = Red500,
    onError = White,
    errorContainer = Color(0xFF4A202D),
    onErrorContainer = Color(0xFFFFD9DE),

    inverseSurface = TextLight,
    inverseOnSurface = Navy900,
    inversePrimary = Blue500
)

private val InsercodeLightColors = lightColorScheme(
    primary = Color(0xFF245CD8),
    onPrimary = White,
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = Color(0xFF102D70),

    secondary = Color(0xFF117F69),
    onSecondary = White,
    secondaryContainer = Color(0xFFD5F7E9),
    onSecondaryContainer = Color(0xFF075541),

    tertiary = Color(0xFF946100),
    onTertiary = White,

    background = Color(0xFFF3F6FC),
    onBackground = Color(0xFF101B30),

    surface = White,
    onSurface = Color(0xFF14233B),

    surfaceVariant = Color(0xFFE7ECF5),
    onSurfaceVariant = Color(0xFF53647C),

    surfaceContainer = Color(0xFFEDF2FA),
    surfaceContainerHigh = Color(0xFFE4EBF5),
    surfaceContainerHighest = Color(0xFFDCE5F2),

    outline = Color(0xFF718199),
    outlineVariant = Color(0xFFCAD4E2),

    error = Color(0xFFBA2635),
    onError = White,
    errorContainer = Color(0xFFFFDADD),
    onErrorContainer = Color(0xFF7C1925),

    inverseSurface = Color(0xFF24334B),
    inverseOnSurface = White,
    inversePrimary = Color(0xFF9DBBFF)
)

@Composable
fun ModoGuardianTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        InsercodeDarkColors
    } else {
        InsercodeLightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
