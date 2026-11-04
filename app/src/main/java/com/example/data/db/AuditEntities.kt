package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_history")
data class AuditHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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
    val pageCount: Int,
    val criticalIssuesCount: Int,
    val topFixSummaryJson: String = ""
)
