package com.example.model

enum class ScoreGrade(val label: String, val minScore: Int) {
    EXCELLENT("A", 90),
    GOOD("B", 80),
    FAIR("C", 65),
    POOR("D", 50),
    CRITICAL("F", 0);

    companion object {
        fun fromScore(score: Int): ScoreGrade = when {
            score >= 90 -> EXCELLENT
            score >= 80 -> GOOD
            score >= 65 -> FAIR
            score >= 50 -> POOR
            else -> CRITICAL
        }
    }
}

enum class AuditCategory(val displayName: String, val iconName: String) {
    SPEED("Speed & Vitals", "speed"),
    SEO("SEO & Search", "search"),
    CONTENT("Content & Readability", "article"),
    USABILITY("Design & UX", "devices"),
    ACCESSIBILITY("Accessibility", "accessibility"),
    TECHNICAL("Technical & Health", "build")
}

enum class ImpactLevel(val label: String) {
    HIGH("High Impact"),
    MEDIUM("Medium Impact"),
    LOW("Low Impact")
}

enum class EffortLevel(val label: String) {
    LOW("Quick Win (< 15m)"),
    MEDIUM("Moderate Effort"),
    HIGH("High Effort")
}

enum class IssueSeverity {
    CRITICAL,
    WARNING,
    INFO
}

enum class PageTrafficStatus(val label: String) {
    HIGH_TRAFFIC_LOW_SCORE("High Traffic, Low Score"),
    STAR_PERFORMER("Star Performer"),
    HIDDEN_GEM("Hidden Gem"),
    IGNORED_PAGE("Ignored / Zero Clicks")
}

data class CoreWebVitals(
    val lcpSeconds: Double,
    val clsScore: Double,
    val inpMs: Int,
    val mobileScore: Int,
    val desktopScore: Int
) {
    val lcpStatus: String
        get() = when {
            lcpSeconds <= 2.5 -> "Good"
            lcpSeconds <= 4.0 -> "Needs Work"
            else -> "Poor"
        }

    val clsStatus: String
        get() = when {
            clsScore <= 0.1 -> "Good"
            clsScore <= 0.25 -> "Needs Work"
            else -> "Poor"
        }

    val inpStatus: String
        get() = when {
            inpMs <= 200 -> "Good"
            inpMs <= 500 -> "Needs Work"
            else -> "Poor"
        }
}

data class AuditIssue(
    val id: String,
    val title: String,
    val category: AuditCategory,
    val severity: IssueSeverity,
    val whatIsWrong: String,
    val whyItMatters: String,
    val exactFix: String,
    val codeSnippet: String? = null,
    val impact: ImpactLevel = ImpactLevel.MEDIUM,
    val effort: EffortLevel = EffortLevel.LOW,
    val pageUrl: String? = null
)

data class AuditFix(
    val id: String,
    val rank: Int,
    val title: String,
    val category: AuditCategory,
    val impact: ImpactLevel,
    val effort: EffortLevel,
    val whatIsWrong: String,
    val whyItMatters: String,
    val exactFix: String,
    val codeSnippet: String? = null,
    val affectedUrl: String = "/"
)

data class WorkingAspect(
    val title: String,
    val description: String,
    val category: AuditCategory,
    val benefit: String
)

data class PageAudit(
    val url: String,
    val path: String,
    val title: String,
    val statusCode: Int,
    val loadTimeMs: Long,
    val overallScore: Int,
    val speedScore: Int,
    val seoScore: Int,
    val contentScore: Int,
    val uxScore: Int,
    val a11yScore: Int,
    val techScore: Int,
    val wordCount: Int,
    val readabilityEase: Int,
    val readabilityGrade: String,
    val toneSummary: String,
    val ctaClarity: String,
    val primaryCtaText: String?,
    val hasMetaDescription: Boolean,
    val hasSingleH1: Boolean,
    val missingAltCount: Int,
    val brokenLinksCount: Int,
    val isHttps: Boolean,
    val isIndexed: Boolean,
    val coreWebVitals: CoreWebVitals,
    val organicClicks: Int,
    val impressions: Int,
    val avgPosition: Double,
    val monthlySessions: Int,
    val bounceRatePercent: Double,
    val trafficStatus: PageTrafficStatus,
    val issues: List<AuditIssue> = emptyList()
)

data class ChannelShare(
    val channel: String,
    val percentage: Int
)

data class GscGaMetrics(
    val isConnected: Boolean,
    val totalClicks: Int,
    val totalImpressions: Int,
    val avgCtr: Double,
    val avgPosition: Double,
    val monthlySessions: Int,
    val bounceRate: Double,
    val avgDurationSeconds: Int,
    val topChannels: List<ChannelShare> = emptyList()
)

data class SentenceRewrite(
    val original: String,
    val improved: String,
    val reason: String
)

data class KeywordSuggestion(
    val keyword: String,
    val intent: String,
    val recommendedPlacement: String
)

data class CtaEnhancement(
    val currentCta: String,
    val strongerCta: String,
    val psychology: String
)

data class AiContentAnalysis(
    val pageUrl: String,
    val readabilityScore: Int,
    val readabilitySummary: String,
    val tone: String,
    val ctaClarity: String,
    val sentenceRewrites: List<SentenceRewrite> = emptyList(),
    val keywordSuggestions: List<KeywordSuggestion> = emptyList(),
    val ctaEnhancements: List<CtaEnhancement> = emptyList()
)

data class AuditResult(
    val id: String,
    val rootUrl: String,
    val domain: String,
    val timestamp: Long,
    val overallScore: Int,
    val speedScore: Int,
    val seoScore: Int,
    val contentScore: Int,
    val uxScore: Int,
    val a11yScore: Int,
    val techScore: Int,
    val crawledPagesCount: Int,
    val totalDiscoveredCount: Int,
    val hasRobotsTxt: Boolean,
    val hasSitemap: Boolean,
    val isHttps: Boolean,
    val pages: List<PageAudit>,
    val topFixes: List<AuditFix>,
    val whatsWorking: List<WorkingAspect>,
    val gscGaMetrics: GscGaMetrics
)

data class CrawlProgress(
    val isCrawling: Boolean = false,
    val currentUrl: String = "",
    val pagesScanned: Int = 0,
    val totalDiscovered: Int = 0,
    val statusMessage: String = "",
    val progressPercent: Float = 0f
)

data class HistoryScanItem(
    val id: Long,
    val rootUrl: String,
    val timestamp: Long,
    val overallScore: Int,
    val speedScore: Int,
    val seoScore: Int,
    val contentScore: Int,
    val uxScore: Int,
    val a11yScore: Int,
    val techScore: Int,
    val pageCount: Int,
    val criticalIssuesCount: Int,
    val scoreDelta: Int? = null
)
