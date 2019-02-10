package com.example.smmoney.misc;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import com.example.smmoney.SMMoney;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.reports.ReportDataSource;

public class PMGlobal {
    public static AccountClass currentAccount;
    public static Context currentContext;
    public static TransactionClass currentTransaction;
    public static SQLiteDatabase database;
    public static ReportDataSource datasource;
    public static boolean programaticUpdate = false;

    public static Resources resources() {
        return SMMoney.getAppContext().getResources();
    }
}
