package com.tisosoft.minimalpersonalos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.tisosoft.minimalpersonalos.ui.components.*
import com.tisosoft.minimalpersonalos.data.AppRepository
import com.tisosoft.minimalpersonalos.viewmodel.SettingsViewModel
import com.tisosoft.minimalpersonalos.R
import com.tisosoft.minimalpersonalos.model.VisualProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectedSettingsScreen(
    onBack: () -> Unit,
    onButtonClick: () -> Unit,
    onError: () -> Unit,
    primaryColor: Color,
    viewModel: SettingsViewModel,
    visualProfile: VisualProfile
) {
    val context = LocalContext.current
    val appRepository = remember { AppRepository(context) }
    val settingsRepository = remember { 
        com.tisosoft.minimalpersonalos.data.SettingsRepository(context) 
    }
    val contactRepository = remember { 
        com.tisosoft.minimalpersonalos.data.ContactRepository(context, settingsRepository) 
    }
    val uiState by viewModel.uiState.collectAsState()

    var showAppSelection by remember { mutableStateOf(false) }
    var showCoreContacts by remember { mutableStateOf(false) }
    var showChangePIN by remember { mutableStateOf(false) }
    var showResetLauncherConfirm by remember { mutableStateOf(false) }

    // Preload strings
    val appConfigStr = stringResource(R.string.app_config_group)
    val manageSlotsStr = stringResource(R.string.manage_app_slots)
    val manageSlotsDescStr = stringResource(R.string.manage_app_slots_desc)

    val contactsStr = stringResource(R.string.contacts_group)
    val coreContactsStr = stringResource(R.string.core_contacts)
    val coreContactsDescStr = stringResource(R.string.core_contacts_desc)

    val securityStr = stringResource(R.string.security_group)
    val changePinStr = stringResource(R.string.change_pin)
    val togglePinProtectionStr = stringResource(R.string.toggle_pin_protection)

    val systemStr = stringResource(R.string.system_group)
    val androidSettingsStr = stringResource(R.string.android_settings_access)
    val resetLauncherStr = stringResource(R.string.reset_launcher)
    val backButtonStr = stringResource(R.string.back_button)

    val settingsTitle =
        if (uiState.pinProtectionEnabled) stringResource(R.string.settings_protected_section) else stringResource(
            R.string.settings_advanced_section
        )

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(settingsTitle) },
            navigationIcon = {
                if (visualProfile == VisualProfile.MINIMALISTIC) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backButtonStr)
                    }
                }
            }
        )
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App Configuration
                SettingsSection(title = appConfigStr) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsButton(
                            title = manageSlotsStr,
                            subtitle = manageSlotsDescStr,
                            onClick = { onButtonClick(); showAppSelection = true }
                        )
                    }
                }

                // Contacts & Communication
                SettingsSection(title = contactsStr) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsButton(
                            title = coreContactsStr,
                            subtitle = coreContactsDescStr,
                            onClick = { onButtonClick(); showCoreContacts = true }
                        )
                    }
                }

                // Security & Access
                SettingsSection(title = securityStr) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsToggle(
                            title = togglePinProtectionStr,
                            subtitle = stringResource(R.string.toggle_pin_protection_desc),
                            isChecked = uiState.pinProtectionEnabled,
                            onCheckedChange = { enabled ->
                                onButtonClick()
                                if (enabled) {
                                    showChangePIN = true
                                } else {
                                    viewModel.updatePINProtectionEnabled(false)
                                }
                            },
                            primaryColor = primaryColor
                        )
                        SettingsButton(
                            title = changePinStr,
                            subtitle = stringResource(R.string.change_pin_desc),
                            onClick = { onButtonClick(); showChangePIN = true },
                            enabled = uiState.pinProtectionEnabled
                        )
                    }
                }

                // System
                SettingsSection(title = systemStr) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsButton(
                            title = androidSettingsStr,
                            subtitle = stringResource(R.string.android_settings_access_desc),
                            onClick = {
                                onButtonClick()
                                val intent =
                                    Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                context.startActivity(intent)
                            }
                        )
                        SettingsButton(
                            title = resetLauncherStr,
                            subtitle = stringResource(R.string.reset_launcher_desc),
                            onClick = {
                                onButtonClick()
                                showResetLauncherConfirm = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))
            }

            VerticalScrollbar(scrollState = scrollState)

            // Back Button
            if (visualProfile == VisualProfile.SIMPLE) {
                Button(
                    onClick = {
                        onButtonClick()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        backButtonStr,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showResetLauncherConfirm) {
        AlertDialog(
            onDismissRequest = { onButtonClick(); showResetLauncherConfirm = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text(resetLauncherStr) },
            text = { Text(stringResource(R.string.reset_launcher_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onButtonClick()
                        appRepository.clearSelectedApps()
                        // reset primary UI settings
                        viewModel.updatePrimaryColor(com.tisosoft.minimalpersonalos.data.DEFAULT_PRIMARY_COLOR)
                        viewModel.updateVisualProfile(com.tisosoft.minimalpersonalos.data.DEFAULT_VISUAL_PROFILE)
                        viewModel.updateFontSize(com.tisosoft.minimalpersonalos.data.DEFAULT_FONT_SIZE)
                        viewModel.updateBrightness(com.tisosoft.minimalpersonalos.data.DEFAULT_BRIGHTNESS)
                        viewModel.updateClickFeedbackEnabled(com.tisosoft.minimalpersonalos.data.DEFAULT_CLICK_FEEDBACK_ENABLED)
                        viewModel.updateClickVolume(com.tisosoft.minimalpersonalos.data.DEFAULT_CLICK_VOLUME)
                        viewModel.updatePINProtectionEnabled(com.tisosoft.minimalpersonalos.data.DEFAULT_PIN_PROTECTION_ENABLED)
                        viewModel.updatePIN(com.tisosoft.minimalpersonalos.data.DEFAULT_PIN)
                        viewModel.resetLauncher()
                        showResetLauncherConfirm = false
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(stringResource(R.string.yes), color = primaryColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onButtonClick(); showResetLauncherConfirm = false },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(stringResource(R.string.cancel), color = primaryColor)
                }
            }
        )
    }

    if (showAppSelection) {
        val selected = appRepository.loadSelectedApps()
        AppSelectionDialog(
            appRepository = appRepository,
            initialSelectedApps = selected,
            onDismiss = { onButtonClick(); showAppSelection = false },
            onConfirm = { apps ->
                onButtonClick(); appRepository.saveSelectedApps(apps); showAppSelection = false
            },
            onItemClick = onButtonClick,
            onError = onError,
            primaryColor = primaryColor
        )
    }

    if (showChangePIN) {
        ChangePINDialog(
            onDismiss = { onButtonClick(); showChangePIN = false },
            onConfirm = { newPin ->
                onButtonClick()
                viewModel.updatePIN(newPin)
                if (!uiState.pinProtectionEnabled) {
                    viewModel.updatePINProtectionEnabled(true)
                }
                showChangePIN = false
            },
            primaryColor = primaryColor
        )
    }

    if (showCoreContacts) {
        val savedFavorites = settingsRepository.loadFavoriteContactIds()
        CoreContactsDialog(
            contactRepository = contactRepository,
            initialSelectedIds = savedFavorites,
            onDismiss = { onButtonClick(); showCoreContacts = false },
            onConfirm = { selectedIds ->
                onButtonClick()
                settingsRepository.saveFavoriteContactIds(selectedIds)
                showCoreContacts = false
            },
            onItemClick = onButtonClick,
            onError = onError,
            primaryColor = primaryColor,
            context = context
        )
    }
}