package com.example.smmoney.misc;

public class Enums {
    public static final int DateChangeTypeNone = 0;
    public static final int DateChangeTypeSeparateTransactionFromRepeating = 1;
    public static final int DateChangeTypeUpdateRepeating = 2;

    public static final int RepeatingChangeTypeNone = 0;
    public static final int RepeatingChangeTypeSeparateTransactionFromRepeating = 1;
    public static final int RepeatingChangeTypeUpdateRepeating = 2;

    public static final int ReportsSortDirectionAscending = 0;
    public static final int ReportsSortDirectionDescending = 1;

    public static final int kAccountTypeAsset = 3;
    public static final int kAccountTypeCash = 1;
    public static final int kAccountTypeChecking = 0;
    public static final int kAccountTypeCreditCard = 2;
    public static final int kAccountTypeCreditLine = 8;
    public static final int kAccountTypeInvestment = 9;
    public static final int kAccountTypeLiability = 4;
    public static final int kAccountTypeMoneyMarket = 7;
    public static final int kAccountTypeOnline = 5;
    public static final int kAccountTypeSavings = 6;

    public static final int kBalanceTypeAvailableCredit = 4;
    public static final int kBalanceTypeAvailableFunds = 3;
    public static final int kBalanceTypeCleared = 1;
    public static final int kBalanceTypeCurrent = 2;
    public static final int kBalanceTypeFiltered = 5;
    public static final int kBalanceTypeFuture = 0;
    public static final int kBalanceTypeNone = -1;

    public static final int kBudgetDisplayBeat = 1;
    public static final int kBudgetDisplayExpenseAvailable = 0;
    public static final int kBudgetDisplayExpenseBudgeted = 2;
    public static final int kBudgetDisplayExpenseOver = 3;

    public static final int kBudgetDisplaySaved = 0;
    public static final int kBudgetDisplayText = 1;

    public static final int kBudgetPeriod4Weeks = 8;
    public static final int kBudgetPeriodBimonthly = 6;
    public static final int kBudgetPeriodBiweekly = 5;
    public static final int kBudgetPeriodDay = 0;
    public static final int kBudgetPeriodHalfYear = 7;
    public static final int kBudgetPeriodMonth = 2;
    public static final int kBudgetPeriodQuarter = 3;
    public static final int kBudgetPeriodWeek = 1;
    public static final int kBudgetPeriodYear = 4;

    public static final int kBudgetsSortTypeActual = 1;
    public static final int kBudgetsSortTypeBudgeted = 2;
    public static final int kBudgetsSortTypeCategory = 0;
    public static final int kBudgetsSortTypePercentage = 3;

    public static final int kCategoryExpense = 0;
    public static final int kCategoryIncome = 1;

    public static final int kChartTypeNegativePie = -1;
    public static final int kChartTypePositivePie = 1;

    public static final int kClearedCleared = 1;
    public static final int kClearedDoesntMatter = 2;
    public static final int kClearedUncleared = 0;

    public static final int kDateRangeCurrentMonth = 5;
    public static final int kDateRangeCurrentQuarter = 7;
    public static final int kDateRangeCurrentWeek = 3;
    public static final int kDateRangeCurrentYear = 9;
    public static final int kDateRangeLast30Days = 15;
    public static final int kDateRangeLast60Days = 16;
    public static final int kDateRangeLast90Days = 17;
    public static final int kDateRangeLastMonth = 6;
    public static final int kDateRangeLastQuarter = 8;
    public static final int kDateRangeLastWeek = 4;
    public static final int kDateRangeLastYear = 10;
    public static final int kDateRangeModifiedToday = 14;
    public static final int kDateRangeNoFromDate = 12;
    public static final int kDateRangeNoToDate = 13;
    public static final int kDateRangeNone = 0;
    public static final int kDateRangeOther = 18;
    public static final int kDateRangeRecentChanges = 11;
    public static final int kDateRangeToday = 1;
    public static final int kDateRangeYesterday = 2;

