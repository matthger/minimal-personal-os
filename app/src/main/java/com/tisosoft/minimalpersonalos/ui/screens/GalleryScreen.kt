package com.tisosoft.minimalpersonalos.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.tisosoft.minimalpersonalos.R
import com.tisosoft.minimalpersonalos.model.VisualProfile
import com.tisosoft.minimalpersonalos.viewmodel.GalleryViewModel
import androidx.core.net.toUri
import com.tisosoft.minimalpersonalos.model.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onBack: () -> Unit,
    onButtonClick: () -> Unit,
    primaryColor: Color,
    viewModel: GalleryViewModel,
    visualProfile: VisualProfile
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var imageToDelete by remember { mutableStateOf<com.tisosoft.minimalpersonalos.model.MediaItem?>(null) }
    var redirectedToSettings by remember { mutableStateOf(false) }

    val readPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val writePermission = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    } else {
        null
    }

    var hasReadPermission by remember {
        mutableStateOf(
            readPermissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        )
    }

    var hasWritePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else if (writePermission != null) {
                ContextCompat.checkSelfPermission(context, writePermission) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasReadPermission = readPermissions.all { results[it] == true }
        if (writePermission != null) {
            hasWritePermission = results[writePermission] == true
        }
        if (hasReadPermission) {
            viewModel.loadImages()
        }
    }

    LaunchedEffect(Unit) {
        if (hasReadPermission) {
            viewModel.loadImages()
        }
    }

    // Special check for MANAGE_EXTERNAL_STORAGE if on Android 11+ and not set
    val manageStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        hasWritePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else true
    }

    val galleryTitle = stringResource(R.string.gallery_button)
    val backButtonStr = stringResource(R.string.back)
    val permissionMessage = stringResource(R.string.phone_screen_permission_msg) // Reusing contact permission message for simplicity or add specific one
    val grantBtnLabel = stringResource(R.string.phone_screen_grant_btn)

    fun shareMedia(ctx: android.content.Context, item: com.tisosoft.minimalpersonalos.model.MediaItem) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, item.uri)
            type = if (item.type == MediaType.VIDEO) "video/*" else "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(
            shareIntent,
            ctx.getString(R.string.share_button)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(chooser)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(galleryTitle) },
                navigationIcon = {
                    if (visualProfile == VisualProfile.MINIMALISTIC) {
                        IconButton(onClick = {
                            onButtonClick()
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backButtonStr)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!hasReadPermission) {
                PermissionDeniedView(
                    message = permissionMessage,
                    buttonLabel = grantBtnLabel,
                    onGrantClick = { 
                        onButtonClick()
                        val list = readPermissions.toMutableList()
                        if (writePermission != null) list.add(writePermission)
                        permissionsLauncher.launch(list.toTypedArray())
                    }
                )
            } else if (!hasWritePermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Prompt specifically for MANAGE_EXTERNAL_STORAGE if we have READ but not WRITE on 11+
                PermissionDeniedView(
                    message = stringResource(R.string.instant_delete_desc),
                    buttonLabel = stringResource(R.string.instant_delete),
                    onGrantClick = {
                        onButtonClick()
                        val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                        manageStorageLauncher.launch(intent)
                    }
                )
            } else if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (state.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "Error loading images", color = Color.Red)
                }
            } else if (state.images.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.gallery_screen_no_images))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.images) { image ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    onButtonClick()
                                    viewModel.selectImage(image)
                                }
                        ) {
                            AsyncImage(
                                model = if (image.type == MediaType.VIDEO) {
                                    try {
                                        context.contentResolver.loadThumbnail(image.uri, android.util.Size(512, 512), null)
                                    } catch (_: Exception) {
                                        image.uri
                                    }
                                } else {
                                    image.uri
                                },
                                contentDescription = image.displayName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            if (image.type == MediaType.VIDEO) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(48.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Video",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Simple Mode Back Button
            if (visualProfile == VisualProfile.SIMPLE) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
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
        }
    }

    // Full Screen Viewer
    state.selectedImage?.let { selected ->
        val initialIndex = state.images.indexOf(selected).coerceAtLeast(0)
        val pagerState = rememberPagerState(initialPage = initialIndex) {
            state.images.size
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    pageSpacing = 16.dp
                ) { pageIndex ->
                    val image = state.images[pageIndex]
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = if (image.type == MediaType.VIDEO) {
                                try {
                                    context.contentResolver.loadThumbnail(image.uri, android.util.Size(1024, 1024), null)
                                } catch (_: Exception) {
                                    image.uri
                                }
                            } else {
                                image.uri
                            },
                            contentDescription = image.displayName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        if (image.type == MediaType.VIDEO) {
                            IconButton(
                                onClick = {
                                    onButtonClick()
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(image.uri, "video/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(72.dp)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }

                // Viewer controls
                val currentImage = state.images[pagerState.currentPage]
                if (visualProfile == VisualProfile.MINIMALISTIC) {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = { 
                                onButtonClick()
                                viewModel.selectImage(null) 
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = backButtonStr,
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                onButtonClick()
                                shareMedia(context, currentImage)
                            }) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = stringResource(R.string.share_button),
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = {
                                onButtonClick()
                                imageToDelete = currentImage
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_button),
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent
                        )
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onButtonClick()
                                shareMedia(context, currentImage)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.share_button),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                onButtonClick()
                                imageToDelete = currentImage
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.delete_button),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    ) {
                        Button(
                            onClick = { 
                                onButtonClick()
                                viewModel.selectImage(null) 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .height(56.dp)
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
        }
    }

    // Delete Confirmation Dialog
    if (imageToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                onButtonClick()
                imageToDelete = null 
            },
            title = { Text(stringResource(R.string.delete_button)) },
            text = {
                val confirmMessage = if (imageToDelete?.type == MediaType.VIDEO) {
                    stringResource(R.string.delete_video_confirm)
                } else {
                    stringResource(R.string.delete_image_confirm)
                }
                Text(confirmMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onButtonClick()
                        imageToDelete?.let { image ->
                            if (hasWritePermission) {
                                viewModel.deleteImage(image)
                            } else if (!redirectedToSettings) {
                                redirectedToSettings = true
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        val fallbackIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = "package:${context.packageName}".toUri()
                                        }
                                        context.startActivity(fallbackIntent)
                                    }
                                } else {
                                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = "package:${context.packageName}".toUri()
                                    }
                                    context.startActivity(intent)
                                }
                            } else {
                                redirectedToSettings = false
                                viewModel.deleteImage(image)
                            }
                        }
                        imageToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        onButtonClick()
                        imageToDelete = null 
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun PermissionDeniedView(
    message: String,
    buttonLabel: String,
    onGrantClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = message,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onGrantClick,
                modifier = Modifier.height(48.dp)
            ) {
                Text(buttonLabel, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
