package com.tisosoft.minimalpersonalos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import com.tisosoft.minimalpersonalos.R
import com.tisosoft.minimalpersonalos.data.Contact
import com.tisosoft.minimalpersonalos.model.VisualProfile
import com.tisosoft.minimalpersonalos.ui.components.*
import com.tisosoft.minimalpersonalos.viewmodel.PhoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneScreen(
    onBack: () -> Unit,
    onButtonClick: () -> Unit,
    primaryColor: Color,
    viewModel: PhoneViewModel,
    visualProfile: VisualProfile
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CALL_PHONE
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showAddContactDialog by remember { mutableStateOf(false) }
    var selectedContactForAction by remember { mutableStateOf<Contact?>(null) }
    var showEditContactDialog by remember { mutableStateOf<Contact?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<Contact?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.READ_CONTACTS] == true &&
                permissions[Manifest.permission.WRITE_CONTACTS] == true &&
                permissions[Manifest.permission.CALL_PHONE] == true
        hasPermission = isGranted
        if (isGranted) {
            viewModel.loadContacts()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.loadContacts()
        }
    }

    // Preload all strings to avoid Composable context errors in lambdas
    val phoneTitle = stringResource(R.string.phone_screen_title)
    val favoritesLabel = stringResource(R.string.phone_screen_favorites)
    val allContactsLabel = stringResource(R.string.phone_screen_all_contacts)
    val noContactsMessage = stringResource(R.string.phone_screen_no_contacts)
    val loadingMessage = stringResource(R.string.phone_screen_loading)
    val permissionMessage = stringResource(R.string.phone_screen_permission_msg)
    val grantBtnLabel = stringResource(R.string.phone_screen_grant_btn)
    val loadErrorLabel = stringResource(R.string.phone_screen_load_error)
    val backButtonStr = stringResource(R.string.back)
    val addContactButtonStr = stringResource(R.string.add_contact_button)
    val successMsg = stringResource(R.string.add_contact_success)
    val updateSuccessMsg = stringResource(R.string.contact_update_success)
    val deleteSuccessMsg = stringResource(R.string.contact_delete_success)
    val errorMsg = stringResource(R.string.add_contact_error)
    val tabContacts = stringResource(R.string.tab_contacts)
    val tabDialer = stringResource(R.string.tab_dialer)

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(phoneTitle) },
                    navigationIcon = {
                        if (visualProfile == VisualProfile.MINIMALISTIC) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backButtonStr)
                            }
                        }
                    }
                )
                SecondaryTabRow(
                    selectedTabIndex = state.activeTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = primaryColor,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(state.activeTab),
                            color = primaryColor
                        )
                    }
                ) {
                    Tab(
                        selected = state.activeTab == 0,
                        onClick = { viewModel.updateTab(0) },
                        text = {
                            Text(
                                tabContacts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = state.activeTab == 1,
                        onClick = { viewModel.updateTab(1) },
                        text = { Text(tabDialer, fontSize = 16.sp, fontWeight = FontWeight.Medium) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (hasPermission && visualProfile == VisualProfile.MINIMALISTIC && state.activeTab == 0) {
                FloatingActionButton(
                    onClick = {
                        viewModel.playClickSound()
                        showAddContactDialog = true
                    },
                    containerColor = primaryColor,
                    modifier = Modifier
                        .padding(12.dp)
                        .height(64.dp)
                        .width(64.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_contact_title),
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!hasPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = permissionMessage,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.WRITE_CONTACTS,
                                        Manifest.permission.CALL_PHONE
                                    )
                                )
                            },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(grantBtnLabel, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(loadingMessage)
                }
            } else if (state.activeTab == 1) {
                // Dialer View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 88.dp)
                ) {
                    DialerDisplay(number = state.dialerNumber)
                    DialerKeypad(
                        onDigitClick = { viewModel.appendDialerDigit(it) },
                        onDeleteClick = { viewModel.removeLastDialerDigit() },
                        onCallClick = {
                            if (state.dialerNumber.isNotEmpty()) {
                                viewModel.initiateCall(context, state.dialerNumber)
                            }
                        },
                        primaryColor = primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else if (state.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.error ?: loadErrorLabel, color = Color.Red)
                }
            } else if (state.contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        noContactsMessage,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val favorites = state.contacts.filter { it.isFavorite }
                val others = state.contacts.filter { !it.isFavorite }
                val listState = rememberLazyListState()

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                    // Favorites Section
                    if (favorites.isNotEmpty()) {
                        item {
                            Text(
                                text = favoritesLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        items(favorites) { contact ->
                            ContactCard(
                                contact = contact,
                                onClick = {
                                    viewModel.playClickSound()
                                    selectedContactForAction = contact
                                },
                                modifier = Modifier.padding(vertical = 8.dp),
                                primaryColor = primaryColor
                            )
                        }

                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = DividerDefaults.Thickness,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }

                    // All Contacts Section
                    if (others.isNotEmpty()) {
                        item {
                            Text(
                                text = allContactsLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        items(others) { contact ->
                            ContactCard(
                                contact = contact,
                                onClick = {
                                    viewModel.playClickSound()
                                    selectedContactForAction = contact
                                },
                                modifier = Modifier.padding(vertical = 8.dp),
                                primaryColor = primaryColor
                            )
                        }
                    }

                        item {
                            // Bottom padding
                            Box(modifier = Modifier.padding(40.dp))
                        }
                    }

                    LazyVerticalScrollbar(listState = listState, totalItems = state.contacts.size)
                }
            }

            // Action Buttons
            if (visualProfile == VisualProfile.SIMPLE) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasPermission && state.activeTab == 0) {
                        // Add Contact Button
                        Button(
                            onClick = {
                                viewModel.playClickSound()
                                showAddContactDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                addContactButtonStr,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Back Button
                    Button(
                        onClick = {
                            onButtonClick()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(56.dp)
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

            if (showAddContactDialog) {
                AddContactDialog(
                    onDismiss = { showAddContactDialog = false },
                    onConfirm = { name, number ->
                        viewModel.addContact(name, number) { success ->
                            showAddContactDialog = false
                            if (success) {
                                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    primaryColor = primaryColor
                )
            }

            selectedContactForAction?.let { contact ->
                ContactActionDialog(
                    contact = contact,
                    onDismiss = { selectedContactForAction = null },
                    onCall = {
                        selectedContactForAction = null
                        viewModel.initiateCall(
                            context = context,
                            phoneNumber = contact.phoneNumber
                        )
                    },
                    onEdit = {
                        selectedContactForAction = null
                        showEditContactDialog = contact
                    },
                    onDelete = {
                        selectedContactForAction = null
                        showDeleteConfirmation = contact
                    },
                    primaryColor = primaryColor,
                    visualProfile = visualProfile
                )
            }

            showEditContactDialog?.let { contact ->
                AddContactDialog(
                    initialName = contact.displayName,
                    initialPhoneNumber = contact.phoneNumber,
                    onDismiss = { showEditContactDialog = null },
                    onConfirm = { name, number ->
                        viewModel.updateContact(contact.id, name, number) { success ->
                            showEditContactDialog = null
                            if (success) {
                                Toast.makeText(context, updateSuccessMsg, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    primaryColor = primaryColor
                )
            }

            showDeleteConfirmation?.let { contact ->
                DeleteConfirmationDialog(
                    onDismiss = { showDeleteConfirmation = null },
                    onConfirm = {
                        viewModel.deleteContact(contact.id) { success ->
                            showDeleteConfirmation = null
                            if (success) {
                                Toast.makeText(context, deleteSuccessMsg, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    primaryColor = primaryColor
                )
            }
        }
    }
}