    public static final int kDesktopSyncFirstSyncActionNone = 0;
    public static final int kDesktopSyncFirstSyncActionReplaceDataOnServer = 1;
    public static final int kDesktopSyncFirstSyncActionRestoreFromServer = 2;
    public static final int kDesktopSyncFirstSyncActionSync = 3;

    public static final int kDesktopSyncStateACKHeaderReceived = 57;
    public static final int kDesktopSyncStateACKProcessed = 61;
    public static final int kDesktopSyncStateACKReceived = 59;
    public static final int kDesktopSyncStateClientConnected = 4;
    public static final int kDesktopSyncStateConnecting = 3;
    public static final int kDesktopSyncStateConnectionLost = 68;
    public static final int kDesktopSyncStateDisconnected = 67;
    public static final int kDesktopSyncStateDisconnecting = 66;
    public static final int kDesktopSyncStateError = 69;
    public static final int kDesktopSyncStateIdle = 1;
    public static final int kDesktopSyncStateInitialized = 2;
    public static final int kDesktopSyncStateNone = 0;
    public static final int kDesktopSyncStatePhotoACKHeaderReceived = 41;
    public static final int kDesktopSyncStatePhotoACKProcessed = 45;
    public static final int kDesktopSyncStatePhotoACKReceived = 43;
    public static final int kDesktopSyncStatePhotoHeaderReceived = 34;
    public static final int kDesktopSyncStatePhotoProcessed = 37;
    public static final int kDesktopSyncStatePhotoReceived = 36;
    public static final int kDesktopSyncStateProcessingACK = 60;
    public static final int kDesktopSyncStateProcessingChanges = 52;
    public static final int kDesktopSyncStateProcessingPhotoACK = 44;
    public static final int kDesktopSyncStateProcessingSyncVersion = 15;
    public static final int kDesktopSyncStateReceivingACK = 58;
    public static final int kDesktopSyncStateReceivingACKHeader = 56;
    public static final int kDesktopSyncStateReceivingPhoto = 35;
    public static final int kDesktopSyncStateReceivingPhotoACK = 42;
    public static final int kDesktopSyncStateReceivingPhotoACKHeader = 40;
    public static final int kDesktopSyncStateReceivingPhotoHeader = 33;
    public static final int kDesktopSyncStateReceivingRecentChanges = 50;
    public static final int kDesktopSyncStateReceivingRecentChangesHeader = 48;
    public static final int kDesktopSyncStateReceivingSyncVersion = 13;
    public static final int kDesktopSyncStateReceivingSyncVersionHeader = 11;
    public static final int kDesktopSyncStateReceivingUDID = 21;
    public static final int kDesktopSyncStateReceivingUDIDHeader = 19;
    public static final int kDesktopSyncStateRecentChangesHeaderReceived = 49;
    public static final int kDesktopSyncStateRecentChangesProcessed = 53;
    public static final int kDesktopSyncStateRecentChangesReceived = 51;
    public static final int kDesktopSyncStateSendPhotos = 30;
    public static final int kDesktopSyncStateSendingACK = 54;
    public static final int kDesktopSyncStateSendingPhoto = 31;
    public static final int kDesktopSyncStateSendingPhotoACK = 38;
    public static final int kDesktopSyncStateSendingRecentChanges = 46;
    public static final int kDesktopSyncStateSendingSyncVersion = 9;
    public static final int kDesktopSyncStateSendingTheEnd = 62;
    public static final int kDesktopSyncStateSendingUDID = 17;
    public static final int kDesktopSyncStateSentACK = 55;
    public static final int kDesktopSyncStateSentPhoto = 32;
    public static final int kDesktopSyncStateSentPhotoACK = 39;
    public static final int kDesktopSyncStateSentRecentChanges = 47;
    public static final int kDesktopSyncStateSentSyncVersion = 10;
    public static final int kDesktopSyncStateSentTheEnd = 65;
    public static final int kDesktopSyncStateSentUDID = 18;
    public static final int kDesktopSyncStateServerAuthenticated = 5;
    public static final int kDesktopSyncStateServerConnected = 8;
    public static final int kDesktopSyncStateServerInitialized = 6;
    public static final int kDesktopSyncStateServerListening = 7;
    public static final int kDesktopSyncStateSyncVersionHeaderReceived = 12;
    public static final int kDesktopSyncStateSyncVersionProcessed = 16;
    public static final int kDesktopSyncStateSyncVersionReceived = 14;

