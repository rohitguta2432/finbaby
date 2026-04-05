package com.finbaby.app.util

import android.content.Context
import android.os.Environment
import com.finbaby.app.data.db.entity.CategoryEntity
import com.finbaby.app.data.db.entity.TransactionEntity
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter

object CsvExporter {

    fun export(
        context: Context,
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>
    ): String {
        val categoryMap = categories.associateBy { it.id }

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val fileName = "finbaby_transactions_${System.currentTimeMillis()}.csv"
        val file = File(downloadsDir, fileName)

        CSVWriter(FileWriter(file)).use { writer ->
            // Header
            writer.writeNext(
                arrayOf(
                    "ID", "Date", "Type", "Amount", "Category",
                    "Account Type", "Note", "Recurring", "Recurring Period"
                )
            )

            // Rows
            transactions.forEach { txn ->
                val categoryName = txn.categoryId?.let { categoryMap[it]?.name } ?: "Uncategorized"
                writer.writeNext(
                    arrayOf(
                        txn.id.toString(),
                        DateUtils.formatDateTime(txn.date),
                        txn.type,
                        txn.amount.toString(),
                        categoryName,
                        txn.accountType,
                        txn.note,
                        txn.isRecurring.toString(),
                        txn.recurringPeriod ?: ""
                    )
                )
            }
        }

        return file.absolutePath
    }
}
