package com.example.smmoney.views.charts.items;

import com.example.smmoney.records.AccountClass;

class AccountChartItem extends ChartItem {
    AccountClass account;

    public AccountChartItem(double value, String label, int color) {
        super(value, label, color);
    }
}
