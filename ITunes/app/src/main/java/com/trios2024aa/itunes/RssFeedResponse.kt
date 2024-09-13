package com.trios2024aa.itunes



//data class RssFeedResponse(
//
//    var title: String = "",
//    var description: String = "",
//    var lastUpdated: String = "",
//    var episodes: MutableList<EpisodeResponse>? = null
//) {
//    data class EpisodeResponse(
//        var title: String? = null,
//        var pubDate: String? = null,
//        var enclosureUrl: String? = null // Add enclosureUrl field
//    )
//}

data class RssFeedResponse(
    var subscribed: Boolean = false,
    var feedTitle: String? = "",
    var feedUrl: String? = "",
    var feedDesc: String? = "",
    var imageUrl: String? = "",
    var episodes: List<EpisodeResponse>
){

data class EpisodeResponse (
    var guid: String? = "",
    var title: String? = "",
    var description: String? = "",
    var mediaUrl: String? = "",
    var duration: String? = ""

)
}
