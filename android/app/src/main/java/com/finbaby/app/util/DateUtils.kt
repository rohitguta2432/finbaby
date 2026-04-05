package com.finbaby.app.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun getCurrentMonth(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    fun getSalaryCycleRange(salaryDate: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_MONTH)

        val start = Calendar.getInstance()
        val end = Calendar.getInstance()

        if (today >= salaryDate) {
            start.set(Calendar.DAY_OF_MONTH, salaryDate)
            end.add(Calendar.MONTH, 1)
            end.set(Calendar.DAY_OF_MONTH, salaryDate)
            end.add(Calendar.DAY_OF_MONTH, -1)
        } else {
            start.add(Calendar.MONTH, -1)
            start.set(Calendar.DAY_OF_MONTH, salaryDate)
            end.set(Calendar.DAY_OF_MONTH, salaryDate)
            end.add(Calendar.DAY_OF_MONTH, -1)
        }

        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        end.set(Calendar.HOUR_OF_DAY, 23)
        end.set(Calendar.MINUTE, 59)
        end.set(Calendar.SECOND, 59)
        end.set(Calendar.MILLISECOND, 999)

        return start.timeInMillis to end.timeInMillis
    }

    fun getMonthRange(monthStr: String): Pair<Long, Long> {
        val parts = monthStr.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() - 1

        val start = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            set(year, month, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return start.timeInMillis to end.timeInMillis
    }

    fun getDaysSinceSalary(salaryDate: Int): Int {
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_MONTH)
        return if (today >= salaryDate) {
            today - salaryDate
        } else {
            val daysInPrevMonth = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
            }.getActualMaximum(Calendar.DAY_OF_MONTH)
            daysInPrevMonth - salaryDate + today
        }
    }

    fun getDaysInCycle(salaryDate: Int): Int {
        val cal = Calendar.getInstance()
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun formatDate(epochMillis: Long): String {
        return SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(epochMillis))
    }

    fun formatDateTime(epochMillis: Long): String {
        return SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(Date(epochMillis))
    }

    fun formatTime(epochMillis: Long): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epochMillis))
    }

    fun isToday(epochMillis: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(epochMillis: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val cal2 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
