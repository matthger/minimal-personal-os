package com.tisosoft.minimalpersonalos.data

import android.content.Context
import androidx.core.content.edit
import com.tisosoft.minimalpersonalos.model.VisualProfile
import com.tisosoft.minimalpersonalos.utils.BrightnessUtils

const val PREF_KEY_PRIMARY_COLOR = "primaryColor"
const val DEFAULT_PRIMARY_COLOR = 0xFF6650a4L

const val PREFS_NAME = "MinimalPersonalOS_Prefs"
const val PREF_KEY_SELECTED_APPS = "selectedApps"

// Appearance
const val PREF_KEY_VISUAL_PROFILE = "visualProfile"
val DEFAULT_VISUAL_PROFILE = VisualProfile.MINIMALISTIC

const val PREF_KEY_FONT_SIZE = "fontSize"
const val DEFAULT_FONT_SIZE = 1.0f

const val PREF_KEY_BRIGHTNESS = "brightness"
const val DEFAULT_BRIGHTNESS = 1.0f

// Sound & Feedback
const val PREF_KEY_CLICK_FEEDBACK_ENABLED = "clickFeedbackEnabled"
const val DEFAULT_CLICK_FEEDBACK_ENABLED = true

const val PREF_KEY_CLICK_VOLUME = "clickVolume"
const val DEFAULT_CLICK_VOLUME = 0.7f

// Security
const val PREF_KEY_PIN = "userPIN"
const val DEFAULT_PIN = "0000" // Default PIN

const val PREF_KEY_PIN_PROTECTION_ENABLED = "pinProtectionEnabled"
const val DEFAULT_PIN_PROTECTION_ENABLED = false

// Phone Favorites
const val PREF_KEY_FAVORITE_CONTACTS = "favoriteContacts"
const val DEFAULT_FAVORITE_CONTACTS = ""

const val PREF_KEY_FIRST_BOOT = "isFirstBoot"

class SettingsRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Primary Color
    fun savePrimaryColor(colorValue: Long) {
        prefs.edit {
            putLong(PREF_KEY_PRIMARY_COLOR, colorValue)
        }
    }

    fun loadPrimaryColor(): Long {
        return prefs.getLong(PREF_KEY_PRIMARY_COLOR, DEFAULT_PRIMARY_COLOR)
    }

    // Visual Profile (MINIMALISTIC vs SIMPLE)
    fun saveVisualProfile(profile: VisualProfile) {
        prefs.edit {
            putString(PREF_KEY_VISUAL_PROFILE, profile.name)
        }
    }

    fun loadVisualProfile(): VisualProfile {
        val profileName = prefs.getString(PREF_KEY_VISUAL_PROFILE, DEFAULT_VISUAL_PROFILE.name)
            ?: DEFAULT_VISUAL_PROFILE.name
        return try {
            VisualProfile.valueOf(profileName)
        } catch (_: IllegalArgumentException) {
            DEFAULT_VISUAL_PROFILE
        }
    }

    // Font Size
    fun saveFontSize(scale: Float) {
        prefs.edit {
            putFloat(PREF_KEY_FONT_SIZE, scale)
        }
    }

    fun loadFontSize(): Float {
        return prefs.getFloat(PREF_KEY_FONT_SIZE, DEFAULT_FONT_SIZE)
    }

    // Brightness
    fun saveBrightness(brightness: Float) {
        prefs.edit {
            putFloat(PREF_KEY_BRIGHTNESS, brightness)
        }
    }

    fun loadBrightness(): Float {
        // Prefer current system brightness if possible to stay in sync
        return try {
            BrightnessUtils.getSystemBrightness(context.contentResolver)
        } catch (_: Exception) {
            prefs.getFloat(PREF_KEY_BRIGHTNESS, DEFAULT_BRIGHTNESS)
        }
    }

    // Click Feedback
    fun saveClickFeedbackEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(PREF_KEY_CLICK_FEEDBACK_ENABLED, enabled)
        }
    }

    fun loadClickFeedbackEnabled(): Boolean {
        return prefs.getBoolean(PREF_KEY_CLICK_FEEDBACK_ENABLED, DEFAULT_CLICK_FEEDBACK_ENABLED)
    }

    // Click Volume
    fun saveClickVolume(volume: Float) {
        prefs.edit {
            putFloat(PREF_KEY_CLICK_VOLUME, volume)
        }
    }

    fun loadClickVolume(): Float {
        return prefs.getFloat(PREF_KEY_CLICK_VOLUME, DEFAULT_CLICK_VOLUME)
    }

    // PIN Management
    fun savePIN(pin: String) {
        prefs.edit {
            putString(PREF_KEY_PIN, pin)
        }
    }

    fun loadPIN(): String {
        return prefs.getString(PREF_KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun verifyPIN(enteredPin: String): Boolean {
        return enteredPin == loadPIN()
    }

    // PIN Protection Toggle
    fun savePINProtectionEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(PREF_KEY_PIN_PROTECTION_ENABLED, enabled)
        }
    }

    fun loadPINProtectionEnabled(): Boolean {
        return prefs.getBoolean(PREF_KEY_PIN_PROTECTION_ENABLED, DEFAULT_PIN_PROTECTION_ENABLED)
    }

    // Favorite Contacts
    fun saveFavoriteContactIds(contactIds: List<Long>) {
        val idsString = contactIds.joinToString(",")
        prefs.edit {
            putString(PREF_KEY_FAVORITE_CONTACTS, idsString)
        }
    }

    fun loadFavoriteContactIds(): List<Long> {
        val idsString = prefs.getString(PREF_KEY_FAVORITE_CONTACTS, DEFAULT_FAVORITE_CONTACTS) ?: DEFAULT_FAVORITE_CONTACTS
        return if (idsString.isEmpty()) {
            emptyList()
        } else {
            idsString.split(",").mapNotNull { it.toLongOrNull() }
        }
    }

    fun getFavoritesSync(): List<Long> {
        return loadFavoriteContactIds()
    }

    // First Boot
    fun isFirstBoot(): Boolean {
        return prefs.getBoolean(PREF_KEY_FIRST_BOOT, true)
    }

    fun setFirstBootCompleted() {
        prefs.edit {
            putBoolean(PREF_KEY_FIRST_BOOT, false)
        }
    }

    fun resetFirstBoot() {
        prefs.edit {
            putBoolean(PREF_KEY_FIRST_BOOT, true)
        }
    }
}
