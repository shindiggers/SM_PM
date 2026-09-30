package com.example.smmoney.views.charts.items;

import android.graphics.Path;

import com.example.smmoney.views.reports.ReportItem;

import java.util.GregorianCalendar;

public class ChartItem {
    public final int color;
    public Path path;
    public double percent = 0.0d;
    public boolean selected = false;
    public final double value;
    public ReportItem reportItem;
    public final String label;
    
    // Properties for drilling down into charts
    public GregorianCalendar fromDate;
    public GregorianCalendar toDate;
    public Boolean isIncome; // Null for NetWorth, true/false for CashFlow

    public ChartItem(double value, String label, int color) {
        this.value = value;
        this.label = label;
        this.color = color;
    }
}
