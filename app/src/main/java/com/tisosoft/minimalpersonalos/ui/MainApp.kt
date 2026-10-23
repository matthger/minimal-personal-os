package com.tisosoft.minimalpersonalos.ui

import android.app.Activity
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.net.toUri
import com.tisosoft.minimalpersonalos.data.AppRepository
import com.tisosoft.minimalpersonalos.data.ContactRepository
import com.tisosoft.minimalpersonalos.data.SettingsRepository
import com.tisosoft.minimalpersonalos.ui.screens.GalleryScreen
import com.tisosoft.minimalpersonalos.ui.screens.LauncherScreen
import com.tisosoft.minimalpersonalos.ui.screens.PhoneScreen
import com.tisosoft.minimalpersonalos.ui.screens.SettingsScreen
import com.tisosoft.minimalpersonalos.ui.screens.SetupWizardScreen
import com.tisosoft.minimalpersonalos.ui.screens.NotesScreen
import com.tisosoft.minimalpersonalos.viewmodel.*
import com.tisosoft.minimalpersonalos.data.notes.NotesDatabase
import com.tisosoft.minimalpersonalos.data.notes.NotesRepository
import com.tisosoft.minimalpersonalos.utils.BrightnessUtils
import com.tisosoft.minimalpersonalos.viewmodel.GalleryViewModel
import com.tisosoft.minimalpersonalos.viewmodel.PhoneViewModel
import com.tisosoft.minimalpersonalos.viewmodel.SettingsViewModel
import com.tisosoft.minimalpersonalos.viewmodel.SettingsViewModelFactory
import com.tisosoft.minimalpersonalos.data.MediaRepository
import com.tisosoft.minimalpersonalos.utils.SoundManager

enum class Screen {
    Launcher,
    Settings,
    Phone,
    Notes,
    Gallery,
    SetupWizard
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val settingsViewModel = remember {
        SettingsViewModelFactory(context).create(SettingsViewModel::class.java)
    }
    val uiState by settingsViewModel.uiState.collectAsState()
    val soundManager = remember { SoundManager(context) }
    
    // Initialize repositories
    val settingsRepository = remember { SettingsRepository(context) }
    val appRepository = remember { AppRepository(context) }
    val contactRepository = remember { ContactRepository(context, settingsRepository) }
    val notesRepository = remember {
        NotesRepository(NotesDatabase.getDatabase(context).noteDao())
    }
    val mediaRepository = remember { MediaRepository(context) }

    // Initialize PhoneViewModel
    val phoneViewModel = remember {
        PhoneViewModel.Factory(
            contactRepository,
            soundManager,
            settingsRepository
        ).create(PhoneViewModel::class.java)
    }

    // Initialize NotesViewModel
    val notesViewModel = remember(uiState.visualProfile) {
        NotesViewModel.Factory(
            notesRepository,
            uiState.visualProfile
        ).create(NotesViewModel::class.java)
    }

    val galleryViewModel = remember {
        GalleryViewModel(mediaRepository)
    }

    var currentScreen by remember {
        mutableStateOf(if (uiState.isFirstBoot) Screen.SetupWizard else Screen.Launcher)
    }

    // Sync screen with isFirstBoot changes
    LaunchedEffect(uiState.isFirstBoot) {
        if (uiState.isFirstBoot) {
            currentScreen = Screen.SetupWizard
        } else if (currentScreen == Screen.SetupWizard) {
            currentScreen = Screen.Launcher
        }
    }

    val primaryColor = Color(uiState.primaryColor.toInt())

    // Tracks if the current brightness change originated from the system slider externally
    var isUpdatingFromSystem by remember { mutableStateOf(false) }
    var lastTimeSetBrightness by remember { mutableLongStateOf(0L) }

