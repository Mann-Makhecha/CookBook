package com.example.cookbook.ui.theme

import androidx.compose.ui.graphics.Color

// Primary - Cyan/Neon Blue (Modern UI styling)
val Orange80 = Color(0xFF00F0FF)  // Light mode on dark
val Orange40 = Color(0xFF00B2FF)  // Dark mode primary

// Secondary - Hot Pink
val Green80 = Color(0xFFFF007F)   // Light mode on dark
val Green40 = Color(0xFFCC0066)   // Dark mode secondary

// Tertiary - Deep Purple
val Brown80 = Color(0xFFB026FF)   // Light mode on dark
val Brown40 = Color(0xFF7000FF)   // Dark mode tertiary

// Background & Surface colors (Liquid Glass Effect)
// Keeping these fully transparent or highly translucent so the animated background is visible
val CreamLight = Color.Transparent
val SurfaceLight = Color(0x1AFFFFFF)    // Very translucent white
val BackgroundDark = Color.Transparent
val SurfaceDark = Color(0x15FFFFFF)     // Very translucent white

// Error colors
val ErrorLight = Color(0xFFFF4040)
val ErrorDark = Color(0xFFFF6060)

// On colors (Text should be bright since it's on dark translucent orbs)
val OnPrimaryLight = Color(0xFF0F172A)
val OnPrimaryDark = Color(0xFF0F172A)
val OnSecondaryLight = Color(0xFF0F172A)
val OnSecondaryDark = Color(0xFFFFFFFF)
val OnBackgroundLight = Color(0xFFFFFFFF)
val OnBackgroundDark = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceDark = Color(0xFFFFFFFF)
val OnSurfaceVariantLight = Color(0xFFE2E8F0)
val OnSurfaceVariantDark = Color(0xFFCBD5E1)