package com.example.smmoney.views.reports;

import com.example.smmoney.database.AccountDB;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.records.SplitsClass;
import com.example.smmoney.records.TransactionClass;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class CategoryReportDataSource extends ReportDataSource {
    public CategoryReportDataSource(ArrayList<TransactionClass> theTrans, FilterClass theFilter) {
        super(theTrans, theFilter);
    }

    public String title() {
        return Locales.kLOC_GENERAL_CATEGORY;
    }

    public FilterClass newFilterBasedOnSelectedRow(String content) {
        FilterClass newFilter = this.filter.copy();
        addCurrentPeriodToFilter(newFilter);
        if (Locales.kLOC_FILTERS_UNFILED.equals(content)) {
            newFilter.setCategory(Locales.kLOC_FILTERS_UNFILED);
        } else {
            newFilter.setCategory(content + "%");
        }
        return newFilter;
    }

    private String stripSubcategories(String category) {
        int index;
        if (Prefs.getBooleanPref(Prefs.REPORTS_GROUPSUBCATEGORIES) && this.filter.getCategory() != null && this.filter.getCategory().length() > 0) {
            if (this.filter.getCategory().endsWith("%")) {
                index = category.indexOf(this.filter.getCategory().replace("%", ":"));
            } else {
                index = category.indexOf(this.filter.getCategory() + ":");
            }
            if (index == -1) {
                return category;
            }
            index = category.indexOf(":", this.filter.getCategory().length() + index);
        } else if (this.filter.getCategory().endsWith("%")) {
            return category;
        } else {
            index = category.indexOf(":");
        }
        return index == -1 ? category : category.substring(0, index);
    }

    public void generateReport() {
        Hashtable<String, ReportItem> scratchReport = new Hashtable<>();
        Hashtable<String, String> caseInsensitiveKeys = new Hashtable<>();
        double xrate = 1.0d;
        boolean multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        boolean allAccounts = this.filter.allAccounts();
        TransactionClass[] relaventTransactions = transactionsFromDateToDate(startOfPeriod(), endOfPeriod());
        if (relaventTransactions == null) {
            this.data = null;
            this.data = new ArrayList<>();
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
                for (SplitsClass split : transaction.getSplits()) {
                    if (this.filter.isValidSplit(split)) {
                        double tAmt;
                        String key = caseInsensitiveKeys.get(stripSubcategories(split.getCategory()).toUpperCase());
                        if (key != null) {
                            reportItem = scratchReport.get(key);
                        } else {
                            key = stripSubcategories(split.getCategory());
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
                        if (reportItem != null) {
                            reportItem.amount += tAmt;

                            reportItem.count++;
                        }
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
        this.data = new ArrayList<>();
        Enumeration<ReportItem> e = scratchReport.elements();
        while (e.hasMoreElements()) {
            this.data.add(e.nextElement());
        }
    }
}
