package com.example.smmoney.views.charts;

import com.example.smmoney.views.charts.items.ChartItem;
import com.example.smmoney.views.charts.views.ChartView;
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
