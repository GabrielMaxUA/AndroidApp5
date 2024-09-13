package com.trios2024aa.itunes

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation

@Composable
fun ItemDetailScreen(viewModel: ITunesViewModel, itemId: String,  navController: NavHostController) {
    // Decode the itemId (feedUrl) before using it
    val decodedUrl = Uri.decode(itemId)
    val selectedItem = viewModel.items.value.find { it.feedUrl == decodedUrl
    }
    // Pass NavController to handle navigation

    // State to keep track of the currently playing episode
    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        onDispose {
            // Release the media player when the composable is disposed
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    selectedItem?.let { item ->
        viewModel.fetchRssFeed(item.feedUrl)

        val rssFeed by viewModel.rssFeed

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .padding(top = 10.dp))
            {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                IconButton(
                    onClick = { navController.popBackStack() } // Navigate back to ListScreen
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                val imageUrl = item.artworkUrl60 ?: "https://via.placeholder.com/60"
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .transformations(RoundedCornersTransformation(14f))
                        .build()
                )

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .width(70.dp)
                        .height(70.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "${item.trackName}",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 25.sp),
                    modifier = Modifier.padding(10.dp)
                )
            }

            rssFeed?.let { feed ->
                Text(
                    text = "${feed.feedTitle}",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    modifier = Modifier.padding(4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Episodes",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                        modifier = Modifier,
                        textAlign = TextAlign.Center
                    )
                }

                LazyColumn {
                    items(feed.episodes ?: emptyList()) { episode ->
                        var isPlaying by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Episode: ${episode.title ?: "Unknown"}",
                                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )

                                        // Use HtmlText to display the HTML-formatted episode description

//                                      episode.description ?.let { htmlDescription ->
//                                            HtmlText(htmlContent = htmlDescription)
//                                        } ?: Text(text = "Review: Unknown")
                                        
                                        Text(
                                            text = "Duration:${episode.duration ?: "Unknown"}",
                                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 8.sp),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }



                                val isCurrentTrackPlaying = currentlyPlayingUrl == episode.mediaUrl
                                Icon(
                                    imageVector = if (isCurrentTrackPlaying) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                    contentDescription = if (isCurrentTrackPlaying) "Pause Preview" else "Play Preview",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            if (isCurrentTrackPlaying) {
                                                mediaPlayer?.stop()
                                                mediaPlayer?.reset()
                                                currentlyPlayingUrl = null
                                            } else {
                                                val previewUrl = episode.mediaUrl
                                                if (!previewUrl.isNullOrEmpty()) {
                                                    try {
                                                        mediaPlayer?.stop()
                                                        mediaPlayer?.reset()
                                                        mediaPlayer = MediaPlayer().apply {
                                                            setDataSource(previewUrl)
                                                            prepare()
                                                            start()
                                                        }
                                                        currentlyPlayingUrl = previewUrl
                                                    } catch (e: Exception) {
                                                        Log.e(
                                                            "CategoryItem",
                                                            "Error playing audio: ${e.message}"
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            } ?: run {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }
    } ?: run {
        Text(
            text = "Item not found.",
            modifier = Modifier.padding(16.dp)
        )
    }
}


@Composable
fun HtmlText(htmlContent: String, modifier: Modifier = Modifier) {
    // Convert the HTML content to Spanned
    val spanned: Spanned =
        Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY)

    // Display the parsed text with formatting
    Text(text = spanned.toString(), modifier = modifier, fontSize = 12.sp)
}
