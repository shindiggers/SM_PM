package com.example.smmoney.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.splits.SplitsActivity;

public class BalanceBar extends FrameLayout {
    public TextView balanceAmountTextView;
    public TextView balanceTypeTextView;
    public LinearLayout balanceView;
    private FilterClass filter;
    LinearLayout innerLinearLayout;
    public View nextButton;
    public View previousButton;
    public ProgressBar progressBar;
    public TextView secondBalanceAmountTextView;
    public TextView secondBalanceTypeTextView;
    LinearLayout secondInnerLinearLayout;
    public ImageView seperatorImage;

    public BalanceBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.singlebalancebar);
        LinearLayout layout1 = new LinearLayout(context);
        this.balanceView = new LinearLayout(context);
        this.balanceView.setLayoutParams(new LayoutParams(-1, -1, 1));
        LayoutParams lp = new LayoutParams(-1, -1, 1);
        layout1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(0, -1, 1.0f);
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
        LinearLayout.LayoutParams innerllp = new LinearLayout.LayoutParams(-1, -1, 1.0f);
        lp = new LayoutParams(-2, -2, 17);
        this.balanceTypeTextView = new TextView(context);
        this.balanceTypeTextView.setText("Current Balance");
        this.balanceTypeTextView.setGravity(17);
        this.balanceAmountTextView = new TextView(context);
        this.balanceAmountTextView.setText("0.00");
        this.balanceAmountTextView.setGravity(17);
        llp.setMargins(10, 0, 0, 0);
        this.innerLinearLayout.addView(this.balanceTypeTextView, innerllp);
        this.innerLinearLayout.addView(this.balanceAmountTextView, innerllp);
        this.balanceView.addView(this.innerLinearLayout, llp);
        this.seperatorImage = new ImageView(context);
        this.seperatorImage.setBackgroundResource(R.drawable.seperatorline);
        this.seperatorImage.setVisibility(GONE);
        this.seperatorImage.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 0.0f));
        this.secondBalanceTypeTextView = new TextView(context);
        this.secondBalanceTypeTextView.setText("Second Balance");
        this.secondBalanceTypeTextView.setVisibility(GONE);
        this.secondBalanceTypeTextView.setGravity(17);
        this.secondBalanceAmountTextView = new TextView(context);
        this.secondBalanceAmountTextView.setText("0.00");
        this.secondBalanceAmountTextView.setVisibility(GONE);
        this.secondBalanceAmountTextView.setGravity(17);
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
        TextView textView = this.secondBalanceTypeTextView;
        if (enable) {
            i = 0;
        } else {
            i = 8;
        }
        textView.setVisibility(i);
        ImageView imageView = this.seperatorImage;
        if (enable) {
            i = 0;
        } else {
            i = 8;
        }
        imageView.setVisibility(i);
        LinearLayout linearLayout = this.innerLinearLayout;
        if (enable) {
            i = 1;
        } else {
            i = 0;
        }
        linearLayout.setOrientation(i);
        LinearLayout linearLayout2 = this.secondInnerLinearLayout;
        if (!enable) {
            i2 = 8;
        }
        linearLayout2.setVisibility(i2);
    }

    public void setFilter(FilterClass aFilter) {
        this.filter = aFilter;
    }

    public int nextBalanceTypeAfter(int type) {
        switch (type) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return 3;
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return (this.filter == null || !this.filter.customFilter()) ? 2 : 5;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                return 0;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                return 4;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                return 1;
            default:
                return 2;
        }
    }

    public int nextBalanceTypeBefore(int type) {
        switch (type) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return 2;
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return (this.filter == null || !this.filter.customFilter()) ? 4 : 5;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                return 1;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                return 0;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                return 3;
            default:
                return 4;
        }
    }
}
