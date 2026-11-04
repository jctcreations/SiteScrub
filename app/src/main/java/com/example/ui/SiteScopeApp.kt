package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ScoreColors
import com.example.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteScopeApp(
    viewModel: SiteScopeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val targetUrlInput by viewModel.targetUrlInput.collectAsStateWithLifecycle()
    val crawlProgress by viewModel.crawlProgress.collectAsStateWithLifecycle()
    val currentAudit by viewModel.currentAudit.collectAsStateWithLifecycle()
    val selectedPage by viewModel.selectedPage.collectAsStateWithLifecycle()
    val aiState by viewModel.aiAssistantState.collectAsStateWithLifecycle()
    val pageFilter by viewModel.pageFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isGscConnected by viewModel.isGscGaConnected.collectAsStateWithLifecycle()
    val historyList by viewModel.auditHistory.collectAsStateWithLifecycle()

    // Handle back button when viewing page details
    BackHandler(enabled = selectedPage != null) {
        viewModel.selectPage(null)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = ScoreColors.AccentTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SiteScope",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            if (currentAudit != null) {
                                Text(
                                    text = currentAudit?.domain.orEmpty(),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (currentAudit != null) {
                        IconButton(onClick = { viewModel.reScanCurrentWebsite() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Re-scan Website")
                        }

                        IconButton(
                            onClick = {
                                val audit = currentAudit ?: return@IconButton
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        """
                                        SiteScope Website Audit Report
                                        Domain: ${audit.domain}
                                        Overall Score: ${audit.overallScore}/100
                                        Speed: ${audit.speedScore} | SEO: ${audit.seoScore} | UX: ${audit.uxScore} | Tech: ${audit.techScore}
                                        Top 5 Fixes:
                                        ${audit.topFixes.joinToString("\n") { "• ${it.title} (${it.impact.label})" }}
                                        """.trimIndent()
                                    )
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share SiteScope Audit")
                                context.startActivity(shareIntent)
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Report")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AppNavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab && selectedPage == null
                    val icon = when (tab) {
                        AppNavTab.OVERVIEW -> if (isSelected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
                        AppNavTab.TOP_FIXES -> if (isSelected) Icons.Filled.Checklist else Icons.Outlined.Checklist
                        AppNavTab.PAGES -> if (isSelected) Icons.Filled.AutoStories else Icons.Outlined.AutoStories
                        AppNavTab.TRAFFIC -> if (isSelected) Icons.Filled.Analytics else Icons.Outlined.Analytics
                        AppNavTab.HISTORY -> if (isSelected) Icons.Filled.History else Icons.Outlined.History
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            viewModel.selectPage(null)
                            viewModel.selectTab(tab)
                        },
                        icon = { Icon(icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedPage != null) {
                PageDetailScreen(
                    page = selectedPage!!,
                    aiState = aiState,
                    onBack = { viewModel.selectPage(null) },
                    onRunAiAnalysis = { viewModel.analyzeWithAi(selectedPage!!) },
                    onCopySnippet = { snippet -> viewModel.copyToClipboard("Code Snippet", snippet) }
                )
            } else {
                when (currentTab) {
                    AppNavTab.OVERVIEW -> {
                        OverviewScreen(
                            currentAudit = currentAudit,
                            crawlProgress = crawlProgress,
                            targetUrlInput = targetUrlInput,
                            onUrlChange = { viewModel.setTargetUrlInput(it) },
                            onRunAudit = { viewModel.runAudit(it) },
                            onReScan = { viewModel.reScanCurrentWebsite() },
                            onNavigateTab = { viewModel.selectTab(it) },
                            onCopySnippet = { snippet -> viewModel.copyToClipboard("Fix Snippet", snippet) }
                        )
                    }

                    AppNavTab.TOP_FIXES -> {
                        TopFixesScreen(
                            currentAudit = currentAudit,
                            onCopySnippet = { snippet -> viewModel.copyToClipboard("Fix Snippet", snippet) }
                        )
                    }

                    AppNavTab.PAGES -> {
                        PagesListScreen(
                            pages = currentAudit?.pages ?: emptyList(),
                            searchQuery = searchQuery,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            currentFilter = pageFilter,
                            onFilterChange = { viewModel.setPageFilter(it) },
                            onSelectPage = { viewModel.selectPage(it) }
                        )
                    }

                    AppNavTab.TRAFFIC -> {
                        TrafficScreen(
                            currentAudit = currentAudit,
                            isConnected = isGscConnected,
                            onToggleConnection = { viewModel.toggleGscGaConnected() },
                            onSelectPage = { viewModel.selectPage(it) }
                        )
                    }

                    AppNavTab.HISTORY -> {
                        HistoryScreen(
                            historyList = historyList,
                            onReScanUrl = { url ->
                                viewModel.selectTab(AppNavTab.OVERVIEW)
                                viewModel.runAudit(url)
                            },
                            onDeleteAudit = { id -> viewModel.deleteHistoryItem(id) },
                            onClearAll = { viewModel.clearAllHistory() }
                        )
                    }
                }
            }
        }
    }
}
