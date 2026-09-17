package com.example.data.importer

import com.example.data.model.ItemEntity
import com.example.data.model.LedgerEntity
import com.example.data.model.VoucherEntity

data class ImportPreviewRow(
    val rowNumber: Int,
    val rawValues: Map<String, String>,
    val mappedValues: Map<String, String>,
    val isValid: Boolean,
    val validationMessage: String
)

data class ImportResult(
    val totalRows: Int,
    val validRows: Int,
    val invalidRows: Int,
    val previewRows: List<ImportPreviewRow>
)

object ExcelCsvManager {

    // Predefined templates
    val LEDGER_TEMPLATE = "Ledger Name,Group,Party Type,Mobile Number,Email,GSTIN,Opening Balance,Balance Type,State\n" +
            "Rohan Enterprise,Sundry Debtors,CUSTOMER,9876500001,rohan@example.com,24AAACR1234F1Z1,25000.0,DR,Gujarat\n" +
            "Bharat Steel Corp,Sundry Creditors,SUPPLIER,9876500002,bharat@example.com,24AAACB5678F1Z2,45000.0,CR,Gujarat"

    val ITEM_TEMPLATE = "Item Name,Item Code,Barcode,Category,Unit,HSN/SAC,GST Rate,Purchase Price,Sales Price,Opening Stock,Min Level\n" +
            "Wireless Mouse M100,WM-101,8909876543210,Peripherals,PCS,84716060,18.0,350.0,699.0,50.0,10.0\n" +
            "Mechanical Keyboard RGB,KB-201,8909876543211,Peripherals,PCS,84716060,18.0,1200.0,1999.0,20.0,5.0"

    val SALES_TEMPLATE = "Date,Invoice No,Customer Name,Item Name,Quantity,Rate,Discount,GST Rate\n" +
            "17-09-2026,INV-5001,Omkar Retail Store,Smart Wireless Earbuds Pro,5,1999.0,0.0,18.0"

    fun parseCsv(csvText: String): List<List<String>> {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val result = mutableListOf<List<String>>()
        for (line in lines) {
            val tokens = line.split(",").map { it.trim().trim('"', '\'') }
            result.add(tokens)
        }
        return result
    }

    fun analyzeAndValidateLedgers(csvText: String, companyId: Long): ImportResult {
        val parsed = parseCsv(csvText)
        if (parsed.isEmpty()) {
            return ImportResult(0, 0, 0, emptyList())
        }

        val headers = parsed.first()
        val dataRows = parsed.drop(1)
        val preview = mutableListOf<ImportPreviewRow>()

        var validCount = 0
        var invalidCount = 0

        dataRows.forEachIndexed { index, row ->
            val rawMap = mutableMapOf<String, String>()
            headers.forEachIndexed { hIdx, h ->
                rawMap[h] = if (hIdx < row.size) row[hIdx] else ""
            }

            val name = rawMap["Ledger Name"] ?: rawMap["Name"] ?: rawMap["Party Name"] ?: (if (row.isNotEmpty()) row[0] else "")
            val group = rawMap["Group"] ?: rawMap["Ledger Group"] ?: "Sundry Debtors"
            val type = rawMap["Party Type"] ?: rawMap["Type"] ?: "CUSTOMER"
            val mobile = rawMap["Mobile Number"] ?: rawMap["Mobile"] ?: ""
            val gstin = rawMap["GSTIN"] ?: ""
            val opBalStr = rawMap["Opening Balance"] ?: rawMap["Balance"] ?: "0.0"

            val opBal = opBalStr.toDoubleOrNull()
            val isValid = name.isNotBlank() && opBal != null

            val validationMsg = when {
                name.isBlank() -> "Missing required Party / Ledger Name."
                opBal == null -> "Invalid opening balance numeric format."
                else -> "Valid ledger ready for import."
            }

            if (isValid) validCount++ else invalidCount++

            preview.add(
                ImportPreviewRow(
                    rowNumber = index + 2,
                    rawValues = rawMap,
                    mappedValues = mapOf(
                        "Name" to name,
                        "Group" to group,
                        "Type" to type,
                        "Mobile" to mobile,
                        "GSTIN" to gstin,
                        "Opening Balance" to (opBal?.toString() ?: "0.0")
                    ),
                    isValid = isValid,
                    validationMessage = validationMsg
                )
            )
        }

        return ImportResult(dataRows.size, validCount, invalidCount, preview)
    }

