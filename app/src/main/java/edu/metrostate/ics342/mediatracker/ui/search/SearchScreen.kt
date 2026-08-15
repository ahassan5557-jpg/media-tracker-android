package edu.metrostate.ics342.mediatracker.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.datastore.DefaultSessionRepository
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSearch: (String) -> Unit,
    onMediaClick: (Int) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()

    // "Popular This Week" now pulls from the real backend instead of FakeMediaRepository —
    // previously these were hardcoded fake ids that never matched real /media/{id} records,
    // so tapping a suggestion here always failed even when the real search flow worked fine.
    val context = LocalContext.current
    var popularItems by remember { mutableStateOf<List<Media>>(emptyList()) }
    var popularLoading by remember { mutableStateOf(true) }
    var popularError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedType) {
        popularLoading = true
        popularError = null
        try {
            val repository = DefaultMediaRepository(DefaultSessionRepository(context))
            val page = repository.search(
                query = "",
                type  = selectedType.ifBlank { null },
                after = null
            )
            popularItems = page.items
        } catch (e: Exception) {
            popularError = "Couldn't load suggestions."
        } finally {
            popularLoading = false
        }
    }

    fun triggerSearch() {
        if (query.isNotBlank()) {
            val q = query
            viewModel.clearQuery()
            onSearch(q)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.app_name)) })

        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { triggerSearch() }) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search_hint))
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { triggerSearch() })
        )

        MediaTypeFilterChips(
            selectedType = selectedType,
            onTypeSelect = viewModel::onTypeSelect,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.search_popular_this_week).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                popularLoading -> {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                popularError != null -> {
                    Text(
                        text     = popularError!!,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                popularItems.isEmpty() -> {
                    Text(
                        text     = "Nothing to show yet.",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                else -> {
                    popularItems.forEach { media ->
                        MediaResultCard(
                            media = media,
                            onClick = { onMediaClick(media.id) }
                        )
                    }
                }
            }
        }
    }
}
