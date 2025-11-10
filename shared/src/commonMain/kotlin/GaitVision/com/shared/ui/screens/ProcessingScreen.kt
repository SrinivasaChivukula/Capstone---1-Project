package GaitVision.com.shared.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import GaitVision.com.shared.ui.theme.GaitVisionTheme

@Composable
fun ProcessingScreen(
    participantId: String,
    height: Int,
    videoUri: String,
    onNavigateToResults: (videoId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var processingProgress by remember { mutableStateOf(0f) }
    var isProcessing by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf("Initializing...") }

    GaitVisionTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Processing Video") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Text("←")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        progress = processingProgress
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = currentStep,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = processingProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Ready to process video",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isProcessing = true
                            // TODO: Implement video processing
                        }
                    ) {
                        Text("Start Processing")
                    }
                }
            }
        }
    }
}

