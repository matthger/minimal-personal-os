package com.tisosoft.minimalpersonalos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tisosoft.minimalpersonalos.data.Contact
import com.tisosoft.minimalpersonalos.data.ContactRepository
import com.tisosoft.minimalpersonalos.data.SettingsRepository
import com.tisosoft.minimalpersonalos.utils.SoundManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri

data class PhoneState(
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val dialerNumber: String = "",
    val activeTab: Int = 0 // 0 for Contacts, 1 for Dialer
)

class PhoneViewModel(
    private val contactRepository: ContactRepository,
    private val soundManager: SoundManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneState())
    val uiState: StateFlow<PhoneState> = _uiState.asStateFlow()

    fun loadContacts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                contactRepository.getContactsWithFavoritesSorted().collect { contacts ->
                    _uiState.value = _uiState.value.copy(
                        contacts = contacts,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun playClickSound() {
        val clickVolume = settingsRepository.loadClickVolume()
        val clickFeedbackEnabled = settingsRepository.loadClickFeedbackEnabled()

        if (clickFeedbackEnabled) {
            soundManager.playClick(clickVolume)
        }
    }

    fun updateTab(index: Int) {
        playClickSound()
        _uiState.value = _uiState.value.copy(activeTab = index)
    }

    fun appendDialerDigit(digit: String) {
        playClickSound()
        val currentNumber = _uiState.value.dialerNumber
        if (currentNumber.length < 20) {
            _uiState.value = _uiState.value.copy(dialerNumber = currentNumber + digit)
        }
    }

    fun removeLastDialerDigit() {
        playClickSound()
        val currentNumber = _uiState.value.dialerNumber
        if (currentNumber.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(dialerNumber = currentNumber.dropLast(1))
        }
    }

    fun initiateCall(context: Context, phoneNumber: String) {
        playClickSound()
        
        val intent = if (context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Intent(Intent.ACTION_CALL).apply {
                data = "tel:${Uri.encode(phoneNumber)}".toUri()
            }
        } else {
            Intent(Intent.ACTION_DIAL).apply {
                data = "tel:${Uri.encode(phoneNumber)}".toUri()
            }
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun addContact(name: String, phoneNumber: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = contactRepository.addContact(name, phoneNumber)
            if (success) {
                loadContacts()
            }
            onComplete(success)
        }
    }

    fun updateContact(contactId: Long, name: String, phoneNumber: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = contactRepository.updateContact(contactId, name, phoneNumber)
            if (success) {
                loadContacts()
            }
            onComplete(success)
        }
    }

    fun deleteContact(contactId: Long, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = contactRepository.deleteContact(contactId)
            if (success) {
                loadContacts()
            }
            onComplete(success)
        }
    }

    class Factory(
        private val contactRepository: ContactRepository,
        private val soundManager: SoundManager,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PhoneViewModel(
                contactRepository,
                soundManager,
                settingsRepository
            ) as T
        }
    }
}
