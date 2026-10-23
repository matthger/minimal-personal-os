package com.tisosoft.minimalpersonalos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.tisosoft.minimalpersonalos.ui.components.HeaderSection
import com.tisosoft.minimalpersonalos.ui.components.AppListSection

@Composable
fun LauncherScreen(
    onSettingsClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onNotesClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onAppClick: () -> Unit,
    primaryColor: Color,
    visualProfile: VisualProfile
) {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }

    var selectedApps by remember { mutableStateOf(emptyList<AppInfo>()) }

    // Fetch initial selected apps
    LaunchedEffect(Unit) {
        selectedApps = repository.loadSelectedApps()
    }

    LauncherContent(
        selectedApps = selectedApps,
        onAppClick = { packageName ->
            onAppClick()
            repository.launchApp(packageName)
        },
        onSettingsClick = onSettingsClick,
        onPhoneClick = onPhoneClick,
        onNotesClick = onNotesClick,
        onGalleryClick = onGalleryClick,
        primaryColor = primaryColor,
        visualProfile = visualProfile
    )
}

@Composable
fun LauncherContent(
    selectedApps: List<AppInfo>,
    onAppClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onNotesClick: () -> Unit,
    onGalleryClick: () -> Unit,
    primaryColor: Color,
    visualProfile: VisualProfile,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            HeaderSection(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
            )

            AppListSection(
                apps = selectedApps,
                onAppClick = onAppClick,
                onAppLongClick = onSettingsClick, // Use settings on long click too for now
                visualProfile = visualProfile,
                modifier = Modifier.align(Alignment.Center)
            )

            if (visualProfile == VisualProfile.MINIMALISTIC) {
                // Minimalistic: Icons only at the bottom
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 30.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LauncherIconButton(
                        icon = Icons.Default.Phone,
                        onClick = onPhoneClick,
                        contentDescription = stringResource(R.string.phone_button),
                        primaryColor = primaryColor
                    )
                    LauncherIconButton(
                        icon = Icons.Default.Photo,
                        onClick = onGalleryClick,
                        contentDescription = stringResource(R.string.gallery_button),
                        primaryColor = primaryColor
                    )
                    LauncherIconButton(
                        icon = Icons.Default.Description,
                        onClick = onNotesClick,
                        contentDescription = stringResource(R.string.notes_button),
                        primaryColor = primaryColor
                    )
                    LauncherIconButton(
                        icon = Icons.Default.Settings,
                        onClick = onSettingsClick,
                        contentDescription = stringResource(R.string.settings_button),
                        primaryColor = primaryColor
                    )
                }
            } else {
                // Simple: Text Buttons in a structured layout
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LauncherTextButton(
                            text = stringResource(R.string.phone_button),
                            icon = Icons.Default.Phone,
                            onClick = onPhoneClick,
                            primaryColor = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                        LauncherTextButton(
                            text = stringResource(R.string.gallery_button),
                            icon = Icons.Default.Photo,
                            onClick = onGalleryClick,
                            primaryColor = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LauncherTextButton(
                            text = stringResource(R.string.notes_button),
                            icon = Icons.Default.Description,
                            onClick = onNotesClick,
                            primaryColor = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                        LauncherTextButton(
                            text = stringResource(R.string.settings_button),
                            icon = Icons.Default.Settings,
                            onClick = onSettingsClick,
                            primaryColor = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LauncherIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    primaryColor: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = primaryColor,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun LauncherTextButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = primaryColor,
            contentColor = Color.White
        ),
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLauncherScreen() {
    LauncherContent(
        selectedApps = listOf(
            AppInfo("com.example.app1", "Photos", null),
            AppInfo("com.example.app2", "Camera", null),
            AppInfo("com.example.app3", "Notes", null),
            AppInfo("com.example.app4", "Phone", null)
        ),
        onAppClick = {},
        onSettingsClick = {},
        onPhoneClick = {},
        onNotesClick = {},
        onGalleryClick = {},
        primaryColor = Color(0xFF6650a4),
        visualProfile = VisualProfile.MINIMALISTIC
    )
}
