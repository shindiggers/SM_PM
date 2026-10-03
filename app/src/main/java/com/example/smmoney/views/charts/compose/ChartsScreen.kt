package com.example.smmoney.views.charts.compose

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import com.example.smmoney.database.AccountDB
import com.example.smmoney.records.AccountClass
import com.example.smmoney.misc.Prefs
import com.example.smmoney.misc.Enums
import com.example.smmoney.misc.Locales
import com.example.smmoney.records.FilterClass
import com.example.smmoney.views.transactions.TransactionsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

@Composable
fun ChartsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 24.dp) // Reduced top padding since Action Bar is gone
    ) {
        NetWorthChart()
        Spacer(modifier = Modifier.height(32.dp))
        CashFlowChart()
        Spacer(modifier = Modifier.height(32.dp))
        CreditCardSummaryList()
    }
}

@Composable
fun CreditCardSummaryList() {
    var summaries by remember { mutableStateOf<List<CreditCardCycleSummary>>(emptyList()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    // Observe lifecycle to refresh data on resume
    DisposableEffect(LocalContext.current) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    LaunchedEffect(refreshTrigger) {
        withContext(Dispatchers.IO) {
            val dbAccounts = AccountDB.queryOnViewType(0) // 0 is All Accounts
            val ccAccounts = dbAccounts.filter { it.type == Enums.kAccountTypeCreditCard && !it.deleted }
            val summs = ccAccounts.mapNotNull { 
                CreditCardComposeDataSource.getCycleSummary(it)
            }
            withContext(Dispatchers.Main) {
                summaries = summs
            }
        }
    }

    if (summaries.isNotEmpty()) {
        Text("Credit Card Summary", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            for (summary in summaries) {
                CreditCardSummaryItem(summary)
            }
        }
    }
}

@Composable
fun CreditCardSummaryItem(summary: CreditCardCycleSummary) {
    var showFullCycleSpent by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = summary.account.account, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Last Stmt Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = summary.account.formatAmountAsCurrency(summary.lastStatementBalance), style = MaterialTheme.typography.titleMedium)
                        
                        val locale = LocalConfiguration.current.locales[0]
                        val formatter = remember(locale) {
                            DateTimeFormatter.ofPattern(
                                DateFormat.getBestDateTimePattern(locale, "MMMd")
                            )
                        }
                        Text(
                            text = " (Due ${summary.nextDueDate.format(formatter)})", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.clickable { showFullCycleSpent = !showFullCycleSpent }
                ) {
                    Text(
                        text = if (showFullCycleSpent) "Spent (Full Cycle)" else "Spent (To Date)", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = summary.account.formatAmountAsCurrency(
                            if (showFullCycleSpent) summary.spentSinceStatementToNext else summary.spentSinceStatementToNow
                        ), 
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE57373)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            val locale = LocalConfiguration.current.locales[0]
            val formatter = remember(locale) {
                DateTimeFormatter.ofPattern(
                    DateFormat.getBestDateTimePattern(locale, "MMMd")
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))) {
                Box(modifier = Modifier.fillMaxWidth(summary.cycleProgressPercentage).height(8.dp).background(Color(0xFF81C784), RoundedCornerShape(4.dp)))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Stmt: ${summary.lastStatementDate.format(formatter)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Next: ${summary.nextStatementDate.format(formatter)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun NetWorthChart() {
    var netWorthData by remember { mutableStateOf<NetWorthData?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var currentPeriod by remember { mutableIntStateOf(Enums.kBudgetPeriodMonth) }
    var lookbackCount by remember { mutableIntStateOf(12) } // Default 12 periods
    var showFilterDialog by remember { mutableStateOf(false) }
    var filteredAccountIds by remember { mutableStateOf<Set<Int>?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val savedFilter = Prefs.getStringPref("net_worth_filtered_accounts")
            if (savedFilter.isNotEmpty()) {
                filteredAccountIds = savedFilter.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            }
        }
    }

    // Observe lifecycle events to trigger a refresh when the screen resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }
    
    LaunchedEffect(refreshTrigger, currentPeriod, lookbackCount, filteredAccountIds) {
        val dataSource = NetWorthComposeDataSource()
        netWorthData = dataSource.getNetWorthData(period = currentPeriod, lookback = lookbackCount, filteredAccountIds = filteredAccountIds)
    }

    if (netWorthData == null) {
        ChartPlaceholder("Loading Net Worth...")
    } else {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Net Worth", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f)) // Added weight so it shrinks if needed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val periodText = when (currentPeriod) {
                        Enums.kBudgetPeriodQuarter -> "Quarterly"
                        Enums.kBudgetPeriodHalfYear -> "Half-Yearly"
                        Enums.kBudgetPeriodYear -> "Yearly"
                        else -> "Monthly"
                    }
                    var expandedPeriod by remember { mutableStateOf(false) }
                    
                    Box {
                        OutlinedButton(
                            onClick = { expandedPeriod = true },
                            modifier = Modifier.padding(end = 4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), // Reduce internal padding
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(periodText, maxLines = 1) // Prevent text wrap
                            Text(" ▼", modifier = Modifier.padding(start = 4.dp))
                        }
                        
                        DropdownMenu(
                            expanded = expandedPeriod,
                            onDismissRequest = { expandedPeriod = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Monthly") },
                                onClick = { 
                                    currentPeriod = Enums.kBudgetPeriodMonth
                                    expandedPeriod = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Quarterly") },
                                onClick = { 
                                    currentPeriod = Enums.kBudgetPeriodQuarter
                                    expandedPeriod = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Half-Yearly") },
                                onClick = { 
                                    currentPeriod = Enums.kBudgetPeriodHalfYear
                                    expandedPeriod = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Yearly") },
                                onClick = { 
                                    currentPeriod = Enums.kBudgetPeriodYear
                                    expandedPeriod = false 
                                }
                            )
                        }
                    }

                    var expandedLookback by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedLookback = true },
                            modifier = Modifier.padding(end = 4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), // Reduce internal padding
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("$lookbackCount", maxLines = 1)
                            Text(" ▼", modifier = Modifier.padding(start = 4.dp))
                        }
                        
                        DropdownMenu(
                            expanded = expandedLookback,
                            onDismissRequest = { expandedLookback = false }
                        ) {
                            listOf(3, 6, 9, 12).forEach { count ->
                                DropdownMenuItem(
                                    text = { Text("$count") },
                                    onClick = { 
                                        lookbackCount = count
                                        expandedLookback = false 
                                    }
                                )
                            }
                        }
                    }

                    // Simple TextButton instead of an Icon since we don't have the material-icons-extended dependency
                    TextButton(
                        onClick = { showFilterDialog = true },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp) // Minimum padding
                    ) {
                        Text("Filter", maxLines = 1)
                    }
                }
            }
            NetWorthBarChart(
                items = netWorthData!!.netWorth,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxSize()
                    .height(200.dp)
            )
        }
    }

    if (showFilterDialog) {
        AccountFilterDialog(
            initialSelectedIds = filteredAccountIds,
            onDismiss = { showFilterDialog = false },
            onApply = { selectedIds ->
                filteredAccountIds = selectedIds
                Prefs.setPref("net_worth_filtered_accounts", selectedIds.joinToString(","))
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun ChartPlaceholder(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun AccountFilterDialog(
    initialSelectedIds: Set<Int>?,
    onDismiss: () -> Unit,
    onApply: (Set<Int>) -> Unit
) {
    var groupedAccounts by remember { mutableStateOf<Map<String, List<AccountClass>>>(emptyMap()) }
    var allAccountIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val accounts = AccountDB.queryOnViewType(Enums.kViewAccountsAll)
            
            // Group accounts by their localized type string and sort groups alphabetically
            val grouped = accounts.groupBy { it.typeAsString() }
                .toSortedMap()
                .mapValues { (_, list) -> list.sortedBy { it.account.lowercase() } }
            
            val ids = accounts.map { it.accountID }.toSet()
            val defaultIds = initialSelectedIds ?: accounts.filter { it.totalWorth }.map { it.accountID }.toSet()
            
            withContext(Dispatchers.Main) {
                groupedAccounts = grouped
                allAccountIds = ids
                selectedIds = defaultIds
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Accounts") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { selectedIds = allAccountIds }) {
                        Text("Select All")
                    }
                    TextButton(onClick = { selectedIds = emptySet() }) {
                        Text("Deselect All")
                    }
                }
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    groupedAccounts.forEach { (typeString, accountsList) ->
                        item {
                            Text(
                                text = typeString,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(accountsList) { account ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newSet = selectedIds.toMutableSet()
                                        if (newSet.contains(account.accountID)) newSet.remove(account.accountID)
                                        else newSet.add(account.accountID)
                                        selectedIds = newSet
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedIds.contains(account.accountID),
                                    onCheckedChange = null // Handled by Row click
                                )
                                Text(
                                    text = account.account,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(selectedIds) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Interop function for Java
fun setChartsScreenContent(view: ComposeView) {
    view.setContent {
        ChartsScreen()
    }
}

@Composable
fun CashFlowChart() {
    var cashFlowData by remember { mutableStateOf<NetWorthData?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var currentPeriod by remember { mutableIntStateOf(Enums.kBudgetPeriodMonth) }
    var lookbackCount by remember { mutableIntStateOf(6) } // Default 6 periods for Cash Flow
    var showFilterDialog by remember { mutableStateOf(false) }
    var filteredAccountIds by remember { mutableStateOf<Set<Int>?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val savedFilter = Prefs.getStringPref("cash_flow_filtered_accounts")
            if (savedFilter.isNotEmpty()) {
                filteredAccountIds = savedFilter.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            }
        }
    }

    // Observe lifecycle events to trigger a refresh when the screen resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }
    
    LaunchedEffect(refreshTrigger, currentPeriod, lookbackCount, filteredAccountIds) {
        val dataSource = CashFlowComposeDataSource()
        cashFlowData = dataSource.getCashFlowData(period = currentPeriod, lookback = lookbackCount, filteredAccountIds = filteredAccountIds)
    }

    if (cashFlowData == null) {
        ChartPlaceholder("Loading Cash Flow...")
    } else {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cash Flow", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val periodText = when (currentPeriod) {
                        Enums.kBudgetPeriodQuarter -> "Quarterly"
                        Enums.kBudgetPeriodHalfYear -> "Half-Yearly"
                        Enums.kBudgetPeriodYear -> "Yearly"
                        else -> "Monthly"
                    }
                    var expandedPeriod by remember { mutableStateOf(false) }
                    
                    Box {
                        OutlinedButton(
                            onClick = { expandedPeriod = true },
                            modifier = Modifier.padding(end = 4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(periodText, maxLines = 1)
                            Text(" ▼", modifier = Modifier.padding(start = 4.dp))
                        }
                        
                        DropdownMenu(
                            expanded = expandedPeriod,
                            onDismissRequest = { expandedPeriod = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Monthly") },
                                onClick = { 
                                    currentPeriod = Enums.kBudgetPeriodMonth
                                    expandedPeriod = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Quarterly") },
                                onClick = { 
                                    currentPeriod = Enums.kBudgetPeriodQuarter
                                    expandedPeriod = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Half-Yearly") },
                                onClick = { 
                                    currentPeriod = Enums.kBudgetPeriodHalfYear
                                    expandedPeriod = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Yearly") },
                                onClick = { 
                                    currentPeriod = Enums.kBudgetPeriodYear
                                    expandedPeriod = false 
                                }
                            )
                        }
                    }

                    var expandedLookback by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedLookback = true },
                            modifier = Modifier.padding(end = 4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("$lookbackCount", maxLines = 1)
                            Text(" ▼", modifier = Modifier.padding(start = 4.dp))
                        }
                        
                        DropdownMenu(
                            expanded = expandedLookback,
                            onDismissRequest = { expandedLookback = false }
                        ) {
                            listOf(3, 6, 9, 12).forEach { count ->
                                DropdownMenuItem(
                                    text = { Text("$count") },
                                    onClick = { 
                                        lookbackCount = count
                                        expandedLookback = false 
                                    }
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = { showFilterDialog = true },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("Filter", maxLines = 1)
                    }
                }
            }
            CashFlowBarChart(
                assets = cashFlowData!!.assets,
                liabilities = cashFlowData!!.liabilities,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxSize()
                    .height(200.dp),
                onItemClick = { clickedItem ->
                    val filter = FilterClass()
                    
                    // Set Date Range
                    filter.date = Locales.kLOC_FILTER_DATES_CUSTOM
                    filter.dateFrom = clickedItem.fromDate
                    filter.dateTo = clickedItem.toDate
                    
                    // Set Income/Expense
                    if (clickedItem.isIncome == true) {
                        filter.type = Enums.kTransactionTypeDeposit
                    } else {
                        filter.type = Enums.kTransactionTypeWithdrawal
                    }
                    
                    // Apply account filters
                    val currentFilters = filteredAccountIds
                    if (!currentFilters.isNullOrEmpty()) {
                        // The FilterClass expects a semicolon-separated string of account names,
                        // so we need to map the IDs back to account names.
                        val accountNames = currentFilters.mapNotNull { 
                            AccountClass.accountForID(it)
                        }.joinToString(";")
                        filter.account = accountNames
                    }
                    
                    filter.setCustomFilter(true)
                    
                    val intent = android.content.Intent(context, TransactionsActivity::class.java)
                    intent.putExtra("Filter", filter)
                    // Pass a nice title like "Jan26 Income" or "Q1 26 Expenses"
                    val typeStr = if (clickedItem.isIncome == true) "Income" else "Expenses"
                    intent.putExtra("title", "${clickedItem.label} $typeStr")
                    context.startActivity(intent)
                }
            )
        }
    }

    if (showFilterDialog) {
        AccountFilterDialog(
            initialSelectedIds = filteredAccountIds,
            onDismiss = { showFilterDialog = false },
            onApply = { selectedIds ->
                filteredAccountIds = selectedIds
                Prefs.setPref("cash_flow_filtered_accounts", selectedIds.joinToString(","))
                showFilterDialog = false
            }
        )
    }
}
