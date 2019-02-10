package com.example.smmoney.views.splits;

import android.content.Context;
import android.widget.TextView;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.SplitsClass;

public class SplitsRowHolder {
    TextView amount;
    TextView category;
    TextView memo;
    SplitsClass split;
    TextView theClass;

    public void setSplit(SplitsClass aSplit, Context aContext) {
        this.split = aSplit;
        if (this.split.isTransfer()) {
            this.category.setText("<" + this.split.getTransferToAccount() + ">");
        } else {
            this.category.setText(this.split.getCategory());
        }
        this.memo.setText(this.split.getMemo());
        this.theClass.setText(this.split.getClassName());
        if (this.split.getAmount() < 0.0d) {
            this.amount.setTextColor(PocketMoneyThemes.redLabelColor());
        } else {
            this.amount.setTextColor(PocketMoneyThemes.greenDepositColor());
        }
        this.amount.setText(this.split.amountAsCurrency());
        this.category.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.memo.setTextColor(PocketMoneyThemes.alternateCellTextColor());
        this.theClass.setTextColor(PocketMoneyThemes.alternateCellTextColor());
    }
}
