package com.example.smmoney.views.budgets;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;

public class BudgetsHeaderHolder extends RelativeLayout {
    public final String label;
    private final String xofy;
    private final TextView labelTextView;
    private final TextView valueTextView;
    private final ImageView iconView;

    public BudgetsHeaderHolder(Context context, String label, String xofy) {
        super(context);
        this.label = label;
        this.xofy = xofy;
        
        LayoutInflater.from(context).inflate(R.layout.header_row, this, true);
        
        this.labelTextView = findViewById(R.id.header_label);
        this.valueTextView = findViewById(R.id.header_value);
        this.iconView = findViewById(R.id.header_icon);
        
        setupTheme();
        updateDisplay();
    }

    private void setupTheme() {
        int backgroundColor = PocketMoneyThemes.actionBarColor();
        setBackgroundColor(backgroundColor);
        
        int textColor = ContextCompat.getColor(getContext(), R.color.black_theme_text);
        this.labelTextView.setTextColor(textColor);
        this.valueTextView.setTextColor(textColor);
        
        this.iconView.setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
    }

    private void updateDisplay() {
        this.labelTextView.setText(this.label);
        this.valueTextView.setText(this.xofy);
    }

    public void setExpanded(boolean expanded) {
        if (expanded) {
            this.iconView.setImageResource(R.drawable.ic_expand_less);
        } else {
            this.iconView.setImageResource(R.drawable.ic_expand_more);
        }
    }
}
