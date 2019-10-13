package com.example.smmoney.views.filters;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.FilterClass;

class FilterRowHolder {
    ImageView editImage;
    public FilterClass filter;
    FrameLayout theRow;
    public TextView title;

    public void setFilter(FilterClass filter) {
        this.filter = filter;
        this.title.setTextColor(PocketMoneyThemes.primaryCellTextColor());
    }
}
