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

    private void formatAmount(TextView view, double amount, boolean useBrackets) {
        String text = showCents() ? CurrencyExt.amountAsCurrency(Math.abs(amount)) : CurrencyExt.amountAsCurrencyWithoutCents(Math.abs(amount));
        if (amount < 0) {
            view.setText(useBrackets ? "(" + text + ")" : text);
        } else {
            view.setText(text);
        }
        // Always use high-contrast text color as requested
        view.setTextColor(PocketMoneyThemes.headerTextColor());
    }

    public void setCategory(CategoryClass category, boolean isUnbudgeted) {
        this.category = category;
        double spent;
        
        // 1. Calculate Actual Spent
        // For Expenses and Unbudgeted items: Spend is positive volume.
        if (category.getType() == Enums.kCategoryExpense || isUnbudgeted) {
            spent = ((double) Math.round(category.spent * -100.0d)) / 100.0d;
            if (spent == -0.0d) spent = 0.0d;
        } else {
            spent = ((double) Math.round(category.spent * 100.0d)) / 100.0d;
        }
        
        double budget = category.budget;
        
        // 2. Calculate Variance (Ahead/Behind)
        double variance;
        if (category.getType() == Enums.kCategoryExpense || isUnbudgeted) {
            variance = budget - spent; // Expenses: Budget - Actual
        } else {
            variance = spent - budget; // Income: Actual - Budget
        }
        
        // 3. Update UI Text
        formatAmount(this.spentTextView, spent, true);
        this.categoryTextView.setText(category.getCategory());
        this.categoryTextView.setTextColor(PocketMoneyThemes.headerTextColor());
        formatAmount(this.budgetTextView, budget, true);
        
        // 4. Update Variance Label
        String statusPrefix = (variance >= 0) ? "• Ahead " : "• Behind ";
        String varText = showCents() ? CurrencyExt.amountAsCurrency(Math.abs(variance)) : CurrencyExt.amountAsCurrencyWithoutCents(Math.abs(variance));
        if (variance < 0) {
            this.varianceTextView.setText(statusPrefix + "(" + varText + ")");
        } else {
            this.varianceTextView.setText(statusPrefix + varText);
        }
        this.varianceTextView.setTextColor(PocketMoneyThemes.headerTextColor());

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
