package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatusSerializer
import kotlinx.serialization.Serializable

@Serializable
data class UpdateLibraryStatusRequest(
    @Serializable(with = LibraryStatusSerializer::class)
    val status: LibraryStatus
)