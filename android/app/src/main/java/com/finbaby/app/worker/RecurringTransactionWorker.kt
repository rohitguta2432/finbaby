package com.finbaby.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.finbaby.app.data.db.FinBabyDatabase
import com.finbaby.app.data.db.entity.TransactionEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: FinBabyDatabase
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "recurring_transaction_check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
                1, TimeUnit.DAYS
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val recurringTransactions = database.transactionDao().getRecurringTransactions()
            val today = Calendar.getInstance()

            for (txn in recurringTransactions) {
                if (isDueToday(txn, today)) {
                    val newTransaction = txn.copy(
                        id = 0,
                        date = today.timeInMillis,
                        isRecurring = false,
                        recurringPeriod = null,
                        createdAt = System.currentTimeMillis()
                    )
                    database.transactionDao().insert(newTransaction)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun isDueToday(txn: TransactionEntity, today: Calendar): Boolean {
        val txnCal = Calendar.getInstance().apply { timeInMillis = txn.date }

        return when (txn.recurringPeriod) {
            "daily" -> true
            "weekly" -> {
                today.get(Calendar.DAY_OF_WEEK) == txnCal.get(Calendar.DAY_OF_WEEK)
            }
            "monthly" -> {
                val txnDay = txnCal.get(Calendar.DAY_OF_MONTH)
                val todayDay = today.get(Calendar.DAY_OF_MONTH)
                val maxDay = today.getActualMaximum(Calendar.DAY_OF_MONTH)
                todayDay == txnDay || (txnDay > maxDay && todayDay == maxDay)
            }
            else -> false
        }
    }
}
