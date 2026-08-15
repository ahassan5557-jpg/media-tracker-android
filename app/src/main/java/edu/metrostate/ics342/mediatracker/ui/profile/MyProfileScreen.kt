package edu.metrostate.ics342.mediatracker.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.toIconRes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.metrostate.ics342.mediatracker.data.datastore.DefaultSessionRepository
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.Quote
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onEditProfile: () -> Unit,
    onSettingsClick: () -> Unit,
    onBrowsePublicQuotes: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user    by viewModel.currentUser.collectAsStateWithLifecycle()
    val library by viewModel.libraryPreview.collectAsState()

    val context = LocalContext.current
    var quotes by remember { mutableStateOf<List<Quote>>(emptyList()) }
    var quotesLoading by remember { mutableStateOf(true) }
    var quotesError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        quotesLoading = true
        quotesError = null
        try {
            val repository = DefaultMediaRepository(DefaultSessionRepository(context))
            quotes = repository.getQuotes()
        } catch (e: Exception) {
            quotesError = "Couldn't load quotes."
        } finally {
            quotesLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.profile_title)) },
            actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Outlined.Settings, stringResource(R.string.profile_settings))
                }
            }
        )

        if (user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        val u = user!!

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(88.dp).clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (u.avatarUrl != null) {
                            AsyncImage(
                                model              = u.avatarUrl,
                                contentDescription = u.displayName,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize()
                            )
                        } else {
                            Surface(
                                color    = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        u.displayName.firstOrNull()?.toString() ?: "?",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(u.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text("@${u.username}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (!u.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(u.bio, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItem(u.followerCount.toString(), R.string.profile_followers)
                        StatItem(u.followingCount.toString(), R.string.profile_following)
                        StatItem(u.trackedCount.toString(), R.string.profile_tracked)
                    }

                    Spacer(Modifier.height(20.dp))

                    OutlinedButton(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.profile_edit_button))
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))

                    Text(
                        stringResource(R.string.profile_recently_tracked),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(Modifier.height(8.dp))

                    if (library.isEmpty()) {
                        Text(
                            stringResource(R.string.profile_nothing_tracked),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                }
            }

            if (library.isNotEmpty()) {
                items(items = library, key = { it.mediaId }) { item ->
                    LibraryRow(item)
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "My Quotes (${quotes.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onBrowsePublicQuotes) { Text("Browse Public") }
                }
                Spacer(Modifier.height(8.dp))

                when {
                    quotesLoading -> {
                        Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    quotesError != null -> {
                        Text(quotesError!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                    quotes.isEmpty() -> {
                        Text(
                            "No quotes saved yet — add one from a book's detail page.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!quotesLoading && quotesError == null && quotes.isNotEmpty()) {
                items(items = quotes, key = { it.id }) { quote ->
                    ProfileQuoteCard(quote)
                    Spacer(Modifier.height(10.dp))
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun LibraryRow(item: LibraryItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp, 56.dp).clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxSize()) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(item.media?.mediaType.toIconRes()),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(item.media?.title ?: "", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(stringResource(item.status.labelRes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileQuoteCard(quote: Quote) {
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
                Text(
                    text  = quote.quoteText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(6.dp))
                val caption = buildList {
                    quote.media?.title?.let { add(it) }
                    quote.pageNumber?.let { add("p. $it") }
                    add(if (quote.isPublic) "Public" else "Private")
                }.joinToString(" · ")
                Text(
                    text  = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun StatItem(value: String, labelRes: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

