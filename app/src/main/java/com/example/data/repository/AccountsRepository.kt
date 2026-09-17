package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CompanyEntity
import com.example.data.model.ItemEntity
import com.example.data.model.LedgerEntity
import com.example.data.model.LedgerGroupEntity
import com.example.data.model.StockTransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.VoucherEntity
import com.example.data.model.VoucherItemEntity
import kotlinx.coroutines.flow.Flow

class AccountsRepository(private val db: AppDatabase) {

    // Companies
    val allCompanies: Flow<List<CompanyEntity>> = db.companyDao().getAllCompanies()

    suspend fun getCompanyById(id: Long): CompanyEntity? = db.companyDao().getCompanyById(id)
    suspend fun getDefaultCompany(): CompanyEntity? = db.companyDao().getDefaultCompany()
    suspend fun insertCompany(company: CompanyEntity): Long = db.companyDao().insertCompany(company)
    suspend fun updateCompany(company: CompanyEntity) = db.companyDao().updateCompany(company)
    suspend fun deleteCompany(company: CompanyEntity) = db.companyDao().deleteCompany(company)

    // Ledgers & Groups
    fun getLedgers(companyId: Long): Flow<List<LedgerEntity>> = db.ledgerDao().getLedgersByCompany(companyId)
    fun getParties(companyId: Long): Flow<List<LedgerEntity>> = db.ledgerDao().getPartiesByCompany(companyId)
    fun getGroups(companyId: Long): Flow<List<LedgerGroupEntity>> = db.ledgerDao().getGroupsByCompany(companyId)
    suspend fun getLedgerById(id: Long): LedgerEntity? = db.ledgerDao().getLedgerById(id)
    suspend fun insertLedger(ledger: LedgerEntity): Long = db.ledgerDao().insertLedger(ledger)
    suspend fun insertLedgers(ledgers: List<LedgerEntity>) = db.ledgerDao().insertLedgers(ledgers)
    suspend fun updateLedger(ledger: LedgerEntity) = db.ledgerDao().updateLedger(ledger)
    suspend fun deleteLedger(id: Long) = db.ledgerDao().softDeleteLedger(id)
    suspend fun insertGroup(group: LedgerGroupEntity): Long = db.ledgerDao().insertGroup(group)

    // Items & Inventory
    fun getItems(companyId: Long): Flow<List<ItemEntity>> = db.itemDao().getItemsByCompany(companyId)
    fun getLowStockItems(companyId: Long): Flow<List<ItemEntity>> = db.itemDao().getLowStockItems(companyId)
    suspend fun getItemById(id: Long): ItemEntity? = db.itemDao().getItemById(id)
    suspend fun getItemByBarcode(companyId: Long, barcode: String): ItemEntity? = db.itemDao().getItemByBarcode(companyId, barcode)
    suspend fun insertItem(item: ItemEntity): Long = db.itemDao().insertItem(item)
    suspend fun insertItems(items: List<ItemEntity>) = db.itemDao().insertItems(items)
    suspend fun updateItem(item: ItemEntity) = db.itemDao().updateItem(item)
    suspend fun deleteItem(id: Long) = db.itemDao().softDeleteItem(id)
    suspend fun adjustStock(id: Long, qty: Double, companyId: Long, notes: String = "") {
        db.itemDao().adjustStock(id, qty)
        db.itemDao().insertStockTransaction(
            StockTransactionEntity(
                companyId = companyId,
                itemId = id,
                type = if (qty >= 0) "IN" else "OUT",
                quantity = Math.abs(qty),
                rate = 0.0,
                notes = notes
            )
        )
    }

    // Vouchers & Double Entry
    fun getVouchers(companyId: Long): Flow<List<VoucherEntity>> = db.voucherDao().getVouchersByCompany(companyId)
    fun getVouchersByType(companyId: Long, type: String): Flow<List<VoucherEntity>> = db.voucherDao().getVouchersByType(companyId, type)
    fun getVouchersForLedger(companyId: Long, ledgerId: Long): Flow<List<VoucherEntity>> = db.voucherDao().getVouchersForLedger(companyId, ledgerId)
    suspend fun getVoucherById(id: Long): VoucherEntity? = db.voucherDao().getVoucherById(id)
    suspend fun getVoucherItems(voucherId: Long): List<VoucherItemEntity> = db.voucherDao().getItemsForVoucher(voucherId)
    fun getVoucherItemsFlow(voucherId: Long): Flow<List<VoucherItemEntity>> = db.voucherDao().getItemsForVoucherFlow(voucherId)
    suspend fun getVouchersInRange(companyId: Long, start: Long, end: Long): List<VoucherEntity> = db.voucherDao().getVouchersInRange(companyId, start, end)

