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

class ReportsRowHolder {
    public TextView amount;
    public CheckBox checked;
    TextView expense;
    private FilterClass filter;
    ReportItem report;
    RelativeLayout theRow;

    void setReport(ReportItem aReport) {
        this.report = null;
        this.report = aReport;
        int PAYEE_NAME_LENGTH = 20;
        this.expense.setText(this.report.expense.length() > PAYEE_NAME_LENGTH ? this.report.expense.substring(0, 19) : this.report.expense);
        //noinspection StringBufferReplaceableByString
        this.amount.setText(new StringBuilder()
                .append(amountString())
                .append(" ")
                .append("\u00b7") // unicode "middle dot"
                .append(" ")
                .append(percentString(this.report.percent))
                .append("% ")
                .append("\u00b7")
                .append(" ")
                .append(this.report.count).toString());
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
