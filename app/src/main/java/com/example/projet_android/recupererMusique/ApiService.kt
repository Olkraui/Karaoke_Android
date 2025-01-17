package com.example.projet_android.recupererMusique

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET("playlist.json")
    suspend fun getSongs(): List<Song>
    @GET
    suspend fun getLyrics(@Url url: String): ResponseBody
}
