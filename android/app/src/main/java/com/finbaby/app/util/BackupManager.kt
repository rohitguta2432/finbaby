package com.finbaby.app.util

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.finbaby.app.data.db.FinBabyDatabase
import com.finbaby.app.data.db.entity.BudgetEntity
import com.finbaby.app.data.db.entity.CategoryEntity
import com.finbaby.app.data.db.entity.ProfileEntity
import com.finbaby.app.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import java.io.File

data class FinBabyBackupData(
    val transactions: List<TransactionEntity>,
    val categories: List<CategoryEntity>,
    val budgets: List<BudgetEntity>,
    val profile: ProfileEntity?,
    val backupTimestamp: Long = System.currentTimeMillis()
)

object BackupManager {

    private val gson = Gson()
    private const val BACKUP_FILE_NAME = "finbaby_backup.json"

    suspend fun backup(database: FinBabyDatabase, context: Context): String {
        val transactions = database.transactionDao().getAllFlow().first()
        val categories = database.categoryDao().getAllCategories().first()
        val budgets = database.budgetDao().getBudgetsForMonth(DateUtils.getCurrentMonth()).first()
        val profile = database.profileDao().getProfileSync()

        val backupData = FinBabyBackupData(
            transactions = transactions,
            categories = categories,
            budgets = budgets,
            profile = profile
        )

        val json = gson.toJson(backupData)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val file = File(downloadsDir, BACKUP_FILE_NAME)
        file.writeText(json)

        return file.absolutePath
    }

    suspend fun restore(database: FinBabyDatabase, context: Context) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, BACKUP_FILE_NAME)

        if (!file.exists()) {
            throw IllegalStateException("Backup file not found at ${file.absolutePath}")
        }

        val json = file.readText()
        val backupData = gson.fromJson(json, FinBabyBackupData::class.java)

        // Restore categories
        backupData.categories.forEach { category ->
            try {
                database.categoryDao().insert(category)
            } catch (e: Exception) {
                database.categoryDao().update(category)
            }
        }

        // Restore transactions
        backupData.transactions.forEach { transaction ->
            try {
                database.transactionDao().insert(transaction)
            } catch (e: Exception) {
                database.transactionDao().update(transaction)
            }
        }

        // Restore budgets
        backupData.budgets.forEach { budget ->
            database.budgetDao().insert(budget)
        }

        // Restore profile
        backupData.profile?.let { profile ->
            try {
                database.profileDao().insert(profile)
            } catch (e: Exception) {
                database.profileDao().update(profile)
            }
        }
    }
}
