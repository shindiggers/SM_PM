package com.example.smmoney.views.reports;

import com.example.smmoney.records.FilterClass;

import java.util.GregorianCalendar;

public class ReportItem {
    public double amount;
    public GregorianCalendar cal;
    public boolean checked = true;
    public int color;
    public int count;
    public String expense;
    public FilterClass filter;
    public double percent;

    public ReportItem(String anExpense, double anAmount) {
        this.expense = anExpense;
        this.amount = anAmount;
    }
}