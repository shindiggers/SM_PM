package com.catamount.pocketmoney.database;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteQueryBuilder;
import com.catamount.pocketmoney.misc.ExchangeRateClass;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.records.AccountClass;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.splits.SplitsActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AccountDB {
    public static HashMap<Integer, AccountClass> map = new HashMap();

    public static AccountClass getAccount(int id) {
        AccountClass retAct = map.get(id);
        if (retAct != null) {
            return retAct;
        }
        retAct = new AccountClass(id);
        map.put(id, retAct);
        return retAct;
    }

    public static ArrayList<AccountClass> queryOnViewType(int viewType) {
        String where;
        ArrayList<AccountClass> accounts = new ArrayList();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] projection = new String[]{"accountID"};
        switch (viewType) {
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                where = "deleted=0 AND totalWorth=1";
                break;
            default:
                where = "deleted=0";
                break;
        }
        qb.setTables(Database.ACCOUNTS_TABLE_NAME);
        Cursor curs = Database.query(qb, projection, where, null, null, null, "displayOrder, UPPER(account)");
        int count = curs.getCount();
        curs.moveToFirst();
        for (int i = 0; i < count; i++) {
            AccountClass acct = new AccountClass(curs.getInt(0));
            if (viewType != 1 || acct.getType() == 5) {
                accounts.add(acct);
            } else {
                double bal = acct.balanceOfType(0);
                if (bal < -0.005d || bal > 0.005d) {
                    accounts.add(acct);
                }
            }
            try {
                curs.moveToNext();
            } catch (CursorIndexOutOfBoundsException e) {
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
        if (account.length() == 0 || account.equals(Locales.kLOC_FILTERS_ALL_ACCOUNTS)) {
            Prefs.setPref(Prefs.LASTFULLEXPORT, System.currentTimeMillis());
            return;
        }
        AccountClass accountRecord = recordFor(account);
        accountRecord.hydrate();
        accountRecord.setLastSyncTime((double) System.currentTimeMillis());
        accountRecord.saveToDatabase();
    }

    public static double totalWorth(int balanceType) {
        return totalWorthForBalanceType(queryOnViewType(Prefs.getIntPref(Prefs.VIEWACCOUNTS)), balanceType);
    }

    public static double totalWorthForBalanceType(ArrayList<AccountClass> accounts, int type) {
        double totalWorth = 0.0d;
        boolean multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        Iterator it = accounts.iterator();
        while (it.hasNext()) {
            AccountClass account = (AccountClass) it.next();
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
        switch (type) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return Locales.kLOC_SHOW_BALANCES_OVERALL;
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return Locales.kLOC_SHOW_BALANCES_CLEARED;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                return Locales.kLOC_SHOW_BALANCES_CURRENT;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                return Locales.kLOC_SHOW_BALANCES_AVAILABLEFUNDS;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                return Locales.kLOC_SHOW_BALANCES_AVAILABLECREDIT;
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                return Locales.kLOC_TOOLS_FILTERED_BALANCE;
            default:
                return "";
        }
    }

    public static void updateExchangeRates() {
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            String homeCurrency = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
            ArrayList<AccountClass> accounts = queryOnViewType(0);
            ExchangeRateClass exchangeRate = new ExchangeRateClass(true, null);
            Iterator it = accounts.iterator();
            while (it.hasNext()) {
                AccountClass act = (AccountClass) it.next();
                if (act.getCurrencyCode().equals(homeCurrency)) {
                    act.setExchangeRate(1.0d);
                } else {
                    exchangeRate.updateExchangeRateForAccount(act);
                }
            }
        }
    }

    public static String[] usedCurrencyCodes() {
        ArrayList<String> codes = new ArrayList();
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
