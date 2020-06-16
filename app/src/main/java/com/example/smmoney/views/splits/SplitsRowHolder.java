package com.example.smmoney.views.splits;

import android.content.Context;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.SplitsClass;

class SplitsRowHolder {
    TextView amount;
    TextView category;
    TextView memo;
    SplitsClass split;
    TextView theClass;

    void setSplit(SplitsClass aSplit, Context aContext) {
        this.split = aSplit;
        if (this.split.isTransfer()) {
            String transferToAccount = aContext.getString(R.string.splits_transfer_to_account, this.split.getTransferToAccount());
            this.category.setText(transferToAccount);
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
