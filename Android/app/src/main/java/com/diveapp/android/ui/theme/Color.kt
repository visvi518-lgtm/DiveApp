package com.diveapp.android.ui.theme

import androidx.compose.ui.graphics.Color

/** Semantic color roles from Docs/08_DesignSystem.md. Real brand colors are
 * not yet defined ("실제 색상값은 추후 정의한다") — these are neutral
 * placeholders until a palette is chosen. */
val DivePrimary = Color(0xFF0A6E8C)
val DivePrimaryDark = Color(0xFF4FC3E0)
val DiveSecondary = Color(0xFF5C6B73)
val DiveError = Color(0xFFB3261E)
val DiveWarning = Color(0xFFF9A825)
val DiveSuccess = Color(0xFF2E7D32)
val DiveInformation = Color(0xFF1565C0)

val DiveBackgroundLight = Color(0xFFFFFFFF)
val DiveSurfaceLight = Color(0xFFF2F5F6)
val DiveBackgroundDark = Color(0xFF101416)
val DiveSurfaceDark = Color(0xFF1B2124)

/** Convenience accessors for the roles above, so call sites read like
 * `AppColor.success` instead of importing each `DiveXxx` constant. */
object AppColor {
    val warning = DiveWarning
    val success = DiveSuccess
    val information = DiveInformation
}
