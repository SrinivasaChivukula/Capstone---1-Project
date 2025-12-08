package com.gaitvision.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gaitvision.data.AppDatabase

@Composable
fun AnalysisScreen(
    onNavigateBack: () -> Unit,
    database: AppDatabase
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analysis Report", style = MaterialTheme.typography.h6) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Patient Header
            Card(
                elevation = 2.dp,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PATIENT ID", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.secondary)
                    Text("John Doe (M, 45)", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Date: Oct 24, 2023", style = MaterialTheme.typography.body2)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Vitals Grid
            Text("GAIT PARAMETERS", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.secondary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                VitalCard(
                    title = "Gait Score",
                    value = "85.0",
                    unit = "/ 100",
                    color = Color(0xFF137333), // Green
                    modifier = Modifier.weight(1f)
                )
                VitalCard(
                    title = "Speed",
                    value = "1.2",
                    unit = "m/s",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                VitalCard(
                    title = "Cadence",
                    value = "110",
                    unit = "steps/min",
                    modifier = Modifier.weight(1f)
                )
                VitalCard(
                    title = "Stride Length",
                    value = "0.75",
                    unit = "m",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Chart Section
            Text("JOINT ANGLES (KNEE)", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.secondary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
            
            Card(
                elevation = 2.dp,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(200.dp)
            ) {
                // Simple Canvas Chart Placeholder
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val width = size.width
                    val height = size.height
                    val points = listOf(0.2f, 0.4f, 0.8f, 0.5f, 0.9f, 0.3f, 0.6f)
                    
                    val path = Path()
                    points.forEachIndexed { index, y ->
                        val x = (index.toFloat() / (points.size - 1)) * width
                        val yPos = height - (y * height)
                        if (index == 0) path.moveTo(x, yPos) else path.lineTo(x, yPos)
                    }
                    
                    drawPath(
                        path = path,
                        color = MedicalBlue,
                        style = Stroke(width = 4f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
fun VitalCard(
    title: String,
    value: String,
    unit: String,
    color: Color = MaterialTheme.colors.primary,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.caption, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.h4, color = color, fontWeight = FontWeight.Bold)
            Text(unit, style = MaterialTheme.typography.body2, color = Color.Gray)
        }
    }
}
