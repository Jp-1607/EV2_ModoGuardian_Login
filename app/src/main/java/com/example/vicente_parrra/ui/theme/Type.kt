package com.example.vicente_parrra.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.vicente_parrra.R

private val Atkinson = FontFamily(
    Font(R.font.atkinson_regular), Font(R.font.atkinson_bold, FontWeight.Bold)
)

private val Barlow = FontFamily(
    Font(R.font.barlow_semibold, FontWeight.SemiBold), Font(R.font.barlow_bold, FontWeight.Bold)
)

val Typography = Typography(
    displaySmall = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 50.sp),
    headlineLarge = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 42.sp),
    headlineMedium = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.SemiBold, fontSize = 25.sp, lineHeight = 29.sp),
    titleMedium = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Atkinson, fontSize = 17.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Atkinson, fontSize = 15.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = Atkinson, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 17.sp),
    labelSmall = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 15.sp)
)
