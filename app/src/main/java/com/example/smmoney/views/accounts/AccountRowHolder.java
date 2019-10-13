package com.example.smmoney.views.accounts;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.PMGlobal;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;

class AccountRowHolder {
    public AccountClass account;
    TextView accountname;
    public TextView exchangeRate;
    ImageView icon_image;
    ImageView newtransbutton;
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

    private void updateBalanceLabel() {
        int balanceType = Prefs.getBooleanPref(Prefs.BALANCEBARUNIFIED) ? Prefs.getIntPref(Prefs.BALANCETYPE) : Prefs.getIntPref(Prefs.BALANCEBARREGISTER);
        if (Enums.kBalanceTypeFiltered/*5*/ == this.account.getType()) {
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
