package com.example.smmoney.views.charts.views;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.smmoney.SMMoney;
import com.example.smmoney.views.charts.ChartViewDataSource;
import com.example.smmoney.views.charts.ChartViewDelegate;
import com.example.smmoney.views.charts.items.ChartItem;
import com.example.smmoney.views.reports.ReportDataSource;

import java.util.ArrayList;

public class ChartView extends View {
    public ChartViewDataSource dataSource;
    public ChartViewDelegate delegate;
    boolean allNegative;
    double negativeMaxValue;
    double negativeTotal;
    double positiveMaxValue;
    double positiveTotal;
    ChartItem selectedItem;
    ArrayList<ArrayList<ChartItem>> series;

    public ChartView(Context context) {
        super(context);
    }

    public ChartView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void reloadData(boolean reloadDataSource) {
        if (reloadDataSource) {
            this.dataSource.reloadData();
        }
        int seriesCount = this.dataSource.numberOfSeriesInChartView(this);
        this.series = new ArrayList<>(seriesCount);
        for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
            int dataCount = this.dataSource.numberOfDataPointsInSeries(this, seriesIndex);
            ArrayList<ChartItem> data = new ArrayList<>(dataCount);
            for (int dataIndex = 0; dataIndex < dataCount; dataIndex++) {
                data.add(this.dataSource.itemForDataAtIndex(this, dataIndex, seriesIndex));
            }
            this.series.add(data);
        }
        this.negativeTotal = 0.0d;
        this.positiveTotal = 0.0d;
        this.negativeMaxValue = 0.0d;
        this.positiveMaxValue = 0.0d;
        this.allNegative = this.dataSource instanceof ReportDataSource;
        for (ArrayList<ChartItem> chartItems : this.series) {
            for (ChartItem anItem : chartItems) {
                if (anItem.value > 0.0d) {
                    this.positiveTotal += anItem.value;
                    this.allNegative = false;
                } else {
                    this.negativeTotal += anItem.value;
                }
                this.negativeMaxValue = Math.min(this.negativeMaxValue, anItem.value);
                this.positiveMaxValue = Math.max(this.positiveMaxValue, anItem.value);
            }
        }
        for (ArrayList<ChartItem> chartItems : this.series) {
            for (ChartItem anItem : chartItems) {
                if (anItem.value < 0.0d) {
                    anItem.percent = anItem.value / this.negativeTotal;
                } else {
                    anItem.percent = anItem.value / this.positiveTotal;
                }
            }
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.selectedItem != null) {
            this.selectedItem.selected = false;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
            float x = event.getX();
            float y = event.getY();
            RectF rect = new RectF();
            Region region = new Region();
            for (ArrayList<ChartItem> chartItems : this.series) {
                for (ChartItem item : chartItems) {
                    try {
                        item.path.computeBounds(rect, true);
                        region.setPath(item.path, new Region((int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom));
                        item.selected = region.contains((int) x, (int) y);
                        if (item.selected) {
                            this.selectedItem = item;
                        }
                    } catch (Exception e) {
                        Log.e(SMMoney.TAG, "Exception in onTouchEvent (computing bounds)", e);
                    }
                }
            }
        }
        try {
            this.delegate.chartViewSelectedItem(this, this.selectedItem);
        } catch (NullPointerException e2) {
            Log.e(SMMoney.TAG, "NullPointerException in onTouchEvent (delegate call)", e2);
        }
        invalidate();
        return true;
    }
}
