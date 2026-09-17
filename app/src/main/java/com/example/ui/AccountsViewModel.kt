package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiBusinessAssistant
import com.example.ai.ChatMessage
import com.example.data.importer.ExcelCsvManager
import com.example.data.importer.ImportResult
import com.example.data.local.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CompanyEntity
import com.example.data.model.ItemEntity
import com.example.data.model.LedgerEntity
import com.example.data.model.LedgerGroupEntity
import com.example.data.model.UserEntity
import com.example.data.model.VoucherEntity
import com.example.data.model.VoucherItemEntity
import com.example.data.repository.AccountsRepository
import com.example.domain.AccountingAuditIssue
import com.example.domain.AccountingEngine
import com.example.domain.AiAccountingChecker
import com.example.domain.DashboardSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AccountsRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AccountsRepository(db)
    }

    // App state
    val companies: StateFlow<List<CompanyEntity>> = repository.allCompanies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentCompanyId = MutableStateFlow<Long>(1L)
    val currentCompanyId = _currentCompanyId.asStateFlow()

    val currentCompany: StateFlow<CompanyEntity?> = combine(companies, currentCompanyId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Reactive streams based on currentCompanyId
    val ledgers: StateFlow<List<LedgerEntity>> = _currentCompanyId
        .flatMapLatest { id -> repository.getLedgers(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items: StateFlow<List<ItemEntity>> = _currentCompanyId
        .flatMapLatest { id -> repository.getItems(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vouchers: StateFlow<List<VoucherEntity>> = _currentCompanyId
        .flatMapLatest { id -> repository.getVouchers(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = _currentCompanyId
        .flatMapLatest { id -> repository.getAuditLogs(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Dashboard Summary
    val dashboardSummary: StateFlow<DashboardSummary> = combine(vouchers, ledgers, items) { v, l, i ->
        AccountingEngine.computeDashboardSummary(v, l, i)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // AI Checker Issues
    val auditIssues: StateFlow<List<AccountingAuditIssue>> = combine(vouchers, ledgers, items) { v, l, i ->
        AiAccountingChecker.runFullAudit(v, l, i)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Assistant Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "m1",
                sender = "AI",
                message = "👋 Namaste! I am your Prajapati Accounts AI Business Assistant.\n\nAsk me about today's sales, pending customer payments, GST collection, inventory levels, or top selling items.",
                quickActions = listOf("Today's sales", "Pending payments", "GST summary", "Top selling items")
            )
        )
    )
    val chatMessages = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking = _isAiThinking.asStateFlow()

    // Global Search & Language
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCompany(companyId: Long) {
        _currentCompanyId.value = companyId
    }

    fun createCompany(company: CompanyEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val newId = repository.insertCompany(company)
            _currentCompanyId.value = newId
            onComplete()
        }
    }

    fun updateCompany(company: CompanyEntity) {
        viewModelScope.launch {
            repository.updateCompany(company)
        }
    }

    // Ledger Actions
    fun saveLedger(ledger: LedgerEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val compId = _currentCompanyId.value
            if (ledger.id == 0L) {
                repository.insertLedger(ledger.copy(companyId = compId, currentBalance = ledger.openingBalance))
                repository.insertAuditLog(
                    AuditLogEntity(
                        companyId = compId,
                        action = "Create Ledger",
                        details = "Created ${ledger.partyType} account: ${ledger.name} with opening balance ₹${ledger.openingBalance}"
                    )
                )
            } else {
                repository.updateLedger(ledger)
                repository.insertAuditLog(
                    AuditLogEntity(
                        companyId = compId,
                        action = "Update Ledger",
                        details = "Updated account: ${ledger.name}"
                    )
                )
            }
            onComplete()
        }
    }

    fun deleteLedger(id: Long) {
        viewModelScope.launch {
            repository.deleteLedger(id)
        }
    }

    // Item Actions
    fun saveItem(item: ItemEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val compId = _currentCompanyId.value
            if (item.id == 0L) {
                repository.insertItem(item.copy(companyId = compId, currentStock = item.openingStock))
                repository.insertAuditLog(
                    AuditLogEntity(
                        companyId = compId,
                        action = "Create Item",
                        details = "Added product: ${item.name} (${item.itemCode}) with opening stock ${item.openingStock} ${item.unit}"
                    )
                )
            } else {
                repository.updateItem(item)
                repository.insertAuditLog(
                    AuditLogEntity(
                        companyId = compId,
                        action = "Update Item",
                        details = "Updated product: ${item.name}"
                    )
                )
            }
            onComplete()
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.deleteItem(id)
        }
    }

    fun adjustStock(itemId: Long, qty: Double, notes: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.adjustStock(itemId, qty, _currentCompanyId.value, notes)
            onComplete()
        }
    }

    // Voucher Creation with Double Entry
    fun createVoucher(
        voucher: VoucherEntity,
        items: List<VoucherItemEntity>,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val compId = _currentCompanyId.value
            val vId = repository.saveVoucherWithItems(voucher.copy(companyId = compId), items)
            onComplete(vId)
        }
    }

    fun cancelVoucher(voucherId: Long) {
        viewModelScope.launch {
            repository.cancelVoucher(voucherId)
        }
    }

    // AI Assistant
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(id = System.currentTimeMillis().toString(), sender = "USER", message = text)
        _chatMessages.value = _chatMessages.value + userMsg

        _isAiThinking.value = true
        viewModelScope.launch {
            val comp = currentCompany.value ?: CompanyEntity(name = "Keshav Traders")
            val answer = AiBusinessAssistant.answerQuery(
                prompt = text,
                company = comp,
                vouchers = vouchers.value,
                ledgers = ledgers.value,
                items = items.value
            )
            _chatMessages.value = _chatMessages.value + ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                sender = "AI",
                message = answer
            )
            _isAiThinking.value = false
        }
    }

    // Excel / CSV Import
    fun importLedgersFromCsv(csvText: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val result: ImportResult = ExcelCsvManager.analyzeAndValidateLedgers(csvText, _currentCompanyId.value)
            val compId = _currentCompanyId.value
            val toInsert = result.previewRows.filter { it.isValid }.map {
                val m = it.mappedValues
                LedgerEntity(
                    companyId = compId,
                    name = m["Name"] ?: "Ledger",
                    groupName = m["Group"] ?: "Sundry Debtors",
                    partyType = m["Type"] ?: "CUSTOMER",
                    mobileNumber = m["Mobile"] ?: "",
                    gstin = m["GSTIN"] ?: "",
                    openingBalance = m["Opening Balance"]?.toDoubleOrNull() ?: 0.0,
                    currentBalance = m["Opening Balance"]?.toDoubleOrNull() ?: 0.0
                )
            }
            repository.insertLedgers(toInsert)
            repository.insertAuditLog(
                AuditLogEntity(
                    companyId = compId,
                    action = "Excel Import Ledgers",
                    details = "Imported ${toInsert.size} ledger accounts from file"
                )
            )
            onComplete(toInsert.size)
        }
    }

    fun importItemsFromCsv(csvText: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val result = ExcelCsvManager.analyzeAndValidateItems(csvText, _currentCompanyId.value)
            val compId = _currentCompanyId.value
            val toInsert = result.previewRows.filter { it.isValid }.map {
                val m = it.mappedValues
                ItemEntity(
                    companyId = compId,
                    name = m["Item Name"] ?: "Item",
                    itemCode = m["Code"] ?: "",
                    unit = m["Unit"] ?: "PCS",
                    hsnSac = m["HSN"] ?: "",
                    gstRate = m["GST Rate"]?.toDoubleOrNull() ?: 18.0,
                    purchasePrice = m["Purchase Price"]?.toDoubleOrNull() ?: 0.0,
                    salesPrice = m["Sales Price"]?.toDoubleOrNull() ?: 0.0,
                    openingStock = m["Opening Stock"]?.toDoubleOrNull() ?: 0.0,
                    currentStock = m["Opening Stock"]?.toDoubleOrNull() ?: 0.0
                )
            }
            repository.insertItems(toInsert)
            repository.insertAuditLog(
                AuditLogEntity(
                    companyId = compId,
                    action = "Excel Import Items",
                    details = "Imported ${toInsert.size} inventory items from file"
                )
            )
            onComplete(toInsert.size)
        }
    }

    // User Management
    fun createUser(user: UserEntity) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }

    fun toggleUserStatus(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user.copy(isActive = !user.isActive))
        }
    }
}
