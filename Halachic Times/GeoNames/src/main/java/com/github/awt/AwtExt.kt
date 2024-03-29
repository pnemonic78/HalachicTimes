package com.github.awt

import java.awt.Color

fun Color.applyAlpha(alpha: Float): Color =
    Color(red, green, blue, ((alpha * 255.0f) + 0.5f).toInt())

fun Color.applyAlpha(alpha: Int): Color = Color(red, green, blue, alpha)
