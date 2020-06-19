package com.example.smmoney.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.records.FilterClass;

public class BalanceBar extends FrameLayout {
    public final TextView balanceAmountTextView;
    public final TextView balanceTypeTextView;
    public final LinearLayout balanceView;
    private FilterClass filter;
    public final View nextButton;
    public final View previousButton;
    public final ProgressBar progressBar;
    public final TextView secondBalanceAmountTextView;
    public final TextView secondBalanceTypeTextView;
    private final LinearLayout innerLinearLayout;
    private final LinearLayout secondInnerLinearLayout;
    private final ImageView seperatorImage;

    public BalanceBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.balance_bar2);
        LinearLayout layout1 = new LinearLayout(context);
        this.balanceView = new LinearLayout(context);
        this.balanceView.setLayoutParams(new LayoutParams(-1/*MATCHPARENT*/, -1/*MATCHPARENT*/, 1/*CENTRE_HORIZONTAL*/));
        LayoutParams lp = new LayoutParams(-1/*MATCHPARENT*/, -1/*MATCHPARENT*/, 1/*CENTRE_HORIZONTAL*/);
        layout1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(0, /*height*/ ViewGroup.LayoutParams.MATCH_PARENT /*-1*/, 1.0f);
        this.previousButton = new View(context);
        this.nextButton = new View(context);
        layout1.addView(this.previousButton, llp);
        layout1.addView(this.nextButton, llp);
        addView(layout1, lp);
        this.innerLinearLayout = new LinearLayout(context);
        this.innerLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        this.secondInnerLinearLayout = new LinearLayout(context);
        this.secondInnerLinearLayout.setOrientation(LinearLayout.VERTICAL);
        this.secondInnerLinearLayout.setVisibility(GONE);
        LinearLayout.LayoutParams innerllp = new LinearLayout.LayoutParams(/*width*/ ViewGroup.LayoutParams.MATCH_PARENT /*-1*/, /*height*/ ViewGroup.LayoutParams.MATCH_PARENT /*-1*/, 1.0f);
        lp = new LayoutParams(/*width*/ ViewGroup.LayoutParams.WRAP_CONTENT /*-2*/, /*height*/ ViewGroup.LayoutParams.WRAP_CONTENT /*-2*/, Gravity.CENTER /*17*/);
        this.balanceTypeTextView = new TextView(context);
        this.balanceTypeTextView.setText(R.string.kLOC_SHOW_BALANCES_CURRENT);
        this.balanceTypeTextView.setGravity(Gravity.CENTER /*17*/);
        this.balanceAmountTextView = new TextView(context);
        this.balanceAmountTextView.setText(R.string.accounts_view_net_worth);
        this.balanceAmountTextView.setGravity(Gravity.CENTER /*17*/);
        llp.setMargins(10, 0, 0, 0);
        this.innerLinearLayout.addView(this.balanceTypeTextView, innerllp);
        this.innerLinearLayout.addView(this.balanceAmountTextView, innerllp);
        this.balanceView.addView(this.innerLinearLayout, llp);
        this.seperatorImage = new ImageView(context);
        this.seperatorImage.setBackgroundResource(R.drawable.seperatorline);
        this.seperatorImage.setVisibility(GONE);
        this.seperatorImage.setLayoutParams(new LinearLayout.LayoutParams(0, /*height*/ ViewGroup.LayoutParams.MATCH_PARENT /*-1*/, 0.0f));
        this.secondBalanceTypeTextView = new TextView(context);
        this.secondBalanceTypeTextView.setText(R.string.kLOC_SHOW_BALANCES_2NDLINE);
        this.secondBalanceTypeTextView.setVisibility(GONE);
        this.secondBalanceTypeTextView.setGravity(17);
        this.secondBalanceAmountTextView = new TextView(context);
        this.secondBalanceAmountTextView.setText(R.string.accounts_view_net_worth);
        this.secondBalanceAmountTextView.setVisibility(GONE);
        this.secondBalanceAmountTextView.setGravity(Gravity.CENTER /*17*/);
        llp.setMargins(10, 0, 0, 0);
        this.secondInnerLinearLayout.addView(this.secondBalanceTypeTextView, innerllp);
        this.secondInnerLinearLayout.addView(this.secondBalanceAmountTextView, innerllp);
        this.balanceView.addView(this.secondInnerLinearLayout, llp);
        this.progressBar = new ProgressBar(context);
        this.progressBar.setIndeterminate(true);
        this.progressBar.setVisibility(GONE);
        this.progressBar.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0.0f));
        this.balanceView.addView(this.progressBar);
        addView(this.balanceView);
    }

    public void setSecondBalanceEnabled(boolean enable) {
        int i;
        int i2 = 0;
        this.secondBalanceAmountTextView.setVisibility(enable ? VISIBLE : GONE);
        if (enable) {
            i = VISIBLE /*0*/;
        } else {
            i = GONE /*8*/;
        }
        this.secondBalanceTypeTextView.setVisibility(i);
        if (enable) {
            i = VISIBLE /*0*/;
        } else {
            i = GONE /*8*/;
        }
        this.seperatorImage.setVisibility(i);
        if (enable) {
            i = LinearLayout.VERTICAL /*1*/;
        } else {
            i = LinearLayout.HORIZONTAL /*0*/;
        }
        this.innerLinearLayout.setOrientation(i);
        if (!enable) {
            i2 = GONE /*8*/;
        }
        this.secondInnerLinearLayout.setVisibility(i2);
    }

    public void setFilter(FilterClass aFilter) {
        this.filter = aFilter;
    }

    public int nextBalanceTypeAfter(int type) {
        switch (type) {
            case Enums.kBalanceTypeFuture /*0*/:
                return Enums.kBalanceTypeAvailableFunds/*3*/;
            case Enums.kBalanceTypeCleared /*1*/:
                return (this.filter == null || !this.filter.customFilter()) ? Enums.kBalanceTypeCurrent/*2*/ : Enums.kBalanceTypeFiltered/*5*/;
            case Enums.kBalanceTypeCurrent /*2*/:
                return Enums.kBalanceTypeFuture /*0*/;
            case Enums.kBalanceTypeAvailableFunds /*3*/:
                return Enums.kBalanceTypeAvailableCredit /*4*/;
            case Enums.kBalanceTypeAvailableCredit /*4*/:
                return Enums.kBalanceTypeCleared/*1*/;
            default:
                return Enums.kBalanceTypeCurrent/*2*/;
        }
    }

    public int nextBalanceTypeBefore(int type) {
        switch (type) {
            case Enums.kBalanceTypeFuture /*0*/:
                return Enums.kBalanceTypeCurrent/*2*/;
            case Enums.kBalanceTypeCleared /*1*/:
                return (this.filter == null || !this.filter.customFilter()) ? Enums.kBalanceTypeAvailableCredit/*4*/ : Enums.kBalanceTypeFiltered/*5*/;
            case Enums.kBalanceTypeCurrent /*2*/:
                return Enums.kBalanceTypeCleared/*1*/;
            case Enums.kBalanceTypeAvailableFunds /*3*/:
                return Enums.kBalanceTypeFuture/*0*/;
            case Enums.kBalanceTypeAvailableCredit /*4*/:
                return Enums.kBalanceTypeAvailableFunds/*3*/;
            default:
                return Enums.kBalanceTypeAvailableCredit/*4*/;
        }
    }
}
