package edu.metrostate.ics342.mediatracker.ui.quotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.datastore.DefaultSessionRepository
import edu.metrostate.ics342.mediatracker.data.model.DuplicateLikeException
import edu.metrostate.ics342.mediatracker.data.model.Quote
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PublicQuotesUiState {
    data object Loading : PublicQuotesUiState
    data object Empty : PublicQuotesUiState
    data class Error(val message: String) : PublicQuotesUiState
    data class Success(
        val quotes: List<Quote>,
        val hasMore: Boolean,
        val isLoadingMore: Boolean = false
    ) : PublicQuotesUiState
}

class PublicQuotesViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: DefaultMediaRepository =
        DefaultMediaRepository(DefaultSessionRepository(application))
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<PublicQuotesUiState>(PublicQuotesUiState.Loading)
    val uiState: StateFlow<PublicQuotesUiState> = _uiState.asStateFlow()

    private val _likedIds = MutableStateFlow<Set<Int>>(emptySet())
    val likedIds: StateFlow<Set<Int>> = _likedIds.asStateFlow()

    private var nextCursor: String? = null

    init { load() }

    fun load() {
        _uiState.value = PublicQuotesUiState.Loading
        viewModelScope.launch {
            try {
                val page = repository.getPublicQuotes()
                nextCursor = page.nextCursor
                _uiState.value = if (page.items.isEmpty()) {
                    PublicQuotesUiState.Empty
                } else {
                    PublicQuotesUiState.Success(quotes = page.items, hasMore = page.hasMore)
                }
            } catch (e: Exception) {
                _uiState.value = PublicQuotesUiState.Error(e.message ?: "Couldn't load public quotes.")
            }
        }
    }

    fun loadMore() {
        val current = _uiState.value as? PublicQuotesUiState.Success ?: return
        if (!current.hasMore || current.isLoadingMore) return

        _uiState.value = current.copy(isLoadingMore = true)
        viewModelScope.launch {
            try {
                val page = repository.getPublicQuotes(after = nextCursor)
                nextCursor = page.nextCursor
                val latest = _uiState.value as? PublicQuotesUiState.Success ?: return@launch
                _uiState.value = latest.copy(
                    quotes        = latest.quotes + page.items,
                    hasMore       = page.hasMore,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                val latest = _uiState.value as? PublicQuotesUiState.Success ?: return@launch
                _uiState.value = latest.copy(isLoadingMore = false)
            }
        }
    }

    fun likeQuote(quoteId: Int) {
        val current = _uiState.value as? PublicQuotesUiState.Success ?: return
        if (quoteId in _likedIds.value) return

        _likedIds.value = _likedIds.value + quoteId
        _uiState.value = current.copy(
            quotes = current.quotes.map { if (it.id == quoteId) it.copy(likeCount = it.likeCount + 1) else it }
        )

        viewModelScope.launch {
            try {
                repository.likeQuote(quoteId)
            } catch (e: DuplicateLikeException) {
            } catch (e: Exception) {
                _likedIds.value = _likedIds.value - quoteId
                val latest = _uiState.value as? PublicQuotesUiState.Success ?: return@launch
                _uiState.value = latest.copy(
                    quotes = latest.quotes.map { if (it.id == quoteId) it.copy(likeCount = it.likeCount - 1) else it }
                )
            }
        }
    }

    fun unlikeQuote(quoteId: Int) {
        val current = _uiState.value as? PublicQuotesUiState.Success ?: return
        if (quoteId !in _likedIds.value) return

        _likedIds.value = _likedIds.value - quoteId
        _uiState.value = current.copy(
            quotes = current.quotes.map { if (it.id == quoteId) it.copy(likeCount = (it.likeCount - 1).coerceAtLeast(0)) else it }
        )

        viewModelScope.launch {
            try {
                repository.unlikeQuote(quoteId)
            } catch (e: Exception) {
                _likedIds.value = _likedIds.value + quoteId
                val latest = _uiState.value as? PublicQuotesUiState.Success ?: return@launch
                _uiState.value = latest.copy(
                    quotes = latest.quotes.map { if (it.id == quoteId) it.copy(likeCount = it.likeCount + 1) else it }
                )
            }
        }
    }
}
