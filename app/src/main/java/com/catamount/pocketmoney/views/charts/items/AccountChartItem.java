package com.catamount.pocketmoney.views.charts.items;

import com.catamount.pocketmoney.records.AccountClass;

public class AccountChartItem extends ChartItem {
    AccountClass account;

    public AccountChartItem(double value, String label, int color) {
        super(value, label, color);
    }
}
