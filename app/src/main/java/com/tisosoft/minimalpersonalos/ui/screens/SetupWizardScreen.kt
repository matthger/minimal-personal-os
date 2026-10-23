package com.tisosoft.minimalpersonalos.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tisosoft.minimalpersonalos.R
import com.tisosoft.minimalpersonalos.data.AppRepository
import com.tisosoft.minimalpersonalos.model.AppInfo
import com.tisosoft.minimalpersonalos.model.VisualProfile
import com.tisosoft.minimalpersonalos.ui.components.AppSelectionDialog
import com.tisosoft.minimalpersonalos.ui.components.availableColors
import com.tisosoft.minimalpersonalos.viewmodel.SettingsViewModel
import androidx.compose.ui.graphics.toArgb

@Composable
fun SetupWizardScreen(
    onFinish: () -> Unit,
    onButtonClick: () -> Unit,
    onError: () -> Unit,
    primaryColor: Color,
    viewModel: SettingsViewModel,
    appRepository: AppRepository
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()

    val totalPages = 5

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            WizardBottomBar(
                currentPage = currentPage,
                totalPages = totalPages,
                onBack = {
                    onButtonClick()
                    if (currentPage > 0) currentPage--
                },
                onNext = {
                    onButtonClick()
                    if (currentPage < totalPages - 1) {
                        currentPage++
                    } else {
                        onFinish()
                    }
                },
                primaryColor = primaryColor
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "WizardPageTransition"
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> AppCountPage(
                        appRepository = appRepository,
                        onButtonClick = onButtonClick,
                        onError = onError,
                        primaryColor = primaryColor
                    )
                    2 -> VisualProfilePage(
                        currentProfile = uiState.visualProfile,
                        onProfileSelected = {
                            onButtonClick()
                            viewModel.updateVisualProfile(it)
                        },
                        primaryColor = primaryColor
                    )
                    3 -> PrimaryColorPage(
                        currentColor = primaryColor,
                        onColorSelected = {
                            onButtonClick()
                            viewModel.updatePrimaryColor(it.toArgb().toLong())
                        },
                        primaryColor = primaryColor
                    )
                    4 -> AllSetPage()
                }
            }
        }
    }
}

@Composable
fun WizardBottomBar(
    currentPage: Int,
    totalPages: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentPage > 0) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.height(56.dp)
            ) {
                Text(
                    stringResource(R.string.back_button),
                    color = primaryColor,
                    fontSize = 18.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.width(64.dp))
        }

        // Page Indicator
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(totalPages) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (index == currentPage) primaryColor else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
            }
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            modifier = Modifier.height(56.dp)
        ) {
            Text(
                if (currentPage == totalPages - 1) stringResource(R.string.wizard_finish) else stringResource(R.string.next_button),
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun WelcomePage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.wizard_welcome_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.wizard_welcome_text),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            lineHeight = 26.sp
        )
    }
}

@Composable
fun AppCountPage(
    appRepository: AppRepository,
    onButtonClick: () -> Unit,
    onError: () -> Unit,
    primaryColor: Color
) {
    var showAppSelection by remember { mutableStateOf(false) }
    var selectedApps by remember { mutableStateOf(emptyList<AppInfo>()) }

    LaunchedEffect(Unit) {
        selectedApps = appRepository.loadSelectedApps()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.wizard_app_count_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.wizard_app_count_text),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onButtonClick()
                    showAppSelection = true
                },
            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, primaryColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.wizard_apps_selected, selectedApps.size),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor
                )
                if (selectedApps.isNotEmpty()) {
                    Text(
                        text = selectedApps.joinToString { it.name },
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.wizard_app_count_science),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
                lineHeight = 20.sp
            )
        }
    }

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
}

@Composable
fun VisualProfilePage(
    currentProfile: VisualProfile,
    onProfileSelected: (VisualProfile) -> Unit,
    primaryColor: Color
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.wizard_visual_profile_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.wizard_visual_profile_text),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))

        ProfileCard(
            title = stringResource(R.string.wizard_profile_minimal_title),
            description = stringResource(R.string.wizard_profile_minimal_desc),
            isSelected = currentProfile == VisualProfile.MINIMALISTIC,
            onClick = { onProfileSelected(VisualProfile.MINIMALISTIC) },
            primaryColor = primaryColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        ProfileCard(
            title = stringResource(R.string.wizard_profile_simple_title),
            description = stringResource(R.string.wizard_profile_simple_desc),
            isSelected = currentProfile == VisualProfile.SIMPLE,
            onClick = { onProfileSelected(VisualProfile.SIMPLE) },
            primaryColor = primaryColor
        )
    }
}

@Composable
fun ProfileCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) primaryColor else Color.Gray.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                )
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
fun PrimaryColorPage(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    primaryColor: Color
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.wizard_primary_color_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.wizard_primary_color_text),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))

        availableColors.forEach { option ->
            ColorOptionCard(
                name = stringResource(option.nameResId),
                color = option.color,
                isSelected = (currentColor.toArgb() == option.color.toArgb()),
                onClick = { onColorSelected(option.color) },
                primaryColor = primaryColor
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ColorOptionCard(
    name: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) primaryColor else Color.Gray.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AllSetPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.wizard_all_set_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.wizard_all_set_text),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}
