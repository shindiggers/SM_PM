package com.example.smmoney.views.budgets;

import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.smmoney.R;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.CategoryClass;

public class BudgetsRowHolder extends RecyclerView.ViewHolder {
    public CategoryClass category;
    public final View therow;
    public final TextView spentTextView;
    public final TextView budgetTextView;
    public final TextView categoryTextView;
    public final TextView varianceTextView;
    public final View barBackground;
    public final View barForeground;

    public BudgetsRowHolder(View rowView) {
        super(rowView);
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

    private void formatAmount(TextView view, double amount) {
        String text = showCents() ? CurrencyExt.amountAsCurrency(Math.abs(amount)) : CurrencyExt.amountAsCurrencyWithoutCents(Math.abs(amount));
        if (amount < 0) {
            view.setText(String.format(SMMoney.getAppContext().getString(R.string.FORMAT_PARENTHESES), text));
        } else {
            view.setText(text);
        }
        view.setTextColor(PocketMoneyThemes.budgetRowTextColor());
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
        int textColor = PocketMoneyThemes.budgetRowTextColor();
        formatAmount(this.spentTextView, spent);
        this.categoryTextView.setText(category.getCategory());
        this.categoryTextView.setTextColor(textColor);
        formatAmount(this.budgetTextView, budget);
        
        // 4. Update Variance Label
        String varText = showCents() ? CurrencyExt.amountAsCurrency(Math.abs(variance)) : CurrencyExt.amountAsCurrencyWithoutCents(Math.abs(variance));
        if (variance < 0) {
            this.varianceTextView.setText(String.format(SMMoney.getAppContext().getString(R.string.FORMAT_BEHIND), varText));
        } else {
            this.varianceTextView.setText(String.format(SMMoney.getAppContext().getString(R.string.FORMAT_AHEAD), varText));
        }
        this.varianceTextView.setTextColor(PocketMoneyThemes.budgetRowVarianceColor());

        updateBars(spent, budget);
    }

    private void updateBars(double spent, double budget) {
        float ratio;
        int backgroundRes;
        int foregroundRes = PocketMoneyThemes.budgetBarYellowColor();

        if (spent == budget) {
            ratio = 1.0f;
            foregroundRes = PocketMoneyThemes.budgetBarYellowColor();
            backgroundRes = PocketMoneyThemes.budgetBarYellowColor();
        } else {
            if (this.category.getType() == Enums.kCategoryExpense) {
                if (spent > budget) {
                    ratio = (budget < 0.0d) ? 0.0f : (float) (budget / spent);
                    backgroundRes = PocketMoneyThemes.budgetBarRedColor();
                } else {
                    ratio = (budget != 0.0d) ? (float) (spent / budget) : 1.0f;
                    backgroundRes = PocketMoneyThemes.budgetBarGreenColor();
                }
            } else { // Income
                if (spent >= budget) {
                    ratio = (spent != 0.0d) ? (float) (budget / spent) : (budget > 0.0d ? 1.0f : 0.0f);
                    backgroundRes = PocketMoneyThemes.budgetBarGreenColor();
                } else {
                    ratio = budget != 0.0d ? (float) (spent / budget) : 1.0f;
                    backgroundRes = PocketMoneyThemes.budgetBarRedColor();
                }
            }
        }

        barBackground.setBackgroundColor(backgroundRes);
        barForeground.setBackgroundColor(foregroundRes);

        therow.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());

        ConstraintLayout layout = (ConstraintLayout) therow;
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        float validatedRatio = Math.max(0.001f, Math.min(0.999f, ratio));
        set.constrainPercentWidth(R.id.budget_bar_foreground, validatedRatio);
        // Ensure progress bar stays pinned to the bottom 5dp during ConstraintSet updates
        set.constrainHeight(R.id.budget_bar_foreground, (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 5.0f, therow.getResources().getDisplayMetrics()));
        set.constrainHeight(R.id.budget_bar_background, (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 5.0f, therow.getResources().getDisplayMetrics()));
        set.connect(R.id.budget_bar_foreground, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.connect(R.id.budget_bar_background, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.clear(R.id.budget_bar_foreground, ConstraintSet.TOP);
        set.clear(R.id.budget_bar_background, ConstraintSet.TOP);
        set.applyTo(layout);
    }
}
