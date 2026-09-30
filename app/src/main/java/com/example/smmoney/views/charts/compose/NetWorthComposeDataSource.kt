package com.example.smmoney.views.charts.compose

import com.example.smmoney.database.AccountDB
import com.example.smmoney.misc.CalExt
import com.example.smmoney.misc.Enums
import com.example.smmoney.misc.PocketMoneyThemes
import com.example.smmoney.misc.Prefs
import com.example.smmoney.views.charts.items.ChartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.GregorianCalendar

data class NetWorthData(
    val assets: List<ChartItem>,
    val liabilities: List<ChartItem>,
    val netWorth: List<ChartItem>
)

class NetWorthComposeDataSource {

    suspend fun getNetWorthData(
        period: Int = Enums.kBudgetPeriodMonth,
        lookback: Int = 12,
        filteredAccountIds: Set<Int>? = null // Null means use default totalWorth logic, non-null means use specific IDs
    ): NetWorthData = withContext(Dispatchers.IO) {
        val chartAssets = ArrayList<ChartItem>(lookback)
        val chartLiabilities = ArrayList<ChartItem>(lookback)
        val chartNetworth = ArrayList<ChartItem>(lookback)

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

        val liabilityColor = PocketMoneyThemes.redBarColor()
        val assetColor = PocketMoneyThemes.greenBarColor()
        val networthColor = PocketMoneyThemes.orangeLabelColor()
        
        val multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)
        
        // 2. Fetch all accounts and apply the requested filter
        val allAccounts = AccountDB.queryOnViewType(Enums.kViewAccountsAll)
        val accounts = if (filteredAccountIds == null) {
            // Default behavior: Include accounts where totalWorth is true
            allAccounts.filter { it.totalWorth }
        } else {
            // Explicit filter: Include only the provided IDs
            allAccounts.filter { filteredAccountIds.contains(it.accountID) }
        }

        for (index in 0 until lookback) {
            var totalAssets = 0.0
            var totalLiabilities = 0.0

            for (accountClass in accounts) {
                // queryOnViewType returns dehydrated account instances but balanceAsOfDate and isAsset hydrate as needed via database lookups
                val xrate = if (multipleCurrencies) accountClass.exchangeRate else 1.0

                val bal = accountClass.balanceAsOfDate(balanceDate) / xrate
                if (accountClass.isAsset) {
                    totalAssets += bal
                } else {
                    totalLiabilities += bal
                }
            }

            // Create a nicely formatted label based on the period
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

            chartAssets.add(ChartItem(totalAssets, labelStr, assetColor))
            chartLiabilities.add(ChartItem(totalLiabilities, labelStr, liabilityColor))
            chartNetworth.add(ChartItem(totalAssets + totalLiabilities, labelStr, networthColor))
            
            // 3. Advance to the next period
            balanceDate = when (period) {
                Enums.kBudgetPeriodMonth -> CalExt.endOfDay(CalExt.endOfMonth(CalExt.addMonth(balanceDate)))
                Enums.kBudgetPeriodQuarter -> CalExt.endOfDay(CalExt.endOfQuarter(CalExt.addMonths(balanceDate, 3)))
                Enums.kBudgetPeriodHalfYear -> CalExt.endOfDay(CalExt.addMonths(balanceDate, 6))
                Enums.kBudgetPeriodYear -> CalExt.endOfDay(CalExt.endOfYear(CalExt.addYear(balanceDate)))
                else -> CalExt.endOfDay(CalExt.endOfMonth(CalExt.addMonth(balanceDate)))
            }
        }

        NetWorthData(chartAssets, chartLiabilities, chartNetworth)
    }
}
