package com.example.projet_android.recupererMusique

data class Song(
    val name: String,
    val artist: String,
    val path: String? = null,
    val locked: Boolean? = null
)