    public static final int kDesktopSyncStateTriggerDataAvailable = 1;
    public static final int kDesktopSyncStateTriggerManual = 3;
    public static final int kDesktopSyncStateTriggerNone = 0;
    public static final int kDesktopSyncStateTriggerSpaceAvailable = 2;

    public static final int kDesktopSyncStateUDIDHeaderReceived = 20;
    public static final int kDesktopSyncStateUDIDProcessed = 24;
    public static final int kDesktopSyncStateUDIDProcessing = 23;
    public static final int kDesktopSyncStateUDIDReceived = 22;
    public static final int kDesktopSyncStateUnchanged = 70;

    public static final int kFeeTypeFixed = 0;
    public static final int kFeeTypePercent = 1;

    public static final int kFilterCurrentAccountID = -2;

    public static final int kModifyEitherEnd = 2;
    public static final int kModifyOtherEndOnly = 0;
    public static final int kModifyThisEndOnly = 1;

    public static final int kPocketMoneySyncClient = 0;
    public static final int kPocketMoneySyncServer = 1;

    public static final int kReportDisplayAmount = 0;
    public static final int kReportDisplayCount = 2;
    public static final int kReportDisplayPercentage = 1;

    public static final int kReportPeriodAll = 5;
    public static final int kReportPeriodOneMonth = 0;
    public static final int kReportPeriodOneYear = 4;
    public static final int kReportPeriodSixMonths = 3;
    public static final int kReportPeriodThreeMonths = 2;
    public static final int kReportPeriodTwoMonths = 1;

    public static final int kReportsChartTypeBar = 2;
    public static final int kReportsChartTypeLine = 3;
    public static final int kReportsChartTypeNone = 0;
    public static final int kReportsChartTypePie = 1;

    public static final int kReportsSortOnAmount = 1;
    public static final int kReportsSortOnCount = 2;
    public static final int kReportsSortOnItem = 0;

    public static final int kSIZEHEADERSIZE = 4;

    public static final int kSumamryChartMoreCharts = 2;
    public static final int kSumamryChartTypeCashFlow = 1;
    public static final int kSumamryChartTypeNetWorth = 0;

    public static final int kTransactionTypeAll = 4;
    public static final int kTransactionTypeDeposit = 1;
    public static final int kTransactionTypeRepeating = 5;
    public static final int kTransactionTypeTransferFrom = 3;
    public static final int kTransactionTypeTransferTo = 2;
    public static final int kTransactionTypeWithdrawal = 0;

    public static final int kTransactionsSortTypeAmount = 1;
    public static final int kTransactionsSortTypeCategory = 4;
    public static final int kTransactionsSortTypeClass = 3;
    public static final int kTransactionsSortTypeCleared = 7;
    public static final int kTransactionsSortTypeDate = 0;
    public static final int kTransactionsSortTypeDateAmount = 8;
    public static final int kTransactionsSortTypeID = 6;
    public static final int kTransactionsSortTypeMemo = 5;
    public static final int kTransactionsSortTypePayee = 2;

    public static final int kViewAccountsAll = 0;
    public static final int kViewAccountsNonZero = 1;
    public static final int kViewAccountsTotalWorth = 2;

    // RepearingTransaction class object RepeatOn field possible values:
    public static final int monthlyDateInMonth = 1;
    public static final int monthlyDayOfMonth = 0;
    public static final int monthlyLastDayOfMonth = 2;
    public static final int monthlyLastOrdinalWeekdayOfMonth = 4;
    public static final int monthlyLastWeekDayOfMonth = 3;

    public static final int repeatDaily = 1;
    public static final int repeatMonthly = 3;
    public static final int repeatNone = 0;
    public static final int repeatWeekly = 2;
    public static final int repeatYearly = 4;
    public static final int repeatingOnce = 5;
}
