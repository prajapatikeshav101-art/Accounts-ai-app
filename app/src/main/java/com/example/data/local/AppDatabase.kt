package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CompanyEntity
import com.example.data.model.ItemEntity
import com.example.data.model.LedgerEntity
import com.example.data.model.LedgerGroupEntity
import com.example.data.model.StockTransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.VoucherEntity
import com.example.data.model.VoucherItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CompanyEntity::class,
        UserEntity::class,
        LedgerGroupEntity::class,
        LedgerEntity::class,
        ItemEntity::class,
        VoucherEntity::class,
        VoucherItemEntity::class,
        AuditLogEntity::class,
        StockTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun itemDao(): ItemDao
    abstract fun voucherDao(): VoucherDao
    abstract fun auditDao(): AuditDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prajapati_accounts.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedInitialData(database)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            // 1. Initial Company
            val companyId = db.companyDao().insertCompany(
                CompanyEntity(
                    name = "Keshav Traders",
                    businessType = "Wholesale & Electronics",
                    address = "Plot 42, GIDC Industrial Estate",
                    city = "Ahmedabad",
                    state = "Gujarat",
                    pinCode = "380015",
                    mobileNumber = "+91 98765 43210",
                    email = "keshav.traders@example.com",
                    gstin = "24AAACK1234F1Z8",
                    pan = "AAACK1234F",
                    financialYear = "2025-2026",
                    booksBeginningFrom = "01-04-2025",
                    currency = "₹",
                    invoicePrefix = "KT-",
                    nextInvoiceNumber = 1004,
                    bankName = "State Bank of India",
                    bankAccountNo = "38920192831",
                    bankIfsc = "SBIN0004567",
                    bankBranch = "Ashram Road",
                    upiId = "keshavtraders@sbi",
                    isDefault = true
                )
            )

            // Second demo company for instant multi-company switching
            db.companyDao().insertCompany(
                CompanyEntity(
                    name = "KP Electronics",
                    businessType = "Retail Electronics & Appliances",
                    address = "Shop 12, Galaxy Market",
                    city = "Surat",
                    state = "Gujarat",
                    pinCode = "395003",
                    mobileNumber = "+91 98250 12345",
                    email = "kpelectronics@example.com",
                    gstin = "24AABCK5678G1Z2",
                    pan = "AABCK5678G",
                    financialYear = "2025-2026",
                    invoicePrefix = "KPE-",
                    nextInvoiceNumber = 2001,
                    isDefault = false
                )
            )

            // 2. Initial Users
            db.userDao().insertUser(
                UserEntity(
                    name = "Keshav Prajapati",
                    email = "keshavprajapati199@gmail.com",
                    mobile = "9876543210",
                    role = "Admin",
                    isActive = true
                )
            )
            db.userDao().insertUser(
                UserEntity(
                    name = "Ramesh Patel",
                    email = "ramesh.patel@example.com",
                    mobile = "9825123456",
                    role = "Accountant",
                    isActive = true
                )
            )
            db.userDao().insertUser(
                UserEntity(
                    name = "Pooja Sharma",
                    email = "pooja.sharma@example.com",
                    mobile = "9898012345",
                    role = "Sales User",
                    isActive = true
                )
            )

            // 3. System Groups for Keshav Traders
            val groups = listOf(
                LedgerGroupEntity(companyId = companyId, name = "Sundry Debtors", category = "ASSETS", isSystem = true),
                LedgerGroupEntity(companyId = companyId, name = "Sundry Creditors", category = "LIABILITIES", isSystem = true),
                LedgerGroupEntity(companyId = companyId, name = "Bank Accounts", category = "ASSETS", isSystem = true),
                LedgerGroupEntity(companyId = companyId, name = "Cash-in-Hand", category = "ASSETS", isSystem = true),
                LedgerGroupEntity(companyId = companyId, name = "Sales Accounts", category = "INCOME", isSystem = true),
                LedgerGroupEntity(companyId = companyId, name = "Purchase Accounts", category = "EXPENSES", isSystem = true),
                LedgerGroupEntity(companyId = companyId, name = "Direct Expenses", category = "EXPENSES", isSystem = true),
                LedgerGroupEntity(companyId = companyId, name = "Indirect Expenses", category = "EXPENSES", isSystem = true),
                LedgerGroupEntity(companyId = companyId, name = "Duties & Taxes", category = "LIABILITIES", isSystem = true),
                LedgerGroupEntity(companyId = companyId, name = "Capital Account", category = "LIABILITIES", isSystem = true)
            )
            groups.forEach { db.ledgerDao().insertGroup(it) }

            // 4. Default Ledgers
            val cashLedgerId = db.ledgerDao().insertLedger(
                LedgerEntity(
                    companyId = companyId,
                    name = "Cash Account",
                    groupName = "Cash-in-Hand",
                    partyType = "CASH",
                    openingBalance = 35000.0,
                    openingBalanceType = "DR",
                    currentBalance = 35000.0,
                    currentBalanceType = "DR"
                )
            )

            val sbiBankId = db.ledgerDao().insertLedger(
                LedgerEntity(
                    companyId = companyId,
                    name = "SBI Current A/C",
                    groupName = "Bank Accounts",
                    partyType = "BANK",
                    openingBalance = 185000.0,
                    openingBalanceType = "DR",
                    currentBalance = 185000.0,
                    currentBalanceType = "DR",
                    bankDetails = "A/C: 38920192831, IFSC: SBIN0004567"
                )
            )

            val hdfcBankId = db.ledgerDao().insertLedger(
                LedgerEntity(
                    companyId = companyId,
                    name = "HDFC Bank A/C",
                    groupName = "Bank Accounts",
                    partyType = "BANK",
                    openingBalance = 75000.0,
                    openingBalanceType = "DR",
                    currentBalance = 75000.0,
                    currentBalanceType = "DR",
                    bankDetails = "A/C: 5020002938192, IFSC: HDFC0001024"
                )
            )

            val customer1Id = db.ledgerDao().insertLedger(
                LedgerEntity(
                    companyId = companyId,
                    name = "Omkar Retail Store",
                    groupName = "Sundry Debtors",
                    partyType = "CUSTOMER",
                    mobileNumber = "+91 98790 11223",
                    email = "omkar.retail@example.com",
                    address = "14 Ring Road, Rajkot",
                    gstin = "24BBLPO8901D1ZS",
                    state = "Gujarat",
                    openingBalance = 18500.0,
                    openingBalanceType = "DR",
                    currentBalance = 18500.0,
                    creditLimit = 100000.0,
                    paymentTerms = "Net 15"
                )
            )

            val customer2Id = db.ledgerDao().insertLedger(
                LedgerEntity(
                    companyId = companyId,
                    name = "Shreeji Infotech",
                    groupName = "Sundry Debtors",
                    partyType = "CUSTOMER",
                    mobileNumber = "+91 97240 55667",
                    email = "shreeji.info@example.com",
                    address = "Crossroads Center, Vadodara",
                    gstin = "24ACIPM9988C1ZR",
                    state = "Gujarat",
                    openingBalance = 32000.0,
                    openingBalanceType = "DR",
                    currentBalance = 32000.0,
                    creditLimit = 150000.0,
                    paymentTerms = "Net 30"
                )
            )

            val supplier1Id = db.ledgerDao().insertLedger(
                LedgerEntity(
                    companyId = companyId,
                    name = "Apex Microelectronics India",
                    groupName = "Sundry Creditors",
                    partyType = "SUPPLIER",
                    mobileNumber = "+91 98110 44332",
                    email = "sales@apexmicro.in",
                    address = "Tech Hub, Mumbai",
                    gstin = "27AAACA9922K1Z5",
                    state = "Maharashtra",
                    openingBalance = 64000.0,
                    openingBalanceType = "CR",
                    currentBalance = 64000.0,
                    paymentTerms = "Net 21"
                )
            )

            val supplier2Id = db.ledgerDao().insertLedger(
                LedgerEntity(
                    companyId = companyId,
                    name = "Delta Cables & Power Ltd",
                    groupName = "Sundry Creditors",
                    partyType = "SUPPLIER",
                    mobileNumber = "+91 94260 77889",
                    email = "delta.cables@example.com",
                    address = "Industrial Zone, Vapi",
                    gstin = "24AABFD8833E1ZQ",
                    state = "Gujarat",
                    openingBalance = 24500.0,
                    openingBalanceType = "CR",
                    currentBalance = 24500.0,
                    paymentTerms = "Net 15"
                )
            )

            // Expense Ledgers
            db.ledgerDao().insertLedger(
                LedgerEntity(companyId = companyId, name = "Shop Rent", groupName = "Indirect Expenses", partyType = "EXPENSE")
            )
            db.ledgerDao().insertLedger(
                LedgerEntity(companyId = companyId, name = "Electricity Bill", groupName = "Indirect Expenses", partyType = "EXPENSE")
            )
            db.ledgerDao().insertLedger(
                LedgerEntity(companyId = companyId, name = "Staff Salary", groupName = "Indirect Expenses", partyType = "EXPENSE")
            )
            db.ledgerDao().insertLedger(
                LedgerEntity(companyId = companyId, name = "Transportation & Freight", groupName = "Direct Expenses", partyType = "EXPENSE")
            )

            // 5. Initial Inventory Items
            val item1Id = db.itemDao().insertItem(
                ItemEntity(
                    companyId = companyId,
                    name = "Smart Wireless Earbuds Pro",
                    itemCode = "EP-101",
                    sku = "EAR-PRO-BLK",
                    barcode = "8901234567890",
                    category = "Audio & Electronics",
                    brand = "AcoustiQ",
                    unit = "PCS",
                    hsnSac = "85183000",
                    gstRate = 18.0,
                    purchasePrice = 1250.0,
                    salesPrice = 1999.0,
                    mrp = 2499.0,
                    openingStock = 45.0,
                    currentStock = 45.0,
                    minStockLevel = 10.0,
                    warehouse = "Main Warehouse",
                    rack = "B-2"
                )
            )

            val item2Id = db.itemDao().insertItem(
                ItemEntity(
                    companyId = companyId,
                    name = "Ultra Fast USB-C Cable 65W (1.5m)",
                    itemCode = "CB-204",
                    sku = "CAB-65W-TYPC",
                    barcode = "8901234567891",
                    category = "Cables & Accessories",
                    brand = "VoltCharge",
                    unit = "PCS",
                    hsnSac = "85444299",
                    gstRate = 18.0,
                    purchasePrice = 160.0,
                    salesPrice = 349.0,
                    mrp = 499.0,
                    openingStock = 120.0,
                    currentStock = 120.0,
                    minStockLevel = 25.0,
                    warehouse = "Main Warehouse",
                    rack = "A-3"
                )
            )

            val item3Id = db.itemDao().insertItem(
                ItemEntity(
                    companyId = companyId,
                    name = "Fast PowerBank 20000mAh Dual Port",
                    itemCode = "PB-305",
                    sku = "PB-20K-MET",
                    barcode = "8901234567892",
                    category = "Power & Batteries",
                    brand = "VoltCharge",
                    unit = "PCS",
                    hsnSac = "85076000",
                    gstRate = 18.0,
                    purchasePrice = 980.0,
                    salesPrice = 1599.0,
                    mrp = 1999.0,
                    openingStock = 4.0, // Triggers low stock alert
                    currentStock = 4.0,
                    minStockLevel = 8.0,
                    warehouse = "Main Warehouse",
                    rack = "C-1"
                )
            )

            val item4Id = db.itemDao().insertItem(
                ItemEntity(
                    companyId = companyId,
                    name = "Heavy Duty Surge Protector (6 Sockets)",
                    itemCode = "SP-402",
                    sku = "SP-6S-IND",
                    barcode = "8901234567893",
                    category = "Electricals",
                    brand = "SafeGrid",
                    unit = "PCS",
                    hsnSac = "85363000",
                    gstRate = 18.0,
                    purchasePrice = 420.0,
                    salesPrice = 750.0,
                    mrp = 950.0,
                    openingStock = 30.0,
                    currentStock = 30.0,
                    minStockLevel = 10.0,
                    warehouse = "Main Warehouse",
                    rack = "D-4"
                )
            )

            // 6. Initial Sample Vouchers (Sales & Purchases)
            val v1Id = db.voucherDao().insertVoucher(
                VoucherEntity(
                    companyId = companyId,
                    voucherType = "SALES",
                    voucherNumber = "KT-1001",
                    dateMillis = System.currentTimeMillis() - 86400000L * 2,
                    dateString = "15-09-2026",
                    partyLedgerId = customer1Id,
                    partyLedgerName = "Omkar Retail Store",
                    paymentMode = "CREDIT",
                    subTotal = 19990.0,
                    discountAmount = 990.0,
                    taxableAmount = 19000.0,
                    cgstAmount = 1710.0,
                    sgstAmount = 1710.0,
                    igstAmount = 0.0,
                    totalGst = 3420.0,
                    roundOff = 0.0,
                    grandTotal = 22420.0,
                    referenceNumber = "PO-882",
                    narration = "10 units Earbuds Pro supplied against purchase order"
                )
            )
            db.voucherDao().insertVoucherItems(
                listOf(
                    VoucherItemEntity(
                        voucherId = v1Id,
                        itemId = item1Id,
                        itemName = "Smart Wireless Earbuds Pro",
                        hsnSac = "85183000",
                        quantity = 10.0,
                        unit = "PCS",
                        rate = 1999.0,
                        discount = 990.0,
                        taxableAmount = 19000.0,
                        gstRate = 18.0,
                        cgst = 1710.0,
                        sgst = 1710.0,
                        igst = 0.0,
                        totalAmount = 22420.0
                    )
                )
            )

            val v2Id = db.voucherDao().insertVoucher(
                VoucherEntity(
                    companyId = companyId,
                    voucherType = "PURCHASE",
                    voucherNumber = "PUR-2041",
                    dateMillis = System.currentTimeMillis() - 86400000L * 5,
                    dateString = "12-09-2026",
                    partyLedgerId = supplier1Id,
                    partyLedgerName = "Apex Microelectronics India",
                    paymentMode = "CREDIT",
                    subTotal = 49000.0,
                    discountAmount = 0.0,
                    taxableAmount = 49000.0,
                    cgstAmount = 0.0,
                    sgstAmount = 0.0,
                    igstAmount = 8820.0, // Inter-state (Maharashtra -> Gujarat)
                    totalGst = 8820.0,
                    roundOff = 0.0,
                    grandTotal = 57820.0,
                    referenceNumber = "INV-7711",
                    narration = "50 units PowerBanks batch stock in"
                )
            )

            // Receipt voucher
            db.voucherDao().insertVoucher(
                VoucherEntity(
                    companyId = companyId,
                    voucherType = "RECEIPT",
                    voucherNumber = "REC-101",
                    dateMillis = System.currentTimeMillis() - 86400000L,
                    dateString = "16-09-2026",
                    partyLedgerId = customer1Id,
                    partyLedgerName = "Omkar Retail Store",
                    secondLedgerId = sbiBankId,
                    secondLedgerName = "SBI Current A/C",
                    paymentMode = "BANK",
                    grandTotal = 15000.0,
                    referenceNumber = "NEFT8921829",
                    narration = "Part payment received via NEFT against INV KT-1001"
                )
            )

            // Audit Log
            db.auditDao().insertLog(
                AuditLogEntity(
                    companyId = companyId,
                    action = "System Initialization",
                    userName = "Keshav Prajapati",
                    details = "Initialized company Keshav Traders with standard Chart of Accounts, initial stock, and ledgers."
                )
            )
        }
    }
}
