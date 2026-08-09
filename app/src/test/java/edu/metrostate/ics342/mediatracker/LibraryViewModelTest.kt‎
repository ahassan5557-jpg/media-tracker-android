package edu.metrostate.ics342.mediatracker.ui.library

import android.app.Application
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import edu.metrostate.ics342.mediatracker.data.network.LibraryPage
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val application = mockk<Application>(relaxed = true)
    private val repository = mockk<DefaultMediaRepository>()

    private val item = LibraryItem(
        userId    = "user-1",
        mediaId   = 1,
        status    = LibraryStatus.WANT_TO,
        addedAt   = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { repository.getLibrary(LibraryStatus.WANT_TO) } returns LibraryPage(listOf(item), null, false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `removeItem removes the item immediately`() = runTest(dispatcher) {
        coEvery { repository.removeFromLibrary(1) } returns Unit

        val viewModel = LibraryViewModel(application, repository)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.removeItem(1)

        val stateBeforeNetworkCompletes = viewModel.uiState.value as LibraryUiState.Success
        assertTrue(stateBeforeNetworkCompletes.items.none { it.mediaId == 1 })
    }

    @Test
    fun `removeItem rolls back when the network call fails`() = runTest(dispatcher) {
        coEvery { repository.removeFromLibrary(1) } throws IOException("network down")

        val viewModel = LibraryViewModel(application, repository)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.removeItem(1)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as LibraryUiState.Success
        assertEquals(1, state.items.size)
        assertTrue(state.items.any { it.mediaId == 1 })
        assertEquals("Couldn't remove item. Try again.", viewModel.errorMessage.value)
    }
}