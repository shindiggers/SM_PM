package com.example.smmoney.views.accounts

import com.example.smmoney.misc.Enums
import com.example.smmoney.views.charts.compose.CashFlowComposeDataSource
import com.example.smmoney.views.charts.compose.NetWorthComposeDataSource
import com.example.smmoney.views.charts.compose.NetWorthData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ChartDataHelper {

    fun fetchNetWorthData(
        accountIds: Set<Int>,
        callback: (NetWorthData) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val dataSource = NetWorthComposeDataSource()
            val data = dataSource.getNetWorthData(
                period = Enums.kBudgetPeriodMonth,
                lookback = 12,
                filteredAccountIds = accountIds
            )
            callback(data)
        }
    }

    fun fetchCashFlowData(
        accountIds: Set<Int>,
        callback: (NetWorthData) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val dataSource = CashFlowComposeDataSource()
            val data = dataSource.getCashFlowData(
                period = Enums.kBudgetPeriodMonth,
                lookback = 6,
                filteredAccountIds = accountIds
            )
            callback(data)
        }
    }
}
