package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*

object ScoreColors {
    val GoodGreen = Color(0xFF10B981)
    val GoodGreenBg = Color(0xFFECFDF5)
    val WarningYellow = Color(0xFFF59E0B)
    val WarningYellowBg = Color(0xFFFFFBEB)
    val CriticalRed = Color(0xFFEF4444)
    val CriticalRedBg = Color(0xFFFEF2F2)
    val DeepSlate = Color(0xFF0F172A)
    val SlateCard = Color(0xFF1E293B)
    val AccentTeal = Color(0xFF0D9488)

    fun forScore(score: Int): Color = when {
        score >= 80 -> GoodGreen
        score >= 50 -> WarningYellow
        else -> CriticalRed
    }

    fun bgForScore(score: Int): Color = when {
        score >= 80 -> GoodGreenBg
        score >= 50 -> WarningYellowBg
        else -> CriticalRedBg
    }
}

@Composable
fun ScoreGauge(
    score: Int,
    size: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 900),
        label = "scoreProgress"
    )
    val scoreColor = ScoreColors.forScore(score)
    val grade = ScoreGrade.fromScore(score)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1.0f },
            modifier = Modifier.fillMaxSize(),
            color = Color.LightGray.copy(alpha = 0.3f),
            strokeWidth = strokeWidth,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = scoreColor,
            strokeWidth = strokeWidth,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontSize = if (size > 100.dp) 32.sp else 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            GradeBadge(grade = grade)
        }
    }
}

@Composable
fun GradeBadge(
    grade: ScoreGrade,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (grade) {
        ScoreGrade.EXCELLENT, ScoreGrade.GOOD -> ScoreColors.GoodGreen to Color.White
        ScoreGrade.FAIR -> ScoreColors.WarningYellow to Color.White
        ScoreGrade.POOR, ScoreGrade.CRITICAL -> ScoreColors.CriticalRed to Color.White
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Grade ${grade.label}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun CategoryScoreBar(
    category: AuditCategory,
    score: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val scoreColor = ScoreColors.forScore(score)
    val progress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(600),
        label = "catProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (category) {
                        AuditCategory.SPEED -> Icons.Default.Speed
                        AuditCategory.SEO -> Icons.Default.Search
                        AuditCategory.CONTENT -> Icons.Default.Article
                        AuditCategory.USABILITY -> Icons.Default.Devices
                        AuditCategory.ACCESSIBILITY -> Icons.Default.AccessibilityNew
                        AuditCategory.TECHNICAL -> Icons.Default.Build
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = category.displayName,
                        tint = scoreColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.displayName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "$score/100",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = scoreColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = scoreColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun ImpactEffortChips(
    impact: ImpactLevel,
    effort: EffortLevel,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        val impactBg = when (impact) {
            ImpactLevel.HIGH -> ScoreColors.CriticalRedBg
            ImpactLevel.MEDIUM -> ScoreColors.WarningYellowBg
            ImpactLevel.LOW -> ScoreColors.GoodGreenBg
        }
        val impactText = when (impact) {
            ImpactLevel.HIGH -> ScoreColors.CriticalRed
            ImpactLevel.MEDIUM -> ScoreColors.WarningYellow
            ImpactLevel.LOW -> ScoreColors.GoodGreen
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(impactBg)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = impact.label,
                color = impactText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val effortBg = if (effort == EffortLevel.LOW) ScoreColors.GoodGreenBg else MaterialTheme.colorScheme.surfaceVariant
        val effortColor = if (effort == EffortLevel.LOW) ScoreColors.GoodGreen else MaterialTheme.colorScheme.onSurfaceVariant

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(effortBg)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = effort.label,
                color = effortColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CodeSnippetBox(
    codeSnippet: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ScoreColors.DeepSlate)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EXACT CODE FIX",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = ScoreColors.GoodGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = codeSnippet,
            color = Color(0xFF38BDF8),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun FixCard(
    fix: AuditFix,
    onCopySnippet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(ScoreColors.AccentTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${fix.rank}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = fix.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            ImpactEffortChips(impact = fix.impact, effort = fix.effort)

            Spacer(modifier = Modifier.height(10.dp))
            // What's wrong
            Text(
                text = "What's wrong:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ScoreColors.CriticalRed
            )
            Text(
                text = fix.whatIsWrong,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            // Why it matters
            Text(
                text = "Why it matters:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ScoreColors.WarningYellow
            )
            Text(
                text = fix.whyItMatters,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            // Exact fix
            Text(
                text = "Exact fix:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ScoreColors.GoodGreen
            )
            Text(
                text = fix.exactFix,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            if (!fix.codeSnippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                CodeSnippetBox(
                    codeSnippet = fix.codeSnippet,
                    onCopy = { onCopySnippet(fix.codeSnippet) }
                )
            }
        }
    }
}
