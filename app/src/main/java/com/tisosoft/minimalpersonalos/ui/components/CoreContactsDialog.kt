package com.tisosoft.minimalpersonalos.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.tisosoft.minimalpersonalos.R
import com.tisosoft.minimalpersonalos.data.Contact
import com.tisosoft.minimalpersonalos.data.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoreContactsDialog(
    contactRepository: ContactRepository,
    initialSelectedIds: List<Long>,
    onDismiss: () -> Unit,
    onConfirm: (List<Long>) -> Unit,
    onItemClick: () -> Unit,
    onError: () -> Unit,
    primaryColor: Color,
    context: android.content.Context
) {
    var allContacts by remember { mutableStateOf(emptyList<Contact>()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedContactIds by remember { mutableStateOf(initialSelectedIds.toSet()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                try {
                    val contacts = contactRepository.getAllContactsFromProvider()
                    allContacts = contacts.sortedBy { it.displayName }
                } catch (e: Exception) {
                    onError()
                }
            } else {
                onError()
            }
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.core_contacts),
                fontSize = 18.sp
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.core_contacts_desc_select),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = primaryColor)
                    } else if (allContacts.isEmpty()) {
                        Text(
                            stringResource(R.string.phone_screen_no_contacts),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val listState = rememberLazyListState()

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(
                                allContacts,
                                key = { _, contact -> contact.id }
                            ) { _, contact ->
                                val isSelected = selectedContactIds.contains(contact.id)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                onItemClick()
                                                selectedContactIds = if (isSelected) {
                                                    selectedContactIds - contact.id
                                                } else {
                                                    selectedContactIds + contact.id
                                                }
                                            }
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        modifier = Modifier.size(24.dp),
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = primaryColor
                                        )
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 12.dp)
                                    ) {
                                        Text(
                                            contact.displayName,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            contact.phoneNumber,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        LazyVerticalScrollbar(listState = listState, totalItems = allContacts.size)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onItemClick()
                    onConfirm(selectedContactIds.toList())
                },
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(stringResource(R.string.confirm))
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
