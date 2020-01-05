package com.example.smmoney.views.budgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.CategoryClass;

public class BudgetsRowHolder extends View {
    private String actualString;
    private String budgetedString;
    public CategoryClass category;
    private String categoryString;
    private Context context;
    private boolean touched = false;
    Rect bounds = new Rect(0, 0, 0, 0);
    Rect firstBarRect = new Rect(0, bounds.top, 0, bounds.bottom);
    Rect secondBarRect = new Rect(0, bounds.top, 0, bounds.bottom);
    Rect rect = new Rect(bounds.left, bounds.top, bounds.right, bounds.bottom);
    Paint p = new Paint();

    public BudgetsRowHolder(Context context) {
        super(context);
        this.context = context;
        ViewGroup.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT/*-1*/, ViewGroup.LayoutParams.WRAP_CONTENT/*-2*/);
        int sizeInDP = 50;
        int marginInDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDP, getResources().getDisplayMetrics());
        setLayoutParams(params);
        setMinimumHeight((int) ((((double) getPrefferedItemHeight()) * 2.0d) / 3.0d));
        setWillNotDraw(false);
        TextView categoryTextView = new TextView(context);
        TextView budgetedTextView = new TextView(context);
        TextView actualTextView = new TextView(context);
        categoryTextView.setGravity(Gravity.CENTER/*17*/);
        budgetedTextView.setGravity(Gravity.CENTER /*was21 now 17*/);
        actualTextView.setGravity(Gravity.CENTER /*was 19 now 17*/);
        budgetedTextView.setPadding(200, 0, 200, 0);
        actualTextView.setPadding(marginInDP, 0, marginInDP, 0);
        categoryTextView.setText("CATEGORY");
        budgetedTextView.setText("(USD1,000.12)");
        actualTextView.setText("USD1,500.12");
        setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN /*0*/:
                        BudgetsRowHolder.this.touched = true;
                        break;
                    case MotionEvent.ACTION_UP /*1*/:
                    case MotionEvent.ACTION_CANCEL /*3*/:
                        BudgetsRowHolder.this.touched = false;
                        break;
                }
                BudgetsRowHolder.this.invalidate();
                return onTouchEvent(event);
            }
        });
    }

    private float getPrefferedItemHeight() {
        TypedValue typedValue = new TypedValue();
        DisplayMetrics displayMetrics = this.context.getResources().getDisplayMetrics();
        this.context.getTheme().resolveAttribute(16842829, typedValue, true);
        return typedValue.getDimension(displayMetrics);
    }

    private boolean showCents() {
        return Prefs.getBooleanPref(Prefs.BUDGETSHOWCENTS);
    }

    private String amountAsString(double amount) {
        if (showCents()) {
            return CurrencyExt.amountAsCurrency(amount);
        }
        return CurrencyExt.amountAsCurrencyWithoutCents(amount);
    }

    public void setCategory(CategoryClass category) {
        double spent;
        double d = 0.0d;
        this.category = category;
        if (category.getType() == Enums.kCategoryExpense/*0*/) {
            spent = ((double) Math.round(category.spent * -100.0d)) / 100.0d;
            if (spent == -0.0d) {
                spent = 0.0d;
            }
        } else {
            spent = ((double) Math.round(category.spent * 100.0d)) / 100.0d;
        }
        double budget = category.budget;
        this.actualString = amountAsString(spent);
        if (Enums.kBudgetDisplayExpenseBudgeted/*2*/ == Prefs.getIntPref(Prefs.BUDGETDISPLAY)) {
            this.budgetedString = amountAsString(budget);
        } else if (Prefs.getIntPref(Prefs.BUDGETDISPLAY) == Enums.kBudgetDisplayExpenseAvailable/*0*/) {
            if (category.getType() == Enums.kCategoryExpense/*0*/) {
                if (budget - spent >= 0.0d) {
                    d = budget - spent;
                }
                this.budgetedString = amountAsString(d);
            } else {
                if (spent - budget >= 0.0d) {
                    d = spent - budget;
                }
                this.budgetedString = amountAsString(d);
            }
        } else if (Enums.kBudgetDisplayExpenseOver/*3*/ == Prefs.getIntPref(Prefs.BUDGETDISPLAY)) {
            if (category.getType() == Enums.kCategoryExpense/*0*/) {
                this.budgetedString = amountAsString(budget - spent);
            } else {
                this.budgetedString = amountAsString(spent - budget);
            }
        }
        this.categoryString = category.getCategory();
    }

    protected void onDraw(Canvas canvas) {
        double height = (double) getHeight();
        double width = (double) getWidth();
        double spent = ((double) Math.round(this.category.spent * 100.0d)) / 100.0d;
        double budget = this.category.budget;
        if (this.category.getType() == Enums.kCategoryExpense/*0*/) {
            spent *= -1.0d;
        }
        bounds.set(getLeft(), 0, getRight(), getBottom() - getTop());
        firstBarRect.set(0, bounds.top, 0, bounds.bottom);
        secondBarRect.set(0, bounds.top, 0, bounds.bottom);
        rect.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
        int firstBitmapRes = R.drawable.budgetgreen;
        int secondBitmapRes = R.drawable.budgetgreen;
        double greenBarWidth;
        double redBarWidth;
        if (spent == budget) {
            if (budget != 0.0d) {
                secondBarRect.set(0, bounds.top, (int) ((budget / budget) * width), bounds.bottom);
                secondBitmapRes = R.drawable.budgetyellow;
            }
        } else if (this.category.getType() == Enums.kCategoryExpense/*0*/) {
            if (spent > budget) {
                if (budget < 0.0d) {
                    greenBarWidth = 0.0d;
                } else {
                    greenBarWidth = spent != 0.0d ? (budget / spent) * width : width;
                }
                redBarWidth = width - greenBarWidth;
                firstBarRect.set(0, bounds.top, (int) greenBarWidth, bounds.bottom);
                firstBitmapRes = R.drawable.budgetyellow;
                secondBarRect.set((bounds.right - ((int) redBarWidth)) - 1, bounds.top, bounds.right, bounds.bottom);
                secondBitmapRes = R.drawable.budgetred;
            } else {
                if (budget < 0.0d) {
                    greenBarWidth = 0.0d;
                } else {
                    greenBarWidth = budget != 0.0d ? (spent / budget) * width : width;
                }
                redBarWidth = width - greenBarWidth;
                firstBarRect.set(0, bounds.top, (int) greenBarWidth, bounds.bottom);
                firstBitmapRes = R.drawable.budgetyellow;
                secondBarRect.set((bounds.right - ((int) redBarWidth)) - 1, bounds.top, bounds.right, bounds.bottom);
                secondBitmapRes = R.drawable.budgetgreen;
            }
        } else if (spent >= budget) {
            greenBarWidth = spent != 0.0d ? (budget / spent) * width : budget > 0.0d ? width : 0.0d;
            firstBarRect.set((bounds.right - ((int) (width - greenBarWidth))) - 1, bounds.top, bounds.right, bounds.bottom);
            firstBitmapRes = R.drawable.budgetgreen;
            secondBarRect.set(0, bounds.top, (int) greenBarWidth, bounds.bottom);
            secondBitmapRes = R.drawable.budgetyellow;
        } else {
            greenBarWidth = budget != 0.0d ? (spent / budget) * width : width;
            redBarWidth = width - greenBarWidth;
            firstBarRect.set(0, bounds.top, (int) greenBarWidth, bounds.bottom);
            firstBitmapRes = R.drawable.budgetyellow;
            secondBarRect.set((bounds.right - ((int) redBarWidth)) - 1, bounds.top, bounds.right, bounds.bottom);
            secondBitmapRes = R.drawable.budgetred;
        }
        //Paint p = new Paint();
        rect.set(1, 1, 1, 1);
        Bitmap firstBitmap = BitmapFactory.decodeResource(getResources(), firstBitmapRes);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), secondBitmapRes), null, secondBarRect, p);
        canvas.drawBitmap(firstBitmap, null, firstBarRect, p);
        p.getTextBounds(this.categoryString, 0, this.categoryString.length(), rect);
        p.setTextSize(((float) getHeight()) * 0.3f);
        p.setTextScaleX(1.0f);
        p.getTextBounds(this.categoryString, 0, this.categoryString.length(), rect);
        float text_w = p.measureText(this.categoryString);
        float totalTextWidth = ((float) getWidth()) * 0.7f;
        float xscale = 1.0f;
        if (totalTextWidth < text_w) {
            xscale = totalTextWidth / text_w;
        }
        p.setTextScaleX(xscale);
        p.setTextAlign(Align.CENTER);
        p.setColor(this.touched ? -7829368 /*DARKISH GREY*/ : -1 /* WHITE*/ /*-16777216 BLACK*/);
        p.setAntiAlias(true);
        float y = (((float) height) / 2.0f) + (p.getTextSize() / 2.0f);
        canvas.drawText(this.categoryString, ((float) width) / 2.0f, y, p);
        text_w = p.measureText(this.actualString);
        totalTextWidth = ((float) getWidth()) * 0.15f;
        xscale = 1.0f;
        if (totalTextWidth < text_w) {
            xscale = totalTextWidth / text_w;
        }
        p.setTextScaleX(xscale);
        canvas.drawText(this.actualString, (p.measureText(this.actualString) / 2.0f) + 50.0f, y, p);
        text_w = p.measureText(this.budgetedString);
        totalTextWidth = ((float) getWidth()) * 0.15f;
        xscale = 1.0f;
        if (totalTextWidth < text_w) {
            xscale = totalTextWidth / text_w;
        }
        p.setTextScaleX(xscale);
        canvas.drawText(this.budgetedString, (((float) width) - (p.measureText(this.budgetedString) / 2.0f)) - 50.0f, y, p);
    }
}
