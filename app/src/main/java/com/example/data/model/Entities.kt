package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val businessType: String = "Wholesaler & Retailer",
    val address: String = "",
    val city: String = "",
    val state: String = "Gujarat",
    val pinCode: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val gstin: String = "",
    val pan: String = "",
    val financialYear: String = "2025-2026",
    val booksBeginningFrom: String = "01-04-2025",
    val currency: String = "₹",
    val invoicePrefix: String = "INV-",
    val nextInvoiceNumber: Int = 1001,
    val termsAndConditions: String = "Subject to local jurisdiction. Payment due in 15 days.",
    val bankName: String = "State Bank of India",
    val bankAccountNo: String = "30492819283",
    val bankIfsc: String = "SBIN0001234",
    val bankBranch: String = "Main Branch",
    val upiId: String = "keshav@sbi",
    val isDefault: Boolean = false
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val mobile: String,
    val role: String, // "Admin", "Accountant", "Sales User", "Purchase User", "Viewer"
    val isActive: Boolean = true
)

@Entity(tableName = "ledger_groups")
data class LedgerGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val name: String,
    val category: String, // "ASSETS", "LIABILITIES", "INCOME", "EXPENSES"
    val parentGroupName: String? = null,
    val isSystem: Boolean = false
)

@Entity(tableName = "ledgers")
data class LedgerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val name: String,
    val groupName: String, // "Sundry Debtors", "Sundry Creditors", "Bank Accounts", "Cash", etc.
    val partyType: String, // "CUSTOMER", "SUPPLIER", "BANK", "CASH", "EXPENSE", "INCOME", "CAPITAL", "OTHER"
    val mobileNumber: String = "",
    val email: String = "",
    val address: String = "",
    val gstin: String = "",
    val pan: String = "",
    val state: String = "Gujarat",
    val openingBalance: Double = 0.0,
    val openingBalanceType: String = "DR", // "DR", "CR"
    val currentBalance: Double = 0.0,
    val currentBalanceType: String = "DR", // "DR", "CR"
    val creditLimit: Double = 0.0,
    val paymentTerms: String = "Net 15",
    val bankDetails: String = "",
    val notes: String = "",
    val isDeleted: Boolean = false
)

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val name: String,
    val itemCode: String = "",
    val sku: String = "",
    val barcode: String = "",
    val category: String = "General",
    val brand: String = "",
    val unit: String = "PCS",
    val hsnSac: String = "",
    val gstRate: Double = 18.0,
    val purchasePrice: Double = 0.0,
    val salesPrice: Double = 0.0,
    val mrp: Double = 0.0,
    val openingStock: Double = 0.0,
    val currentStock: Double = 0.0,
    val minStockLevel: Double = 5.0,
    val maxStockLevel: Double = 100.0,
    val warehouse: String = "Main Warehouse",
    val rack: String = "A-1",
    val batch: String = "",
    val expiryDate: String = "",
    val isDeleted: Boolean = false
)

@Entity(tableName = "vouchers")
data class VoucherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val voucherType: String, // "SALES", "PURCHASE", "RECEIPT", "PAYMENT", "CONTRA", "JOURNAL", "SALES_RETURN", "PURCHASE_RETURN", "DEBIT_NOTE", "CREDIT_NOTE", "EXPENSE", "INCOME"
    val voucherNumber: String,
    val dateMillis: Long,
    val dateString: String,
    val partyLedgerId: Long,
    val partyLedgerName: String,
    val secondLedgerId: Long = 0,
    val secondLedgerName: String = "",
    val paymentMode: String = "CREDIT", // "CASH", "BANK", "UPI", "CARD", "CREDIT"
    val subTotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxableAmount: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val totalGst: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0,
    val referenceNumber: String = "",
    val narration: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "voucher_items")
data class VoucherItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voucherId: Long,
    val itemId: Long,
    val itemName: String,
    val hsnSac: String = "",
    val quantity: Double,
    val unit: String = "PCS",
    val rate: Double,
    val discount: Double = 0.0,
    val taxableAmount: Double,
    val gstRate: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double,
    val totalAmount: Double
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val action: String,
    val userName: String = "Keshav Prajapati",
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_transactions")
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val itemId: Long,
    val voucherId: Long = 0,
    val type: String, // "IN", "OUT", "ADJUSTMENT"
    val quantity: Double,
    val rate: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)
