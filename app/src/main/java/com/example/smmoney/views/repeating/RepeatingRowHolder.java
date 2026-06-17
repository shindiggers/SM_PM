package com.example.smmoney.views.repeating;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;

import java.util.GregorianCalendar;

class RepeatingRowHolder {
    public TextView account;
    public TextView amount;
    public TextView category;
    public TextView date;
    TextView frequency;
    public TextView payee;
    Button postButton;
    RepeatingTransactionClass repeatingTransaction;
    public TransactionClass transaction;

    void setTransaction(TransactionClass trans, Context context) {
        this.transaction = trans;
        this.repeatingTransaction = new RepeatingTransactionClass(this.transaction);
        this.repeatingTransaction.hydrate();
        if (this.repeatingTransaction.isOverdueOnDate(new GregorianCalendar())) {
            //this.postButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_orange));
            Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.button_orange, null);
            this.postButton.setBackground(drawable);
        } else if (this.repeatingTransaction.isOverdue()) {
            //this.postButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_red));
            Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.button_red, null);
            this.postButton.setBackground(drawable);
        } else {
            //this.postButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_grey));
            Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.button_grey, null);
            this.postButton.setBackground(drawable);
        }

        // find date on which transaction next repeats and assign to temp GregorianCalendar local variable
        GregorianCalendar dateOfNextRepeat = this.repeatingTransaction.getNextTransactionDateAfter(this.repeatingTransaction.lastProcessedDate);
        this.date.setText(CalExt.descriptionWithShortDate(this.transaction.getDate()).replaceFirst("198", "8").replaceFirst("199", "9").replaceFirst("200", "0").replaceFirst("201", "1").replaceFirst("202", "2").replaceFirst("203", "3").replaceFirst("204", "4"));
        this.date.setText(CalExt.descriptionWithShortDate(dateOfNextRepeat).replaceFirst("198", "8").replaceFirst("199", "9").replaceFirst("200", "0").replaceFirst("201", "1").replaceFirst("202", "2").replaceFirst("203", "3").replaceFirst("204", "4"));
        this.frequency.setText(this.repeatingTransaction.typeEveryAsString());
        if (this.transaction.isTransfer()) {
            this.payee.setText(this.transaction.getPayee() + " <" + this.transaction.getTransferToAccount() + ">");
        } else {
            this.payee.setText(this.transaction.getPayee());
        }
        this.amount.setText(this.transaction.subTotalAsCurrency());
        if (this.transaction.getSubTotal() < 0.0d) {
            this.amount.setTextColor(PocketMoneyThemes.redLabelColor());
        } else {
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
