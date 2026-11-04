package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuditCategory
import com.example.model.AuditResult
import com.example.model.CrawlProgress
import com.example.ui.AppNavTab
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    currentAudit: AuditResult?,
    crawlProgress: CrawlProgress,
    targetUrlInput: String,
    onUrlChange: (String) -> Unit,
    onRunAudit: (String) -> Unit,
    onReScan: () -> Unit,
    onNavigateTab: (AppNavTab) -> Unit,
    onCopySnippet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val presetWebsites = listOf(
        "novacloud.io" to "SaaS Platform",
        "artisancafebakery.com" to "Local Bakery",
        "techinsightspress.org" to "News / Blog",
        "nordiclivinghome.shop" to "E-commerce"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. Search & Audit URL Input
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Enter Website to Crawl & Audit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = targetUrlInput,
                        onValueChange = onUrlChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("example.com or https://...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (targetUrlInput.isNotBlank()) {
                                IconButton(onClick = { onUrlChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                keyboardController?.hide()
                                onRunAudit(targetUrlInput)
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                onRunAudit(targetUrlInput)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !crawlProgress.isCrawling && targetUrlInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (crawlProgress.isCrawling) "Crawling..." else "Audit Website")
                        }

                        if (currentAudit != null) {
                            OutlinedButton(
                                onClick = onReScan,
                                shape = RoundedCornerShape(12.dp),
                                enabled = !crawlProgress.isCrawling
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Re-scan", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Re-scan")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Or test popular profiles:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetWebsites) { (domain, label) ->
                            FilterChip(
                                selected = targetUrlInput == domain,
                                onClick = {
                                    onUrlChange(domain)
                                    onRunAudit(domain)
                                },
                                label = { Text("$domain ($label)", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // 2. Crawl Progress Banner
        item {
            AnimatedVisibility(visible = crawlProgress.isCrawling) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = crawlProgress.statusMessage,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { crawlProgress.progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Crawling: ${crawlProgress.currentUrl}",
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${crawlProgress.pagesScanned} / ${crawlProgress.totalDiscovered} pages",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // 3. Overall Score Hero Card
        if (currentAudit != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentAudit.domain,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${currentAudit.crawledPagesCount} pages crawled · ${if (currentAudit.isHttps) "HTTPS Encrypted" else "HTTP"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // GSC connection status badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ScoreColors.GoodGreenBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable { onNavigateTab(AppNavTab.TRAFFIC) }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = ScoreColors.GoodGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "GSC Synced",
                                        color = ScoreColors.GoodGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ScoreGauge(score = currentAudit.overallScore, size = 130.dp)

                        Spacer(modifier = Modifier.height(12.dp))

                        val summaryText = when {
                            currentAudit.overallScore >= 85 -> "Great health! Only a few quick polish items remain."
                            currentAudit.overallScore >= 70 -> "Good foundation, but high-impact quick wins are available."
                            else -> "Action needed: Critical issues are hurting ranking and mobile conversion."
                        }
                        Text(
                            text = summaryText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. Category Score Breakdown
            item {
                Text(
                    text = "Category Performance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryScoreBar(category = AuditCategory.SPEED, score = currentAudit.speedScore)
                    CategoryScoreBar(category = AuditCategory.SEO, score = currentAudit.seoScore)
                    CategoryScoreBar(category = AuditCategory.CONTENT, score = currentAudit.contentScore)
                    CategoryScoreBar(category = AuditCategory.USABILITY, score = currentAudit.uxScore)
                    CategoryScoreBar(category = AuditCategory.ACCESSIBILITY, score = currentAudit.a11yScore)
                    CategoryScoreBar(category = AuditCategory.TECHNICAL, score = currentAudit.techScore)
                }
            }

            // 5. Top Fixes Teaser Card
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = ScoreColors.WarningYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Top Fixes (Impact vs Effort)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            TextButton(onClick = { onNavigateTab(AppNavTab.TOP_FIXES) }) {
                                Text("View All (${currentAudit.topFixes.size})")
                            }
                        }

                        if (currentAudit.topFixes.isNotEmpty()) {
                            val topFix = currentAudit.topFixes.first()
                            Spacer(modifier = Modifier.height(8.dp))
                            FixCard(fix = topFix, onCopySnippet = onCopySnippet)
                        }
                    }
                }
            }

            // 6. What's Working Section (Celebrate good practices!)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ScoreColors.GoodGreenBg),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ScoreColors.GoodGreen.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ScoreColors.GoodGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "What's Working (Keep Doing This!)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF065F46)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        currentAudit.whatsWorking.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    tint = ScoreColors.GoodGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF064E3B)
                                    )
                                    Text(
                                        text = item.description,
                                        fontSize = 12.sp,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
