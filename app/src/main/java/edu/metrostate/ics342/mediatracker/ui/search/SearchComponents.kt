package edu.metrostate.ics342.mediatracker.ui.search


import androidx.compose.foundation.background
import edu.metrostate.ics342.mediatracker.data.model.MediaType
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.creatorCredit
import edu.metrostate.ics342.mediatracker.theme.MovieContainer
import edu.metrostate.ics342.mediatracker.theme.OnMovieContainer

@Composable
fun MediaTypeFilterChips(
    selectedType: String,
    onTypeSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = listOf(
        "" to R.string.filter_all,
        "book" to R.string.filter_books,
        "movie" to R.string.filter_movies,
        "show" to R.string.filter_shows,
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { (type, labelRes) ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelect(type) },
                label = { Text(stringResource(labelRes)) }
            )
        }
    }
}

@Composable
fun MediaResultCard(
    media: Media,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val containerColor = when (media.mediaType) {
                MediaType.BOOK              -> MaterialTheme.colorScheme.primaryContainer
                MediaType.MOVIE             -> MovieContainer
                MediaType.SHOW, MediaType.UNKNOWN -> MaterialTheme.colorScheme.secondaryContainer
            }
            val iconTint = when (media.mediaType) {
                MediaType.BOOK              -> MaterialTheme.colorScheme.onPrimaryContainer
                MediaType.MOVIE             -> OnMovieContainer
                MediaType.SHOW, MediaType.UNKNOWN -> MaterialTheme.colorScheme.secondary
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(when (media.mediaType) {

                        MediaType.BOOK              -> R.drawable.menu_book_24px
                        MediaType.MOVIE             -> R.drawable.movie_24px
                        MediaType.SHOW, MediaType.UNKNOWN -> R.drawable.tv_24px

                    }),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = iconTint
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = media.creatorCredit(LocalContext.current),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildString {
                        append("★ ${"%.1f".format(media.averageRating)}")
                        append(" · ${media.mediaType.displayName}")
                        media.publishedYear?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}