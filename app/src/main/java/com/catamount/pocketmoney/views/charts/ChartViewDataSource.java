package com.catamount.pocketmoney.views.charts;

import com.catamount.pocketmoney.views.charts.items.ChartItem;
import com.catamount.pocketmoney.views.charts.views.ChartView;
import java.util.GregorianCalendar;

public interface ChartViewDataSource {
    GregorianCalendar dateForRow(int i);

    ChartItem itemForDataAtIndex(ChartView chartView, int i, int i2);

    double networthForRow(int i);

    int numberOfDataPointsInSeries(ChartView chartView, int i);

    int numberOfSeriesInChartView(ChartView chartView);

    void reloadData();

    int rowOfChartItem(ChartItem chartItem);

    void selectAllDataPointsForRow(int i);

    String title();
}
