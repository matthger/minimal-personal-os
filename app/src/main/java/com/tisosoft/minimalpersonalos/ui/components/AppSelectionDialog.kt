package com.tisosoft.minimalpersonalos.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tisosoft.minimalpersonalos.R
import com.tisosoft.minimalpersonalos.data.AppRepository
import com.tisosoft.minimalpersonalos.model.AppInfo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppSelectionDialog(
    appRepository: AppRepository,
    initialSelectedApps: List<AppInfo>,
    onDismiss: () -> Unit,
    onConfirm: (List<AppInfo>) -> Unit,
    onItemClick: () -> Unit,
    onError: () -> Unit,
    primaryColor: Color
) {
    var allApps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val installedApps = appRepository.getInstalledApps()
        allApps = installedApps.map { app: AppInfo ->
            val isSelected = initialSelectedApps.any { it.packageName == app.packageName }
            app.copy(isSelected = isSelected)
        }
        isLoading = false
    }

    val selectedCount = allApps.count { it.isSelected }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text(stringResource(R.string.select_apps_dialog_title, selectedCount)) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = primaryColor)
                } else {
                    val listState = rememberLazyListState()
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(allApps, key = { _, app -> app.packageName }) { _, app ->
                                val toggleSelection = {
                                    if (app.isSelected || selectedCount < 4) {
                                        onItemClick()
                                        allApps = allApps.map {
                                            if (it.packageName == app.packageName) it.copy(isSelected = !it.isSelected) else it
                                        }
                                    } else {
                                        onError()
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .combinedClickable(onClick = toggleSelection)
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    Checkbox(
                                        checked = app.isSelected,
                                        onCheckedChange = { toggleSelection() },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = primaryColor,
                                            checkmarkColor = Color.White,
                                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    )
                                    Text(
                                        text = app.name,
                                        modifier = Modifier.padding(start = 8.dp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        LazyVerticalScrollbar(listState = listState, totalItems = allApps.size)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(allApps.filter { it.isSelected }) },
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
