package com.example.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.AccountingAuditIssue
import com.example.ui.AccountsViewModel
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun AiToolsScreen(
    viewModel: AccountsViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: AI Checker, 1: AI Chat Assistant

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_tools_screen")
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("AI Accounting Checker", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("AI Business Assistant", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        if (selectedTab == 0) {
            AiCheckerContent(viewModel)
        } else {
            AiAssistantChatContent(viewModel)
        }
    }
}

@Composable
fun AiCheckerContent(viewModel: AccountsViewModel) {
    val issues by viewModel.auditIssues.collectAsStateWithLifecycle()

    val criticalCount = issues.count { it.severity == "CRITICAL" }
    val warningCount = issues.count { it.severity == "WARNING" }
    val infoCount = issues.count { it.severity == "INFO" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Automated Books Verification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (issues.isEmpty()) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (issues.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (issues.isEmpty()) SuccessGreen else WarningAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scans vouchers, GST computations, stock ledgers, and party balances in real-time. Discrepancies are presented for your review.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AuditBadge("Critical", criticalCount, Color(0xFFFFEBEE), Color(0xFFC62828), Modifier.weight(1f))
                        AuditBadge("Warnings", warningCount, Color(0xFFFFF3E0), Color(0xFFE65100), Modifier.weight(1f))
                        AuditBadge("Info / Notice", infoCount, Color(0xFFE3F2FD), Color(0xFF1565C0), Modifier.weight(1f))
                    }
                }
            }
        }

        if (issues.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("All Accounting Checks Passed!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF1B5E20))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No duplicate invoices, GST math mismatches, negative stocks, or orphaned transactions found.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                    }
                }
            }
        } else {
            items(issues, key = { it.id }) { issue ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audit_issue_${issue.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (issue.severity) {
                                    "CRITICAL" -> Color(0xFFFFEBEE)
                                    "WARNING" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFE3F2FD)
                                }
                            ) {
                                Text(
                                    text = "${issue.severity} • ${issue.category}",
                                    color = when (issue.severity) {
                                        "CRITICAL" -> Color(0xFFC62828)
                                        "WARNING" -> Color(0xFFE65100)
                                        else -> Color(0xFF1565C0)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = issue.voucherOrItemRef,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                        }

                        Text(
                            text = issue.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = issue.problem,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Expected vs Recorded table
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Recorded Value:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(issue.recorded, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Expected / Compliant:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(issue.expected, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                            }
                        }

                        // Suggested Action
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Action: ${issue.suggestedAction}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryNavy
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditBadge(label: String, count: Int, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count.toString(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = fg)
            Text(label, style = MaterialTheme.typography.labelSmall, color = fg)
        }
    }
}

@Composable
fun AiAssistantChatContent(viewModel: AccountsViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "Today's sales",
        "Pending payments",
        "GST summary",
        "Top selling items",
        "Duplicate invoices",
        "Stock status"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 2.dp,
                            bottomEnd = if (isUser) 2.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isUser) "You" else "🤖 Prajapati Accounts AI",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else PrimaryNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )

                            if (msg.quickActions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(msg.quickActions) { action ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.clickable { viewModel.sendChatMessage(action) }
                                        ) {
                                            Text(
                                                text = action,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                color = PrimaryNavy
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing financial books...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // Quick suggestions row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickQuestions) { q ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { viewModel.sendChatMessage(q) }
                ) {
                    Text(
                        text = q,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask about sales, GST, payments, inventory...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 2
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .clickable {
                        if (inputText.isNotBlank()) {
                            val txt = inputText
                            inputText = ""
                            viewModel.sendChatMessage(txt)
                        }
                    }
                    .testTag("btn_send_ai_chat")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
