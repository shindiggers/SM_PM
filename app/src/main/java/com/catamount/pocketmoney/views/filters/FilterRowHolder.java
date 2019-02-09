package com.catamount.pocketmoney.views.filters;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.records.FilterClass;

public class FilterRowHolder {
    public ImageView editImage;
    public FilterClass filter;
    public FrameLayout theRow;
    public TextView title;

    public void setFilter(FilterClass filter) {
        this.filter = filter;
        this.title.setTextColor(PocketMoneyThemes.primaryCellTextColor());
    }
}
