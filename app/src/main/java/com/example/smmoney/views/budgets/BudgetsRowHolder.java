package com.example.smmoney.views.budgets;

import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.smmoney.R;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.CategoryClass;

public class BudgetsRowHolder {
    public CategoryClass category;
    public View therow;
    public TextView spentTextView;
    public TextView budgetTextView;
    public TextView categoryTextView;
    public TextView varianceTextView;
    public View barBackground;
    public View barForeground;

    public BudgetsRowHolder(View rowView) {
        this.therow = rowView;
        this.spentTextView = rowView.findViewById(R.id.spent);
        this.budgetTextView = rowView.findViewById(R.id.budget);
        this.categoryTextView = rowView.findViewById(R.id.category);
        this.varianceTextView = rowView.findViewById(R.id.variance);
        this.barBackground = rowView.findViewById(R.id.budget_bar_background);
        this.barForeground = rowView.findViewById(R.id.budget_bar_foreground);
    }

    private boolean showCents() {
        return Prefs.getBooleanPref(Prefs.BUDGETSHOWCENTS);
    }

    private String formatCurrency(double amount) {
        String text = showCents() ? CurrencyExt.amountAsCurrency(Math.abs(amount)) : CurrencyExt.amountAsCurrencyWithoutCents(Math.abs(amount));
        if (amount < 0) {
            return "(" + text + ")";
        }
        return text;
    }

    public void setCategory(CategoryClass category) {
        this.category = category;
        double spent;
        
        // 1. Calculate Actual Spent
        if (category.getType() == Enums.kCategoryExpense) {
            spent = ((double) Math.round(category.spent * -100.0d)) / 100.0d;
            if (spent == -0.0d) spent = 0.0d;
        } else {
            spent = ((double) Math.round(category.spent * 100.0d)) / 100.0d;
        }
        
        double budget = category.budget;
        
        // 2. Calculate Variance (Ahead/Behind)
        double variance;
        if (category.getType() == Enums.kCategoryExpense) {
            variance = budget - spent; // Expenses: Budget - Actual
        } else {
            variance = spent - budget; // Income: Actual - Budget
        }
        
        // 3. Update UI Text (All same color as requested)
        this.spentTextView.setText(formatCurrency(spent));
        this.categoryTextView.setText(category.getCategory());
        this.budgetTextView.setText(formatCurrency(budget));
        
        // 4. Update Variance Label
        String statusPrefix = (variance >= 0) ? "• Ahead " : "• Behind ";
        this.varianceTextView.setText(statusPrefix + formatCurrency(variance));
        
        // Use standard text color for all
        int textColor = PocketMoneyThemes.headerTextColor();
        this.spentTextView.setTextColor(textColor);
        this.categoryTextView.setTextColor(textColor);
        this.budgetTextView.setTextColor(textColor);
        this.varianceTextView.setTextColor(textColor);

        updateBars(spent, budget);
    }

    private void updateBars(double spent, double budget) {
        float ratio;
        int backgroundRes = PocketMoneyThemes.budgetBarGreenColor();
        int foregroundRes = PocketMoneyThemes.budgetBarYellowColor();

        if (spent == budget) {
            ratio = 1.0f;
            foregroundRes = PocketMoneyThemes.budgetBarYellowColor();
            backgroundRes = PocketMoneyThemes.budgetBarYellowColor();
        } else {
            if (this.category.getType() == Enums.kCategoryExpense) {
                if (spent > budget) {
                    ratio = (budget < 0.0d) ? 0.0f : (spent != 0.0d ? (float) (budget / spent) : 1.0f);
                    backgroundRes = PocketMoneyThemes.budgetBarRedColor();
                } else {
                    ratio = (budget != 0.0d) ? (float) (spent / budget) : 1.0f;
                    backgroundRes = PocketMoneyThemes.budgetBarGreenColor();
                }
            } else { // Income
                if (spent >= budget) {
                    ratio = spent != 0.0d ? (float) (budget / spent) : (budget > 0.0d ? 1.0f : 0.0f);
                    backgroundRes = PocketMoneyThemes.budgetBarGreenColor();
                } else {
                    ratio = budget != 0.0d ? (float) (spent / budget) : 1.0f;
                    backgroundRes = PocketMoneyThemes.budgetBarRedColor();
                }
            }
        }

        barBackground.setBackgroundColor(backgroundRes);
        barForeground.setBackgroundColor(foregroundRes);

        ConstraintLayout layout = (ConstraintLayout) therow;
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        float validatedRatio = Math.max(0.001f, Math.min(0.999f, ratio));
        set.constrainPercentWidth(R.id.budget_bar_foreground, validatedRatio);
        set.applyTo(layout);
    }
}
