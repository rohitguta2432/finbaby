package com.finbaby.app.util

data class SavingsTip(
    val title: String,
    val body: String,
    val type: TipType, // warning, tip, goal, win
    val actionLabel: String? = null
)

enum class TipType { WARNING, TIP, GOAL, WIN }

object TipsEngine {

    fun generateTips(
        salary: Double,
        totalExpense: Double,
        totalIncome: Double,
        emiTotal: Double,
        foodOrderingTotal: Double,
        foodBudget: Double,
        entertainmentTotal: Double,
        daysUnderBudget: Int,
        savingsGoal: Double,
        currentSavings: Double,
        daysLeftInCycle: Int
    ): List<SavingsTip> {
        val tips = mutableListOf<SavingsTip>()

        // EMI Health Check
        if (salary > 0 && emiTotal > 0) {
            val emiPercent = (emiTotal / salary * 100).toInt()
            if (emiPercent > 35) {
                tips.add(SavingsTip(
                    title = "EMI Alert",
                    body = "Your EMIs are $emiPercent% of your salary. Safe limit is 35%. Consider prioritizing high-interest debt.",
                    type = TipType.WARNING,
                    actionLabel = "View EMI Breakdown"
                ))
            }
        }

        // Food Ordering vs Budget
        if (foodBudget > 0 && foodOrderingTotal > foodBudget * 0.7) {
            val monthlySaving = (foodOrderingTotal * 0.4).toLong()
            val yearlySaving = monthlySaving * 12
            tips.add(SavingsTip(
                title = "Save ${CurrencyFormatter.format(monthlySaving.toDouble())}/month on Food",
                body = "You spent ${CurrencyFormatter.format(foodOrderingTotal)} on food ordering. Cooking 4 more meals/week could save ${CurrencyFormatter.format(yearlySaving.toDouble())}/year.",
                type = TipType.TIP
            ))
        }

        // Entertainment/Subscriptions
        if (entertainmentTotal > 1500) {
            tips.add(SavingsTip(
                title = "Review Subscriptions",
                body = "You spent ${CurrencyFormatter.format(entertainmentTotal)} on entertainment. Check if all subscriptions are being used.",
                type = TipType.TIP,
                actionLabel = "View Details"
            ))
        }

        // Savings Goal Progress
        if (savingsGoal > 0) {
            val percent = (currentSavings / savingsGoal * 100).toInt()
            tips.add(SavingsTip(
                title = "Emergency Fund Progress",
                body = "${CurrencyFormatter.format(currentSavings)} / ${CurrencyFormatter.format(savingsGoal)} saved ($percent%)",
                type = TipType.GOAL,
                actionLabel = "Add to savings"
            ))
        }

        // Over budget warning
        if (salary > 0 && daysLeftInCycle > 7) {
            val spentPercent = (totalExpense / salary * 100).toInt()
            if (spentPercent > 80) {
                tips.add(SavingsTip(
                    title = "Budget Warning",
                    body = "You've used $spentPercent% of your budget with $daysLeftInCycle days left. Try to limit spending.",
                    type = TipType.WARNING
                ))
            }
        }

        // Winning streak
        if (daysUnderBudget >= 7) {
            tips.add(SavingsTip(
                title = "Great job!",
                body = "$daysUnderBudget days under budget! Keep up the streak.",
                type = TipType.WIN
            ))
        }

        // Under spending celebration
        if (salary > 0 && totalExpense < salary * 0.5 && totalExpense > 0) {
            tips.add(SavingsTip(
                title = "Excellent Saver!",
                body = "You've only spent ${(totalExpense / salary * 100).toInt()}% of your salary. You're on track to save ${CurrencyFormatter.format(salary - totalExpense)} this month.",
                type = TipType.WIN
            ))
        }

        return tips
    }

    fun calculateMonthlyScore(
        salary: Double,
        totalExpense: Double,
        budgetsKept: Int,
        totalBudgets: Int
    ): Int {
        if (salary <= 0) return 5
        var score = 5

        // Savings ratio (up to 3 points)
        val savingsRatio = (salary - totalExpense) / salary
        score += when {
            savingsRatio >= 0.3 -> 3
            savingsRatio >= 0.2 -> 2
            savingsRatio >= 0.1 -> 1
            else -> 0
        }

        // Budget adherence (up to 2 points)
        if (totalBudgets > 0) {
            val adherenceRatio = budgetsKept.toFloat() / totalBudgets
            score += when {
                adherenceRatio >= 0.9 -> 2
                adherenceRatio >= 0.7 -> 1
                else -> 0
            }
        }

        return score.coerceIn(1, 10)
    }
}
