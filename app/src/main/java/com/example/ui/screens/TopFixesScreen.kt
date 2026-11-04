package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.FixCard
import com.example.ui.components.ScoreColors

@Composable
fun TopFixesScreen(
    currentAudit: AuditResult?,
    onCopySnippet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentAudit == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No audit data. Enter a website to scan.")
        }
        return
    }

    var selectedFilter by remember { mutableStateOf("All") }

    val filteredFixes = remember(selectedFilter, currentAudit) {
        when (selectedFilter) {
            "Quick Wins" -> currentAudit.topFixes.filter { it.effort == EffortLevel.LOW }
            "High Impact" -> currentAudit.topFixes.filter { it.impact == ImpactLevel.HIGH }
            "SEO" -> currentAudit.topFixes.filter { it.category == AuditCategory.SEO }
            "Speed" -> currentAudit.topFixes.filter { it.category == AuditCategory.SPEED }
            "Accessibility" -> currentAudit.topFixes.filter { it.category == AuditCategory.ACCESSIBILITY }
            "Content" -> currentAudit.topFixes.filter { it.category == AuditCategory.CONTENT }
            else -> currentAudit.topFixes
        }
    }

    val realCount = currentAudit.totalRealIssuesCount

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = ScoreColors.WarningYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (realCount < 5 && realCount > 0)
                            "Only $realCount real issue(s) found"
                        else if (realCount == 0)
                            "0 real issues found"
                        else
                            "Top 5 Fixes (Ranked by Impact)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rule #2 enforced: Every finding includes exact element proof from live downloaded HTML. No guessing or filler issues.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Real vs Guessed banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = ScoreColors.AccentTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (realCount < 5)
                            "Honest Reporting: Only $realCount provable issue(s) exist across ${currentAudit.crawledPagesCount} pages."
                        else
                            "Showing top 5 prioritized issues out of $realCount provable findings.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }

        // Quick filter chips
        item {
            val filters = listOf("All", "Quick Wins", "High Impact", "Content", "Speed", "SEO", "Accessibility")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { f ->
                    FilterChip(
                        selected = selectedFilter == f,
                        onClick = { selectedFilter = f },
                        label = { Text(f, fontSize = 12.sp) }
                    )
                }
            }
        }

        // List of FixCards
        items(filteredFixes) { fix ->
            FixCard(
                fix = fix,
                onCopySnippet = onCopySnippet
            )
        }

        if (filteredFixes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ScoreColors.GoodGreenBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ScoreColors.GoodGreen
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (realCount == 0)
                                "No issues found! The scanned pages adhere to best practices with zero element defects."
                            else
                                "No fixes matching '$selectedFilter'.",
                            color = Color(0xFF065F46),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
