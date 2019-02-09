package com.catamount.pocketmoney.misc;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import com.catamount.pocketmoney.PocketMoney;
import com.catamount.pocketmoney.records.AccountClass;
import com.catamount.pocketmoney.records.TransactionClass;
import com.catamount.pocketmoney.views.reports.ReportDataSource;

public class PMGlobal {
    public static AccountClass currentAccount;
    public static Context currentContext;
    public static TransactionClass currentTransaction;
    public static SQLiteDatabase database;
    public static ReportDataSource datasource;
    public static boolean programaticUpdate = false;

    public static Resources resources() {
        return PocketMoney.getAppContext().getResources();
    }
}
