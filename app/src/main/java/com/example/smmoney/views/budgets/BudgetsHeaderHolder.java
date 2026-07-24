package com.example.smmoney.views.budgets;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;

public class BudgetsHeaderHolder extends RelativeLayout {
    public String label;
    private TextView labelTextView;
    private TextView valueTextView;
    private ImageView iconView;

    public BudgetsHeaderHolder(Context context) {
        super(context);
        init(context);
    }

    public BudgetsHeaderHolder(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BudgetsHeaderHolder(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public BudgetsHeaderHolder(Context context, String label, String xofy) {
        super(context);
        init(context);
        setData(label, xofy);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.header_row, this, true);
        
        this.labelTextView = findViewById(R.id.header_label);
        this.valueTextView = findViewById(R.id.header_value);
        this.iconView = findViewById(R.id.header_icon);
        
        setupTheme();
    }

    private void setupTheme() {
        int backgroundColor = PocketMoneyThemes.actionBarColor();
        setBackgroundColor(backgroundColor);
        
        int textColor = PocketMoneyThemes.headerTextColor();
        this.labelTextView.setTextColor(textColor);
        this.valueTextView.setTextColor(textColor);
        
        this.iconView.setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
    }

    public void setData(String label, String xofy) {
        this.label = label;
        this.labelTextView.setText(label);
        this.valueTextView.setText(xofy);
    }

    public void setExpanded(boolean expanded) {
        if (expanded) {
            this.iconView.setImageResource(R.drawable.ic_expand_less);
        } else {
            this.iconView.setImageResource(R.drawable.ic_expand_more);
        }
    }
}
