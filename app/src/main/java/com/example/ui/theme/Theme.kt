package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemePreset {
  AMETHYST,
  COSMIC_OCEAN,
  FOREST_MINT,
  SUNSET_RUBY
}

fun getCustomColorScheme(preset: AppThemePreset, isDark: Boolean): ColorScheme {
  return if (isDark) {
    when (preset) {
      AppThemePreset.AMETHYST -> darkColorScheme(
        primary = Color(0xFFA78BFA), // Amethyst Violet
        onPrimary = Color(0xFF1E1B4B),
        primaryContainer = Color(0xFF6D28D9),
        onPrimaryContainer = Color(0xFFEDE9FE),
        secondary = Color(0xFFC084FC),
        onSecondary = Color(0xFF1E1B4B),
        secondaryContainer = Color(0xFF5B21B6),
        onSecondaryContainer = Color(0xFFFAE8FF),
        tertiary = Color(0xFFF472B6),
        onTertiary = Color(0xFF500724),
        background = Color(0xFF030712),
        surface = Color(0xFF0F172A),
        onBackground = Color(0xFFF3F4F6),
        onSurface = Color(0xFFF3F4F6),
        surfaceVariant = Color(0xFF1E293B),
        onSurfaceVariant = Color(0xFFCBD5E1),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A)
      )
      AppThemePreset.COSMIC_OCEAN -> darkColorScheme(
        primary = Color(0xFF38BDF8), // Sky Blue
        onPrimary = Color(0xFF082F49),
        primaryContainer = Color(0xFF0369A1),
        onPrimaryContainer = Color(0xFFE0F2FE),
        secondary = Color(0xFF2DD4BF), // Teal
        onSecondary = Color(0xFF042F2E),
        secondaryContainer = Color(0xFF115E59),
        onSecondaryContainer = Color(0xFFCCFBF1),
        tertiary = Color(0xFF818CF8), // Indigo
        onTertiary = Color(0xFF1E1B4B),
        background = Color(0xFF020617),
        surface = Color(0xFF0F172A),
        onBackground = Color(0xFFF8FAFC),
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = Color(0xFF1E293B),
        onSurfaceVariant = Color(0xFF94A3B8),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A)
      )
      AppThemePreset.FOREST_MINT -> darkColorScheme(
        primary = Color(0xFF34D399), // Mint Green
        onPrimary = Color(0xFF064E3B),
        primaryContainer = Color(0xFF047857),
        onPrimaryContainer = Color(0xFFD1FAE5),
        secondary = Color(0xFF6EE7B7),
        onSecondary = Color(0xFF022C22),
        secondaryContainer = Color(0xFF065F46),
        onSecondaryContainer = Color(0xFFA7F3D0),
        tertiary = Color(0xFF10B981),
        onTertiary = Color(0xFF022C22),
        background = Color(0xFF051B13),
        surface = Color(0xFF062E21),
        onBackground = Color(0xFFECFDF5),
        onSurface = Color(0xFFECFDF5),
        surfaceVariant = Color(0xFF114E3C),
        onSurfaceVariant = Color(0xFFD1FAE5),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A)
      )
      AppThemePreset.SUNSET_RUBY -> darkColorScheme(
        primary = Color(0xFFFB7185), // Rose
        onPrimary = Color(0xFF4C0519),
        primaryContainer = Color(0xFFBE123C),
        onPrimaryContainer = Color(0xFFFFE4E6),
        secondary = Color(0xFFFBBF24), // Gold
        onSecondary = Color(0xFF451A03),
        secondaryContainer = Color(0xFF92400E),
        onSecondaryContainer = Color(0xFFFEF3C7),
        tertiary = Color(0xFFF43F5E),
        onTertiary = Color(0xFF4C0519),
        background = Color(0xFF1C0A10),
        surface = Color(0xFF250D15),
        onBackground = Color(0xFFFFF1F2),
        onSurface = Color(0xFFFFF1F2),
        surfaceVariant = Color(0xFF3F1321),
        onSurfaceVariant = Color(0xFFFECDD3),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A)
      )
    }
  } else {
    when (preset) {
      AppThemePreset.AMETHYST -> lightColorScheme(
        primary = Color(0xFF8B5CF6), // Violet
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDDD6FE),
        onPrimaryContainer = Color(0xFF4C1D95),
        secondary = Color(0xFF7C3AED),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFEDE9FE),
        onSecondaryContainer = Color(0xFF6D28D9),
        tertiary = Color(0xFFEC4899),
        onTertiary = Color.White,
        background = Color(0xFFF9FAFB),
        surface = Color(0xFFFFFFFF),
        onBackground = Color(0xFF111827),
        onSurface = Color(0xFF111827),
        surfaceVariant = Color(0xFFF3F4F6),
        onSurfaceVariant = Color(0xFF4B5563),
        error = Color(0xFFDC2626),
        onError = Color.White
      )
      AppThemePreset.COSMIC_OCEAN -> lightColorScheme(
        primary = Color(0xFF0EA5E9), // Sky Blue
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE0F2FE),
        onPrimaryContainer = Color(0xFF0369A1),
        secondary = Color(0xFF0D9488), // Teal
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFCCFBF1),
        onSecondaryContainer = Color(0xFF115E59),
        tertiary = Color(0xFF4F46E5), // Indigo
        onTertiary = Color.White,
        background = Color(0xFFF0F9FF),
        surface = Color(0xFFFFFFFF),
        onBackground = Color(0xFF0F172A),
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFE2E8F0),
        onSurfaceVariant = Color(0xFF475569),
        error = Color(0xFFDC2626),
        onError = Color.White
      )
      AppThemePreset.FOREST_MINT -> lightColorScheme(
        primary = Color(0xFF10B981), // Emerald
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD1FAE5),
        onPrimaryContainer = Color(0xFF065F46),
        secondary = Color(0xFF059669),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFECEFEF),
        onSecondaryContainer = Color(0xFF047857),
        tertiary = Color(0xFF0D9488),
        onTertiary = Color.White,
        background = Color(0xFFF0FDF4),
        surface = Color(0xFFFFFFFF),
        onBackground = Color(0xFF062E21),
        onSurface = Color(0xFF062E21),
        surfaceVariant = Color(0xFFE6F4EA),
        onSurfaceVariant = Color(0xFF0F5132),
        error = Color(0xFFDC2626),
        onError = Color.White
      )
      AppThemePreset.SUNSET_RUBY -> lightColorScheme(
        primary = Color(0xFFF43F5E), // Rose red
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFE4E6),
        onPrimaryContainer = Color(0xFF9F1239),
        secondary = Color(0xFFD97706), // Amber
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFEF3C7),
        onSecondaryContainer = Color(0xFF92400E),
        tertiary = Color(0xFFE11D48),
        onTertiary = Color.White,
        background = Color(0xFFFFF1F2),
        surface = Color(0xFFFFFFFF),
        onBackground = Color(0xFF4C0519),
        onSurface = Color(0xFF4C0519),
        surfaceVariant = Color(0xFFFFE4E6),
        onSurfaceVariant = Color(0xFFBE123C),
        error = Color(0xFFDC2626),
        onError = Color.White
      )
    }
  }
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  preset: AppThemePreset = AppThemePreset.AMETHYST,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      else -> getCustomColorScheme(preset, darkTheme)
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

