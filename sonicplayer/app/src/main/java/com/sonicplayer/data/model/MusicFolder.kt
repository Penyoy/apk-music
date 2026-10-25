package com.sonicplayer.data.model

/** A device folder that contains at least one scanned track. */
data class MusicFolder(
    val path: String,
    val name: String,
    val songCount: Int
)