    fun analyzeAndValidateItems(csvText: String, companyId: Long): ImportResult {
        val parsed = parseCsv(csvText)
        if (parsed.isEmpty()) {
            return ImportResult(0, 0, 0, emptyList())
        }

        val headers = parsed.first()
        val dataRows = parsed.drop(1)
        val preview = mutableListOf<ImportPreviewRow>()

        var validCount = 0
        var invalidCount = 0

        dataRows.forEachIndexed { index, row ->
            val rawMap = mutableMapOf<String, String>()
            headers.forEachIndexed { hIdx, h ->
                rawMap[h] = if (hIdx < row.size) row[hIdx] else ""
            }

            val name = rawMap["Item Name"] ?: rawMap["Name"] ?: (if (row.isNotEmpty()) row[0] else "")
            val code = rawMap["Item Code"] ?: rawMap["Code"] ?: "ITM-${index + 101}"
            val unit = rawMap["Unit"] ?: "PCS"
            val hsn = rawMap["HSN/SAC"] ?: rawMap["HSN"] ?: ""
            val gstRateStr = rawMap["GST Rate"] ?: rawMap["GST"] ?: "18.0"
            val pRateStr = rawMap["Purchase Price"] ?: rawMap["Purchase Rate"] ?: "0.0"
            val sRateStr = rawMap["Sales Price"] ?: rawMap["Sales Rate"] ?: "0.0"
            val stockStr = rawMap["Opening Stock"] ?: rawMap["Stock"] ?: "0.0"

            val pRate = pRateStr.toDoubleOrNull()
            val sRate = sRateStr.toDoubleOrNull()
            val stock = stockStr.toDoubleOrNull()
            val gst = gstRateStr.toDoubleOrNull()

            val isValid = name.isNotBlank() && pRate != null && sRate != null && stock != null

            val validationMsg = when {
                name.isBlank() -> "Item Name cannot be empty."
                sRate == null || pRate == null -> "Invalid purchase/sales price."
                stock == null -> "Invalid stock quantity."
                else -> "Valid item ready for import."
            }

            if (isValid) validCount++ else invalidCount++

            preview.add(
                ImportPreviewRow(
                    rowNumber = index + 2,
                    rawValues = rawMap,
                    mappedValues = mapOf(
                        "Item Name" to name,
                        "Code" to code,
                        "Unit" to unit,
                        "HSN" to hsn,
                        "GST Rate" to (gst?.toString() ?: "18.0"),
                        "Purchase Price" to (pRate?.toString() ?: "0.0"),
                        "Sales Price" to (sRate?.toString() ?: "0.0"),
                        "Opening Stock" to (stock?.toString() ?: "0.0")
                    ),
                    isValid = isValid,
                    validationMessage = validationMsg
                )
            )
        }

        return ImportResult(dataRows.size, validCount, invalidCount, preview)
    }

    fun exportLedgersToCsv(ledgers: List<LedgerEntity>): String {
        val sb = StringBuilder("Ledger Name,Group,Party Type,Mobile Number,Email,GSTIN,Current Balance,Balance Type,State\n")
        for (l in ledgers) {
            sb.append("\"${l.name}\",\"${l.groupName}\",\"${l.partyType}\",\"${l.mobileNumber}\",\"${l.email}\",\"${l.gstin}\",${l.currentBalance},${l.currentBalanceType},\"${l.state}\"\n")
        }
        return sb.toString()
    }

    fun exportItemsToCsv(items: List<ItemEntity>): String {
        val sb = StringBuilder("Item Name,Code,SKU,Barcode,Category,Unit,HSN/SAC,GST Rate,Purchase Price,Sales Price,Current Stock\n")
        for (i in items) {
            sb.append("\"${i.name}\",\"${i.itemCode}\",\"${i.sku}\",\"${i.barcode}\",\"${i.category}\",\"${i.unit}\",\"${i.hsnSac}\",${i.gstRate},${i.purchasePrice},${i.salesPrice},${i.currentStock}\n")
        }
        return sb.toString()
    }

    fun exportVouchersToCsv(vouchers: List<VoucherEntity>): String {
        val sb = StringBuilder("Voucher Type,Voucher No,Date,Party Name,Payment Mode,Taxable Amount,Total GST,Grand Total,Status\n")
        for (v in vouchers) {
            sb.append("\"${v.voucherType}\",\"${v.voucherNumber}\",\"${v.dateString}\",\"${v.partyLedgerName}\",\"${v.paymentMode}\",${v.taxableAmount},${v.totalGst},${v.grandTotal},\"${v.status}\"\n")
        }
        return sb.toString()
    }
}
