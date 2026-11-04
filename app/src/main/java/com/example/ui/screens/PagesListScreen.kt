package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.model.PageTrafficStatus
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
                PageFilter.HIGH_TRAFFIC -> page.organicClicks > 3000
                PageFilter.CRITICAL_ISSUES -> page.overallScore < 70 || page.issues.isNotEmpty()
                PageFilter.IGNORED -> page.trafficStatus == PageTrafficStatus.IGNORED_PAGE || page.organicClicks < 500
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
                    text = "Crawled Pages Report",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Individual page health, speed, and real search traffic metrics.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by URL path or title...") },
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
                text = "Showing ${filteredPages.size} of ${pages.size} pages",
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
                            text = "No pages matching your filter or search query.",
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

            // Badges row: Speed, Traffic status, and Issues
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Traffic status chip
                val (statusBg, statusTextColor) = when (page.trafficStatus) {
                    PageTrafficStatus.HIGH_TRAFFIC_LOW_SCORE -> ScoreColors.CriticalRedBg to ScoreColors.CriticalRed
                    PageTrafficStatus.STAR_PERFORMER -> ScoreColors.GoodGreenBg to ScoreColors.GoodGreen
                    PageTrafficStatus.HIDDEN_GEM -> Color(0xFFEFF6FF) to Color(0xFF2563EB)
                    PageTrafficStatus.IGNORED_PAGE -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = page.trafficStatus.label,
                        color = statusTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Clicks
                Text(
                    text = "${page.organicClicks} clicks/mo",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
