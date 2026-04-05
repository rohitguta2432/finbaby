package com.finbaby.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.finbaby.app.data.db.FinBabyDatabase
import com.finbaby.app.util.CurrencyFormatter
import com.finbaby.app.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class BudgetAlertWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: FinBabyDatabase
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "finbaby_budget_alerts"
        const val NOTIFICATION_ID = 2001
        const val WORK_NAME = "budget_alert_check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<BudgetAlertWorker>(
                6, TimeUnit.HOURS
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
            val currentMonth = DateUtils.getCurrentMonth()
            val (start, end) = DateUtils.getMonthRange(currentMonth)
            val budgets = database.budgetDao().getBudgetsForMonth(currentMonth).first()
            val categories = database.categoryDao().getAllCategories().first()
            val categoryMap = categories.associateBy { it.id }

            val alerts = mutableListOf<String>()

            for (budget in budgets) {
                val spent = database.transactionDao().getTotalExpenseSync(start, end) ?: 0.0
                val categorySpent = database.transactionDao()
                    .getTotalExpenseByCategory(budget.categoryId, start, end)
                    .first() ?: 0.0
                val percent = if (budget.limitAmount > 0) (categorySpent / budget.limitAmount * 100).toInt() else 0

                if (percent >= 80) {
                    val catName = categoryMap[budget.categoryId]?.name ?: "Unknown"
                    alerts.add("$catName: ${percent}% spent (${CurrencyFormatter.format(categorySpent)} / ${CurrencyFormatter.format(budget.limitAmount)})")
                }
            }

            if (alerts.isNotEmpty()) {
                sendNotification(alerts)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(alerts: List<String>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when category spending exceeds budget thresholds"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val body = if (alerts.size == 1) {
            alerts[0]
        } else {
            "${alerts.size} categories over 80% budget:\n${alerts.joinToString("\n")}"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Budget Alert")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
