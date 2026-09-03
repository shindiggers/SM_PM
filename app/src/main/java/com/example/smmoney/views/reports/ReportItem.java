package com.example.smmoney.views.reports;

import com.example.smmoney.records.FilterClass;

public class ReportItem {
    public double amount;
    public boolean checked = true;
    public int color;
    public int count;
    public FilterClass filter;
    public String expense;
    double percent;

    public ReportItem(String anExpense, double anAmount) {
        this.expense = anExpense;
        this.amount = anAmount;
    }
}
