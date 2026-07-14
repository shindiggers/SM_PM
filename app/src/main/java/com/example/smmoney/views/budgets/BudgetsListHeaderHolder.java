package com.example.smmoney.views.budgets;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.smmoney.R;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;

public class BudgetsListHeaderHolder extends ConstraintLayout {
    public String label;
    private final TextView actualValueView;
    private final TextView groupLabelView;
    private final TextView sentenceView;
    private final TextView targetLabelView;
    private final TextView targetValueView;
    private final ImageView iconView;

    public BudgetsListHeaderHolder(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.budget_header_row, this, true);
        
        this.actualValueView = findViewById(R.id.header_actual_value);
        this.groupLabelView = findViewById(R.id.header_group_label);
        this.sentenceView = findViewById(R.id.header_sentence);
        this.targetLabelView = findViewById(R.id.header_target_label);
        this.targetValueView = findViewById(R.id.header_target_value);
        this.iconView = findViewById(R.id.header_icon);
        
        setupTheme();
    }

    private void setupTheme() {
        setBackgroundColor(PocketMoneyThemes.actionBarColor());
        int textColor = PocketMoneyThemes.headerTextColor();
        
        ((TextView)findViewById(R.id.header_actual_label)).setTextColor(textColor);
        this.actualValueView.setTextColor(textColor);
        this.groupLabelView.setTextColor(textColor);
        this.sentenceView.setTextColor(textColor);
        this.targetLabelView.setTextColor(textColor);
        this.targetValueView.setTextColor(textColor);
        this.iconView.setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
    }

    public void setData(String groupLabel, double actual, String targetLabel, double target, String sentence) {
        this.label = groupLabel;
        this.groupLabelView.setText(groupLabel);
        this.targetLabelView.setText(targetLabel);
        this.sentenceView.setText(sentence);
        
        formatAmount(this.actualValueView, actual);
        formatAmount(this.targetValueView, target);
    }

    private void formatAmount(TextView view, double amount) {
        String text;
        double roundedAmount = Math.round(amount * 100.0) / 100.0;
        double absAmount = Math.abs(roundedAmount);
        
        if (Prefs.getBooleanPref(Prefs.BUDGETSHOWCENTS)) {
            text = CurrencyExt.amountAsCurrency(absAmount);
        } else {
            text = CurrencyExt.amountAsCurrencyWithoutCents(absAmount);
        }

        if (roundedAmount < 0) {
            view.setText(String.format("(%s)", text));
        } else {
            view.setText(text);
        }
        // Always use standard text color as requested
        view.setTextColor(PocketMoneyThemes.headerTextColor());
    }

    public void setExpanded(boolean expanded) {
        this.iconView.setImageResource(expanded ? R.drawable.ic_expand_less : R.drawable.ic_expand_more);
    }
}
