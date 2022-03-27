package top.sanqii.finance.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color

val Green500 = Color(0xFF1EB980)
val DarkBlue900 = Color(0xFF26282F)

val IncomesStartColor = Color(0xFF004940)
val BillsStartColor = Color(0xFFAFAC12)
val AccountStartColor = Color(0xFF888888)

// Rally is always dark themed.
val ColorPalette = darkColors(
    primary = Green500,
    surface = DarkBlue900,
    onSurface = Color.White,
    background = DarkBlue900,
    onBackground = Color.White
)