    suspend fun saveVoucherWithItems(
        voucher: VoucherEntity,
        items: List<VoucherItemEntity>,
        actorName: String = "Keshav Prajapati"
    ): Long {
        val voucherId = db.voucherDao().insertVoucher(voucher)
        val preparedItems = items.map { it.copy(voucherId = voucherId) }
        db.voucherDao().insertVoucherItems(preparedItems)

        // Double-entry ledger updates & stock movements
        when (voucher.voucherType) {
            "SALES" -> {
                // Debit Customer
                val party = db.ledgerDao().getLedgerById(voucher.partyLedgerId)
                if (party != null) {
                    val updatedBal = party.currentBalance + voucher.grandTotal
                    db.ledgerDao().updateLedger(party.copy(currentBalance = updatedBal, currentBalanceType = "DR"))
                }
                // Reduce stock
                for (item in items) {
                    if (item.itemId > 0) {
                        db.itemDao().adjustStock(item.itemId, -item.quantity)
                        db.itemDao().insertStockTransaction(
                            StockTransactionEntity(
                                companyId = voucher.companyId,
                                itemId = item.itemId,
                                voucherId = voucherId,
                                type = "OUT",
                                quantity = item.quantity,
                                rate = item.rate,
                                notes = "Sale Invoice ${voucher.voucherNumber}"
                            )
                        )
                    }
                }
            }
            "PURCHASE" -> {
                // Credit Supplier
                val party = db.ledgerDao().getLedgerById(voucher.partyLedgerId)
                if (party != null) {
                    val updatedBal = party.currentBalance + voucher.grandTotal
                    db.ledgerDao().updateLedger(party.copy(currentBalance = updatedBal, currentBalanceType = "CR"))
                }
                // Increase stock
                for (item in items) {
                    if (item.itemId > 0) {
                        db.itemDao().adjustStock(item.itemId, item.quantity)
                        db.itemDao().insertStockTransaction(
                            StockTransactionEntity(
                                companyId = voucher.companyId,
                                itemId = item.itemId,
                                voucherId = voucherId,
                                type = "IN",
                                quantity = item.quantity,
                                rate = item.rate,
                                notes = "Purchase ${voucher.voucherNumber}"
                            )
                        )
                    }
                }
            }
            "RECEIPT" -> {
                // Credit Customer (reduces receivable)
                val customer = db.ledgerDao().getLedgerById(voucher.partyLedgerId)
                if (customer != null) {
                    val updated = customer.currentBalance - voucher.grandTotal
                    db.ledgerDao().updateLedger(customer.copy(currentBalance = updated))
                }
                // Debit Bank/Cash
                if (voucher.secondLedgerId > 0) {
                    val bank = db.ledgerDao().getLedgerById(voucher.secondLedgerId)
                    if (bank != null) {
                        db.ledgerDao().updateLedger(bank.copy(currentBalance = bank.currentBalance + voucher.grandTotal))
                    }
                }
            }
            "PAYMENT" -> {
                // Debit Supplier (reduces payable)
                val supplier = db.ledgerDao().getLedgerById(voucher.partyLedgerId)
                if (supplier != null) {
                    val updated = supplier.currentBalance - voucher.grandTotal
                    db.ledgerDao().updateLedger(supplier.copy(currentBalance = updated))
                }
                // Credit Bank/Cash
                if (voucher.secondLedgerId > 0) {
                    val bank = db.ledgerDao().getLedgerById(voucher.secondLedgerId)
                    if (bank != null) {
                        db.ledgerDao().updateLedger(bank.copy(currentBalance = bank.currentBalance - voucher.grandTotal))
                    }
                }
            }
            "CONTRA" -> {
                // Transfer from first to second (Debit To, Credit From)
                val fromLedger = db.ledgerDao().getLedgerById(voucher.partyLedgerId)
                val toLedger = db.ledgerDao().getLedgerById(voucher.secondLedgerId)
                if (fromLedger != null) {
                    db.ledgerDao().updateLedger(fromLedger.copy(currentBalance = fromLedger.currentBalance - voucher.grandTotal))
                }
                if (toLedger != null) {
                    db.ledgerDao().updateLedger(toLedger.copy(currentBalance = toLedger.currentBalance + voucher.grandTotal))
                }
            }
            "SALES_RETURN" -> {
                val customer = db.ledgerDao().getLedgerById(voucher.partyLedgerId)
                if (customer != null) {
                    db.ledgerDao().updateLedger(customer.copy(currentBalance = customer.currentBalance - voucher.grandTotal))
                }
                for (item in items) {
                    if (item.itemId > 0) {
                        db.itemDao().adjustStock(item.itemId, item.quantity)
                    }
                }
            }
            "PURCHASE_RETURN" -> {
                val supplier = db.ledgerDao().getLedgerById(voucher.partyLedgerId)
                if (supplier != null) {
                    db.ledgerDao().updateLedger(supplier.copy(currentBalance = supplier.currentBalance - voucher.grandTotal))
                }
                for (item in items) {
                    if (item.itemId > 0) {
                        db.itemDao().adjustStock(item.itemId, -item.quantity)
                    }
                }
            }
        }

        // Add audit trail entry
        db.auditDao().insertLog(
            AuditLogEntity(
                companyId = voucher.companyId,
                action = "Create ${voucher.voucherType}",
                userName = actorName,
                details = "Created ${voucher.voucherType} ${voucher.voucherNumber} for ₹${String.format("%.2f", voucher.grandTotal)} with ${voucher.partyLedgerName}"
            )
        )

        return voucherId
    }

