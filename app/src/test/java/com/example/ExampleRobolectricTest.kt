package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.crawler.WebsiteCrawler
import com.example.model.AuditCategory
import com.example.model.AuditFix
import com.example.model.ScoreGrade
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SiteScope", appName)
    }

    @Test
    fun `verify self test catches deliberate issues with element proof`() {
        val crawler = WebsiteCrawler(null)
        val result = crawler.runSelfTest {}

        assertEquals(true, result.isSelfTest)
        assertEquals(5, result.totalRealIssuesCount)
        assertEquals(5, result.topFixes.size)

        // Verify each issue has non-empty exact element proof
        result.topFixes.forEach { fix ->
            assert(fix.exactElementProof.isNotBlank()) { "Fix ${fix.title} has empty element proof" }
        }

        // Verify known issues are caught
        val titles = result.topFixes.map { it.title }
        assert(titles.any { it.contains("Unclickable") }) { "Missing unclickable link fix" }
        assert(titles.any { it.contains("Missing alt") }) { "Missing alt text fix" }
        assert(titles.any { it.contains("Mismatched Button") }) { "Missing section CTA mismatch fix" }
        assert(titles.any { it.contains("Heavy Embedded Videos") }) { "Missing heavy video embeds fix" }
        assert(titles.any { it.contains("Sends Users Away") }) { "Missing form redirect fix" }
    }

    @Test
    fun `verify crawler on jctcreations catches printables button and heavy videos with zero fake alt`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val crawler = WebsiteCrawler(context)
        val result = crawler.auditWebsite("https://jctcreations.com", maxPages = 5) {}

        // 1. Verify educator section printables button is caught
        val ctaIssue = result.topFixes.find { it.title.contains("Mismatched Button in Educator Section") || it.exactElementProof.contains("printables") }
        assert(ctaIssue != null) { "Did not catch printables button in educator section: ${result.topFixes.map { it.title }}" }
        assert(ctaIssue?.exactElementProof?.contains("printables") == true)

        // 2. Verify heavy video embeds caught on /book
        val videoIssue = result.topFixes.find { it.title.contains("Heavy Embedded Videos") || it.exactElementProof.contains("youtube") }
        assert(videoIssue != null) { "Did not catch heavy YouTube embeds on /book" }
        assert(videoIssue?.exactElementProof?.contains("youtube") == true)

        val altIssues = result.topFixes.filter { it.category == AuditCategory.ACCESSIBILITY && it.title.contains("alt", ignoreCase = true) }
        assertEquals("Should not invent missing alt issues for jctcreations.com", 0, altIssues.size)

        // 4. Verify only real issues found message
        assert(result.totalRealIssuesCount in 2..4) { "Expected 2-4 real issues, found ${result.totalRealIssuesCount}" }
    }
}

