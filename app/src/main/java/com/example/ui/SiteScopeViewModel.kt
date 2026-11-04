package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAssistantUiState
import com.example.data.ai.AiContentAssistant
import com.example.data.crawler.WebsiteCrawler
import com.example.data.db.AuditDatabase
import com.example.data.db.AuditHistoryEntity
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String, val icon: String) {
    OVERVIEW("Overview", "dashboard"),
    TOP_FIXES("Top Fixes", "checklist"),
    PAGES("Pages", "pages"),
    TRAFFIC("Traffic (GSC)", "analytics"),
    HISTORY("History", "history")
}

enum class PageFilter(val label: String) {
    ALL("All Pages"),
    HIGH_TRAFFIC("High Traffic"),
    CRITICAL_ISSUES("Needs Fixes"),
    IGNORED("Ghost Pages (0 Clicks)")
}

class SiteScopeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AuditDatabase.getDatabase(application)
    private val crawler = WebsiteCrawler()
    private val aiAssistant = AiContentAssistant()

    // Navigation and UI state
    private val _currentTab = MutableStateFlow(AppNavTab.OVERVIEW)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    private val _targetUrlInput = MutableStateFlow("novacloud.io")
    val targetUrlInput: StateFlow<String> = _targetUrlInput.asStateFlow()

    private val _crawlProgress = MutableStateFlow(CrawlProgress())
    val crawlProgress: StateFlow<CrawlProgress> = _crawlProgress.asStateFlow()

    private val _currentAudit = MutableStateFlow<AuditResult?>(null)
    val currentAudit: StateFlow<AuditResult?> = _currentAudit.asStateFlow()

    private val _selectedPage = MutableStateFlow<PageAudit?>(null)
    val selectedPage: StateFlow<PageAudit?> = _selectedPage.asStateFlow()

    private val _aiAssistantState = MutableStateFlow<AiAssistantUiState>(AiAssistantUiState.Idle)
    val aiAssistantState: StateFlow<AiAssistantUiState> = _aiAssistantState.asStateFlow()

    private val _pageFilter = MutableStateFlow(PageFilter.ALL)
    val pageFilter: StateFlow<PageFilter> = _pageFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isGscGaConnected = MutableStateFlow(true)
    val isGscGaConnected: StateFlow<Boolean> = _isGscGaConnected.asStateFlow()

    val auditHistory: StateFlow<List<AuditHistoryEntity>> = db.auditDao().getAllHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Kick off default initial audit so the user immediately sees a rich, functional dashboard on first launch
        runAudit("novacloud.io")
    }

    fun setTargetUrlInput(url: String) {
        _targetUrlInput.value = url
    }

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun selectPage(page: PageAudit?) {
        _selectedPage.value = page
        _aiAssistantState.value = AiAssistantUiState.Idle
    }

    fun setPageFilter(filter: PageFilter) {
        _pageFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleGscGaConnected() {
        val nextState = !_isGscGaConnected.value
        _isGscGaConnected.value = nextState
        _currentAudit.value = _currentAudit.value?.let { current ->
            current.copy(gscGaMetrics = current.gscGaMetrics.copy(isConnected = nextState))
        }
    }

    fun runAudit(url: String) {
        val cleanedUrl = url.trim()
        if (cleanedUrl.isBlank()) return

        _targetUrlInput.value = cleanedUrl
        _selectedPage.value = null
        _aiAssistantState.value = AiAssistantUiState.Idle

        viewModelScope.launch {
            try {
                val result = crawler.auditWebsite(cleanedUrl, maxPages = 8) { progress ->
                    _crawlProgress.value = progress
                }
                _currentAudit.value = result

                // Persist to Room DB
                db.auditDao().insertAudit(
                    AuditHistoryEntity(
                        rootUrl = result.rootUrl,
                        domain = result.domain,
                        timestamp = result.timestamp,
                        overallScore = result.overallScore,
                        speedScore = result.speedScore,
                        seoScore = result.seoScore,
                        contentScore = result.contentScore,
                        uxScore = result.uxScore,
                        a11yScore = result.a11yScore,
                        techScore = result.techScore,
                        pageCount = result.pages.size,
                        criticalIssuesCount = result.pages.sumOf { p -> p.issues.count { it.severity == IssueSeverity.CRITICAL } }
                    )
                )
            } catch (e: Exception) {
                _crawlProgress.value = CrawlProgress(
                    isCrawling = false,
                    statusMessage = "Audit encountered an issue: ${e.message}"
                )
            }
        }
    }

    fun reScanCurrentWebsite() {
        val current = _currentAudit.value ?: return
        runAudit(current.rootUrl)
    }

    fun analyzeWithAi(page: PageAudit) {
        val domain = _currentAudit.value?.domain ?: "website.com"
        viewModelScope.launch {
            _aiAssistantState.value = AiAssistantUiState.Loading
            val outcome = aiAssistant.analyzePageContent(page, domain)
            outcome.fold(
                onSuccess = { analysis ->
                    _aiAssistantState.value = AiAssistantUiState.Success(analysis)
                },
                onFailure = { error ->
                    _aiAssistantState.value = AiAssistantUiState.Error(error.message ?: "AI Analysis failed.")
                }
            )
        }
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            db.auditDao().deleteAuditById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            db.auditDao().clearAll()
        }
    }
}
