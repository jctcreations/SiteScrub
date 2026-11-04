package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AuditHistoryEntity
import com.example.ui.components.ScoreColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    historyList: List<AuditHistoryEntity>,
    onReScanUrl: (String) -> Unit,
    onDeleteAudit: (Long) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Audit Progression & History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Track your website score improvements over time.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (historyList.isNotEmpty()) {
                    IconButton(onClick = onClearAll) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All History")
                    }
                }
            }
        }

        // 1. History Trend Chart
        if (historyList.size >= 2) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Score Progression Trend",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            val first = historyList.last().overallScore
                            val latest = historyList.first().overallScore
                            val delta = latest - first
                            val deltaColor = if (delta >= 0) ScoreColors.GoodGreen else ScoreColors.CriticalRed
                            val sign = if (delta > 0) "+$delta" else "$delta"

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(deltaColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$sign pts overall",
                                    color = deltaColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Canvas Line Chart
                        val scores = historyList.reversed().map { it.overallScore.toFloat() }
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            if (scores.size < 2) return@Canvas
                            val maxScore = 100f
                            val minScore = (scores.minOrNull() ?: 50f) - 10f
                            val scoreRange = (maxScore - minScore).coerceAtLeast(20f)

                            val stepX = size.width / (scores.size - 1)
                            val path = Path()

                            val points = scores.mapIndexed { index, score ->
                                val x = index * stepX
                                val y = size.height - (((score - minScore) / scoreRange) * size.height)
                                Offset(x, y.coerceIn(0f, size.height))
                            }

                            path.moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                path.lineTo(points[i].x, points[i].y)
                            }

                            // Draw baseline grid lines
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1f
                            )

                            // Draw trend line
                            drawPath(
                                path = path,
                                color = ScoreColors.AccentTeal,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Draw points and values
                            points.forEachIndexed { idx, pt ->
                                drawCircle(
                                    color = ScoreColors.forScore(scores[idx].toInt()),
                                    radius = 5.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.5.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Scan History Items List
        item {
            Text(
                text = "Past Scans (${historyList.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        if (historyList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No scan history yet. Complete an audit to track history here.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(historyList) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.domain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = dateFormat.format(Date(item.timestamp)),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Speed: ${item.speedScore} · SEO: ${item.seoScore} · UX: ${item.uxScore}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ScoreColors.bgForScore(item.overallScore))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${item.overallScore}/100",
                                    color = ScoreColors.forScore(item.overallScore),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(onClick = { onReScanUrl(item.rootUrl) }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Re-scan this website")
                            }

                            IconButton(onClick = { onDeleteAudit(item.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
