package com.example.smmoney.views.charts.compose

import com.example.smmoney.calendar.CreditCardScheduleConfig
import com.example.smmoney.calendar.DueDateRule
import com.example.smmoney.calendar.StatementCycleRule
import com.example.smmoney.calendar.WeekendRollPolicy
import com.example.smmoney.database.TransactionDB
import com.example.smmoney.misc.Enums
import com.example.smmoney.records.AccountClass
import java.time.LocalDate
import java.time.ZoneId
import java.util.GregorianCalendar

data class CreditCardCycleSummary(
    val account: AccountClass,
    val lastStatementDate: LocalDate,
    val nextStatementDate: LocalDate,
    val nextDueDate: LocalDate,
    val spentSinceStatementToNow: Double,
    val spentSinceStatementToNext: Double,
    val lastStatementBalance: Double,
    val cycleProgressPercentage: Float
)

object CreditCardComposeDataSource {

    fun getCycleSummary(account: AccountClass, currentDate: LocalDate = LocalDate.now()): CreditCardCycleSummary? {
        val statementRule = StatementCycleRule.fromString(account.ccStatementCycleRule) ?: return null
        val dueDateRule = DueDateRule.fromString(account.ccDueDateRule) ?: return null
        val weekendPolicy = try {
            WeekendRollPolicy.valueOf(account.ccWeekendPolicy)
        } catch (_: Exception) {
            WeekendRollPolicy.NEXT_WORKING_DAY
        }

        val config = CreditCardScheduleConfig(
            statementRule = statementRule,
            dueDateRule = dueDateRule,
            weekendPolicy = weekendPolicy
        )

        // Find the statement that covers the current date
        val schedule = config.calculateNextPaymentDate(currentDate)
        
        // This is a naive heuristic for 'last statement' to power the UI.
        val lastSchedule = config.calculateNextPaymentDate(currentDate.minusMonths(1))

        val totalDaysInCycle = schedule.statementDate.toEpochDay() - lastSchedule.statementDate.toEpochDay()
        val daysElapsed = currentDate.toEpochDay() - lastSchedule.statementDate.toEpochDay()
        
        val progress = if (totalDaysInCycle > 0) {
            (daysElapsed.toFloat() / totalDaysInCycle.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        
        // Helper to convert LocalDate to GregorianCalendar for legacy DB methods
        fun LocalDate.toGregorianCalendar(): GregorianCalendar {
            return GregorianCalendar.from(this.atStartOfDay(ZoneId.systemDefault()))
        }

        val lastStmtCal = lastSchedule.statementDate.toGregorianCalendar()
        val nextStmtCal = schedule.statementDate.toGregorianCalendar()
        val nowCal = currentDate.toGregorianCalendar()

        // Calculate actual historical statement balance
        val lastStatementBalance = account.balanceAsOfDate(lastStmtCal)
        
        // Calculate spent since statement using TransactionDB for withdrawals (type 0 = withdrawal)
        val accountFilter = setOf(account.accountID)
        val spentSinceStatementToNow = TransactionDB.cashFlowBalanceWith(
            Enums.kTransactionTypeWithdrawal, 
            lastStmtCal, 
            nowCal, 
            accountFilter
        )
        
        val spentSinceStatementToNext = TransactionDB.cashFlowBalanceWith(
            Enums.kTransactionTypeWithdrawal, 
            lastStmtCal, 
            nextStmtCal, 
            accountFilter
        )

        return CreditCardCycleSummary(
            account = account,
            lastStatementDate = lastSchedule.statementDate,
            nextStatementDate = schedule.statementDate,
            nextDueDate = lastSchedule.cashflowDebitDate,
            spentSinceStatementToNow = spentSinceStatementToNow,
            spentSinceStatementToNext = spentSinceStatementToNext,
            lastStatementBalance = lastStatementBalance,
            cycleProgressPercentage = progress
        )
    }
}