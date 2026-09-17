package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndianRupeeGold
import com.example.ui.theme.SuccessGreen

@Composable
fun SummaryMetricCard(
    title: String,
    amount: Double,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .testTag("metric_${title.replace(" ", "_").lowercase()}")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Surface(
                    shape = CircleShape,
                    color = bgColor,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹${String.format("%,.2f", amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .testTag("action_${label.replace(" ", "_").lowercase()}")
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = containerColor,
                modifier = Modifier.size(54.dp),
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = contentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            if (badgeCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = badgeCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun TrendMiniGraph(
    title: String,
    points: List<Float>,
    lineColor: Color,
    fillColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "7 Days Trend",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                if (points.isEmpty()) return@Canvas
                val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
                val widthPerPoint = size.width / (points.size - 1).coerceAtLeast(1)

                val path = Path()
                val fillPath = Path()

                points.forEachIndexed { i, p ->
                    val x = i * widthPerPoint
                    val y = size.height - (p / maxVal * (size.height * 0.8f)) - 10f
                    if (i == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, size.height)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                    // Draw node point
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                fillPath.lineTo(size.width, size.height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillColor.copy(alpha = 0.45f), fillColor.copy(alpha = 0.05f))
                    )
                )

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status.uppercase()) {
        "ACTIVE" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "CANCELLED" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
        "DR" -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
        "CR" -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
        "SALES" -> Pair(Color(0xFFE0F2F1), Color(0xFF00796B))
        "PURCHASE" -> Pair(Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        "RECEIPT" -> Pair(Color(0xFFE8F5E9), Color(0xFF388E3C))
        "PAYMENT" -> Pair(Color(0xFFFFEBEE), Color(0xFFD32F2F))
        else -> Pair(Color(0xFFECEFF1), Color(0xFF455A64))
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = modifier
    ) {
        Text(
            text = status,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
