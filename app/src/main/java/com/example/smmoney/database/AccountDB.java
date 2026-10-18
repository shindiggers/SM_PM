package com.example.smmoney.database;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.ExchangeRateClass;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;

import java.util.ArrayList;

public class AccountDB {
    public static ArrayList<AccountClass> queryOnViewType(int viewType) {
        String where;
        ArrayList<AccountClass> accounts = new ArrayList<>();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] projection = new String[]{"accountID"};
        if (viewType == Enums.kViewAccountsTotalWorth /*2*/) {
            where = "deleted=0 AND totalWorth=1";
        } else {
            where = "deleted=0";
        }
        qb.setTables(Database.ACCOUNTS_TABLE_NAME);
        Cursor curs = Database.query(qb, projection, where, null, null, null, "displayOrder, UPPER(account)");
        int count = curs.getCount();
        curs.moveToFirst();
        for (int i = 0; i < count; i++) {
            AccountClass acct = new AccountClass(curs.getInt(0));
            if (viewType != Enums.kViewAccountsNonZero/*1*/) {
                accounts.add(acct);
            } else {
                double bal = acct.balanceOfType(Enums.kBalanceTypeCleared/*1*/);
                if (bal < -0.005d || bal > 0.005d) {
                    accounts.add(acct);
                }
            }
            try {
                curs.moveToNext();
            } catch (CursorIndexOutOfBoundsException e) {
                Log.e(SMMoney.TAG, "CursorIndexOutOfBoundsException in queryOnViewType", e);
            }
        }
        curs.close();
        return accounts;
    }

    public static int uniqueID(String account) {
        return AccountClass.idForAccount(account);
    }

    public static AccountClass recordFor(String account) {
        int accountID = uniqueID(account);
        if (accountID > 0) {
            return new AccountClass(accountID);
        }
        return null;
    }

    public static void setLastExportTimestampForAccount(String account) {
        if (account.isEmpty() || account.equals(Locales.kLOC_FILTERS_ALL_ACCOUNTS)) {
            Prefs.setPref(Prefs.LASTFULLEXPORT, System.currentTimeMillis());
            return;
        }
        AccountClass accountRecord = recordFor(account);
        if (accountRecord != null) {
            accountRecord.hydrate();
        }
        if (accountRecord != null) {
            accountRecord.setLastSyncTime((double) System.currentTimeMillis());
        }
        if (accountRecord != null) {
            accountRecord.saveToDatabase();
        }
    }

    public static double totalWorth(int balanceType) {
        return totalWorthForBalanceType(queryOnViewType(Prefs.getIntPref(Prefs.VIEWACCOUNTS)), balanceType);
    }

    private static double totalWorthForBalanceType(ArrayList<AccountClass> accounts, int type) {
        double totalWorth = 0.0d;
        boolean multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        for (AccountClass account : accounts) {
            if (account.getTotalWorth()) {
                double xrate;
                if (multipleCurrencies) {
                    xrate = account.getExchangeRate();
                } else {
                    xrate = 1.0d;
                }
                totalWorth += account.balanceOfType(type) / xrate;
            }
        }
        return totalWorth;
    }

    public static String totalWorthLabel(int type) {
        return switch (type) {
            case Enums.kBalanceTypeFuture /*0*/ -> Locales.kLOC_SHOW_BALANCES_OVERALL;
            case Enums.kBalanceTypeCleared /*1*/ -> Locales.kLOC_SHOW_BALANCES_CLEARED;
            case Enums.kBalanceTypeCurrent /*2*/ -> Locales.kLOC_SHOW_BALANCES_CURRENT;
            case Enums.kBalanceTypeAvailableFunds /*3*/ ->
                    Locales.kLOC_SHOW_BALANCES_AVAILABLEFUNDS;
            case Enums.kBalanceTypeAvailableCredit /*4*/ ->
                    Locales.kLOC_SHOW_BALANCES_AVAILABLECREDIT;
            case Enums.kBalanceTypeFiltered /*5*/ -> Locales.kLOC_TOOLS_FILTERED_BALANCE;
            default -> "";
        };
    }

    public static void updateExchangeRates() {
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            String homeCurrency = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
            ArrayList<AccountClass> accounts = queryOnViewType(0);
            ExchangeRateClass exchangeRate = new ExchangeRateClass(true, null);
            for (AccountClass act : accounts) {
                if (act.getCurrencyCode().equals(homeCurrency)) {
                    act.setExchangeRate(1.0d);
                } else {
                    exchangeRate.updateExchangeRateForAccount(act);
                }
            }
        }
    }

    public static String[] usedCurrencyCodes() {
        ArrayList<String> codes = new ArrayList<>();
        Cursor c = Database.rawQuery("SELECT DISTINCT currencyCode FROM splits UNION SELECT DISTINCT currencyCode FROM accounts", null);
        while (c.moveToNext()) {
            codes.add(c.getString(0));
        }
        String[] retVal = new String[codes.size()];
        codes.toArray(retVal);
        c.close();
        return retVal;
    }
}
