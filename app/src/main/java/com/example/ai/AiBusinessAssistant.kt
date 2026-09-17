package com.example.ai

import com.example.BuildConfig
import com.example.data.model.CompanyEntity
import com.example.data.model.ItemEntity
import com.example.data.model.LedgerEntity
import com.example.data.model.VoucherEntity
import com.example.domain.AccountingEngine
import com.example.domain.AiAccountingChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quickActions: List<String> = emptyList()
)

object AiBusinessAssistant {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun answerQuery(
        prompt: String,
        company: CompanyEntity,
        vouchers: List<VoucherEntity>,
        ledgers: List<LedgerEntity>,
        items: List<ItemEntity>
    ): String = withContext(Dispatchers.IO) {
        val lower = prompt.lowercase().trim()

        // 1. Direct local business intelligence matching for exact live data queries
        if (lower.contains("today") && lower.contains("sale")) {
            val summary = AccountingEngine.computeDashboardSummary(vouchers, ledgers, items)
            return@withContext "📊 Today's Total Sales for **${company.name}** are **₹${String.format("%,.2f", summary.todaySales)}**.\n\n" +
                    "• Total today's purchase: ₹${String.format("%,.2f", summary.todayPurchase)}\n" +
                    "• Total today's receipts: ₹${String.format("%,.2f", summary.todayReceipt)}\n" +
                    "• Cash in hand: ₹${String.format("%,.2f", summary.cashBalance)}"
        }

        if (lower.contains("pending") || lower.contains("receivable") || lower.contains("due") || lower.contains("outstanding")) {
            val outstanding = AccountingEngine.getOutstandingReceivables(ledgers)
            if (outstanding.isEmpty()) {
                return@withContext "✅ Excellent! There are currently no outstanding receivables. All customer dues are fully cleared."
            }
            val sb = StringBuilder("📋 **Customers with Pending Payments (${outstanding.size})**:\n\n")
            var totalPending = 0.0
            outstanding.take(5).forEachIndexed { idx, o ->
                totalPending += o.balance
                sb.append("${idx + 1}. **${o.partyName}**: ₹${String.format("%,.2f", o.balance)} (Overdue ~${o.overdueDays} days)\n")
                if (o.mobile.isNotBlank()) sb.append("   📞 Mobile: ${o.mobile}\n")
            }
            sb.append("\n**Total Receivables Outstanding**: ₹${String.format("%,.2f", totalPending)}")
            return@withContext sb.toString()
        }

        if (lower.contains("gst") && (lower.contains("collect") || lower.contains("summary") || lower.contains("month") || lower.contains("tax"))) {
            val gst = AccountingEngine.generateGstSummary(vouchers)
            return@withContext "🇮🇳 **GST Summary for ${company.name}**:\n\n" +
                    "• Total Taxable Sales: ₹${String.format("%,.2f", gst.totalTaxableSales)}\n" +
                    "• **Output GST Collected**: ₹${String.format("%,.2f", gst.totalOutputGst)} " +
                    "(CGST: ₹${String.format("%,.2f", gst.outputCgst)}, SGST: ₹${String.format("%,.2f", gst.outputSgst)}, IGST: ₹${String.format("%,.2f", gst.outputIgst)})\n\n" +
                    "• Total Taxable Purchases: ₹${String.format("%,.2f", gst.totalTaxablePurchases)}\n" +
                    "• **Input Tax Credit (ITC)**: ₹${String.format("%,.2f", gst.totalInputGst)}\n\n" +
                    "👉 **Net GST Payable / (Credit)**: **₹${String.format("%,.2f", gst.netGstPayable)}**"
        }

        if (lower.contains("top") && (lower.contains("sell") || lower.contains("item") || lower.contains("product"))) {
            return@withContext "🏆 **Top Inventory Products by Value & Stock**:\n\n" +
                    items.sortedByDescending { it.salesPrice * it.currentStock }.take(5).mapIndexed { idx, itm ->
                        "${idx + 1}. **${itm.name}**\n   • Sales Rate: ₹${itm.salesPrice} | Current Stock: ${itm.currentStock} ${itm.unit} | Inventory Value: ₹${String.format("%,.2f", itm.currentStock * itm.purchasePrice)}"
                    }.joinToString("\n\n")
        }

        if (lower.contains("duplicate") || lower.contains("invoice")) {
            val issues = AiAccountingChecker.runFullAudit(vouchers, ledgers, items)
                .filter { it.category == "DUPLICATE" }
            if (issues.isEmpty()) {
                return@withContext "✅ **No duplicate invoices found!** All active invoices have unique reference numbers."
            } else {
                return@withContext "⚠️ **Duplicate Invoices Found**:\n\n" + issues.joinToString("\n\n") {
                    "• **${it.voucherOrItemRef}**: ${it.problem}\n  Action: ${it.suggestedAction}"
                }
            }
        }

        if (lower.contains("stock") && (lower.contains("negative") || lower.contains("low") || lower.contains("alert"))) {
            val lowItems = items.filter { it.currentStock <= it.minStockLevel }
            if (lowItems.isEmpty()) {
                return@withContext "✅ **Inventory Status Normal**: No negative or critically low stock items detected across warehouses."
            } else {
                return@withContext "⚠️ **Low / Critical Stock Items (${lowItems.size})**:\n\n" +
                        lowItems.joinToString("\n") {
                            "• **${it.name}**: Current ${it.currentStock} ${it.unit} (Min Level: ${it.minStockLevel} ${it.unit})"
                        } + "\n\nSuggested Action: Issue Purchase Invoices to replenish stock."
            }
        }

        if (lower.contains("expense") || lower.contains("spending")) {
            val expenses = vouchers.filter { it.voucherType == "EXPENSE" && it.status == "ACTIVE" }
            val totalExp = expenses.sumOf { it.grandTotal }
            return@withContext "💳 **Business Expenses**:\n\n" +
                    "• Total Recorded Expenses: ₹${String.format("%,.2f", totalExp)}\n" +
                    expenses.take(5).joinToString("\n") { "• ${it.partyLedgerName}: ₹${it.grandTotal} (${it.narration})" }
        }

        // 2. Try Gemini API for conversational AI analysis
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY") && !apiKey.contains("placeholder")) {
            try {
                val summary = AccountingEngine.computeDashboardSummary(vouchers, ledgers, items)
                val systemContext = "You are Prajapati Accounts AI, an expert Indian chartered accountant and business advisor for ${company.name} (${company.city}, ${company.state}). " +
                        "Current Financial Snapshot: Today's Sales: ₹${summary.todaySales}, Total Receivables: ₹${summary.totalReceivables}, " +
                        "Total Payables: ₹${summary.totalPayables}, Cash: ₹${summary.cashBalance}, Bank: ₹${summary.bankBalance}, Stock Value: ₹${summary.stockValuation}. " +
                        "Provide professional, concise, actionable financial advice following Indian accounting and GST rules."

                val jsonPayload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$systemContext\n\nUser Question: $prompt"))
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val text = json.optJSONArray("candidates")
                            ?.optJSONObject(0)
                            ?.optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.optJSONObject(0)
                            ?.optString("text")
                        if (!text.isNullOrBlank()) {
                            return@withContext text
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to intelligent local reasoning
            }
        }

        // Local comprehensive answer fallback
        val pnl = AccountingEngine.generateProfitLoss(vouchers, items)
        return@withContext "🤖 **Prajapati Accounts AI Insight**:\n\n" +
                "For **${company.name}**, your net business sales stand at **₹${String.format("%,.2f", pnl.netSales)}** with estimated Gross Profit of **₹${String.format("%,.2f", pnl.grossProfit)}**.\n\n" +
                "• Active transactions recorded: ${vouchers.size}\n" +
                "• Products in catalog: ${items.size}\n" +
                "• Ledger accounts: ${ledgers.size}\n\n" +
                "You can ask me specifically: 'Show today sales', 'Pending payments', 'GST summary', 'Top selling items', or 'Find duplicate invoices'."
    }
}
