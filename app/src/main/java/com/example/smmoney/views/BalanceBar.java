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
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.FilterClass;

public class BalanceBar extends FrameLayout {
    public final TextView balanceAmountTextView;
    public final TextView balanceTypeTextView;
    public final LinearLayout balanceView;
    private FilterClass filter;
    public final ImageView nextButton;
    public final ImageView previousButton;
    public final ProgressBar progressBar;
    public final TextView secondBalanceAmountTextView;
    public final TextView secondBalanceTypeTextView;
    private final LinearLayout innerLinearLayout;
    private final LinearLayout secondInnerLinearLayout;
    private final ImageView seperatorImage;

    public BalanceBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(PocketMoneyThemes.balanceBarBackgroundColor());
        
        float density = getResources().getDisplayMetrics().density;

        // Previous Button (Left Arrow)
        this.previousButton = new ImageView(context);
        this.previousButton.setImageResource(R.drawable.leftarrow);
        this.previousButton.setColorFilter(PocketMoneyThemes.balanceBarArrowColor());
        this.previousButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        this.previousButton.setPadding((int)(8 * density), 0, (int)(16 * density), 0);
        LayoutParams prevLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START);
        addView(this.previousButton, prevLp);

        // Next Button (Right Arrow)
        this.nextButton = new ImageView(context);
        this.nextButton.setImageResource(R.drawable.rightarrow);
        this.nextButton.setColorFilter(PocketMoneyThemes.balanceBarArrowColor());
        this.nextButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        this.nextButton.setPadding((int)(16 * density), 0, (int)(8 * density), 0);
        LayoutParams nextLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END);
        addView(this.nextButton, nextLp);

        this.balanceView = new LinearLayout(context);
        this.balanceView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        this.balanceView.setGravity(Gravity.CENTER);
        
        this.innerLinearLayout = new LinearLayout(context);
        this.innerLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        this.innerLinearLayout.setGravity(Gravity.CENTER);

        this.secondInnerLinearLayout = new LinearLayout(context);
        this.secondInnerLinearLayout.setOrientation(LinearLayout.VERTICAL);
        this.secondInnerLinearLayout.setVisibility(GONE);
        this.secondInnerLinearLayout.setGravity(Gravity.CENTER);

        this.balanceTypeTextView = new TextView(context);
        this.balanceTypeTextView.setText(R.string.kLOC_SHOW_BALANCES_CURRENT);
        this.balanceTypeTextView.setGravity(Gravity.CENTER);
        this.balanceTypeTextView.setTextColor(PocketMoneyThemes.balanceBarTextViewColor());
        
        this.balanceAmountTextView = new TextView(context);
        this.balanceAmountTextView.setText(R.string.accounts_view_net_worth);
        this.balanceAmountTextView.setGravity(Gravity.CENTER);
        this.balanceAmountTextView.setTextColor(PocketMoneyThemes.balanceBarTextViewColor());
        
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textLp.setMargins((int)(10 * density), 0, (int)(10 * density), 0);

        this.innerLinearLayout.addView(this.balanceTypeTextView, textLp);
        this.innerLinearLayout.addView(this.balanceAmountTextView, textLp);
        this.balanceView.addView(this.innerLinearLayout);

        this.seperatorImage = new ImageView(context);
        this.seperatorImage.setBackgroundResource(R.drawable.seperatorline);
        this.seperatorImage.setVisibility(GONE);
        this.seperatorImage.setLayoutParams(new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));
        this.balanceView.addView(this.seperatorImage);

        this.secondBalanceTypeTextView = new TextView(context);
        this.secondBalanceTypeTextView.setText(R.string.kLOC_SHOW_BALANCES_2NDLINE);
        this.secondBalanceTypeTextView.setVisibility(GONE);
        this.secondBalanceTypeTextView.setGravity(Gravity.CENTER);
        this.secondBalanceTypeTextView.setTextColor(PocketMoneyThemes.balanceBarTextViewColor());
        
        this.secondBalanceAmountTextView = new TextView(context);
        this.secondBalanceAmountTextView.setText(R.string.accounts_view_net_worth);
        this.secondBalanceAmountTextView.setVisibility(GONE);
        this.secondBalanceAmountTextView.setGravity(Gravity.CENTER);
        this.secondBalanceAmountTextView.setTextColor(PocketMoneyThemes.balanceBarTextViewColor());

        this.secondInnerLinearLayout.addView(this.secondBalanceTypeTextView, textLp);
        this.secondInnerLinearLayout.addView(this.secondBalanceAmountTextView, textLp);
        this.balanceView.addView(this.secondInnerLinearLayout);

        this.progressBar = new ProgressBar(context);
        this.progressBar.setIndeterminate(true);
        this.progressBar.setVisibility(GONE);
        this.progressBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.balanceView.addView(this.progressBar);

        addView(this.balanceView);
    }

    public void setSecondBalanceEnabled(boolean enable) {
        this.secondBalanceAmountTextView.setVisibility(enable ? VISIBLE : GONE);
        this.secondBalanceTypeTextView.setVisibility(enable ? VISIBLE : GONE);
        this.seperatorImage.setVisibility(enable ? VISIBLE : GONE);
        this.innerLinearLayout.setOrientation(enable ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        this.secondInnerLinearLayout.setVisibility(enable ? VISIBLE : GONE);
        
        if (enable) {
            this.previousButton.setVisibility(GONE);
        }
    }

    public void setFilter(FilterClass aFilter) {
        this.filter = aFilter;
        if (aFilter != null && aFilter.customFilter()) {
            setBackgroundColor(PocketMoneyThemes.balanceBarBackgroundColor());
            if (this.secondBalanceAmountTextView.getVisibility() != VISIBLE) {
                this.nextButton.setVisibility(GONE);
            }
            this.previousButton.setVisibility(GONE);
        } else {
            setBackgroundColor(PocketMoneyThemes.balanceBarBackgroundColor());
            this.nextButton.setVisibility(VISIBLE);
            this.previousButton.setVisibility(VISIBLE);
        }
    }

    public int nextBalanceTypeAfter(int type) {
        return switch (type) {
            case Enums.kBalanceTypeFuture /*0*/ -> Enums.kBalanceTypeAvailableFunds;/*3*/
            case Enums.kBalanceTypeCleared /*1*/ ->
                    (this.filter == null || !this.filter.customFilter()) ? Enums.kBalanceTypeCurrent/*2*/ : Enums.kBalanceTypeFiltered;/*5*/
            case Enums.kBalanceTypeCurrent /*2*/ -> Enums.kBalanceTypeFuture; /*0*/
            case Enums.kBalanceTypeAvailableFunds /*3*/ -> Enums.kBalanceTypeAvailableCredit; /*4*/
            case Enums.kBalanceTypeAvailableCredit /*4*/ -> Enums.kBalanceTypeCleared;/*1*/
            default -> Enums.kBalanceTypeCurrent;/*2*/
        };
    }

    public int nextBalanceTypeBefore(int type) {
        return switch (type) {
            case Enums.kBalanceTypeFuture /*0*/ -> Enums.kBalanceTypeCurrent;/*2*/
            case Enums.kBalanceTypeCleared /*1*/ ->
                    (this.filter == null || !this.filter.customFilter()) ? Enums.kBalanceTypeAvailableCredit/*4*/ : Enums.kBalanceTypeFiltered;/*5*/
            case Enums.kBalanceTypeCurrent /*2*/ -> Enums.kBalanceTypeCleared;/*1*/
            case Enums.kBalanceTypeAvailableFunds /*3*/ -> Enums.kBalanceTypeFuture;/*0*/
            case Enums.kBalanceTypeAvailableCredit /*4*/ -> Enums.kBalanceTypeAvailableFunds;/*3*/
            default -> Enums.kBalanceTypeAvailableCredit;/*4*/
        };
    }
}
