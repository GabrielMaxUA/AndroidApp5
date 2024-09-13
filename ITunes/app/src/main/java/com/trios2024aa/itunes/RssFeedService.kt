package com.trios2024aa.itunes

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Node

class RssFeedService private constructor() {

    private val retrofit: Retrofit

    init {
        // OkHttp client with logging for network requests
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Adjust the timeout as necessary
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()

        // Retrofit initialization
        retrofit = Retrofit.Builder()
            .baseUrl("https://your-rss-feed-url.com/") // You can change the base URL if needed
            .client(client)
            .build()
    }

    private val feedService: FeedService = retrofit.create(FeedService::class.java)

    /**
     * Fetch and parse the RSS feed from a provided XML URL.
     * Returns an RssFeedResponse object or null if there's an error.
     */
    suspend fun getFeed(xmlFileURL: String): RssFeedResponse? {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch the feed from the network
                val result = feedService.getFeed(xmlFileURL)
                if (result.isSuccessful) {
                    result.body()?.byteStream()?.use { inputStream ->
                        val dbFactory = DocumentBuilderFactory.newInstance()
                        val dBuilder = dbFactory.newDocumentBuilder()
                        val doc = dBuilder.parse(inputStream)

                        val rssFeed = RssFeedResponse(episodes = mutableListOf())
                        parseDocument(doc, rssFeed)
                        return@withContext rssFeed
                    }
                } else {
                    Log.e("RssFeedService", "Failed to fetch RSS feed: ${result.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("RssFeedService", "Exception occurred while fetching RSS feed: ${e.localizedMessage}", e)
            }
            return@withContext null
        }
    }

    /**
     * Recursively parse the XML document into an RssFeedResponse object.
     */
    private fun parseDocument(node: Node, rssFeedResponse: RssFeedResponse) {
        if (node.nodeType == Node.ELEMENT_NODE) {
            val nodeName = node.nodeName

            when (nodeName) {
                "title" -> rssFeedResponse.feedTitle = node.textContent
                "summary" -> rssFeedResponse.feedDesc = node.textContent
                "image" -> {
                    val imageUrlNode = node.attributes.getNamedItem("href")
                    rssFeedResponse.imageUrl = imageUrlNode?.textContent
                }
                "item" -> {
                    val episode = RssFeedResponse.EpisodeResponse()
                    val itemNodes = node.childNodes
                    for (i in 0 until itemNodes.length) {
                        val child = itemNodes.item(i)
                        when (child.nodeName) {
                            "title" -> episode.title = child.textContent
                            "guid" -> episode.guid = child.textContent
                            "description" -> episode.description = child.textContent
                            "enclosure" -> {
                                val mediaUrl = child.attributes.getNamedItem("url")
                                episode.mediaUrl = mediaUrl?.textContent
                            }
                            "itunes:duration" -> episode.duration = child.textContent
                        }
                    }
                    rssFeedResponse.episodes = rssFeedResponse.episodes + episode
                }
            }
        }

        // Recursively parse child nodes
        val nodeList = node.childNodes
        for (i in 0 until nodeList.length) {
            val childNode = nodeList.item(i)
            parseDocument(childNode, rssFeedResponse)
        }
    }

    companion object {
        val instance: RssFeedService by lazy { RssFeedService() }
    }
}

interface FeedService {
    @GET
    suspend fun getFeed(@Url xmlFileURL: String): retrofit2.Response<ResponseBody>
}
