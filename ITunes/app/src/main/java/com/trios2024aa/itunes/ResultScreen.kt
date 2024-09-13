package com.trios2024aa.itunes

import android.media.MediaPlayer
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryItem(
    repository: ITunesRepository = ITunesRepository(itunesService, RssFeedService.instance),
    viewModel: ITunesViewModel = viewModel(factory = ITunesViewModelFactory(repository))
) {
    val items by viewModel.items
    val searched by viewModel.searched
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var visibleEpisodeIndex by remember { mutableStateOf<Int?>(null) }
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) } // Set SearchBar to inactive by default
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }
    var visibleItemIndex by remember { mutableStateOf<Int?>(null) }

    // Ensure MediaPlayer is properly disposed when the Composable is destroyed
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    BackHandler(enabled = visibleItemIndex != null) {
        // Reset visibleItemIndex to make all items visible again
        visibleItemIndex = null
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar(
            query = query,
            onQueryChange = { query = it }, // Update the query as the user types
            onSearch = {
                // Trigger search when user submits query
                if (query.isNotBlank()) {
                    viewModel.searchTunes(query.trim()) // Perform the search using the ViewModel
                }
                active = false // Close the SearchBar after the search is performed
            },
            active = active,
            onActiveChange = { isActive ->
                active = isActive // Manage the active state of the search bar
            },
            placeholder = { Text(text = "What are you looking for?", style = TextStyle(color = Color.Gray)) },
            trailingIcon = {
                // Optional: Search button (can also be triggered by pressing Enter)
                IconButton(onClick = {
                    if (query.isNotBlank()) {
                        viewModel.searchTunes(query.trim()) // Perform the search
                    }
                    active = false
                }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        {}

        if (isLoading) {
            // Show a loading indicator
            Text(text = "Loading...", modifier = Modifier.padding(16.dp))
        } else if (errorMessage != null) {
            // Show error message
            Text(text = errorMessage ?: "", color = Color.Red, modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                when {
                    !searched -> {
                        // Initial state before any search
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    items.isNotEmpty() -> {
                        items(items.size) { index ->
                            val item = items[index]

                            // If visibleItemIndex is not null, only show the row for that index
                            if (visibleItemIndex == null || visibleItemIndex == index) {
                                Column (
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
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

                                        val isCurrentTrackPlaying = currentlyPlayingUrl == item.previewUrl
                                        Icon(
                                            imageVector = if (isCurrentTrackPlaying) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                            contentDescription = if (isCurrentTrackPlaying) "Pause Preview" else "Play Preview",
                                            modifier = Modifier
                                                .width(30.dp)
                                                .height(30.dp)
                                                .padding(end = 8.dp)
                                                .clickable {
                                                    if (isCurrentTrackPlaying) {
                                                        mediaPlayer?.stop()
                                                        mediaPlayer?.reset()
                                                        currentlyPlayingUrl = null
                                                    } else {
                                                        val previewUrl = item.previewUrl
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

                                        Column(modifier = Modifier.padding(start = 8.dp),
                                            horizontalAlignment = Alignment.Start) {
                                            Text(
                                                text = item.artistName ?: "Unknown Artist",
                                                style = TextStyle(fontWeight = FontWeight.Bold),
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .width(150.dp) // Fixed width for the artist name
                                                    .fillMaxWidth(0.5f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = item.trackName ?: "Unknown Track",
                                                style = TextStyle(fontWeight = FontWeight.Light),
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .fillMaxWidth(0.5f), // Fixed width for the track name
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.kind ?: "Unknown",
                                                style = TextStyle(fontWeight = FontWeight.Light),
                                                modifier = Modifier
                                                    .padding(top = 8.dp)
                                                    .fillMaxWidth(0.5f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Show Info",
                                                modifier = Modifier
                                                    .width(30.dp)
                                                    .height(30.dp)
                                                    .padding(end = 8.dp)
                                                    .clickable {
                                                        // Set the visible item index to the tapped one
                                                        visibleItemIndex = index
                                                        // Trigger RSS feed fetching for the selected item
                                                        viewModel.fetchRssFeed(item.feedUrl)
                                                    }
                                            )
                                        }
                                    }

                                    // Show additional information if this item is the selected one
                                    if (visibleItemIndex == index) {
                                        // Fetch RSS feed when the item is selected

                                        val rssFeed by viewModel.rssFeed
                                        rssFeed?.let { feed ->
                                            // Display RSS feed details
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "Podcast Title: ${feed.feedTitle}",
                                                    style = TextStyle(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                )



                                                // Display episodes in a LazyColumn
                                                LazyColumn {
                                                    items(feed.episodes ?: emptyList()) { episode ->
                                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                            Text(
                                                                text = "Episode: ${episode.title ?: "Unknown"}",
                                                                style = TextStyle(fontWeight = FontWeight.Bold),
                                                                modifier = Modifier.padding(bottom = 4.dp)
                                                            )
                                                            Text(
                                                                text = "Published: ${episode.guid ?: "Unknown"}",
                                                                style = TextStyle(fontWeight = FontWeight.Light)
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        } ?: run {
                                            // Show loading indicator while the RSS feed is being fetched
                                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                        }
                                    }


                                }
                            }
                        }
                    }
                    else -> {
                        item {
                            Text(
                                text = "No results found",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


