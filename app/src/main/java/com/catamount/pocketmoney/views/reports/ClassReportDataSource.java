package com.catamount.pocketmoney.views.reports;

import com.catamount.pocketmoney.database.AccountDB;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.records.AccountClass;
import com.catamount.pocketmoney.records.FilterClass;
import com.catamount.pocketmoney.records.SplitsClass;
import com.catamount.pocketmoney.records.TransactionClass;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public class ClassReportDataSource extends ReportDataSource {
    public ClassReportDataSource(ArrayList<TransactionClass> theTrans, FilterClass theFilter) {
        super(theTrans, theFilter);
    }

    public String title() {
        return Locales.kLOC_GENERAL_CLASS;
    }

    public FilterClass newFilterBasedOnSelectedRow(String content) {
        FilterClass newFilter = this.filter.copy();
        addCurrentPeriodToFilter(newFilter);
        String selectedClass = content;
        if (Locales.kLOC_FILTERS_UNFILED.equals(selectedClass)) {
            newFilter.setClassName(Locales.kLOC_FILTERS_UNFILED);
        } else {
            newFilter.setClassName(new StringBuilder(String.valueOf(selectedClass)).append("%").toString());
        }
        return newFilter;
    }

    public void generateReport() {
        Hashtable<String, ReportItem> scratchReport = new Hashtable();
        Hashtable<String, String> caseInsensitiveKeys = new Hashtable();
        double xrate = 1.0d;
        boolean multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        boolean allAccounts = this.filter.allAccounts();
        TransactionClass[] relaventTransactions = transactionsFromDateToDate(startOfPeriod(), endOfPeriod());
        if (relaventTransactions == null) {
            this.data = null;
            this.data = new ArrayList();
            return;
        }
        ReportItem reportItem;
        int oldAccountViewType = Prefs.getIntPref(Prefs.VIEWACCOUNTS);
        Prefs.setPref(Prefs.VIEWACCOUNTS, 0);
        this.totalActions = relaventTransactions.length;
        this.currentAction = 0;
        int i = 0;
        while (i < relaventTransactions.length) {
            if (ReportsActivity.processData) {
                TransactionClass transaction = relaventTransactions[i];
                updateProgress();
                if (multipleCurrencies && allAccounts) {
                    xrate = new AccountClass(AccountDB.uniqueID(transaction.getAccount())).getExchangeRate();
                }
                Iterator it = transaction.getSplits().iterator();
                while (it.hasNext()) {
                    SplitsClass split = (SplitsClass) it.next();
                    if (this.filter.isValidSplit(split)) {
                        double tAmt;
                        String key = caseInsensitiveKeys.get(split.getClassName().toUpperCase());
                        if (key != null) {
                            reportItem = scratchReport.get(key);
                        } else {
                            key = split.getClassName();
                            caseInsensitiveKeys.put(key.toUpperCase(), key);
                            reportItem = new ReportItem(key, 0.0d);
                            reportItem.filter = this.filter;
                            scratchReport.put(key, reportItem);
                        }
                        if (multipleCurrencies && allAccounts) {
                            tAmt = split.getAmount() / xrate;
                        } else {
                            tAmt = split.getAmount();
                        }
                        reportItem.amount += tAmt;
                        reportItem.count++;
                    }
                }
                relaventTransactions[i] = null;
                this.currentAction++;
                i++;
            } else {
                return;
            }
        }
        Prefs.setPref(Prefs.VIEWACCOUNTS, oldAccountViewType);
        reportItem = scratchReport.get("");
        if (reportItem != null) {
            reportItem.expense = Locales.kLOC_FILTERS_UNFILED;
        }
        this.data = null;
        this.data = new ArrayList();
        Enumeration<ReportItem> e = scratchReport.elements();
        while (e.hasMoreElements()) {
            this.data.add(e.nextElement());
        }
    }
}
