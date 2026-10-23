package com.tisosoft.minimalpersonalos.utils

import android.content.ContentResolver
import android.provider.Settings
import kotlin.math.*

object BrightnessUtils {
    // Hidden setting key for perceptual brightness (0.0 to 1.0)
    // Introduced in API 29 (Android 10)
    private const val SCREEN_BRIGHTNESS_FLOAT = "screen_brightness_float"

    // HLG (Hybrid Log-Gamma) constants used by AOSP for brightness curve
    private const val R = 0.5f
    private const val A = 0.17883277f
    private const val B = 0.28466892f
    private const val C = 0.55991073f

    /**
     * Converts from the perceptual space (0.0 to 1.0) to linear space (0.0 to 1.0).
     */
    fun convertPerceptualToLinear(value: Float): Float {
        val ret = if (value <= R) {
            (value / R) * (value / R)
        } else {
            exp(((value - C) / A).toDouble()).toFloat() + B
        }
        return (ret / 12.0f).coerceIn(0f, 1f)
    }

    /**
     * Converts from linear space (0.0 to 1.0) to perceptual space (0.0 to 1.0).
     */
    fun convertLinearToPerceptual(value: Float): Float {
        val normalizedVal = value * 12.0f
        val ret = if (normalizedVal <= 1.0f) {
            sqrt(normalizedVal.toDouble()).toFloat() * R
        } else {
            A * log((normalizedVal - B).toDouble(), E).toFloat() + C
        }
        return ret.coerceIn(0f, 1f)
    }

    /**
     * Gets the current system brightness as a perceptual value (0.0 to 1.0).
     */
    fun getSystemBrightness(contentResolver: ContentResolver): Float {
        return try {
            // Try reading the float value first (API 29+)
            // This is the most accurate as it matches the system slider directly
            Settings.System.getFloat(contentResolver, SCREEN_BRIGHTNESS_FLOAT)
        } catch (_: Exception) {
            // Fallback: Read integer brightness and convert
            val brightnessInt = try {
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (_: Exception) {
                127
            }
            val linear = brightnessInt / 255f
            
            // On Android 9+ (which is all our supported versions), AOSP uses HLG curve
            convertLinearToPerceptual(linear)
        }
    }

    /**
     * Sets the system brightness using a perceptual value (0.0 to 1.0).
     */
    fun setSystemBrightness(contentResolver: ContentResolver, brightness: Float) {
        // 1. Calculate linear value for the legacy integer setting
        val linear = convertPerceptualToLinear(brightness)
        val brightnessInt = (linear * 255).toInt().coerceIn(0, 255)
        
        // 2. Set the integer brightness (legacy and for apps that don't know about the float)
        try {
            val currentInt = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            if (currentInt != brightnessInt) {
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessInt
                )
            }
        } catch (_: Exception) {
            // Fallback: just try to put it if we can't read it
            try {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessInt)
            } catch (_: Exception) {}
        }

        // 3. Set the float brightness (API 29+)
        // This ensures the system slider is perfectly aligned
        try {
            val currentFloat = Settings.System.getFloat(contentResolver, SCREEN_BRIGHTNESS_FLOAT)
            if (abs(currentFloat - brightness) > 0.001f) {
                Settings.System.putFloat(
                    contentResolver,
                    SCREEN_BRIGHTNESS_FLOAT,
                    brightness
                )
            }
        } catch (_: Exception) {
            // Fallback: just try to put it if we can't read it
            try {
                Settings.System.putFloat(contentResolver, SCREEN_BRIGHTNESS_FLOAT, brightness)
            } catch (_: Exception) {}
        }
    }
}
