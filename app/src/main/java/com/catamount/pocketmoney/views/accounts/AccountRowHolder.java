package com.catamount.pocketmoney.views.accounts;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.catamount.pocketmoney.misc.CurrencyExt;
import com.catamount.pocketmoney.misc.PMGlobal;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.records.AccountClass;

public class AccountRowHolder {
    public AccountClass account;
    public TextView accountname;
    public TextView exchangeRate;
    public ImageView icon_image;
    public ImageView newtransbutton;
    public CheckBox selected;
    public RelativeLayout therow;
    public TextView totalworth;

    public void setAccount(AccountClass act, Context mContext) {
        this.account = act;
        this.accountname.setText(this.account.getAccount());
        this.accountname.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.icon_image.setImageResource(this.account.getIconFileNameResourceIDUsingContext(mContext));
        PMGlobal.programaticUpdate = true;
        this.selected.setChecked(this.account.getTotalWorth());
        this.selected.setButtonDrawable(Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android"));
        PMGlobal.programaticUpdate = false;
        updateBalanceLabel();
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            this.exchangeRate.setVisibility(View.VISIBLE);
        } else {
            this.exchangeRate.setVisibility(View.GONE);
        }
        if (this.account.getExchangeRate() == 1.0d) {
            this.exchangeRate.setVisibility(View.GONE);
        } else {
            this.exchangeRate.setText(CurrencyExt.exchangeRateAsString(this.account.getExchangeRate()));
        }
        this.exchangeRate.setTextColor(PocketMoneyThemes.alternateCellTextColor());
        if (this.account.getType() == 5) {
            this.newtransbutton.setVisibility(View.VISIBLE);
        }
    }

    public void updateBalanceLabel() {
        int balanceType = Prefs.getBooleanPref(Prefs.BALANCEBARUNIFIED) ? Prefs.getIntPref(Prefs.BALANCETYPE) : Prefs.getIntPref(Prefs.BALANCEBARREGISTER);
        if (5 == this.account.getType()) {
            this.totalworth.setText("");
            return;
        }
        double balance = this.account.balanceOfType(balanceType);
        this.totalworth.setText(this.account.formatAmountAsCurrency(balance));
        if (this.account.balanceExceedsLimit()) {
            this.totalworth.setTextColor(PocketMoneyThemes.redLabelColor());
        } else if (balance < 0.0d) {
            this.totalworth.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        } else {
            this.totalworth.setTextColor(PocketMoneyThemes.greenDepositColor());
        }
    }
}
