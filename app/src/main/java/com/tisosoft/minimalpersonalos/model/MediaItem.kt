package com.tisosoft.minimalpersonalos.model

import android.net.Uri

enum class MediaType {
    IMAGE, VIDEO
}

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val path: String,
    val displayName: String,
    val dateTaken: Long,
    val type: MediaType
)
