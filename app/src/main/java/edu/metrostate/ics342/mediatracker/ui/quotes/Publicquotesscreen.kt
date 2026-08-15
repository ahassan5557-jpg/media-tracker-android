package edu.metrostate.ics342.mediatracker.ui.quotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.metrostate.ics342.mediatracker.data.model.Quote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicQuotesScreen(
    onNavigateBack: () -> Unit,
    viewModel: PublicQuotesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val likedIds by viewModel.likedIds.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Public Quotes") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        when (val state = uiState) {
            is PublicQuotesUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PublicQuotesUiState.Empty -> {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text  = "No public quotes yet — be the first to share one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is PublicQuotesUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.load() }) { Text("Retry") }
                    }
                }
            }
            is PublicQuotesUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(items = state.quotes, key = { it.id }) { quote ->
                        PublicQuoteCard(
                            quote    = quote,
                            isLiked  = quote.id in likedIds,
                            onToggleLike = {
                                if (quote.id in likedIds) viewModel.unlikeQuote(quote.id)
                                else viewModel.likeQuote(quote.id)
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    if (state.hasMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isLoadingMore) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    OutlinedButton(onClick = { viewModel.loadMore() }) {
                                        Text("Load More")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicQuoteCard(quote: Quote, isLiked: Boolean, onToggleLike: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
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
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val initial = quote.userId.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = initial,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text     = quote.userId,
                    style    = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                IconButton(onClick = onToggleLike, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector        = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        modifier           = Modifier.size(18.dp),
                        tint               = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text  = quote.likeCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
