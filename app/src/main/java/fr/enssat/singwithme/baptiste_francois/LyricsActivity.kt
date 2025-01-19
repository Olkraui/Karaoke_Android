package com.example.baptiste_francois

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baptiste_francois.recupererMusique.ApiService
import com.example.baptiste_francois.ui.theme.Projet_androidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request

class LyricsActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lyricsUrl = intent.getStringExtra("LYRICS_URL") ?: ""
        val audioUrl = intent.getStringExtra("AUDIO_URL") ?: ""

        val audioUrl2 = "https://gcpa-enssat-24-25.s3.eu-west-3.amazonaws.com/" + audioUrl

        val retrofit = Retrofit.Builder()
            .baseUrl("https://gcpa-enssat-24-25.s3.eu-west-3.amazonaws.com/")
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        setContent {
            Projet_androidTheme {
                var lyricLines by remember { mutableStateOf<List<LyricLine>?>(null) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var isAudioReady by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            apiService.getLyrics(lyricsUrl)
                        }
                        val rawLyrics = response.string()
                        Log.d("LyricsActivity", "Raw Lyrics : $rawLyrics")
                        lyricLines = parseLyrics(rawLyrics)

                        playAudio(audioUrl2){
                            isAudioReady = true
                        }

                    } catch (e: Exception) {
                        Log.e("LyricsActivity", "Erreur : ${e.message}")
                        errorMessage = "Erreur de connexion : ${e.message}"
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        when {
                            lyricLines != null && isAudioReady -> {
                                LyricsScreen(lyrics = lyricLines!!)
                            }
                            errorMessage != null -> {
                                Text(
                                    text = errorMessage ?: "Erreur inconnue",
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            else -> {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun playAudio(audioUrl: String, onAudioStart: () -> Unit) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioUrl)
            setOnPreparedListener {
                start()
                onAudioStart()
            }
            setOnErrorListener { _, what, extra ->
                Log.e("LyricsActivity", "Erreur MediaPlayer : what=$what extra=$extra")
                false
            }
            prepareAsync()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private suspend fun fetchLyrics(url: String): String {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Erreur HTTP : ${response.code()}")
                }
                response.body()?.string() ?: throw Exception("Réponse vide")
            }
        }
    }

    private fun parseLyrics(rawLyrics: String): List<LyricsActivity.LyricLine> {
        val lines = mutableListOf<LyricsActivity.LyricLine>()
        val regexDebut = Regex("""\{ (\d+):(\d+) \}(.*)""")
        val matchesDebut = regexDebut.findAll(rawLyrics).toList()
        var endTime = 0
        for (i in matchesDebut.indices) {
            val matchD = matchesDebut[i]
            val startMinutes = matchD.groupValues[1].toInt()
            val startSeconds = matchD.groupValues[2].toInt()
            val startTime = startMinutes * 60 + startSeconds

            var textWithTime = matchD.groupValues[3].trim()

            val regexFin = Regex("""(.*)\{ (\d+):(\d+) \}\s*$""")
            val matchesFin = regexFin.findAll(textWithTime).toList()
            if (matchesFin.size == 1) {
                for (j in matchesFin.indices) {
                    val matchF = matchesFin[j]
                    val endMinutes = matchF.groupValues[2].toInt()
                    val endSeconds = matchF.groupValues[3].toInt()
                    endTime = endMinutes * 60 + endSeconds

                    textWithTime = matchF.groupValues[1].trim()

                }
            } else {
                if (i + 1 < matchesDebut.size) {
                    val nextMatch = matchesDebut[i + 1]
                    val nextStartMinutes = nextMatch.groupValues[1].toInt()
                    val nextStartSeconds = nextMatch.groupValues[2].toInt()
                    endTime = nextStartMinutes * 60 + nextStartSeconds
                } else {
                    endTime = startTime + 2 // Durée par défaut pour la dernière ligne
                }
            }

            val regex = Regex("""\{ \d+:\d+ \}""")
            val text = textWithTime.replace(regex, "").trim()

            lines.add(
                LyricsActivity.LyricLine(
                    text = text,
                    textWithTime = textWithTime,
                    startTime = startTime,
                    endTime = endTime
                )
            )
        }
    return lines
}

    data class LyricLine(
        val text: String,
        val textWithTime: String,
        val startTime: Int,
        val endTime: Int
    )

    @Composable
    fun LyricsScreen(lyrics: List<LyricsActivity.LyricLine>) {
        var currentLineIndex by remember { mutableStateOf(-1) } // -1 signifie aucune ligne active
        var elapsedTime by remember { mutableStateOf(0L) } // Temps écoulé en millisecondes
        var lineProgress by remember { mutableStateOf(0f) } // Progression dans la ligne actuelle (0f à 1f)

        // Timer global pour calculer le temps écoulé
        LaunchedEffect(Unit) {
            val startTime = System.currentTimeMillis()
            while (true) {
                elapsedTime = System.currentTimeMillis() - startTime
                delay(10) // Mettre à jour toutes les 100 ms
            }
        }

        // Met à jour l'index de la ligne courante et la progression
        LaunchedEffect(elapsedTime) {
            val newIndex = lyrics.indexOfFirst { line ->
                elapsedTime in (line.startTime * 1000)..(line.endTime * 1000)
            }

            if (newIndex != currentLineIndex) {
                currentLineIndex = newIndex
                lineProgress = 0f // Réinitialise la progression pour la nouvelle ligne
            }

            // Calcule la progression de la ligne actuelle
            if (currentLineIndex != -1) {
                val currentLine = lyrics[currentLineIndex]
                val lineDuration = (currentLine.endTime - currentLine.startTime) * 1000
                lineProgress = ((elapsedTime - currentLine.startTime * 1000).toFloat() / lineDuration).coerceIn(0f, 1f)
            }
        }

        // Affiche les paroles avec une barre de progression
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (currentLineIndex != -1) {
                val currentLine = lyrics[currentLineIndex]
                val text = currentLine.text

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    // Texte avec surlignage dynamique
                    Text(
                        text = buildAnnotatedString {
                            val highlightedLength = (text.length * lineProgress).toInt()
                            append(text.substring(0, highlightedLength))
                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                append(text.substring(highlightedLength))
                            }
                        },
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Affiche un espace vide si aucune phrase n'est active
                Text(
                    text = "",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }}