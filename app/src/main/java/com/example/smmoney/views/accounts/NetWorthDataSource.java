package com.example.smmoney.views.accounts;

import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.views.charts.ChartViewDataSource;
import com.example.smmoney.views.charts.items.ChartItem;
import com.example.smmoney.views.charts.views.ChartView;
import com.example.smmoney.views.splits.SplitsActivity;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class NetWorthDataSource implements ChartViewDataSource {
    ArrayList<ChartItem> chartAssets;
    ArrayList<ChartItem> chartLiabilities;
    ArrayList<ChartItem> chartNetworth;
    AccountRowAdapter datasource;

    public NetWorthDataSource(AccountRowAdapter adapter) {
        this.datasource = adapter;
    }

    public void reloadData() {
        GregorianCalendar balanceDate = CalExt.endOfDay(CalExt.endOfMonth(new GregorianCalendar()));
        this.chartAssets = new ArrayList(12);
        this.chartLiabilities = new ArrayList(12);
        this.chartNetworth = new ArrayList(12);
        balanceDate = CalExt.endOfDay(CalExt.endOfMonth(CalExt.subtractMonths(balanceDate, 11)));
        int liabilityColor = PocketMoneyThemes.redBarColor();
        int assetColor = PocketMoneyThemes.greenBarColor();
        int networthColor = PocketMoneyThemes.orangeLabelColor();
        boolean multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        for (int index = 0; index < 12; index++) {
            double totalAssets = 0.0d;
            double totalLiabilities = 0.0d;
            Iterator it = this.datasource.getElements().iterator();
            while (it.hasNext()) {
                double xrate;
                AccountClass account = (AccountClass) it.next();
                if (multipleCurrencies) {
                    xrate = account.getExchangeRate();
                } else {
                    xrate = 1.0d;
                }
                if (account.getTotalWorth()) {
                    if (account.isAsset()) {
                        totalAssets += account.balanceAsOfDate(balanceDate) / xrate;
                    } else {
                        totalLiabilities += account.balanceAsOfDate(balanceDate) / xrate;
                    }
                }
            }
            this.chartAssets.add(new ChartItem(totalAssets, "M" + index, assetColor));
            this.chartLiabilities.add(new ChartItem(totalLiabilities, "M" + index, liabilityColor));
            this.chartNetworth.add(new ChartItem(totalAssets + totalLiabilities, "M" + index, networthColor));
            balanceDate = CalExt.endOfDay(CalExt.endOfMonth(CalExt.addMonth(balanceDate)));
        }
    }

    public GregorianCalendar dateForRow(int row) {
        return CalExt.endOfMonth(CalExt.subtractMonths(new GregorianCalendar(), 11 - row));
    }

    public double networthForRow(int row) {
        return this.chartNetworth.get(row).value;
    }

    public int numberOfDataPointsInSeries(ChartView chartView, int series) {
        return this.chartLiabilities.size();
    }

    public ChartItem itemForDataAtIndex(ChartView chartView, int row, int section) {
        switch (section) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return this.chartAssets.get(row);
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return this.chartLiabilities.get(row);
            default:
                return this.chartNetworth.get(row);
        }
    }

    public void selectAllDataPointsForRow(int row) {
        this.chartAssets.get(row).selected = true;
        this.chartLiabilities.get(row).selected = true;
        this.chartNetworth.get(row).selected = true;
    }

    public int rowOfChartItem(ChartItem chartItem) {
        int row = this.chartAssets.indexOf(chartItem);
        if (row != -1) {
            return row;
        }
        row = this.chartLiabilities.indexOf(chartItem);
        if (row != -1) {
            return row;
        }
        row = this.chartNetworth.indexOf(chartItem);
        if (row != -1) {
            return row;
        }
        return -1;
    }

    public int numberOfSeriesInChartView(ChartView chartView) {
        return 3;
    }

    public String title() {
        return Locales.kLOC_CHARTS_NETWORTH;
    }
}
