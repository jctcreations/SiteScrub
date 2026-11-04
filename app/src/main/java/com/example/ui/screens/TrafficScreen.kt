package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.ScoreColors

@Composable
fun TrafficScreen(
    currentAudit: AuditResult?,
    isConnected: Boolean,
    onToggleConnection: () -> Unit,
    onSelectPage: (PageAudit) -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentAudit == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Enter a website on Overview to view search & traffic data.")
        }
        return
    }

    val metrics = currentAudit.gscGaMetrics
    val pages = currentAudit.pages

    val highTrafficLowScore = pages.filter { it.trafficStatus == PageTrafficStatus.HIGH_TRAFFIC_LOW_SCORE }
    val starPerformers = pages.filter { it.trafficStatus == PageTrafficStatus.STAR_PERFORMER }
    val hiddenGems = pages.filter { it.trafficStatus == PageTrafficStatus.HIDDEN_GEM }
    val ghostPages = pages.filter { it.trafficStatus == PageTrafficStatus.IGNORED_PAGE }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. GSC & GA Connection Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) ScoreColors.GoodGreenBg else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                border = if (isConnected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ScoreColors.GoodGreen.copy(alpha = 0.5f))) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = if (isConnected) ScoreColors.GoodGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isConnected) "Search Console & GA4 Synced" else "Connect Search Console & GA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isConnected) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isConnected)
                                "Real traffic, rankings, and clicks linked for ${currentAudit.domain}."
                            else
                                "Tap switch to link your Google properties or load verified domain telemetry.",
                            fontSize = 12.sp,
                            color = if (isConnected) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isConnected,
                        onCheckedChange = { onToggleConnection() }
                    )
                }
            }
        }

        // 2. Metrics Scorecard
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "30-Day Search & Traffic Performance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrafficKpi(
                            label = "Organic Clicks",
                            value = "${metrics.totalClicks / 1000}k",
                            subText = "Google Search",
                            color = Color(0xFF0284C7)
                        )
                        TrafficKpi(
                            label = "Impressions",
                            value = "${metrics.totalImpressions / 1000}k",
                            subText = "Search appearances",
                            color = Color(0xFF0D9488)
                        )
                        TrafficKpi(
                            label = "Avg. CTR",
                            value = "${metrics.avgCtr}%",
                            subText = "Click-through rate",
                            color = Color(0xFF10B981)
                        )
                        TrafficKpi(
                            label = "Avg. Position",
                            value = "#${metrics.avgPosition}",
                            subText = "SERP rank",
                            color = Color(0xFFF59E0B)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = "Monthly Users: ${metrics.monthlySessions}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Bounce Rate: ${metrics.bounceRate}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Avg Time: ${metrics.avgDurationSeconds}s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 3. Traffic Opportunity Segments
        // A. High Traffic, Low Score (The Goldmine!)
        if (highTrafficLowScore.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ScoreColors.CriticalRedBg),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ScoreColors.CriticalRed.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = ScoreColors.CriticalRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "High Traffic, Low Health Pages (Top Opportunity!)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Text(
                            text = "These pages get substantial organic clicks but suffer from low audit scores. Fixing them will yield massive conversion gains.",
                            fontSize = 11.sp,
                            color = Color(0xFFB91C1C)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        highTrafficLowScore.forEach { p ->
                            TrafficPageItem(p, onSelectPage)
                        }
                    }
                }
            }
        }

        // B. Ghost Pages (Ignored Pages)
        if (ghostPages.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ghost Pages (Zero / Low Clicks)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "These ${ghostPages.size} pages receive almost no organic traffic. Consider consolidating, updating content, or adding internal links.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ghostPages.forEach { p ->
                            TrafficPageItem(p, onSelectPage)
                        }
                    }
                }
            }
        }

        // C. Star Performers
        if (starPerformers.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = ScoreColors.WarningYellow)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Star Performers (${starPerformers.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "High organic traffic and strong page health.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        starPerformers.forEach { p ->
                            TrafficPageItem(p, onSelectPage)
                        }
                    }
                }
            }
        }

        // 4. Acquisition Channel Share
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Traffic Channels (Google Analytics 4)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    metrics.topChannels.forEach { ch ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = ch.channel, fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { ch.percentage / 100f },
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = ScoreColors.AccentTeal
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${ch.percentage}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrafficPageItem(
    page: PageAudit,
    onSelect: (PageAudit) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect(page) }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = page.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text = "${page.path} · Pos #${page.avgPosition}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${page.organicClicks} clicks",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Score ${page.overallScore}/100",
                fontSize = 11.sp,
                color = ScoreColors.forScore(page.overallScore),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TrafficKpi(
    label: String,
    value: String,
    subText: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = subText, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
