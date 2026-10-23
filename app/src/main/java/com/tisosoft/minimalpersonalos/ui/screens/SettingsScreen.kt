package com.tisosoft.minimalpersonalos.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tisosoft.minimalpersonalos.R
import com.tisosoft.minimalpersonalos.data.AppRepository
import com.tisosoft.minimalpersonalos.model.AppInfo
import com.tisosoft.minimalpersonalos.model.VisualProfile
import com.tisosoft.minimalpersonalos.ui.components.*
import com.tisosoft.minimalpersonalos.viewmodel.SettingsViewModel
import com.tisosoft.minimalpersonalos.viewmodel.SettingsViewModelFactory
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onButtonClick: () -> Unit,
    onError: () -> Unit,
    onColorChanged: (Color) -> Unit,
    primaryColor: Color,
    viewModel: SettingsViewModel,
    visualProfile: VisualProfile
) {
    val context = LocalContext.current
    val appRepository = remember { AppRepository(context) }
    val uiState by viewModel.uiState.collectAsState()
    
    var showColorSelection by remember { mutableStateOf(false) }
    var showAppSelection by remember { mutableStateOf(false) }
    var selectedApps by remember { mutableStateOf(emptyList<AppInfo>()) }
    
    // Preload all strings outside of lambdas
    val minimalisticModeStr = stringResource(R.string.minimalistic_mode_label)
    val simpleModeStr = stringResource(R.string.simple_mode_label)
    val fontSizeStr = stringResource(R.string.font_size)
    val fontSizeDescStr = stringResource(R.string.font_size_desc)
    val brightnessStr = stringResource(R.string.brightness)
    val brightnessDescStr = stringResource(R.string.brightness_desc)
    val primaryColorStr = stringResource(R.string.select_primary_color)
    val primaryColorDescStr = stringResource(R.string.select_primary_color_desc)
    val clickFeedbackStr = stringResource(R.string.click_feedback)
    val clickFeedbackDescStr = stringResource(R.string.click_feedback_desc)
    val clickVolumeStr = stringResource(R.string.click_volume)
    val clickVolumeDescStr = stringResource(R.string.click_volume_desc)
    val settingsProtectedStr = stringResource(R.string.settings_protected_section)
    val backButtonStr = stringResource(R.string.back_button)
    val visualProfileStr = stringResource(R.string.visual_profile)
    val soundFeedbackGroupStr = stringResource(R.string.sound_feedback_group)

    val protectedBtnStr = stringResource(R.string.protected_settings_button)
    val advancedBtnStr = stringResource(R.string.advanced_settings_button)
    val protectedBtnDesc = stringResource(R.string.protected_settings_desc)
    val advancedBtnDesc = stringResource(R.string.advanced_settings_desc)
    
    LaunchedEffect(Unit) {
        selectedApps = appRepository.loadSelectedApps()
    }

    var showProtectedScreen by remember { mutableStateOf(false) }

    // PIN Dialog trigger
    if (uiState.showPINPrompt && uiState.pinProtectionEnabled) {
        PINDialog(
            onDismiss = {
                onButtonClick()
                viewModel.showPINPrompt(false)
            },
            onConfirm = {
                onButtonClick()
                viewModel.showPINPrompt(false)
                showProtectedScreen = true
            },
            validatePin = { pin -> viewModel.verifyPIN(pin) },
            onError = { _ -> onError() },
            primaryColor = primaryColor
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    if (visualProfile == VisualProfile.MINIMALISTIC) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backButtonStr)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // OPEN SETTINGS
                SettingsSection(title = stringResource(R.string.appearance_group)) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Visual Profile Toggle
                        SettingsButtonGroup(
                            title = visualProfileStr,
                            options = listOf(minimalisticModeStr, simpleModeStr),
                            selectedOption = if (uiState.visualProfile == VisualProfile.MINIMALISTIC)
                                minimalisticModeStr
                            else 
                                simpleModeStr,
                            onOptionSelected = { selected ->
                                onButtonClick()
                                viewModel.updateVisualProfile(
                                    if (selected == minimalisticModeStr) VisualProfile.MINIMALISTIC else VisualProfile.SIMPLE
                                )
                            },
                            primaryColor = primaryColor
                        )
                        
                        // Font Size Slider
                        SettingsSlider(
                            title = fontSizeStr,
                            subtitle = fontSizeDescStr,
                            value = uiState.fontSize,
                            onValueChange = { 
                                onButtonClick()
                                viewModel.updateFontSize(it)
                            },
                            valueRange = 0.8f..1.5f,
                            steps = 6,
                            primaryColor = primaryColor
                        )
                        
                        // Brightness Slider
                        var sliderValue by remember { mutableFloatStateOf(uiState.brightness) }
                        var isDragging by remember { mutableStateOf(false) }
                        
                        // Sync slider with external changes (e.g. from notification shade)
                        // But ONLY when the user is not actively touching the slider
                        LaunchedEffect(uiState.brightness) {
                            if (!isDragging) {
                                sliderValue = uiState.brightness
                            }
                        }

                        SettingsSlider(
                            title = brightnessStr,
                            subtitle = brightnessDescStr,
                            value = sliderValue,
                            onValueChange = { newValue ->
                                isDragging = true
                                sliderValue = newValue
                                if (Settings.System.canWrite(context)) {
                                    viewModel.updateBrightness(newValue)
                                }
                            },
                            onValueChangeFinished = {
                                isDragging = false
                                onButtonClick()
                                // Ensure the final position is synced
                                viewModel.updateBrightness(sliderValue)

                                if (!Settings.System.canWrite(context)) {
                                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                                        data = "package:${context.packageName}".toUri()
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            valueRange = 0.0f..1.0f,
                            steps = 6,
                            primaryColor = primaryColor
                        )
                        
                        // Primary Color Button
                        SettingsButton(
                            title = primaryColorStr,
                            subtitle = primaryColorDescStr,
                            onClick = {
                                onButtonClick()
                                showColorSelection = true
                            }
                        )
                    }
                }
                
                // SOUND & FEEDBACK
                SettingsSection(title = soundFeedbackGroupStr) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Click Feedback Toggle
                        SettingsToggle(
                            title = clickFeedbackStr,
                            subtitle = clickFeedbackDescStr,
                            isChecked = uiState.clickFeedbackEnabled,
                            onCheckedChange = {
                                onButtonClick()
                                viewModel.updateClickFeedbackEnabled(it)
                            },
                            primaryColor = primaryColor
                        )
                        
                        // Click Volume Slider
                        SettingsSlider(
                            title = clickVolumeStr,
                            subtitle = clickVolumeDescStr,
                            value = uiState.clickVolume,
                            onValueChange = {
                                onButtonClick()
                                viewModel.updateClickVolume(it)
                            },
                            valueRange = 0f..1.0f,
                            steps = 9,
                            enabled = uiState.clickFeedbackEnabled,
                            primaryColor = primaryColor
                        )
                    }
                }

                // PROTECTED SETTINGS
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                Text(
                    text = settingsProtectedStr,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // Protected / Advanced Settings button
                val protectedTitle = if (uiState.pinProtectionEnabled) protectedBtnStr else advancedBtnStr
                val protectedSubtitle = if (uiState.pinProtectionEnabled) protectedBtnDesc else advancedBtnDesc

                SettingsButton(
                    title = protectedTitle,
                    subtitle = protectedSubtitle,
                    onClick = {
                        onButtonClick()
                        if (uiState.pinProtectionEnabled && !uiState.isPINVerified) {
                            viewModel.showPINPrompt(true)
                        } else {
                            showProtectedScreen = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
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

    // Color Selection Dialog
    if (showColorSelection) {
        val originalColor = remember { Color(uiState.primaryColor.toInt()) }
        ColorSelectionDialog(
            initialColor = originalColor,
            onDismiss = {
                onButtonClick()
                onColorChanged(originalColor)
                showColorSelection = false
            },
            onConfirm = { color ->
                onButtonClick()
                viewModel.updatePrimaryColor(color.toArgb().toLong())
                onColorChanged(color)
                showColorSelection = false
            },
            onPreview = { previewColor ->
                onColorChanged(previewColor)
            },
            onItemClick = onButtonClick,
            primaryColor = primaryColor
        )
    }
    
    // App Selection Dialog (read-only view)
    if (showAppSelection) {
        AppSelectionDialog(
            appRepository = appRepository,
            initialSelectedApps = selectedApps,
            onDismiss = {
                onButtonClick()
                showAppSelection = false
            },
            onConfirm = { apps ->
                onButtonClick()
                selectedApps = apps
                appRepository.saveSelectedApps(apps)
                showAppSelection = false
            },
            onItemClick = onButtonClick,
            onError = onError,
            primaryColor = primaryColor
        )
    }

    // Protected Settings Screen
    AnimatedVisibility(
        visible = showProtectedScreen,
        enter = slideInHorizontally(animationSpec = tween(400)) { it } + fadeIn(tween(400)),
        exit = slideOutHorizontally(animationSpec = tween(400)) { it } + fadeOut(tween(400))
    ) {
        ProtectedSettingsScreen(
            onBack = {
                onButtonClick()
                showProtectedScreen = false
                viewModel.resetPINVerification()
            },
            onButtonClick = onButtonClick,
            onError = onError,
            primaryColor = primaryColor,
            viewModel = viewModel,
            visualProfile = visualProfile
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    val context = LocalContext.current
    SettingsScreen(
        onBack = {},
        onButtonClick = {},
        onError = {},
        onColorChanged = {},
        primaryColor = Color(0xFF6650a4),
        viewModel = remember {
            SettingsViewModelFactory(context).create(SettingsViewModel::class.java)
        },
        visualProfile = VisualProfile.MINIMALISTIC
    )
}
