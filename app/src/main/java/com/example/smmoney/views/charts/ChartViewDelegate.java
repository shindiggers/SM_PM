package com.example.smmoney.views.charts;

import com.example.smmoney.views.charts.items.ChartItem;
import com.example.smmoney.views.charts.views.ChartView;

public interface ChartViewDelegate {
    void chartViewSelectedItem(ChartView chartView, ChartItem chartItem);
}