    // Apply Brightness
    LaunchedEffect(uiState.brightness) {
        if (isUpdatingFromSystem) {
            // Change came from the system slider observer; clear the flag and skip writing back
            isUpdatingFromSystem = false
            return@LaunchedEffect
        }

        // Apply System-wide Brightness
        if (Settings.System.canWrite(context)) {
            try {
                lastTimeSetBrightness = System.currentTimeMillis()
                // Use our utility to set system brightness (handles float and int correctly)
                BrightnessUtils.setSystemBrightness(
                    context.contentResolver,
                    uiState.brightness
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Ensure window doesn't override system brightness
        // This removes the "Controlled by app" restriction in the system notification menu
        (context as? Activity)?.window?.let { window ->
            if (window.attributes.screenBrightness != -1.0f) {
                val lp = window.attributes
                lp.screenBrightness = -1.0f
                window.attributes = lp
            }
        }
    }

    // Sync system brightness back to our state if changed externally (e.g., from notification menu)
    DisposableEffect(context, settingsViewModel) {
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                // If the update happens within 500ms of our own programmatic change, ignore it
                if (System.currentTimeMillis() - lastTimeSetBrightness < 500L) {
                    return
                }
                try {
                    val systemBrightness = BrightnessUtils.getSystemBrightness(
                        context.contentResolver
                    )
                    // Always read from the live real-time state of the ViewModel to avoid stale Compose captures
                    val currentLiveBrightness = settingsViewModel.uiState.value.brightness
                    
                    // Only update if it's different from our current state (ignoring minor inaccuracies)
                    if (Math.abs(currentLiveBrightness - systemBrightness) > 0.02f) {
                        isUpdatingFromSystem = true
                        settingsViewModel.updateBrightness(systemBrightness)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
            false,
            observer
        )

        // Also observe the float setting for more immediate updates on modern systems
        try {
            val floatUri = "content://settings/system/screen_brightness_float".toUri()
            context.contentResolver.registerContentObserver(floatUri, false, observer)
        } catch (_: Exception) {
            // Ignore if float setting is not observable
        }

        onDispose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    val playClick = {
        if (uiState.clickFeedbackEnabled) {
            soundManager.playClick(uiState.clickVolume)
        }
    }
    
    val playError = {
        if (uiState.clickFeedbackEnabled) {
            soundManager.playError(uiState.clickVolume)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    val density = LocalDensity.current
    val customDensity = Density(
        density = density.density,
        fontScale = density.fontScale * uiState.fontSize
    )

    CompositionLocalProvider(LocalDensity provides customDensity) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState != Screen.Launcher && initialState == Screen.Launcher) {
                    // Moving from Launcher to a sub-screen (forward)
                    slideInHorizontally(animationSpec = tween(400)) { it } + fadeIn(tween(400)) togetherWith
                            slideOutHorizontally(animationSpec = tween(400)) { -it } + fadeOut(tween(400))
                } else {
                    // Moving back to Launcher or other transitions (backward)
                    slideInHorizontally(animationSpec = tween(400)) { -it } + fadeIn(tween(400)) togetherWith
                            slideOutHorizontally(animationSpec = tween(400)) { it } + fadeOut(tween(400))
                }
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                Screen.Launcher -> LauncherScreen(
                    onSettingsClick = {
                        playClick()
                        currentScreen = Screen.Settings
                    },
                    onPhoneClick = {
                        playClick()
                        currentScreen = Screen.Phone
                    },
                    onNotesClick = {
                        playClick()
                        currentScreen = Screen.Notes
                    },
                    onGalleryClick = {
                        playClick()
                        currentScreen = Screen.Gallery
                    },
                    onAppClick = { playClick() },
                    primaryColor = primaryColor,
                    visualProfile = uiState.visualProfile
                )

                Screen.Settings -> SettingsScreen(
                    onBack = {
                        playClick()
                        currentScreen = Screen.Launcher
                    },
                    onButtonClick = { playClick() },
                    onError = { playError() },
                    onColorChanged = { newColor ->
                        settingsViewModel.updatePrimaryColor(newColor.toArgb().toLong())
                    },
                    primaryColor = primaryColor,
                    viewModel = settingsViewModel,
                    visualProfile = uiState.visualProfile
                )

                Screen.Phone -> PhoneScreen(
                    onBack = {
                        playClick()
                        currentScreen = Screen.Launcher
                    },
                    onButtonClick = { playClick() },
                    primaryColor = primaryColor,
                    viewModel = phoneViewModel,
                    visualProfile = uiState.visualProfile
                )

                Screen.Gallery -> GalleryScreen(
                    onBack = {
                        playClick()
                        currentScreen = Screen.Launcher
                    },
                    onButtonClick = { playClick() },
                    primaryColor = primaryColor,
                    viewModel = galleryViewModel,
                    visualProfile = uiState.visualProfile
                )

                Screen.Notes -> NotesScreen(
                    onBack = {
                        playClick()
                        currentScreen = Screen.Launcher
                    },
                    onButtonClick = { playClick() },
                    primaryColor = primaryColor,
                    visualProfile = uiState.visualProfile,
                    viewModel = notesViewModel
                )

                Screen.SetupWizard -> SetupWizardScreen(
                    onFinish = {
                        playClick()
                        settingsViewModel.completeFirstBoot()
                        currentScreen = Screen.Launcher
                    },
                    onButtonClick = { playClick() },
                    onError = { playError() },
                    primaryColor = primaryColor,
                    viewModel = settingsViewModel,
                    appRepository = appRepository
                )
            }
        }
    }
}
