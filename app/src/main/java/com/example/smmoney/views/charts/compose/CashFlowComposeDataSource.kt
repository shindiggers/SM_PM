package com.example.smmoney.views.charts.compose

import com.example.smmoney.database.TransactionDB
import com.example.smmoney.misc.CalExt
import com.example.smmoney.misc.Enums
import com.example.smmoney.misc.PocketMoneyThemes
import com.example.smmoney.views.charts.items.ChartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.math.abs

class CashFlowComposeDataSource {

    suspend fun getCashFlowData(
        period: Int = Enums.kBudgetPeriodMonth,
        lookback: Int = 6,
        filteredAccountIds: Set<Int>? = null // Support passing filtered account IDs
    ): NetWorthData = withContext(Dispatchers.IO) {
        val chartIncome = ArrayList<ChartItem>(lookback)
        val chartExpenses = ArrayList<ChartItem>(lookback)
        val dummyNetWorth = ArrayList<ChartItem>(lookback)

        // 1. Determine starting date and step function based on the requested period
        val calNow = GregorianCalendar()
        var balanceDate = when (period) {
            Enums.kBudgetPeriodMonth -> {
                var d = CalExt.endOfDay(CalExt.endOfMonth(calNow))
                d = CalExt.endOfDay(CalExt.endOfMonth(CalExt.subtractMonths(d, lookback - 1)))
                d
            }
            Enums.kBudgetPeriodQuarter -> {
                var d = CalExt.endOfDay(CalExt.endOfQuarter(calNow))
                d = CalExt.endOfDay(CalExt.endOfQuarter(CalExt.subtractMonths(d, (lookback - 1) * 3)))
                d
            }
            Enums.kBudgetPeriodHalfYear -> {
                var d = CalExt.endOfDay(if (calNow.get(Calendar.MONTH) < 6) CalExt.addMonths(CalExt.beginningOfYear(calNow), 5) else CalExt.endOfYear(calNow))
                d = CalExt.endOfDay(CalExt.subtractMonths(d, (lookback - 1) * 6))
                d
            }
            Enums.kBudgetPeriodYear -> {
                var d = CalExt.endOfDay(CalExt.endOfYear(calNow))
                d = CalExt.endOfDay(CalExt.endOfYear(CalExt.addYears(d, -(lookback - 1))))
                d
            }
            else -> { // Default to Month
                var d = CalExt.endOfDay(CalExt.endOfMonth(calNow))
                d = CalExt.endOfDay(CalExt.endOfMonth(CalExt.subtractMonths(d, lookback - 1)))
                d
            }
        }

        val expenseColor = PocketMoneyThemes.redBarColor()
        val incomeColor = PocketMoneyThemes.greenBarColor()

        for (index in 0 until lookback) {
            
            // Calculate fromDate based on the period
            val fromDate = when (period) {
                Enums.kBudgetPeriodMonth -> CalExt.beginningOfDay(CalExt.beginningOfMonth(balanceDate))
                Enums.kBudgetPeriodQuarter -> CalExt.beginningOfDay(CalExt.beginningOfQuarter(balanceDate))
                Enums.kBudgetPeriodHalfYear -> {
                    val half = if (balanceDate.get(Calendar.MONTH) < 6) 0 else 6
                    val cal = CalExt.beginningOfYear(balanceDate)
                    cal.add(Calendar.MONTH, half)
                    CalExt.beginningOfDay(cal)
                }
                Enums.kBudgetPeriodYear -> CalExt.beginningOfDay(CalExt.beginningOfYear(balanceDate))
                else -> CalExt.beginningOfDay(CalExt.beginningOfMonth(balanceDate))
            }

            // Type 1 = Income (Assets), Type 0 = Expenses (Liabilities)
            // We pass the filteredAccountIds down so TransactionDB can filter the raw SQL query
            val totalIncome = TransactionDB.cashFlowBalanceWith(1, fromDate, balanceDate, filteredAccountIds)
            val totalExpenses = TransactionDB.cashFlowBalanceWith(0, fromDate, balanceDate, filteredAccountIds)

            val yearStr = balanceDate.get(Calendar.YEAR).toString().takeLast(2)
            val month = balanceDate.get(Calendar.MONTH)
            
            val labelStr = when (period) {
                Enums.kBudgetPeriodMonth -> {
                    val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    val monthStr = monthNames[month]
                    if (month == Calendar.JANUARY || index == 0) "${monthStr}${yearStr}" else monthStr
                }
                Enums.kBudgetPeriodQuarter -> {
                    val quarter = (month / 3) + 1
                    if (quarter == 1 || index == 0) "Q$quarter $yearStr" else "Q$quarter"
                }
                Enums.kBudgetPeriodHalfYear -> {
                    val half = if (month < 6) 1 else 2
                    if (half == 1 || index == 0) "H$half $yearStr" else "H$half"
                }
                Enums.kBudgetPeriodYear -> {
                    balanceDate.get(Calendar.YEAR).toString()
                }
                else -> "M$index"
            }

            val incomeItem = ChartItem(totalIncome, labelStr, incomeColor)
            incomeItem.fromDate = fromDate
            incomeItem.toDate = balanceDate
            incomeItem.isIncome = true
            chartIncome.add(incomeItem)
            
            val expenseItem = ChartItem(abs(totalExpenses), labelStr, expenseColor)
            expenseItem.fromDate = fromDate
            expenseItem.toDate = balanceDate
            expenseItem.isIncome = false
            chartExpenses.add(expenseItem)
            
            // 3. Advance to the next period
            balanceDate = when (period) {
                Enums.kBudgetPeriodMonth -> CalExt.endOfDay(CalExt.endOfMonth(CalExt.addMonth(balanceDate)))
                Enums.kBudgetPeriodQuarter -> CalExt.endOfDay(CalExt.endOfQuarter(CalExt.addMonths(balanceDate, 3)))
                Enums.kBudgetPeriodHalfYear -> CalExt.endOfDay(CalExt.addMonths(balanceDate, 6))
                Enums.kBudgetPeriodYear -> CalExt.endOfDay(CalExt.endOfYear(CalExt.addYear(balanceDate)))
                else -> CalExt.endOfDay(CalExt.endOfMonth(CalExt.addMonth(balanceDate)))
            }
        }

        // We reuse NetWorthData as a container, mapping income to assets and expenses to liabilities
        NetWorthData(chartIncome, chartExpenses, dummyNetWorth)
    }
}
