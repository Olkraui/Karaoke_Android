package com.example.baptiste_francois

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.example.baptiste_francois.recupererMusique.ApiService
import com.example.baptiste_francois.recupererMusique.Song
import com.example.baptiste_francois.ui.theme.Projet_androidTheme
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://gcpa-enssat-24-25.s3.eu-west-3.amazonaws.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        setContent {
            Projet_androidTheme {
                var songs by remember { mutableStateOf<List<Song>>(emptyList()) }

                LaunchedEffect(Unit) {
                    try {
                        songs = apiService.getSongs()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erreur lors de la récupération des données : ${e.message}")
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlayList(
                        songs = songs,
                        modifier = Modifier.padding(innerPadding)
                    ) { song ->
                        val audioUrl = song.path?.replace(".md", ".mp3")
                        // Lancer l'activité LyricsActivity
                        val intent = Intent(this@MainActivity, LyricsActivity::class.java).apply {
                            putExtra("LYRICS_URL", song.path)
                            putExtra("AUDIO_URL", audioUrl)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayList(songs: List<Song>, modifier: Modifier = Modifier, onSongClick: (Song) -> Unit) {
    val unlockedSongs = songs.filter { it.locked != true }

    LazyColumn(
        modifier = modifier.padding(16.dp)
    ) {
        items(unlockedSongs) { song ->
            SongButton(
                title = song.name,
                artist = song.artist,
                onClick = { onSongClick(song) }
            )
        }
    }
}

@Composable
fun SongButton(title: String, artist: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1C4E9)), // Violet clair
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = artist,
                color = Color.DarkGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
