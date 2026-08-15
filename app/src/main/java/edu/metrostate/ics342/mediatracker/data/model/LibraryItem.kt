package edu.metrostate.ics342.mediatracker.data.model

import androidx.annotation.StringRes
import edu.metrostate.ics342.mediatracker.R
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
@Serializable
data class LibraryItem(
    val userId: String,
    val mediaId: Int,
    @Serializable(with = LibraryStatusSerializer::class)
    val status: LibraryStatus,
    val addedAt: String,
    val updatedAt: String,
    val media: Media? = null
)

enum class LibraryStatus(@param:StringRes val labelRes: Int) {
    WANT_TO(R.string.status_want_to),
    IN_PROGRESS(R.string.status_in_progress),
    FINISHED(R.string.status_finished);

    fun toApiString(): String = when (this) {
        WANT_TO     -> "want_to"
        IN_PROGRESS -> "in_progress"
        FINISHED    -> "finished"
    }

    companion object {
        fun fromString(value: String): LibraryStatus = when (value) {
            "want_to"     -> WANT_TO
            "in_progress" -> IN_PROGRESS
            "finished"    -> FINISHED
            else          -> WANT_TO
        }
    }
}
object LibraryStatusSerializer : KSerializer<LibraryStatus> {
    override val descriptor = PrimitiveSerialDescriptor("LibraryStatus", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LibraryStatus) =
        encoder.encodeString(value.toApiString())
    override fun deserialize(decoder: Decoder) =
        LibraryStatus.fromString(decoder.decodeString())
}
