package com.catamount.pocketmoney.views.charts;

import com.catamount.pocketmoney.views.charts.items.ChartItem;
import com.catamount.pocketmoney.views.charts.views.ChartView;

public interface ChartViewDelegate {
    void chartViewSelectedItem(ChartView chartView, ChartItem chartItem);
}
