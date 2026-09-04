package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Spendly Brand Colors - Fintech Premium Mockup Palette
val FintechAccent = Color(0xFFDDF247) // High-contrast electric neon lime
val AmbientOrangeGlow = Color(0xFFC06240) // Warm sunset orange / amber copper
val AmbientIndigoGlow = Color(0xFF2C1E3D) // Cosmic indigo / violet oxide
val CoralRed = Color(0xFFF87171) // Elegantly styled outflow red
val OpaqueDialogBg = Color(0xFF15131B)

// Raw values for constructing the Glassmorphic Fintech Theme in Theme.kt
val RawSlateBg = Color(0xFF09080E) // Deep pitch black background base
val RawSlateSurface = Color(0x3D1F222C) // Translucent dark grey `#1F222C` with 24% opacity (frosted glass)
val RawSlateCard = Color(0x3D1F222C) // Translucent dark grey card fill with 24% opacity
val RawSlateLine = Color(0x19FFFFFF) // Subtle transparent white stroke `#19FFFFFF` (1dp)

val RawLightText = Color(0xFFFFFFFF) // Pure white `#FFFFFF`
val RawGrayText = Color(0xFFA1A1AA) // Muted slate grey `#A1A1AA`

val RawLightBg = Color(0xFF09080E) // Deep pitch black background base (even in light mode for consistency)
val RawLightSurface = Color(0x3D1F222C)
val RawLightCard = Color(0x3D1F222C)
val RawLightBorder = Color(0x19FFFFFF)

val RawDarkText = Color(0xFFFFFFFF)
val RawDarkGrayText = Color(0xFFA1A1AA)

// Dynamic theme-aware colors mapping directly to current MaterialTheme.colorScheme properties
val SlateBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val SlateSurface: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val SlateCard: Color
    @Composable
    get() = MaterialTheme.colorScheme.surfaceVariant

val SlateLine: Color
    @Composable
    get() = MaterialTheme.colorScheme.outline

val LightText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground

val GrayText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant
