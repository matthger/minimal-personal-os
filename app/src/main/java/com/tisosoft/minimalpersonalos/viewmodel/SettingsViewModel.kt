package com.tisosoft.minimalpersonalos.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tisosoft.minimalpersonalos.data.SettingsRepository
import com.tisosoft.minimalpersonalos.model.VisualProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsState(
    // Appearance
    val visualProfile: VisualProfile = VisualProfile.MINIMALISTIC,
    val fontSize: Float = 1.0f,
    val brightness: Float = 1.0f,
    val primaryColor: Long = 0xFF6650a4L,
    
    // Sound & Feedback
    val clickFeedbackEnabled: Boolean = true,
    val clickVolume: Float = 0.7f,
    
    // Security
    val pinProtectionEnabled: Boolean = false,
    val isPINVerified: Boolean = false,
    val showPINPrompt: Boolean = false,

    // First Boot
    val isFirstBoot: Boolean = true
)

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        val state = _uiState.value
        _uiState.value = state.copy(
            visualProfile = settingsRepository.loadVisualProfile(),
            fontSize = settingsRepository.loadFontSize(),
            brightness = settingsRepository.loadBrightness(),
            primaryColor = settingsRepository.loadPrimaryColor(),
            clickFeedbackEnabled = settingsRepository.loadClickFeedbackEnabled(),
            clickVolume = settingsRepository.loadClickVolume(),
            pinProtectionEnabled = settingsRepository.loadPINProtectionEnabled(),
            isFirstBoot = settingsRepository.isFirstBoot()
        )
    }
    
    // Appearance Updates
    fun updateVisualProfile(profile: VisualProfile) {
        settingsRepository.saveVisualProfile(profile)
        _uiState.value = _uiState.value.copy(visualProfile = profile)
    }
    
    fun updateFontSize(scale: Float) {
        settingsRepository.saveFontSize(scale)
        _uiState.value = _uiState.value.copy(fontSize = scale)
    }
    
    fun updateBrightness(brightness: Float) {
        settingsRepository.saveBrightness(brightness)
        _uiState.value = _uiState.value.copy(brightness = brightness)
    }
    
    fun updatePrimaryColor(color: Long) {
        settingsRepository.savePrimaryColor(color)
        _uiState.value = _uiState.value.copy(primaryColor = color)
    }
    
    // Sound & Feedback Updates
    fun updateClickFeedbackEnabled(enabled: Boolean) {
        settingsRepository.saveClickFeedbackEnabled(enabled)
        _uiState.value = _uiState.value.copy(clickFeedbackEnabled = enabled)
    }
    
    fun updateClickVolume(volume: Float) {
        settingsRepository.saveClickVolume(volume)
        _uiState.value = _uiState.value.copy(clickVolume = volume)
    }
    
    // Security/PIN Management
    fun verifyPIN(enteredPin: String): Boolean {
        val isValid = settingsRepository.verifyPIN(enteredPin)
        if (isValid) {
            _uiState.value = _uiState.value.copy(isPINVerified = true)
        }
        return isValid
    }
    
    fun resetPINVerification() {
        _uiState.value = _uiState.value.copy(isPINVerified = false)
    }
    
    fun showPINPrompt(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPINPrompt = show)
    }
    
    fun updatePIN(newPin: String) {
        settingsRepository.savePIN(newPin)
    }
    
    fun updatePINProtectionEnabled(enabled: Boolean) {
        settingsRepository.savePINProtectionEnabled(enabled)
        _uiState.value = _uiState.value.copy(pinProtectionEnabled = enabled)
    }

    // First Boot
    fun completeFirstBoot() {
        settingsRepository.setFirstBootCompleted()
        _uiState.value = _uiState.value.copy(isFirstBoot = false)
    }

    fun resetLauncher() {
        settingsRepository.resetFirstBoot()
        _uiState.value = _uiState.value.copy(isFirstBoot = true)
    }
}

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(SettingsRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
