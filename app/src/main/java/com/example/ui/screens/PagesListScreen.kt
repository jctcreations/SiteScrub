package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.model.PageAudit
import com.example.ui.PageFilter
import com.example.ui.components.ScoreColors

@Composable
fun PagesListScreen(
    pages: List<PageAudit>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    currentFilter: PageFilter,
    onFilterChange: (PageFilter) -> Unit,
    onSelectPage: (PageAudit) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredPages = remember(pages, searchQuery, currentFilter) {
        pages.filter { page ->
            val matchesQuery = searchQuery.isBlank() ||
                    page.title.contains(searchQuery, ignoreCase = true) ||
                    page.path.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (currentFilter) {
                PageFilter.ALL -> true
                PageFilter.HAS_VIDEOS -> page.videosCount > 0
                PageFilter.NEEDS_FIXES -> page.issues.isNotEmpty()
                PageFilter.HIGH_TRAFFIC -> page.organicClicks > 1000
            }

            matchesQuery && matchesFilter
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Live Scanned Pages Inventory",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real element counts parsed from live downloaded HTML: images, links, buttons, and videos.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by path (e.g. /book or /reviews)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PageFilter.entries) { filter ->
                        FilterChip(
                            selected = currentFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter.label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Showing ${filteredPages.size} of ${pages.size} scanned pages",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(filteredPages) { page ->
            PageAuditCard(
                page = page,
                onClick = { onSelectPage(page) }
            )
        }

        if (filteredPages.isEmpty()) {
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
                            text = "No pages matching your filter.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PageAuditCard(
    page: PageAudit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scoreColor = ScoreColors.forScore(page.overallScore)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = page.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = page.path,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ScoreColors.bgForScore(page.overallScore))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${page.overallScore}/100",
                        color = scoreColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real Element Inventory Row (User requirement)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElementPill(icon = Icons.Default.Image, label = "${page.imagesCount} imgs", alert = page.missingAltCount > 0)
                ElementPill(icon = Icons.Default.Link, label = "${page.linksCount} links", alert = false)
                ElementPill(icon = Icons.Default.SmartButton, label = "${page.buttonsCount} btns", alert = false)
                ElementPill(
                    icon = Icons.Default.Videocam,
                    label = "${page.videosCount} vids",
                    alert = page.videosCount >= 2,
                    highlight = page.videosCount > 0
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Load time: ${page.loadTimeMs}ms",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (page.issues.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ScoreColors.WarningYellowBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${page.issues.size} proven issue(s)",
                            color = ScoreColors.WarningYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ScoreColors.GoodGreenBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "0 element issues",
                            color = ScoreColors.GoodGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElementPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    alert: Boolean = false,
    highlight: Boolean = false
) {
    val tint = when {
        alert -> ScoreColors.CriticalRed
        highlight -> ScoreColors.AccentTeal
        else -> Color(0xFF64748B)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (highlight || alert) FontWeight.Bold else FontWeight.Normal,
            color = if (alert) ScoreColors.CriticalRed else Color(0xFF334155)
        )
    }
}
