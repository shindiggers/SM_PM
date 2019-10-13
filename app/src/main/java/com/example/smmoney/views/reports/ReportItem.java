package com.example.smmoney.views.reports;

import com.example.smmoney.records.FilterClass;

import java.util.GregorianCalendar;

public class ReportItem {
    public double amount;
    @SuppressWarnings("unused")
    public GregorianCalendar cal;
    public boolean checked = true;
    public int color;
    public int count;
    String expense;
    public FilterClass filter;
    double percent;

    public ReportItem(String anExpense, double anAmount) {
        this.expense = anExpense;
        this.amount = anAmount;
    }
}
