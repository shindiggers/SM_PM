package com.example.smmoney.views.charts.items;

import android.graphics.Path;

import com.example.smmoney.views.reports.ReportItem;

public class ChartItem {
    public int color;
    private String label;
    public Path path;
    public double percent = 0.0d;
    public boolean selected = false;
    public double value;
    public ReportItem reportItem;

    public ChartItem(double value, String label, int color) {
        this.value = value;
        this.label = label;
        this.color = color;
    }
}
