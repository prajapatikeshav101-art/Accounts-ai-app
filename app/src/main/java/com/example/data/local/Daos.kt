package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies ORDER BY id ASC")
    fun getAllCompanies(): Flow<List<CompanyEntity>>

    @Query("SELECT * FROM companies WHERE id = :id LIMIT 1")
    suspend fun getCompanyById(id: Long): CompanyEntity?

    @Query("SELECT * FROM companies WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultCompany(): CompanyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity): Long

    @Update
    suspend fun updateCompany(company: CompanyEntity)

    @Delete
    suspend fun deleteCompany(company: CompanyEntity)
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledgers WHERE companyId = :companyId AND isDeleted = 0 ORDER BY name ASC")
    fun getLedgersByCompany(companyId: Long): Flow<List<LedgerEntity>>

    @Query("SELECT * FROM ledgers WHERE companyId = :companyId AND isDeleted = 0 AND partyType IN ('CUSTOMER', 'SUPPLIER') ORDER BY name ASC")
    fun getPartiesByCompany(companyId: Long): Flow<List<LedgerEntity>>

    @Query("SELECT * FROM ledgers WHERE id = :id LIMIT 1")
    suspend fun getLedgerById(id: Long): LedgerEntity?

    @Query("SELECT * FROM ledgers WHERE companyId = :companyId AND name = :name AND isDeleted = 0 LIMIT 1")
    suspend fun getLedgerByName(companyId: Long, name: String): LedgerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedger(ledger: LedgerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgers(ledgers: List<LedgerEntity>)

    @Update
    suspend fun updateLedger(ledger: LedgerEntity)

    @Query("UPDATE ledgers SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteLedger(id: Long)

    @Query("SELECT * FROM ledger_groups WHERE companyId = :companyId ORDER BY name ASC")
    fun getGroupsByCompany(companyId: Long): Flow<List<LedgerGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: LedgerGroupEntity): Long
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE companyId = :companyId AND isDeleted = 0 ORDER BY name ASC")
    fun getItemsByCompany(companyId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): ItemEntity?

    @Query("SELECT * FROM items WHERE companyId = :companyId AND barcode = :barcode AND isDeleted = 0 LIMIT 1")
    suspend fun getItemByBarcode(companyId: Long, barcode: String): ItemEntity?

    @Query("SELECT * FROM items WHERE companyId = :companyId AND currentStock <= minStockLevel AND isDeleted = 0")
    fun getLowStockItems(companyId: Long): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Query("UPDATE items SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteItem(id: Long)

    @Query("UPDATE items SET currentStock = currentStock + :qty WHERE id = :id")
    suspend fun adjustStock(id: Long, qty: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransaction(tx: StockTransactionEntity)

    @Query("SELECT * FROM stock_transactions WHERE companyId = :companyId AND itemId = :itemId ORDER BY dateMillis DESC")
    fun getStockTransactions(companyId: Long, itemId: Long): Flow<List<StockTransactionEntity>>
}

@Dao
interface VoucherDao {
    @Query("SELECT * FROM vouchers WHERE companyId = :companyId ORDER BY dateMillis DESC, id DESC")
    fun getVouchersByCompany(companyId: Long): Flow<List<VoucherEntity>>

    @Query("SELECT * FROM vouchers WHERE companyId = :companyId AND voucherType = :type ORDER BY dateMillis DESC")
    fun getVouchersByType(companyId: Long, type: String): Flow<List<VoucherEntity>>

    @Query("SELECT * FROM vouchers WHERE id = :id LIMIT 1")
    suspend fun getVoucherById(id: Long): VoucherEntity?

    @Query("SELECT * FROM vouchers WHERE companyId = :companyId AND voucherNumber = :voucherNumber LIMIT 1")
    suspend fun getVoucherByNumber(companyId: Long, voucherNumber: String): VoucherEntity?

    @Query("SELECT * FROM vouchers WHERE companyId = :companyId AND (partyLedgerId = :ledgerId OR secondLedgerId = :ledgerId) AND status = 'ACTIVE' ORDER BY dateMillis ASC")
    fun getVouchersForLedger(companyId: Long, ledgerId: Long): Flow<List<VoucherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: VoucherEntity): Long

    @Update
    suspend fun updateVoucher(voucher: VoucherEntity)

    @Query("UPDATE vouchers SET status = 'CANCELLED' WHERE id = :id")
    suspend fun cancelVoucher(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucherItems(items: List<VoucherItemEntity>)

    @Query("SELECT * FROM voucher_items WHERE voucherId = :voucherId")
    suspend fun getItemsForVoucher(voucherId: Long): List<VoucherItemEntity>

    @Query("SELECT * FROM voucher_items WHERE voucherId = :voucherId")
    fun getItemsForVoucherFlow(voucherId: Long): Flow<List<VoucherItemEntity>>

    @Query("SELECT * FROM vouchers WHERE companyId = :companyId AND dateMillis >= :startMillis AND dateMillis <= :endMillis AND status = 'ACTIVE'")
    suspend fun getVouchersInRange(companyId: Long, startMillis: Long, endMillis: Long): List<VoucherEntity>
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs WHERE companyId = :companyId ORDER BY timestamp DESC LIMIT 150")
    fun getLogsByCompany(companyId: Long): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)
}