    suspend fun cancelVoucher(voucherId: Long, actorName: String = "Keshav Prajapati") {
        val voucher = db.voucherDao().getVoucherById(voucherId) ?: return
        if (voucher.status == "CANCELLED") return

        db.voucherDao().cancelVoucher(voucherId)

        // Reverse effects
        when (voucher.voucherType) {
            "SALES" -> {
                val party = db.ledgerDao().getLedgerById(voucher.partyLedgerId)
                if (party != null) {
                    db.ledgerDao().updateLedger(party.copy(currentBalance = party.currentBalance - voucher.grandTotal))
                }
                val items = db.voucherDao().getItemsForVoucher(voucherId)
                for (item in items) {
                    if (item.itemId > 0) db.itemDao().adjustStock(item.itemId, item.quantity)
                }
            }
            "PURCHASE" -> {
                val party = db.ledgerDao().getLedgerById(voucher.partyLedgerId)
                if (party != null) {
                    db.ledgerDao().updateLedger(party.copy(currentBalance = party.currentBalance - voucher.grandTotal))
                }
                val items = db.voucherDao().getItemsForVoucher(voucherId)
                for (item in items) {
                    if (item.itemId > 0) db.itemDao().adjustStock(item.itemId, -item.quantity)
                }
            }
            "RECEIPT" -> {
                val customer = db.ledgerDao().getLedgerById(voucher.partyLedgerId)
                if (customer != null) db.ledgerDao().updateLedger(customer.copy(currentBalance = customer.currentBalance + voucher.grandTotal))
                if (voucher.secondLedgerId > 0) {
                    val bank = db.ledgerDao().getLedgerById(voucher.secondLedgerId)
                    if (bank != null) db.ledgerDao().updateLedger(bank.copy(currentBalance = bank.currentBalance - voucher.grandTotal))
                }
            }
            "PAYMENT" -> {
                val supplier = db.ledgerDao().getLedgerById(voucher.partyLedgerId)
                if (supplier != null) db.ledgerDao().updateLedger(supplier.copy(currentBalance = supplier.currentBalance + voucher.grandTotal))
                if (voucher.secondLedgerId > 0) {
                    val bank = db.ledgerDao().getLedgerById(voucher.secondLedgerId)
                    if (bank != null) db.ledgerDao().updateLedger(bank.copy(currentBalance = bank.currentBalance + voucher.grandTotal))
                }
            }
        }

        db.auditDao().insertLog(
            AuditLogEntity(
                companyId = voucher.companyId,
                action = "Cancel Voucher",
                userName = actorName,
                details = "Cancelled ${voucher.voucherType} ${voucher.voucherNumber} (Reversed ₹${String.format("%.2f", voucher.grandTotal)})"
            )
        )
    }

    // Audit Logs & Users
    fun getAuditLogs(companyId: Long): Flow<List<AuditLogEntity>> = db.auditDao().getLogsByCompany(companyId)
    val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsers()
    suspend fun insertUser(user: UserEntity): Long = db.userDao().insertUser(user)
    suspend fun updateUser(user: UserEntity) = db.userDao().updateUser(user)
    suspend fun insertAuditLog(log: AuditLogEntity) = db.auditDao().insertLog(log)
}
