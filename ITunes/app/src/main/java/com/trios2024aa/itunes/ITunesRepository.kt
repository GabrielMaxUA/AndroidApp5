package com.trios2024aa.itunes

import retrofit2.Response

class ITunesRepository(private val apiService: ApiService, private val rssFeedService: RssFeedService) {

    // Function to perform a search and return the response
    suspend fun searchTunes(query: String): Response<ITunesResponse> {
        return apiService.search(term = query)
    }

    // Function to get the RSS feed
    suspend fun getRssFeed(feedUrl: String): RssFeedResponse? {
        return rssFeedService.getFeed(feedUrl)
    }

    fun mapTuneItemToRssFeedResponse(tuneItem: TuneItem): RssFeedResponse {
        return RssFeedResponse(
            subscribed = false,
            feedTitle = tuneItem.trackName,
            feedUrl = tuneItem.feedUrl,
            feedDesc = "Description of ${tuneItem.trackName}",
            imageUrl = tuneItem.artworkUrl60,
            episodes = listOf() // You can later populate with actual episodes
        )
    }
}
