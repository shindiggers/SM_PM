package com.catamount.pocketmoney.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import com.catamount.pocketmoney.PocketMoney;
import com.catamount.pocketmoney.misc.CalExt;
import com.catamount.pocketmoney.misc.CurrencyExt;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.misc.TransactionTransferRetVals;
import com.catamount.pocketmoney.records.AccountClass;
import com.catamount.pocketmoney.records.FilterClass;
import com.catamount.pocketmoney.records.RepeatingTransactionClass;
import com.catamount.pocketmoney.records.SplitsClass;
import com.catamount.pocketmoney.records.TransactionClass;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.repeating.RepeatingActivity;
import com.catamount.pocketmoney.views.splits.SplitsActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimeZone;

public class TransactionDB {
    static String match_ofxAmountDate_statement = null;
    static String match_ofxCheck_statement = null;
    static String match_ofxid_statement = null;
    static String select_matching_statement = null;

    public static String[] transactionSortTypes() {
        return new String[]{Locales.kLOC_GENERAL_DATE, Locales.kLOC_GENERAL_AMOUNT, Locales.kLOC_GENERAL_PAYEE, Locales.kLOC_GENERAL_CLASS, Locales.kLOC_GENERAL_CATEGORY, Locales.kLOC_GENERAL_NOTE, Locales.kLOC_GENERAL_ID, Locales.kLOC_GENERAL_CLEARED, Locales.kLOC_TRANSACTION_SORTDATEAMOUNT};
    }

    public static int transactionSortTypeFromString(String aType) {
        if (aType.equals(Locales.kLOC_GENERAL_DATE)) {
            return 0;
        }
        if (aType.equals(Locales.kLOC_GENERAL_AMOUNT)) {
            return 1;
        }
        if (aType.equals(Locales.kLOC_GENERAL_PAYEE)) {
            return 2;
        }
        if (aType.equals(Locales.kLOC_GENERAL_CLASS)) {
            return 3;
        }
        if (aType.equals(Locales.kLOC_GENERAL_CATEGORY)) {
            return 4;
        }
        if (aType.equals(Locales.kLOC_GENERAL_NOTE)) {
            return 5;
        }
        if (aType.equals(Locales.kLOC_GENERAL_ID)) {
            return 6;
        }
        if (aType.equals(Locales.kLOC_GENERAL_CLEARED)) {
            return 7;
        }
        if (aType.equals(Locales.kLOC_TRANSACTION_SORTDATEAMOUNT)) {
            return 8;
        }
        return 0;
    }

