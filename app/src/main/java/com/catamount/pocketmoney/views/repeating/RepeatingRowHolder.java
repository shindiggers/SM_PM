package com.catamount.pocketmoney.views.repeating;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.CalExt;
import com.catamount.pocketmoney.misc.CurrencyExt;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.records.RepeatingTransactionClass;
import com.catamount.pocketmoney.records.TransactionClass;
import java.util.GregorianCalendar;

public class RepeatingRowHolder {
    public TextView account;
    public TextView amount;
    public TextView category;
    public TextView date;
    public TextView frequency;
    public TextView payee;
    public Button postButton;
    public RepeatingTransactionClass repeatingTransaction;
    public LinearLayout therow;
    public TransactionClass transaction;

    public void setTransaction(TransactionClass trans, Context context) {
        this.transaction = trans;
        this.repeatingTransaction = new RepeatingTransactionClass(this.transaction);
        this.repeatingTransaction.hydrate();
        if (this.repeatingTransaction.isOverdueOnDate(new GregorianCalendar())) {
            this.postButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_orange));
        } else if (this.repeatingTransaction.isOverdue()) {
            this.postButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_red));
        } else {
            this.postButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_grey));
        }
        this.date.setText(CalExt.descriptionWithShortDate(this.transaction.getDate()).replaceFirst("198", "8").replaceFirst("199", "9").replaceFirst("200", "0").replaceFirst("201", "1").replaceFirst("202", "2"));
        scaleTextField(this.date);
        this.frequency.setText(this.repeatingTransaction.typeEveryAsString());
        scaleTextField(this.frequency);
        if (this.transaction.isTransfer()) {
            this.payee.setText(String.valueOf(this.transaction.getPayee()) + " <" + this.transaction.getTransferToAccount() + ">");
        } else {
            this.payee.setText(this.transaction.getPayee());
        }
        if (this.transaction.getSubTotal() < 0.0d) {
            this.amount.setText(CurrencyExt.amountAsCurrency(this.transaction.getSubTotal(), this.transaction.getCurrencyCode()));
            this.amount.setTextColor(PocketMoneyThemes.redLabelColor());
        } else {
            this.amount.setText(CurrencyExt.amountAsCurrency(this.transaction.getSubTotal(), this.transaction.getCurrencyCode()));
            this.amount.setTextColor(PocketMoneyThemes.greenDepositColor());
        }
        if (this.transaction.getNumberOfSplits() > 1) {
            this.category.setText(Locales.kLOC_GENERAL_SPLITS);
        } else {
            this.category.setText(this.transaction.getCategory());
        }
        this.account.setText(this.transaction.getAccount());
        this.date.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.payee.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.frequency.setTextColor(PocketMoneyThemes.alternateCellTextColor());
        this.category.setTextColor(PocketMoneyThemes.alternateCellTextColor());
        this.account.setTextColor(PocketMoneyThemes.alternateCellTextColor());
    }

    private void scaleTextField(TextView txtView) {
        int parentWidth = txtView.getWidth();
        float freqWidth = txtView.getPaint().measureText((String) txtView.getText());
        if (freqWidth > ((float) parentWidth)) {
            txtView.setTextScaleX(((float) parentWidth) / freqWidth);
        }
    }
}
