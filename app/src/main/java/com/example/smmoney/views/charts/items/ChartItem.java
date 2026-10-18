package com.example.smmoney.views.charts.items;

import java.util.GregorianCalendar;

public class ChartItem {
    public final int color;
    public final double value;
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