    public static String queryWithFilterOrderByClause() {
        String orderString;
        String sortFieldString;
        if (Prefs.getStringPref(Prefs.NEWESTTRANSACTIONFIRST).equals(Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING)) {
            orderString = "DESC";
        } else {
            orderString = "ASC";
        }
        switch (transactionSortTypeFromString(Prefs.getStringPref(Prefs.TRANSACTIONS_SORTON))) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                sortFieldString = "t.subtotal";
                break;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                sortFieldString = "UPPER(t.payee)";
                break;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                sortFieldString = "UPPER(s.classID)";
                break;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                sortFieldString = "UPPER(s.categoryID)";
                break;
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                sortFieldString = "UPPER(s.memo)";
                break;
            case LookupsListActivity.CLASS_LOOKUP /*6*/:
                sortFieldString = "UPPER(t.checkNumber)";
                break;
            case LookupsListActivity.ID_LOOKUP /*7*/:
                sortFieldString = "t.cleared";
                break;
            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                return "(t.date + " + timeOffsetForCurrentTimezone() + ")/86400 " + orderString + ", t.subTotal DESC";
            default:
                sortFieldString = "t.date";
                break;
        }
        return new StringBuilder(String.valueOf(sortFieldString)).append(" ").append(orderString).toString();
    }

    public static long timeOffsetForCurrentTimezone() {
        return (long) (TimeZone.getDefault().getOffset(0) / 1000);
    }

    public static String queryWithFilterWhereClause(FilterClass filter) {
        String where = "deleted=0";
        int type = filter.getType();
        if (type == 5) {
            where = where.concat(" AND t.type = " + type);
        } else if (type == 4) {
            where = where.concat(" AND t.type <> 5");
        } else if (type == 3 || type == 2) {
            where = where.concat(" AND t.type >= 2 AND t.type <= 3");
        } else {
            where = where.concat(" AND t.type = " + type);
        }
        if (!filter.allAccounts()) {
            where = where.concat(" AND t.accountID=" + AccountClass.idForAccount(filter.getAccount()));
        } else if (2 == Prefs.getIntPref(Prefs.VIEWACCOUNTS)) {
            where = where.concat(" AND t.accountID IN (SELECT accountID FROM accounts WHERE deleted=0 AND totalWorth=1)");
        } else if (1 == Prefs.getIntPref(Prefs.VIEWACCOUNTS)) {
            where = where.concat(" AND t.accountID IN (SELECT accountID FROM transactions WHERE deleted=0 AND type<>5 GROUP BY accountID HAVING (sum(subTotal) < -0.005) OR (sum(subTotal) > 0.005))");
        }
        if (!(filter.getDate() == null || filter.getDate().length() <= 0 || filter.getDate().equals(Locales.kLOC_FILTER_DATES_ALL))) {
            if (filter.getDate().equals(Locales.kLOC_FILTER_DATES_RECENTLYCHANGED)) {
                filter.allAccounts();
            } else if (filter.getDate().equals(Locales.kLOC_FILTER_DATES_MODIFIEDTODAY)) {
                where = where.concat(" AND (t.timestamp >= " + (CalExt.beginningOfToday().getTimeInMillis() / 1000) + " AND t.timestamp <= " + (CalExt.endOfToday().getTimeInMillis() / 1000) + ")");
            } else {
                long fromDate;
                long toDate;
                if (filter.getDate().equals(Locales.kLOC_FILTER_DATES_CUSTOM)) {
                    if (filter.getDateFrom() != null) {
                        fromDate = CalExt.beginningOfDay(filter.getDateFrom()).getTimeInMillis() / 1000;
                    } else {
                        fromDate = CalExt.distantPast().getTimeInMillis() / 1000;
                    }
                    if (filter.getDateTo() != null) {
                        toDate = CalExt.endOfDay(filter.getDateTo()).getTimeInMillis() / 1000;
                    } else {
                        toDate = CalExt.distantFuture().getTimeInMillis() / 1000;
                    }
                } else {
                    fromDate = FilterClass.convertFilterDateIsFromDate(filter.getDate(), true);
                    toDate = FilterClass.convertFilterDateIsFromDate(filter.getDate(), false);
                }
                where = new StringBuilder(String.valueOf(where)).append(" AND t.date>=").append(fromDate).append(" AND t.date<=").append(toDate).toString();
            }
        }
        if (filter.getPayee().length() > 0) {
            where = where.concat(" AND t.payee LIKE " + Database.SQLFormat(filter.getPayee() + "%"));
        }
        if (filter.getCheckNumber().length() > 0) {
            where = where.concat(" AND t.checkNumber LIKE " + Database.SQLFormat(filter.getCheckNumber()));
        }
        if (filter.getCleared() == 1) {
            where = where.concat(" AND t.cleared = 1");
        } else if (filter.getCleared() == 0) {
            where = where.concat(" AND t.cleared = 0");
        }
        if (filter.getCategory().length() > 0 && !filter.getCategory().equals(Locales.kLOC_FILTERS_ALL_CATEGORIES)) {
            if (filter.getCategory().equals(Locales.kLOC_FILTERS_UNFILED)) {
                where = where.concat(" AND s.categoryID = ''");
            } else {
                where = where.concat(" AND s.categoryID LIKE " + Database.SQLFormat(filter.getCategory()));
            }
        }
        if (filter.getClassName().length() <= 0 || filter.getClassName().equals(Locales.kLOC_FILTERS_ALL_CLASSES)) {
            return where;
        }
        if (filter.getClassName().equals(Locales.kLOC_FILTERS_UNFILED)) {
            return where.concat(" AND (s.classID = '' OR s.classID ISNULL)");
        }
        return where.concat(" AND s.classID LIKE " + Database.SQLFormat(filter.getClassName()));
    }

    public static String queryWithFilterSpotlightClause(String spotlight) {
        if (spotlight == null || spotlight.length() == 0) {
            return "1";
        }
        if (spotlight.startsWith("<")) {
            return "s.amount < " + CurrencyExt.amountFromString(spotlight.replace("<", ""));
        }
        if (spotlight.startsWith("=")) {
            return "s.amount = " + CurrencyExt.amountFromString(spotlight.replace("=", ""));
        }
        if (spotlight.startsWith(">")) {
            return "s.amount > " + CurrencyExt.amountFromString(spotlight.replace(">", ""));
        }
        if (spotlight.contains("...")) {
            int index = spotlight.indexOf("...");
            double from = CurrencyExt.amountFromString(spotlight.substring(0, index));
            double to = CurrencyExt.amountFromString(spotlight.substring(index + 3, spotlight.length()));
            if (from > to) {
                double swap = to;
                to = from;
            }
            return "s.amount >= " + from + " AND s.amount <= " + to;
        }
        double aNum = CurrencyExt.amountFromString(spotlight);
        String amountWhere = "";
        if (aNum > 0.0d) {
            amountWhere = "OR s.amount = " + aNum + " OR s.amount = " + (-1.0d * aNum);
        }
        return "t.checkNumber LIKE " + Database.SQLFormat("%" + spotlight + "%") + " OR " + "t.payee LIKE " + Database.SQLFormat("%" + spotlight + "%") + " OR " + "s.memo LIKE " + Database.SQLFormat("%" + spotlight + "%") + " OR " + "s.categoryID LIKE " + Database.SQLFormat("%" + spotlight + "%") + " OR " + "s.classID LIKE " + Database.SQLFormat("%" + spotlight + "%") + " OR " + "s.currencyCode LIKE " + Database.SQLFormat("%" + spotlight + "%") + " " + amountWhere;
    }

    public static ArrayList<TransactionClass> queryWithFilter(FilterClass filter) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] projection = new String[]{"t.transactionID", "t.subTotal"};
        double runningBalance = 0.0d;
        ArrayList<TransactionClass> list = new ArrayList();
        qb.setDistinct(true);
        qb.setTables("transactions t INNER JOIN splits s ON t.transactionID=s.transactionID");
        String where = queryWithFilterWhereClause(filter) + " AND (" + queryWithFilterSpotlightClause(filter.getSpotlight()) + ")";
        where.replaceAll("s.", "splits.");
        where.replaceAll("t.", "transactions.");
        Cursor curs = Database.query(qb, projection, where, null, null, null, queryWithFilterOrderByClause());
        if (curs.getCount() == 0) {
            curs.close();
        } else {
            boolean descending = Prefs.getStringPref(Prefs.NEWESTTRANSACTIONFIRST).equals(Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING);
            if (descending) {
                curs.moveToLast();
            } else {
                curs.moveToFirst();
            }
            while (true) {
                int transactionID = curs.getInt(0);
                double amount = curs.getDouble(1);
                TransactionClass transaction = new TransactionClass(transactionID);
                transaction.setSubTotal(amount);
                transaction.runningBalance = runningBalance + amount;
                runningBalance += amount;
                transaction.dirty = false;
                if (descending) {
                    list.add(0, transaction);
                } else {
                    list.add(transaction);
                }
                if (descending) {
                    if (!curs.moveToPrevious()) {
                        break;
                    }
                } else if (!curs.moveToNext()) {
                    break;
                }
            }
            curs.close();
        }
        return list;
    }

    public static TransactionClass[] queryWithFilterToCArray(FilterClass filter) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] projection = new String[]{"t.transactionID", "t.subTotal"};
        double runningBalance = 0.0d;
        qb.setDistinct(true);
        qb.setTables("transactions t INNER JOIN splits s ON t.transactionID=s.transactionID");
        String where = queryWithFilterWhereClause(filter) + " AND (" + queryWithFilterSpotlightClause(filter.getSpotlight()) + ")";
        where.replaceAll("s.", "splits.");
        where.replaceAll("t.", "transactions.");
        Cursor curs = Database.query(qb, projection, where, null, null, null, queryWithFilterOrderByClause());
        int cursCount = curs.getCount();
        if (cursCount == 0) {
            curs.close();
            return null;
        }
        TransactionClass[] list = new TransactionClass[cursCount];
        int i = 0;
        while (curs.moveToNext()) {
            int transactionID = curs.getInt(0);
            double amount = curs.getDouble(1);
            TransactionClass transaction = new TransactionClass(transactionID);
            transaction.setSubTotal(amount);
            transaction.runningBalance = runningBalance + amount;
            runningBalance += amount;
            transaction.dirty = false;
            int i2 = i + 1;
            list[i] = transaction;
            i = i2;
        }
        curs.close();
        return list;
    }

    public static ArrayList<TransactionClass> queryWithFilterOrderByAccount(FilterClass filter) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] projection = new String[]{"t.transactionID", "t.subTotal"};
        double runningBalance = 0.0d;
        ArrayList<TransactionClass> list = new ArrayList();
        qb.setDistinct(true);
        qb.setTables("transactions t INNER JOIN splits s ON t.transactionID=s.transactionID");
        String where = queryWithFilterWhereClause(filter) + " AND (" + queryWithFilterSpotlightClause(filter.getSpotlight()) + ")";
        where.replaceAll("s.", "splits.");
        where.replaceAll("t.", "transactions.");
        Cursor curs = Database.query(qb, projection, where, null, null, null, "t.accountID ASC");
        if (curs.getCount() == 0) {
            curs.close();
        } else {
            boolean descending = Prefs.getStringPref(Prefs.NEWESTTRANSACTIONFIRST).equals(Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING);
            if (descending) {
                curs.moveToLast();
            } else {
                curs.moveToFirst();
            }
            while (true) {
                int transactionID = curs.getInt(0);
                double amount = curs.getDouble(1);
                TransactionClass transaction = new TransactionClass(transactionID);
                transaction.setSubTotal(amount);
                transaction.runningBalance = runningBalance + amount;
                runningBalance += amount;
                transaction.dirty = false;
                if (descending) {
                    list.add(0, transaction);
                } else {
                    list.add(transaction);
                }
                if (descending) {
                    if (!curs.moveToPrevious()) {
                        break;
                    }
                } else if (!curs.moveToNext()) {
                    break;
                }
            }
            curs.close();
        }
        return list;
    }

    public static boolean splitIDExistsWithinFilter(int splitID, FilterClass filter) {
        int transactionID = 0;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] projection = new String[]{"t.transactionID", "s.splitID"};
        qb.setTables("transactions t INNER JOIN splits s ON t.transactionID=s.transactionID");
        Cursor curs = Database.query(qb, projection, "s.splitID=" + splitID + " AND " + queryWithFilterWhereClause(filter) + " AND (" + queryWithFilterSpotlightClause(filter.getSpotlight()) + ")", null, null, null, null);
        while (curs.moveToNext()) {
            transactionID = curs.getInt(0);
        }
        curs.close();
        return transactionID != 0;
    }

    public static double balanceWithFilter(FilterClass filter) {
        double amount = 0.0d;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setDistinct(true);
        String[] projection = new String[1];
        projection[0] = "sum(s.amount " + (filter.allAccounts() ? "/ (SELECT CASE WHEN exchangeRate >0 THEN exchangeRate ELSE 1.0 END FROM accounts WHERE accountID = t.accountID )" : "") + ")";
        qb.setTables("transactions t INNER JOIN splits s ON t.transactionID=s.transactionID");
        Cursor curs = Database.query(qb, projection, queryWithFilterWhereClause(filter) + " AND (" + queryWithFilterSpotlightClause(filter.getSpotlight()) + ")", null, null, null, null);
        if (curs.getCount() > 0) {
            curs.moveToFirst();
            amount = curs.getDouble(0);
        }
        curs.close();
        return amount;
    }

    public static double cashFlowBalanceWith(int type, GregorianCalendar fromDate, GregorianCalendar toDate) {
        double amount = 0.0d;
        int viewAccountType = Prefs.getIntPref(Prefs.VIEWACCOUNTS);
        String sql = "";
        String accountsWhere = "";
        if (2 == viewAccountType) {
            accountsWhere = " AND t.accountID IN (SELECT accountID FROM accounts WHERE deleted=0 AND totalWorth=1)  AND (NOT s.transferToAccountID IN (SELECT accountID FROM accounts WHERE deleted=0 AND totalWorth=1))";
        } else if (1 == viewAccountType) {
            accountsWhere = " AND t.accountID IN (SELECT accountID FROM transactions WHERE deleted=0 AND type<>5 GROUP BY accountID HAVING (sum(subTotal) < -0.005) OR (sum(subTotal) > 0.005))";
        }
        StringBuilder append = new StringBuilder("SELECT  sum(s.amount / (SELECT CASE WHEN exchangeRate >0 THEN exchangeRate ELSE 1.0 END FROM accounts WHERE accountID = t.accountID )) FROM transactions t INNER JOIN splits s ON t.transactionID=s.transactionID WHERE deleted=0 AND type<>5 AND ").append(type == 0 ? -1.0d : 1.0d).append(" * s.amount > 0 AND t.date>=");
        if (fromDate == null) {
            fromDate = CalExt.distantPast();
        }
        append = append.append(CalExt.beginningOfDay(fromDate).getTimeInMillis() / 1000).append(" AND t.date<=");
        if (toDate == null) {
            toDate = CalExt.distantFuture();
        }
        Cursor curs = Database.rawQuery(append.append(CalExt.beginningOfDay(toDate).getTimeInMillis() / 1000).append(" ").append(accountsWhere).toString(), null);
        if (curs.getCount() > 0) {
            curs.moveToFirst();
            amount = curs.getDouble(0);
        }
        curs.close();
        return amount;
    }

    public static void rollupTransactionsInFilter(ArrayList<TransactionClass> transactions, FilterClass filter) {
        Hashtable<String, Hashtable<String, Hashtable<String, Double>>> accountListings = new Hashtable();
        GregorianCalendar lastDate = null;
        String com = PocketMoney.getAppContext().getPackageName();
        Iterator it = transactions.iterator();
        while (it.hasNext()) {
            TransactionClass transaction = (TransactionClass) it.next();
            Hashtable<String, Hashtable<String, Double>> accountRollup = accountListings.get(transaction.getAccount());
            if (accountRollup == null) {
                accountListings.put(transaction.getAccount(), new Hashtable());
                accountRollup = accountListings.get(transaction.getAccount());
            }
            Iterator it2 = transaction.getSplits().iterator();
            while (it2.hasNext()) {
                Double amount;
                SplitsClass split = (SplitsClass) it2.next();
                Hashtable<String, Double> categoryRollup = accountRollup.get(split.getCategory());
                if (categoryRollup == null) {
                    accountRollup.put(split.getCategory(), new Hashtable());
                    categoryRollup = accountRollup.get(split.getCategory());
                }
                if (transaction.getCleared()) {
                    amount = categoryRollup.get("cleared");
                    if (amount == null) {
                        amount = 0.0d;
                    }
                    categoryRollup.put("cleared", amount + split.getAmount());
                } else {
                    amount = categoryRollup.get("uncleared");
                    if (amount == null) {
                        amount = 0.0d;
                    }
                    categoryRollup.put("uncleared", amount + split.getAmount());
                }
            }
            lastDate = transaction.getDate();
        }
        it = transactions.iterator();
        while (it.hasNext()) {
            TransactionClass transaction = (TransactionClass) it.next();
            disconnectionTransfersForTransaction(transaction);
            transaction.deleteFromDatabase();
        }
        if (lastDate.getTimeInMillis() > System.currentTimeMillis()) {
            lastDate = new GregorianCalendar();
        }
        Enumeration<String> e = accountListings.keys();
        while (e.hasMoreElements()) {
            String accountNameKey = e.nextElement();
            Hashtable<String, Hashtable<String, Double>> accountRollup = accountListings.get(accountNameKey);
            String accountCurrencyCode = AccountDB.recordFor(accountNameKey).getCurrencyCode();
            TransactionClass transaction = new TransactionClass();
            transaction.setAccount(accountNameKey);
            transaction.setDate(lastDate);
            if (filter.getPayee() == null || filter.getPayee().length() <= 0) {
                transaction.setPayee(Locales.kLOC_TOOLS_ROLLUP);
            } else {
                transaction.setPayee(filter.getPayee());
            }
            transaction.setCleared(false);
            TransactionClass clearedTransaction = new TransactionClass();
            clearedTransaction.setAccount(accountNameKey);
            clearedTransaction.setDate(lastDate);
            if (filter.getPayee() == null || filter.getPayee().length() <= 0) {
                clearedTransaction.setPayee(Locales.kLOC_TOOLS_ROLLUP);
            } else {
                clearedTransaction.setPayee(filter.getPayee());
            }
            clearedTransaction.setCleared(true);
            Enumeration<String> en = accountRollup.keys();
            while (en.hasMoreElements()) {
                String categoryKey = en.nextElement();
                Hashtable<String, Double> categoryRollup = accountRollup.get(categoryKey);
                Double amount = categoryRollup.get("uncleared");
                if (!(amount == null || amount == 0.0d)) {
                    SplitsClass split = new SplitsClass();
                    split.hydrated = true;
                    split.setCategory(categoryKey);
                    split.setAmount(amount);
                    split.setCurrencyCode(accountCurrencyCode);
                    transaction.getSplits().add(split);
                    transaction.setSubTotal(transaction.getSubTotal() + split.getAmount());
                }
                amount = categoryRollup.get("cleared");
                if (!(amount == null || amount == 0.0d)) {
                    SplitsClass split = new SplitsClass();
                    split.hydrated = true;
                    split.setCategory(categoryKey);
                    split.setAmount(amount);
                    split.setCurrencyCode(accountCurrencyCode);
                    clearedTransaction.getSplits().add(split);
                    clearedTransaction.setSubTotal(clearedTransaction.getSubTotal() + split.getAmount());
                }
            }
            if (transaction.getNumberOfSplits() > 1 || transaction.getSubTotal() != 0.0d) {
                transaction.getSplits().remove(0);
                transaction.setMemo(Locales.kLOC_TOOLS_ROLLUP_NOTE);
                transaction.initType();
                transaction.saveToDatabase();
            }
            if (clearedTransaction.getNumberOfSplits() > 1 || clearedTransaction.getSubTotal() != 0.0d) {
                clearedTransaction.getSplits().remove(0);
                clearedTransaction.setMemo(Locales.kLOC_TOOLS_ROLLUP_NOTE);
                clearedTransaction.initType();
                clearedTransaction.saveToDatabase();
            }
        }
    }

    public static void disconnectionTransfersForTransaction(TransactionClass transaction) {
        Iterator it = transaction.getSplits().iterator();
        while (it.hasNext()) {
            SplitsClass split = (SplitsClass) it.next();
            if (split.getTransferToAccount() != null && split.getTransferToAccount().length() > 0) {
                if (AccountDB.recordFor(split.getTransferToAccount()) == null) {
                    split.setCategory(split.getTransferToAccount());
                    split.setTransferToAccount("");
                } else {
                    double amount;
                    String str;
                    boolean regularTransfer = split.getCurrencyCode().equals(AccountDB.recordFor(split.getTransferToAccount()).getCurrencyCode());
                    TransactionTransferRetVals ret = new TransactionTransferRetVals();
                    String transferToAccount = split.getTransferToAccount();
                    String account = transaction.getAccount();
                    GregorianCalendar date = transaction.getDate();
                    if (regularTransfer) {
                        amount = (-1.0d * split.getAmount()) / split.getXrate();
                    } else {
                        amount = -1.0d * split.getAmount();
                    }
                    if (regularTransfer) {
                        str = null;
                    } else {
                        str = split.getCurrencyCode();
                    }
                    transactionGetTransfer(transferToAccount, account, date, amount, str, ret);
                    int transferRecID = ret.transferRecID;
                    int transferSplitItem = ret.transferSplitItem;
                    if (transferRecID != 0) {
                        TransactionClass transferRec = new TransactionClass(transferRecID);
                        if (transferRec.getPayee() == null || transferRec.getPayee().length() == 0) {
                            transferRec.setPayee(transferRec.getTransferToAccountAtIndex(transferSplitItem));
                        } else if (transferRec.getCategoryAtIndex(transferSplitItem) == null || transferRec.getCategoryAtIndex(transferSplitItem).length() == 0) {
                            transferRec.setCategoryAtIndex(transferRec.getTransferToAccountAtIndex(transferSplitItem), transferSplitItem);
                        }
                        transferRec.setTransferToAccountAtIndex(null, transferSplitItem);
                        transferRec.initType();
                        transferRec.saveToDatabase();
                    }
                }
            }
        }
    }

    public static void transactionGetTransfer(String transToAccount, String transFromAccount, GregorianCalendar transDate, double transAmount, String tToCurrencyCode, TransactionTransferRetVals ret) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] projection = new String[]{"transactionID"};
        ret.transferRecID = 0;
        ret.transferSplitItem = 0;
        if (transToAccount != null && transToAccount.length() != 0) {
            int transToAccountID = AccountDB.uniqueID(transToAccount);
            qb.setDistinct(true);
            qb.setTables(Database.TRANSACTIONS_TABLE_NAME);
            Cursor curs = Database.query(qb, projection, "deleted=0 AND accountID=" + transToAccountID + " AND date=" + (transDate.getTimeInMillis() / 1000) + " AND type<>" + 5, null, null, null, null);
            if (curs.getCount() == 0) {
                curs.close();
                return;
            }
            curs.moveToPosition(-1);
            while (curs.moveToNext()) {
                int transactionID = curs.getInt(0);
                int splitIndex = 0;
                Iterator it = new TransactionClass(transactionID).getSplits().iterator();
                while (it.hasNext()) {
                    SplitsClass split = (SplitsClass) it.next();
                    double diff = transAmount - split.getAmount();
                    if (CurrencyExt.amountAsCurrency(transAmount).equals(CurrencyExt.amountAsCurrency(split.getAmount()))) {
                        diff = 0.0d;
                    }
                    if (diff < -9.0E-6d || diff > 1.0E-6d) {
                        if (tToCurrencyCode != null) {
                            if (!tToCurrencyCode.equals(split.getCurrencyCode())) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                        splitIndex++;
                    }
                    if (transFromAccount.compareToIgnoreCase(split.getTransferToAccount() == null ? "" : split.getTransferToAccount()) == 0) {
                        ret.transferRecID = transactionID;
                        ret.transferSplitItem = splitIndex;
                        curs.close();
                        return;
                    }
                    splitIndex++;
                }
            }
            curs.close();
        }
    }

    public static int getRepeatingTransactionFor(TransactionClass aTransaction) {
        int i = 0;
        if (aTransaction.getAccount() == null || aTransaction.getAccount().length() == 0) {
            return 0;
        }
        int accountID = AccountDB.uniqueID(aTransaction.getAccount());
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("transactions t INNER JOIN repeatingTransactions r ON t.transactionID = r.transactionID");
        Cursor curs = Database.query(qb, new String[]{"t.transactionID", "r.repeatingID"}, "t.deleted=0 AND t.type=5 AND t.accountID=" + accountID + " AND t.payee=" + Database.SQLFormat(aTransaction.getPayee()) + " AND t.subTotal=" + aTransaction.getSubTotal(), null, null, null, null);
        while (curs.moveToNext()) {
            i = curs.getInt(0);
            RepeatingTransactionClass repeatingTransaction = new RepeatingTransactionClass(curs.getInt(1));
            repeatingTransaction.hydrate();
            if (repeatingTransaction.repeatsOnDate(aTransaction.getDate())) {
                TransactionClass transactionClass = new TransactionClass(i);
                transactionClass.hydrate();
                if (transactionClass.getNumberOfSplits() == aTransaction.getNumberOfSplits()) {
                    int splitIndex = 0;
                    Iterator it = transactionClass.getSplits().iterator();
                    while (it.hasNext()) {
                        SplitsClass split = (SplitsClass) it.next();
                        SplitsClass matchSplit = aTransaction.getSplits().get(splitIndex);
                        if (matchSplit.amountAsString().equals(split.amountAsString())) {
                            if (matchSplit.getCategory().equals(split.getCategory())) {
                                if (matchSplit.getTransferToAccount() != null && split.getTransferToAccount() != null && !matchSplit.getTransferToAccount().equalsIgnoreCase(split.getTransferToAccount())) {
                                    i = 0;
                                    break;
                                }
                                splitIndex++;
                            } else {
                                i = 0;
                                break;
                            }
                        }
                        i = 0;
                        break;
                    }
                }
                i = 0;
            } else {
                i = 0;
            }
            if (i != 0) {
                break;
            }
        }
        curs.close();
        return i;
    }

    public static void setupNotifications() {
        Iterator it = queryAllRepeatingTransactions().iterator();
        while (it.hasNext()) {
            RepeatingTransactionClass repeatingTrans = (RepeatingTransactionClass) it.next();
            if (repeatingTrans.getSendLocalNotifications()) {
                repeatingTrans.setupNotification(PocketMoney.getAppContext());
            }
        }
    }

    public static boolean addRepeatingTransactions() {
        if (!Prefs.getBooleanPref(Prefs.RECURPOSTINGENABLED)) {
            return false;
        }
        GregorianCalendar processRepeatingEventsThrough = new GregorianCalendar();
        processRepeatingEventsThrough.add(Calendar.DAY_OF_YEAR, Prefs.getIntPref(Prefs.RECURRDAYSINADVANCE));
        return addRepeatingEventsThroughDate(processRepeatingEventsThrough, PocketMoney.getAppContext(), true);
    }

    public static boolean addRepeatingEventsThroughDate(GregorianCalendar checkDate, RepeatingActivity repeating) {
        return addRepeatingEventsThroughDate(checkDate, repeating, false);
    }

    public static boolean addRepeatingEventsThroughDate(GregorianCalendar checkDate, Context context, boolean ignoreRepeating) {
        boolean repeatingTransactionAdded = false;
        checkDate = CalExt.beginningOfDay(checkDate);
        Iterator it = queryAllRepeatingTransactions().iterator();
        while (it.hasNext()) {
            RepeatingTransactionClass repeatingTransaction = (RepeatingTransactionClass) it.next();
            if (repeatingTransaction.getTransaction() != null && repeatingTransaction.getTransaction().getDate() != null) {
                GregorianCalendar lastDate = CalExt.beginningOfDay(repeatingTransaction.getLastProcessedDate());
                repeatingTransaction.hydrate();
                if (repeatingTransaction.getSendLocalNotifications() && ignoreRepeating) {
                    repeatingTransaction.setupNotification(context);
                } else {
                    while (CalExt.beginningOfDay(repeatingTransaction.getTransaction().getDate()).before(checkDate)) {
                        postTransactionOnDate(repeatingTransaction, repeatingTransaction.getTransaction().getDate());
                        lastDate = repeatingTransaction.getTransaction().getDate();
                        repeatingTransactionAdded = true;
                        if (repeatingTransaction.getType() == 5 || repeatingTransaction.getTransaction().getDate().after(repeatingTransaction.getEndDate())) {
                            repeatingTransaction.getTransaction().deleteFromDatabase();
                            repeatingTransaction.deleteFromDatabase();
                            repeatingTransaction.saveToDatabase();
                            break;
                        }
                        repeatingTransaction.advanceTransactionDateToNextPostDateAfterDate(repeatingTransaction.getTransaction().getDate());
                        if (repeatingTransaction.getTransaction().dirty) {
                            repeatingTransaction.dirty = true;
                        }
                        if (repeatingTransaction.getType() == 5 || repeatingTransaction.getTransaction().getDate().after(repeatingTransaction.getEndDate())) {
                            repeatingTransaction.getTransaction().deleteFromDatabase();
                            repeatingTransaction.deleteFromDatabase();
                            repeatingTransaction.saveToDatabase();
                            break;
                        }
                        if (CalExt.beginningOfDay(repeatingTransaction.getLastProcessedDate()).before(CalExt.beginningOfDay(lastDate))) {
                            repeatingTransaction.setLastProcessedDate(lastDate);
                        }
                        repeatingTransaction.saveToDatabase();
                    }
                }
            }
        }
        return repeatingTransactionAdded;
    }

    public static void fixRepeatingTransactionsThatDontRepeatOnDate() {
        for (RepeatingTransactionClass r : queryAllRepeatingTransactions()) {
            r.hydrate();
            TransactionClass typeFive = r.getTransaction();
            if (typeFive == null) {
                Log.i(PocketMoney.TAG, "repeating Transaction is unlinked");
            } else if (new RepeatingTransactionClass(typeFive, true).getTransaction() == null) {
                r.advanceTransactionDateToNextPostDateAfterDateIgnoringCurrentlySetDate(typeFive.getDate());
                r.saveToDatabase();
            }
        }
    }

    public static ArrayList<RepeatingTransactionClass> queryAllRepeatingTransactions() {
        ArrayList<RepeatingTransactionClass> array = new ArrayList();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] projection = new String[]{"repeatingID"};
        qb.setTables(Database.REPEATINGTRANSACTIONS_TABLE_NAME);
        Cursor curs = Database.query(qb, projection, "deleted=0", null, null, null, null);
        while (curs.moveToNext()) {
            array.add(new RepeatingTransactionClass(curs.getInt(0)));
        }
        curs.close();
        return array;
    }

    public static ArrayList<RepeatingTransactionClass> queryAllRepeatingTransactionsWithLocalNotifications() {
        ArrayList<RepeatingTransactionClass> array = new ArrayList();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String[] projection = new String[]{"repeatingID"};
        qb.setTables(Database.REPEATINGTRANSACTIONS_TABLE_NAME);
        Cursor curs = Database.query(qb, projection, "deleted=0 AND sendLocalNotifications=1", null, null, null, null);
        while (curs.moveToNext()) {
            array.add(new RepeatingTransactionClass(curs.getInt(0)));
        }
        curs.close();
        return array;
    }

    public static void postTransactionOnDate(RepeatingTransactionClass repeatingTransaction, GregorianCalendar date) {
        TransactionClass transaction = repeatingTransaction.getTransaction().copy();
        transaction.initType();
        transaction.isRepeatingTransaction = false;
        transaction.setImageLocation("");
        transaction.setCleared(false);
        transaction.setDate(date);
        transaction.saveToDatabase();
        for (int i = 0; i < transaction.getNumberOfSplits(); i++) {
            String transferToString = transaction.getTransferToAccountAtIndex(i);
            if (transferToString != null && transferToString.length() > 0) {
                TransactionClass transferRecord = new TransactionClass();
                transferRecord.setAccount(transaction.getTransferToAccountAtIndex(i));
                transferRecord.setPayee(transaction.getPayee());
                transferRecord.setTransferToAccount(transaction.getAccount());
                transferRecord.setCategory(transaction.getCategoryAtIndex(i));
                transferRecord.setClassName(transaction.getCategoryAtIndex(i));
                transferRecord.setMemo(transaction.getMemoAtIndex(i));
                transferRecord.setCurrencyCode(transaction.getCurrencyCodeAtIndex(i));
                transferRecord.setSubTotal((-transaction.getAmountAtIndex(i)) / transaction.getXrateAtIndex(i));
                transferRecord.setAmount((-transaction.getAmountAtIndex(i)) / transaction.getXrateAtIndex(i));
                transferRecord.setXrate(1.0d / transaction.getXrateAtIndex(i));
                transferRecord.setDate(transaction.getDate());
                transferRecord.setCleared(transaction.getCleared());
                transferRecord.initType();
                transferRecord.saveToDatabase();
            }
        }
    }

    public static void postTransactionOnDateWithSubtotal(RepeatingTransactionClass repeatingTransaction, GregorianCalendar date, double subtotal) {
        TransactionClass transaction = repeatingTransaction.getTransaction().copy();
        transaction.initType();
        transaction.isRepeatingTransaction = false;
        transaction.setImageLocation("");
        transaction.setCleared(false);
        int sign = (int) (transaction.getSubTotal() / Math.abs(transaction.getSubTotal()));
        transaction.setSubTotal(subtotal);
        transaction.setDate(date);
        transaction.saveToDatabase();
        for (int i = 0; i < transaction.getNumberOfSplits(); i++) {
            String transferToString = transaction.getTransferToAccountAtIndex(i);
            if (transferToString != null && transferToString.length() > 0) {
                TransactionClass transferRecord = new TransactionClass();
                transferRecord.setAccount(transaction.getTransferToAccountAtIndex(i));
                transferRecord.setPayee(transaction.getPayee());
                transferRecord.setTransferToAccount(transaction.getAccount());
                transferRecord.setCategory(transaction.getCategoryAtIndex(i));
                transferRecord.setClassName(transaction.getCategoryAtIndex(i));
                transferRecord.setMemo(transaction.getMemoAtIndex(i));
                transferRecord.setCurrencyCode(transaction.getCurrencyCodeAtIndex(i));
                transferRecord.setSubTotal((-transaction.getSubTotal()) / transaction.getXrateAtIndex(i));
                transferRecord.setAmount((-transaction.getAmountAtIndex(i)) / transaction.getXrateAtIndex(i));
                transferRecord.setXrate(1.0d / transaction.getXrateAtIndex(i));
                transferRecord.setDate(transaction.getDate());
                transferRecord.setCleared(transaction.getCleared());
                transferRecord.initType();
                transferRecord.saveToDatabase();
            }
        }
    }

    public static void skipTransactionToDate(RepeatingTransactionClass repeatingTransaction, GregorianCalendar date) {
        repeatingTransaction.advanceTransactionDateToNextPostDateAfterDate(date);
        if (CalExt.beginningOfDay(repeatingTransaction.getLastProcessedDate()).before(date)) {
            repeatingTransaction.setLastProcessedDate(date);
        }
        repeatingTransaction.setupNotification(PocketMoney.getAppContext());
        repeatingTransaction.saveToDatabase();
    }

    public static void deleteRecordsFromAccount(String act) {
        FilterClass filter = new FilterClass();
        filter.setAccount(act);
        Iterator it = queryWithFilter(filter).iterator();
        while (it.hasNext()) {
            TransactionClass trans = (TransactionClass) it.next();
            disconnectionTransfersForTransaction(trans);
            trans.deleteFromDatabase();
        }
    }

    public static void deleteRepeatingRecordsFromAccount(String account) {
        Database.execSQL("UPDATE repeatingTransactions SET deleted='1', timestamp=" + Long.toString(System.currentTimeMillis()) + " WHERE transactionID IN (SELECT transactionID FROM transactions WHERE deleted = 0 AND accountID IN (SELECT accountID FROM accounts WHERE account LIKE " + Database.SQLFormat(account) + " AND deleted = 0))");
    }

    public static void deleteRepetaingRecordsFromTransactionForAccount(String account) {
        Database.execSQL("UPDATE transactions SET deleted='1', timestamp=" + System.currentTimeMillis() + " WHERE type=" + 5 + " AND accountID IN (SELECT accountID FROM accounts WHERE account = " + Database.SQLFormat(account) + ")");
    }

    public static int transactionIDForOFXID(String ofxID) {
        int transactionID = 0;
        if (match_ofxid_statement == null) {
            match_ofxid_statement = "SELECT transactionID FROM transactions WHERE deleted=0 AND ofxid LIKE ?";
        }
        Cursor c = Database.rawQuery(match_ofxid_statement, new String[]{ofxID});
        if (c.getCount() > 0) {
            c.moveToFirst();
            transactionID = c.getInt(0);
        }
        c.close();
        return transactionID;
    }

    public static int transactionIDForCheckNumber(String checkNumber, double amount, GregorianCalendar date, String account) {
        int transactionID = 0;
        if (match_ofxCheck_statement == null) {
            match_ofxCheck_statement = "SELECT transactionID FROM transactions WHERE deleted=0 AND checkNumber LIKE ?  AND subTotal > ? AND subTotal < ? AND date >= ? AND date <= ? AND (ofxID ISNULL OR (NOT LENGTH(ofxID) > 0)) AND accountID IN (SELECT accountID FROM accounts WHERE account LIKE ?)";
        }
        Cursor c = Database.rawQuery(match_ofxCheck_statement, new String[]{checkNumber, String.valueOf(amount - 1.0E-6d), String.valueOf(1.0E-6d + amount), String.valueOf((CalExt.beginningOfDay(date).getTimeInMillis() / 1000) - 15552000), String.valueOf((CalExt.endOfDay(date).getTimeInMillis() / 1000) + 15552000), account});
        if (c.getCount() > 0) {
            c.moveToFirst();
            transactionID = c.getInt(0);
        }
        c.close();
        return transactionID;
    }

    public static int transactionIDForAmount(double amount, GregorianCalendar date, String account) {
        int transactionID = 0;
        if (match_ofxAmountDate_statement == null) {
            match_ofxAmountDate_statement = "SELECT transactionID FROM transactions WHERE deleted=0 AND subTotal > ? AND subTotal < ? AND date >= ? AND date <= ? AND (ofxID ISNULL OR (NOT LENGTH(ofxID) > 0)) AND accountID IN (SELECT accountID FROM accounts WHERE account LIKE ?)";
        }
        Cursor c = Database.rawQuery(match_ofxAmountDate_statement, new String[]{String.valueOf(amount - 1.0E-6d), String.valueOf(1.0E-6d + amount), String.valueOf((CalExt.beginningOfDay(date).getTimeInMillis() / 1000) - 15552000), String.valueOf((CalExt.endOfDay(date).getTimeInMillis() / 1000) + 15552000), account});
        if (c.getCount() > 0) {
            c.moveToFirst();
            transactionID = c.getInt(0);
        }
        c.close();
        return transactionID;
    }

    public static TransactionClass closestTransactionMatchFor(String payee, String account) {
        TransactionClass transaction = null;
        if (select_matching_statement == null) {
            select_matching_statement = "SELECT transactionID FROM transactions WHERE deleted=0 AND payee LIKE ? AND type<>? ORDER BY date DESC";
        }
        Cursor c = Database.rawQuery(select_matching_statement, new String[]{new StringBuilder(String.valueOf(payee)).append("%").toString(), "5"});
        int count = c.getCount();
        if (count > 0) {
            c.moveToFirst();
            transaction = new TransactionClass(c.getInt(0));
            if ((transaction.getType() == 3 || transaction.getType() == 2) && !transaction.getAccount().equals(account) && count > 1) {
                c.moveToNext();
                transaction = new TransactionClass(c.getInt(0));
            }
        }
        c.close();
        return transaction;
    }
}
