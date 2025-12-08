package com.gaitvision.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.gaitvision.data.AppDatabase
import com.gaitvision.logic.GaitAnalyzer
import com.gaitvision.platform.FilePicker
import com.gaitvision.platform.VideoProcessor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share // Using Share as generic upload icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    database: AppDatabase,
    videoProcessor: VideoProcessor
) {
    val scope = rememberCoroutineScope()
    val analyzer = remember { GaitAnalyzer() }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }

    val filePicker = remember {
        FilePicker { path ->
            if (path != null) {
                isProcessing = true
                scope.launch {
                    val outputPath = path.substringBeforeLast(".") + "_processed.mp4"
                    videoProcessor.processVideo(
                        inputPath = path,
                        outputPath = outputPath,
                        onProgress = { p -> progress = p },
                        onPoseDetected = { pose -> analyzer.addPose(pose) }
                    )
                    val score = analyzer.analyze()
                    // TODO: Save score to database
                    // database.gaitScoreDao().insert(score)
                    
                    isProcessing = false
                    onNavigateToAnalysis()
                }
            }
        }
    }
    filePicker.register()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GaitVision Dashboard", style = MaterialTheme.typography.h6) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                elevation = 4.dp
            )
        },
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Welcome Section
            Text(
                text = "Welcome, Dr. Smith",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(vertical = 16.dp).align(Alignment.Start)
            )

            // Action Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(
                    title = "New Analysis",
                    icon = Icons.Default.Add,
                    onClick = onNavigateToCamera,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "Upload Video",
                    icon = Icons.Default.Share,
                    onClick = { filePicker.launch() },
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f)
                )
            }

            if (isProcessing) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colors.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Processing Video: $progress%", style = MaterialTheme.typography.body1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Patients Section
            Text(
                text = "Recent Patients",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(5) { index ->
                    PatientCard(
                        name = "Patient #100${index + 1}",
                        date = "Oct ${20 - index}, 2023",
                        score = 85 - (index * 2)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(enabled = enabled, onClick = onClick),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = if (enabled) MaterialTheme.colors.surface else Color.LightGray
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.button, color = MaterialTheme.colors.onSurface)
        }
    }
}

@Composable

