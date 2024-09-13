package com.trios2024aa.itunes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class TuneItem(
    val id: String,
    val kind: String,
    val artistName: String?,
    val trackName: String?,
    val artworkUrl60: String?,
    val primaryGenreName: String?,
    val previewUrl: String?,
    val feedUrl: String
) : Parcelable



data class ITunesResponse (
    val results: List<TuneItem>
)
