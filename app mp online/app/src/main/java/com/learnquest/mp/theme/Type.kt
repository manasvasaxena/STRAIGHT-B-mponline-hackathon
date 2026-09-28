package com.learnquest.mp.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Sizes use sp so they scale with the user's system font-size setting.
val AppTypography = Typography(
    headlineMedium = TextStyle(FontFamily.Default, FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(FontFamily.Default, FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(FontFamily.Default, FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(FontFamily.Default, FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(FontFamily.Default, FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(FontFamily.Default, FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
)
