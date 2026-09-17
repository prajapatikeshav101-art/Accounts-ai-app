package com.example.ui.navigation

object AppStrings {
    private val en = mapOf(
        "app_title" to "Prajapati Accounts AI",
        "tab_dashboard" to "Dashboard",
        "tab_transactions" to "Transactions",
        "tab_inventory" to "Inventory",
        "tab_reports" to "Reports",
        "tab_more" to "More",
        "today_sales" to "Today's Sales",
        "today_purchase" to "Today's Purchase",
        "today_receipt" to "Today's Receipt",
        "today_payment" to "Today's Payment",
        "cash_balance" to "Cash in Hand",
        "bank_balance" to "Bank Balance",
        "receivables" to "Total Receivable",
        "payables" to "Total Payable",
        "stock_value" to "Stock Valuation",
        "est_profit" to "Gross Profit",
        "quick_actions" to "Quick Actions",
        "action_sale" to "Sale",
        "action_purchase" to "Purchase",
        "action_receipt" to "Receipt",
        "action_payment" to "Payment",
        "action_customer" to "Add Party",
        "action_item" to "Add Item",
        "action_ai_check" to "AI Check",
        "action_ai_assistant" to "AI Assistant",
        "action_excel" to "Excel",
        "action_reports" to "Reports"
    )

    private val hi = mapOf(
        "app_title" to "प्रजापति एकाउंट्स AI",
        "tab_dashboard" to "डैशबोर्ड",
        "tab_transactions" to "लेन-देन",
        "tab_inventory" to "स्टॉक / माल",
        "tab_reports" to "रिपोर्ट्स",
        "tab_more" to "अन्य",
        "today_sales" to "आज की बिक्री (Sales)",
        "today_purchase" to "आज की खरीद (Purchase)",
        "today_receipt" to "आज की आवक (Receipt)",
        "today_payment" to "आज का भुगतान (Payment)",
        "cash_balance" to "कैश रोकड़ (Cash)",
        "bank_balance" to "बैंक बैलेंस",
        "receivables" to "लेना बाकी (उधारी)",
        "payables" to "देना बाकी",
        "stock_value" to "कुल स्टॉक मूल्य",
        "est_profit" to "अनुमानित मुनाफा",
        "quick_actions" to "त्वरित कार्य",
        "action_sale" to "बिक्री बिल",
        "action_purchase" to "खरीद बिल",
        "action_receipt" to "रसीद जमा",
        "action_payment" to "भुगतान",
        "action_customer" to "नया खाता",
        "action_item" to "नया आइटम",
        "action_ai_check" to "AI जांच",
        "action_ai_assistant" to "AI सहायक",
        "action_excel" to "एक्सेल",
        "action_reports" to "रिपोर्ट्स"
    )

    private val gu = mapOf(
        "app_title" to "પ્રજાપતિ એકાઉન્ટ્સ AI",
        "tab_dashboard" to "ડેશબોર્ડ",
        "tab_transactions" to "વ્યવહારો",
        "tab_inventory" to "સ્ટોક / માલ",
        "tab_reports" to "રિપોર્ટ્સ",
        "tab_more" to "વધુ",
        "today_sales" to "આજનું વેચાણ (Sales)",
        "today_purchase" to "આજની ખરીદી (Purchase)",
        "today_receipt" to "આજની આવક (Receipt)",
        "today_payment" to "આજની ચૂકવણી (Payment)",
        "cash_balance" to "હાથ પર રોકડ (Cash)",
        "bank_balance" to "બેંક બેલેન્સ",
        "receivables" to "લેવાના બાકી",
        "payables" to "ચૂકવવાના બાકી",
        "stock_value" to "સ્ટોક મૂલ્યાંકન",
        "est_profit" to "અંદાજિત નફો",
        "quick_actions" to "ઝડપી ક્રિયાઓ",
        "action_sale" to "વેચાણ બિલ",
        "action_purchase" to "ખરીદ બિલ",
        "action_receipt" to "જમા રસીદ",
        "action_payment" to "ચૂકવણી",
        "action_customer" to "નવું ખાતું",
        "action_item" to "નવી આઇટમ",
        "action_ai_check" to "AI તપાસ",
        "action_ai_assistant" to "AI સહાયક",
        "action_excel" to "એક્સેલ",
        "action_reports" to "રિપોર્ટ્સ"
    )

    fun get(key: String, language: String): String {
        val dict = when (language) {
            "हिन्दी (Hindi)" -> hi
            "ગુજરાતી (Gujarati)" -> gu
            else -> en
        }
        return dict[key] ?: en[key] ?: key
    }
}
