package com.example.smmoney.views.reports;

import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.FilterClass;
import java.text.DecimalFormat;

public class ReportsRowHolder {
    private final int PAYEE_NAME_LENGTH = 20;
    public TextView amount;
    public CheckBox checked;
    public TextView expense;
    private FilterClass filter;
    public ReportItem report;
    public RelativeLayout theRow;

    public void setReport(ReportItem aReport) {
        this.report = null;
        this.report = aReport;
        this.expense.setText(this.report.expense.length() > 20 ? this.report.expense.substring(0, 19) : this.report.expense);
        this.amount.setText(amountString() + " " + "\u00b7" + " " + percentString(this.report.percent) + "% " + "\u00b7" + " " + this.report.count);
        this.expense.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.amount.setTextColor(aReport.color);
    }

    private String percentString(double percent) {
        DecimalFormat formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(1);
        return formatter.format(percent);
    }

    private String amountString() {
        AccountClass account = null;
        if (!this.report.filter.allAccounts()) {
            account = AccountDB.recordFor(this.report.filter.getAccount());
        }
        boolean multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        double d = this.report.amount;
        String stringPref = (!multipleCurrencies || account == null) ? Prefs.getStringPref(Prefs.HOMECURRENCYCODE) : account.getCurrencyCode();
        return CurrencyExt.amountAsCurrency(d, stringPref);
    }
}
