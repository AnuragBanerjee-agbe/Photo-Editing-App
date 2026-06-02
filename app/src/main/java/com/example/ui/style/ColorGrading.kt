package com.example.ui.style

import androidx.compose.ui.graphics.ColorMatrix

data class ColorGradePreset(
    val name: String,
    val description: String,
    val matrix: ColorMatrix,
    val vignetteColor: androidx.compose.ui.graphics.Color? = null,
    val vignetteIntensity: Float = 0f
)

object ColorGrading {
    val presets = listOf(
        ColorGradePreset(
            name = "Corporate Clean",
            description = "Crisp, balanced whites and elevated blues. High-fidelity contrast for executive pitches.",
            matrix = ColorMatrix(floatArrayOf(
                1.05f, 0.0f,  0.0f,  0.0f, 5f,   // Red
                0.0f,  1.05f, 0.0f,  0.0f, 5f,   // Green
                0.0f,  0.0f,  1.15f, 0.0f, 15f,  // Blue (elevate blues slightly)
                0.0f,  0.0f,  0.0f,  1.0f, 0.0f  // Alpha
            ))
        ),
        ColorGradePreset(
            name = "Warm Sunset",
            description = "Warm golden ambers and deep crimson shadows. Inviting look for social recaps.",
            matrix = ColorMatrix(floatArrayOf(
                1.2f,  0.0f,  0.0f,  0.0f, 15f,  // Red (emphasize red)
                0.0f,  1.1f,  0.0f,  0.0f, 10f,  // Green (yellow warmth)
                0.0f,  0.0f,  0.9f,  0.0f, -10f, // Blue (subdue coldness)
                0.0f,  0.0f,  0.0f,  1.0f, 0.0f
            )),
            vignetteColor = androidx.compose.ui.graphics.Color(0xFF5D4037),
            vignetteIntensity = 0.25f
        ),
        ColorGradePreset(
            name = "Cyber Tech Blue",
            description = "High-contrast midnight cyber blue with neon cyan reflections. Gamers and builders showcase.",
            matrix = ColorMatrix(floatArrayOf(
                0.8f,  0.0f,  0.0f,  0.0f, -15f,
                0.0f,  0.95f, 0.0f,  0.0f, 5f,
                0.0f,  0.0f,  1.25f, 0.0f, 25f,  // Boost blue
                0.0f,  0.0f,  0.0f,  1.0f, 0.0f
            )),
            vignetteColor = androidx.compose.ui.graphics.Color(0xFF001122),
            vignetteIntensity = 0.4f
        ),
        ColorGradePreset(
            name = "Retro Cam",
            description = "Soft saturation, warm nostalgic yellow hues. Analog film look for highlights.",
            matrix = ColorMatrix(floatArrayOf(
                1.1f,  0.0f,  0.0f,  0.0f, 8f,
                0.0f,  1.05f, 0.0f,  0.0f, 12f,
                0.0f,  0.0f,  0.9f,  0.0f, -5f,
                0.0f,  0.0f,  0.0f,  1.0f, 0.0f
            )).apply {
                // Lower overall saturation mathematically
                setToSaturation(0.75f)
            },
            vignetteColor = androidx.compose.ui.graphics.Color(0xFF3E2723),
            vignetteIntensity = 0.3f
        ),
        ColorGradePreset(
            name = "Teal & Orange",
            description = "Cinematic block-buster grade. Rich warm highlights matched with icy teal shadows.",
            matrix = ColorMatrix(floatArrayOf(
                1.15f, 0.0f,  0.0f,  0.0f, 18f,  // Red
                0.0f,  1.02f, 0.0f,  0.0f, 4f,   // Green
                0.0f,  0.0f,  1.1f,  0.0f, 8f,   // Blue
                0.0f,  0.0f,  0.0f,  1.0f, 0.0f
            )).apply {
                setToSaturation(1.15f)
            },
            vignetteColor = androidx.compose.ui.graphics.Color(0xFF001015),
            vignetteIntensity = 0.35f
        ),
        ColorGradePreset(
            name = "B&W Editorial",
            description = "Grit, high contrast, timeless monocromatic film grain. Bold style for slides.",
            matrix = ColorMatrix(floatArrayOf(
                0.2126f, 0.7152f, 0.0722f, 0.0f, -10f, // Gray brightness weights
                0.2126f, 0.7152f, 0.0722f, 0.0f, -10f,
                0.2126f, 0.7152f, 0.0722f, 0.0f, -10f,
                0.0f,    0.0f,    0.0f,    1.0f, 0.0f
            )),
            vignetteColor = androidx.compose.ui.graphics.Color(0xFF121212),
            vignetteIntensity = 0.5f
        )
    )

    fun getByName(name: String): ColorGradePreset {
        return presets.find { it.name.lowercase() == name.lowercase() } ?: presets[0]
    }
}
