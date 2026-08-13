package edu.metrostate.ics342.mediatracker.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun WriteReviewScreen(
    mediaId: Int,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Write Review is not implemented yet.\n(mediaId = $mediaId)",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
