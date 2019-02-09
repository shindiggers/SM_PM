package com.catamount.pocketmoney.views.accounts;

import com.catamount.pocketmoney.database.TransactionDB;
import com.catamount.pocketmoney.misc.CalExt;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.views.charts.items.ChartItem;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class CashFlowDataSource extends NetWorthDataSource {
    public CashFlowDataSource(AccountRowAdapter adapter) {
        super(adapter);
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
        for (int index = 0; index < 12; index++) {
            double totalAssets = TransactionDB.cashFlowBalanceWith(1, CalExt.beginningOfDay(CalExt.beginningOfMonth(balanceDate)), balanceDate);
            double totalLiabilities = TransactionDB.cashFlowBalanceWith(0, CalExt.beginningOfDay(CalExt.beginningOfMonth(balanceDate)), balanceDate);
            this.chartAssets.add(new ChartItem(totalAssets, "M" + index, assetColor));
            this.chartLiabilities.add(new ChartItem(totalLiabilities, "M" + index, liabilityColor));
            this.chartNetworth.add(new ChartItem(totalAssets + totalLiabilities, "M" + index, networthColor));
            balanceDate = CalExt.endOfDay(CalExt.endOfMonth(CalExt.addMonth(balanceDate)));
        }
    }

    public String title() {
        return Locales.kLOC_CHARTS_CASHFLOW;
    }
}
