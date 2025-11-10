package GaitVision.com.shared.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import GaitVision.com.shared.ui.theme.GaitVisionTheme

@Composable
fun MainScreen(
    onNavigateToProcessing: (participantId: String, height: Int, videoUri: String) -> Unit
) {
    var participantId by remember { mutableStateOf("") }
    var selectedFeet by remember { mutableStateOf(5) }
    var selectedInches by remember { mutableStateOf(9) }
    var videoUri by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf<String?>(null) }

    GaitVisionTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "GaitVision",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Subtitle
                Text(
                    text = "2D Gait Analysis for Clinical Use",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Participant ID Input
                OutlinedTextField(
                    value = participantId,
                    onValueChange = { participantId = it },
                    label = { Text("Participant ID") },
                    placeholder = { Text("Enter participant ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Height Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Height",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Feet Dropdown
                            var expandedFeet by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedFeet,
                                    onExpandedChange = { expandedFeet = !expandedFeet }
                                ) {
                                    OutlinedTextField(
                                        value = "$selectedFeet ft",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Feet") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFeet) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedFeet,
                                        onDismissRequest = { expandedFeet = false }
                                    ) {
                                        (3..8).forEach { feet ->
                                            DropdownMenuItem(
                                                text = { Text("$feet ft") },
                                                onClick = {
                                                    selectedFeet = feet
                                                    expandedFeet = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Inches Dropdown
                            var expandedInches by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedInches,
                                    onExpandedChange = { expandedInches = !expandedInches }
                                ) {
                                    OutlinedTextField(
                                        value = "$selectedInches in",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Inches") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInches) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedInches,
                                        onDismissRequest = { expandedInches = false }
                                    ) {
                                        (0..11).forEach { inches ->
                                            DropdownMenuItem(
                                                text = { Text("$inches in") },
                                                onClick = {
                                                    selectedInches = inches
                                                    expandedInches = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Video Selection Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Video Selection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    // TODO: Implement camera capture
                                    showError = "Camera feature coming soon"
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Record Video")
                            }

                            OutlinedButton(
                                onClick = {
                                    // TODO: Implement gallery picker
                                    showError = "Gallery picker coming soon"
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Select Video")
                            }
                        }

                        // Video Preview Placeholder
                        if (videoUri != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Video Preview")
                                }
                            }
                        }
                    }
                }

                // Error Message
                showError?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            IconButton(onClick = { showError = null }) {
                                Text("×")
                            }
                        }
                    }
                }

                // Confirm Button
                Button(
                    onClick = {
                        if (participantId.isEmpty()) {
                            showError = "Please enter a Participant ID"
                            return@Button
                        }
                        val heightInInches = (selectedFeet * 12) + selectedInches
                        if (heightInInches <= 0) {
                            showError = "Please select a valid height"
                            return@Button
                        }
                        if (videoUri == null) {
                            showError = "Please select or record a video first"
                            return@Button
                        }
                        onNavigateToProcessing(participantId, heightInInches, videoUri!!)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = participantId.isNotEmpty() && videoUri != null
                ) {
                    Text(
                        text = "Confirm Video",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

