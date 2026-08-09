package edu.metrostate.ics342.mediatracker.data.model


import androidx.annotation.DrawableRes
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MediaTypeSerializer::class)
enum class MediaType(val apiString: String) {
    BOOK("book"),
    MOVIE("movie"),
    SHOW("show"),
    UNKNOWN("unknown");

    val displayName: String get() = apiString.replaceFirstChar { it.uppercase() }
}

object MediaTypeSerializer : KSerializer<MediaType> {
    override val descriptor = PrimitiveSerialDescriptor("MediaType", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: MediaType) = encoder.encodeString(value.apiString)
    override fun deserialize(decoder: Decoder): MediaType {
        val raw = decoder.decodeString()
        return MediaType.entries.find { it.apiString == raw } ?: MediaType.UNKNOWN
    }
}

@DrawableRes
fun MediaType?.toIconRes(): Int = when (this) {
    MediaType.BOOK  -> edu.metrostate.ics342.mediatracker.R.drawable.menu_book_24px
    MediaType.MOVIE -> edu.metrostate.ics342.mediatracker.R.drawable.movie_24px
    else            -> edu.metrostate.ics342.mediatracker.R.drawable.tv_24px
}