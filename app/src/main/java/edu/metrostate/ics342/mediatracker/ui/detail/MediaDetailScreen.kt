package edu.metrostate.ics342.mediatracker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.MediaType
import edu.metrostate.ics342.mediatracker.data.model.Quote
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.model.creatorCredit
import edu.metrostate.ics342.mediatracker.theme.MovieContainer
import edu.metrostate.ics342.mediatracker.theme.OnMovieContainer
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    mediaId: Int,
    onNavigateBack: () -> Unit,
    onWriteReview: (Int) -> Unit,
    viewModel: MediaDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionError by viewModel.actionError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddQuoteDialog by remember { mutableStateOf(false) }
    var quoteBeingEdited by remember { mutableStateOf<Quote?>(null) }
    var quotePendingDelete by remember { mutableStateOf<Quote?>(null) }

    LaunchedEffect(mediaId) { viewModel.load(mediaId) }

    LaunchedEffect(actionError) {
        actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {  }) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.action_more_options)
                        )
                    }
                }
            )

            when (val state = uiState) {
                is MediaDetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is MediaDetailUiState.NotFound -> {
                    Box(
                        Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = stringResource(R.string.detail_not_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is MediaDetailUiState.Error -> {
                    Box(
                        Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text  = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.load(mediaId) }) {
                                Text(stringResource(R.string.detail_retry))
                            }
                        }
                    }
                }

                is MediaDetailUiState.Success -> {
                    SuccessContent(
                        state            = state,
                        onAddToLibrary   = { viewModel.addToLibrary() },
                        onToggleFavorite = { viewModel.toggleFavorite() },
                        onWriteReview    = onWriteReview,
                        onAddQuote       = { showAddQuoteDialog = true },
                        onEditQuote      = { quoteBeingEdited = it },
                        onDeleteQuote    = { quotePendingDelete = it }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showAddQuoteDialog) {
        QuoteFormDialog(
            initial   = null,
            onDismiss = { showAddQuoteDialog = false },
            onSave    = { quoteText, pageNumber, isPublic ->
                viewModel.addQuote(quoteText, pageNumber, isPublic)
                showAddQuoteDialog = false
            }
        )
    }

    quoteBeingEdited?.let { quote ->
        QuoteFormDialog(
            initial   = quote,
            onDismiss = { quoteBeingEdited = null },
            onSave    = { quoteText, pageNumber, isPublic ->
                viewModel.editQuote(quote.id, quoteText, pageNumber, isPublic)
                quoteBeingEdited = null
            }
        )
    }

    quotePendingDelete?.let { quote ->
        AlertDialog(
            onDismissRequest = { quotePendingDelete = null },
            title = { Text("Delete this quote?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteQuote(quote.id)
                    quotePendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { quotePendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun QuoteFormDialog(
    initial: Quote?,
    onDismiss: () -> Unit,
    onSave: (quoteText: String, pageNumber: Int?, isPublic: Boolean) -> Unit
) {
    var quoteText by remember { mutableStateOf(initial?.quoteText ?: "") }
    var pageNumberText by remember { mutableStateOf(initial?.pageNumber?.toString() ?: "") }
    var isPublic by remember { mutableStateOf(initial?.isPublic ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Save a quote" else "Edit quote") },
        text = {
            Column {
                OutlinedTextField(
                    value         = quoteText,
                    onValueChange = { if (it.length <= 500) quoteText = it },
                    label         = { Text("Quote") },
                    supportingText = { Text("${quoteText.length}/500") },
                    minLines      = 3,
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = pageNumberText,
                    onValueChange = { input -> if (input.all { it.isDigit() }) pageNumberText = input },
                    label         = { Text("Page number (optional)") },
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Make public", modifier = Modifier.weight(1f))
                    Switch(checked = isPublic, onCheckedChange = { isPublic = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = quoteText.isNotBlank(),
                onClick = { onSave(quoteText.trim(), pageNumberText.toIntOrNull(), isPublic) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SuccessContent(
    state: MediaDetailUiState.Success,
    onAddToLibrary: () -> Unit,
    onToggleFavorite: () -> Unit,
    onWriteReview: (Int) -> Unit,
    onAddQuote: () -> Unit,
    onEditQuote: (Quote) -> Unit,
    onDeleteQuote: (Quote) -> Unit
) {
    val detail = state.detail
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MediaCover(detail)
            Spacer(Modifier.height(14.dp))
            Text(text = detail.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(
                text  = detail.creatorCredit(LocalContext.current),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            RatingSummary(averageRating = detail.averageRating, ratingCount = detail.ratingCount)
        }

        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.libraryStatus != null) {
                FilledTonalButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text(stringResource(state.libraryStatus.labelRes))
                }
            } else {
                Button(onClick = onAddToLibrary, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.detail_add_want_to))
                }
            }
            if (state.isFavorited) {
                FilledTonalButton(onClick = onToggleFavorite, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.detail_saved))
                }
            } else {
                OutlinedButton(onClick = onToggleFavorite, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.detail_save))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (!detail.description.isNullOrBlank()) {
            SectionCaption(stringResource(R.string.detail_about))
            Spacer(Modifier.height(6.dp))
            Text(text = detail.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
        }

        StatGrid(detail)
        Spacer(Modifier.height(20.dp))


        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionCaption(text = "Quotes (${state.quotes.size})", modifier = Modifier.weight(1f))
            TextButton(onClick = onAddQuote) { Text("Add Quote") }
        }
        Spacer(Modifier.height(4.dp))

        if (state.quotes.isEmpty()) {
            Text(
                text     = "No quotes saved yet — add one above.",
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            state.quotes.forEach { quote ->
                QuoteCard(
                    quote     = quote,
                    isOwnQuote = quote.userId == state.currentUserId,
                    onEdit    = { onEditQuote(quote) },
                    onDelete  = { onDeleteQuote(quote) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionCaption(
                text     = stringResource(R.string.detail_reviews_count, detail.reviewCount),
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { onWriteReview(detail.id) }) { Text(stringResource(R.string.detail_write_review)) }
        }
        Spacer(Modifier.height(4.dp))

        if (state.reviews.isEmpty()) {
            Text(
                text     = stringResource(R.string.detail_no_reviews),
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            state.reviews.forEach { review ->
                ReviewCard(review)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun QuoteCard(
    quote: Quote,
    isOwnQuote: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                Icons.Filled.FormatQuote,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = quote.quoteText, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                if (quote.pageNumber != null || quote.media != null) {
                    Spacer(Modifier.height(6.dp))
                    val caption = buildList {
                        quote.media?.title?.let { add(it) }
                        quote.pageNumber?.let { add("p. $it") }
                    }.joinToString(" · ")
                    Text(text = caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (isOwnQuote) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit quote", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete quote", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun MediaCover(detail: MediaDetail) {
    val containerColor = when (detail.mediaType) {
        MediaType.BOOK  -> MaterialTheme.colorScheme.primaryContainer
        MediaType.MOVIE -> MovieContainer
        else            -> MaterialTheme.colorScheme.secondaryContainer
    }
    val iconTint = when (detail.mediaType) {
        MediaType.BOOK  -> MaterialTheme.colorScheme.onPrimaryContainer
        MediaType.MOVIE -> OnMovieContainer
        else            -> MaterialTheme.colorScheme.secondary
    }
    val placeholder = when (detail.mediaType) {
        MediaType.BOOK  -> R.drawable.menu_book_24px
        MediaType.MOVIE -> R.drawable.movie_24px
        else            -> R.drawable.tv_24px
    }

    Box(
        modifier = Modifier.size(width = 110.dp, height = 160.dp).clip(RoundedCornerShape(12.dp)).background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (detail.coverUrl != null) {
            AsyncImage(
                model              = detail.coverUrl,
                contentDescription = detail.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        } else {
            Icon(painter = painterResource(placeholder), contentDescription = null, modifier = Modifier.size(52.dp), tint = iconTint)
        }
    }
}

@Composable
private fun RatingSummary(averageRating: Float, ratingCount: Int) {
    if (ratingCount <= 0) {
        Text(
            text  = stringResource(R.string.detail_not_yet_rated),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        StarRow(rating = averageRating)
        Spacer(Modifier.width(6.dp))
        Text(text = "%.1f".format(averageRating), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(4.dp))
        Text(text = "(${"%,d".format(ratingCount)})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StarRow(rating: Float, starSize: Int = 16) {
    val rounded = (rating * 2).roundToInt()
    Row {
        for (i in 1..5) {
            val icon = when {
                rounded >= i * 2     -> Icons.Filled.Star
                rounded == i * 2 - 1 -> Icons.Outlined.StarHalf
                else                 -> Icons.Outlined.StarBorder
            }
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(starSize.dp), tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun StatGrid(detail: MediaDetail) {
    val stats = buildList {
        detail.publishedYear?.let { add(stringResource(R.string.detail_stat_year) to it.toString()) }
        when (detail.mediaType) {
            MediaType.BOOK  -> detail.pageCount?.let { add(stringResource(R.string.detail_stat_pages) to it.toString()) }
            MediaType.MOVIE -> detail.runtimeMinutes?.let { add(stringResource(R.string.detail_stat_runtime) to stringResource(R.string.detail_runtime_minutes, it)) }
            MediaType.SHOW  -> detail.seasonCount?.let { add(stringResource(R.string.detail_stat_seasons) to it.toString()) }
            MediaType.UNKNOWN -> Unit
        }
        detail.genres.firstOrNull()?.let { add(stringResource(R.string.detail_stat_genre) to it) }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.forEach { (label, value) -> StatBox(label = label, value = value, modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun SectionCaption(text: String, modifier: Modifier = Modifier) {
    Text(text = text.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier)
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            val displayName = review.user?.displayName ?: "?"
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = displayName.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = review.user?.username?.let { "@$it" } ?: displayName,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.weight(1f)
                    )
                    Text(text = review.createdAt.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                StarRow(rating = review.rating.toFloat(), starSize = 14)
                if (!review.reviewText.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(text = review.reviewText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
