package edu.metrostate.ics342.mediatracker.ui.detail

import android.app.Application
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.Quote
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import edu.metrostate.ics342.mediatracker.data.network.SessionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@OptIn(ExperimentalCoroutinesApi::class)
class MediaDetailViewModelQuoteTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addQuote_appendsNewQuoteToSuccessState() = runTest(testDispatcher) {
        val application = mockk<Application>(relaxed = true)
        val repository = mockk<DefaultMediaRepository>()
        val sessionRepository = mockk<SessionRepository>()

        val mediaId = 1
        val fakeDetail = mockk<MediaDetail>(relaxed = true)

        coEvery { sessionRepository.getUser() } returns null
        coEvery { repository.getMediaDetail(mediaId) } returns fakeDetail
        coEvery { repository.getLibraryItem(mediaId) } returns null
        coEvery { repository.getFavorite(mediaId) } returns null
        coEvery { repository.getReviews(mediaId) } returns emptyList()
        coEvery { repository.getQuotes() } returns emptyList()

        val newQuote = Quote(
            id         = 42,
            userId     = "user-1",
            mediaId    = mediaId,
            quoteText  = "A great line",
            pageNumber = 10,
            isPublic   = false,
            likeCount  = 0,
            createdAt  = "2026-01-01T00:00:00Z"
        )
        coEvery {
            repository.createQuote(mediaId = mediaId, quoteText = "A great line", pageNumber = 10, isPublic = false)
        } returns newQuote

        val viewModel = MediaDetailViewModel(application, repository, sessionRepository)

        viewModel.load(mediaId)
        testDispatcher.scheduler.advanceUntilIdle()

        val loaded = viewModel.uiState.value
        assertTrue(loaded is MediaDetailUiState.Success)
        assertEquals(0, (loaded as MediaDetailUiState.Success).quotes.size)

        viewModel.addQuote("A great line", 10, false)
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.uiState.value
        assertTrue(updated is MediaDetailUiState.Success)
        updated as MediaDetailUiState.Success
        assertEquals(1, updated.quotes.size)
        assertEquals("A great line", updated.quotes.first().quoteText)
        assertEquals(42, updated.quotes.first().id)
    }
}
