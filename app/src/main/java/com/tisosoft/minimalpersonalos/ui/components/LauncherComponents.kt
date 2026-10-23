package com.tisosoft.minimalpersonalos.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.tisosoft.minimalpersonalos.model.AppInfo
import com.tisosoft.minimalpersonalos.model.VisualProfile
import com.tisosoft.minimalpersonalos.utils.getCurrentTimeString
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HeaderSection(modifier: Modifier = Modifier) {
    val currentDate = remember {
        SimpleDateFormat("EEE, MMMM d", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CurrentTimeText()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = currentDate,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CurrentTimeText() {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(getCurrentTimeString(context)) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1000.milliseconds)
            currentTime = getCurrentTimeString(context)
        }
    }

    Text(
        text = currentTime,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListSection(
    apps: List<AppInfo>,
    onAppClick: (String) -> Unit,
    onAppLongClick: () -> Unit,
    visualProfile: VisualProfile,
    modifier: Modifier = Modifier
) {
    if (visualProfile == VisualProfile.SIMPLE) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(apps) { app ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .combinedClickable(
                            onClick = { onAppClick(app.packageName) },
                            onLongClick = onAppLongClick
                        )
                        .padding(8.dp)
                ) {
                    app.icon?.let { icon ->
                        Image(
                            bitmap = icon.toBitmap().asImageBitmap(),
                            contentDescription = app.name,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = app.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            apps.forEach { app ->
                Text(
                    text = app.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onClick = { onAppClick(app.packageName) },
                            onLongClick = onAppLongClick
                        )
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                )
            }
        }
    }
}
