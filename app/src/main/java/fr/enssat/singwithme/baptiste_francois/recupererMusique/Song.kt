package fr.enssat.singwithme.baptiste_francois.recupererMusique

data class Song(
    val name: String,
    val artist: String,
    val path: String? = null,
    val locked: Boolean? = null
)
