package com.example.smmoney.views.transactions;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.example.smmoney.database.AccountDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PMGlobal;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.CheckBoxTint;

import java.util.GregorianCalendar;
import java.util.Objects;

class TransactionRowHolder {
    public TextView amount;
    public TextView category;
    public TextView date;
    public TextView payee;
    public CheckBox selected;
    public LinearLayout therow;
    public TransactionClass transaction;
    TextView checkNumber;
    LinearLayout dateAndChecknumberLayout;
    TextView runningTotal;

    void setTransaction(TransactionClass trans, Context context) {
        this.transaction = trans;
        if (willDisplay(Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_DATE_FIELD), this.date)) {
            this.date.setText(CalExt.descriptionWithShortDate(this.transaction.getDate()).replaceFirst("197", "7").replaceFirst("198", "8").replaceFirst("199", "9").replaceFirst("200", "0").replaceFirst("201", "1").replaceFirst("202", "2").replaceFirst("203", "3").replaceFirst("204", "4"));
            this.date.setTextColor(PocketMoneyThemes.alternateCellTextColor());
        }
        if (this.transaction.getType() != Enums.kTransactionTypeTransferFrom /*3*/ && this.transaction.getType() != Enums.kTransactionTypeTransferTo /*2*/) {
            this.payee.setText(this.transaction.getPayee());
        } else if (Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_TRANSTOANDTO_FIELD)) {
            TextView textView = this.payee;
            CharSequence payee = this.transaction.getPayee().length() != 0 ? this.transaction.getPayee() : (this.transaction.getTransferToAccount() == null || this.transaction.getTransferToAccount().length() <= 0) ? "" : "<" + this.transaction.getTransferToAccount() + ">";
            textView.setText(payee);
        } else if (!(this.transaction.getTransferToAccount() == null || this.transaction.getTransferToAccount().length() == 0)) {
            this.payee.setText("<" + this.transaction.getTransferToAccount() + ">");
        }
        if (this.transaction.getDate().after(CalExt.endOfDay(new GregorianCalendar()))) {
            this.payee.setTextColor(PocketMoneyThemes.alternateCellTextColor());
        } else {
            this.payee.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        }
        if (this.transaction.getType() == Enums.kTransactionTypeWithdrawal /*0*/ || this.transaction.getType() == Enums.kTransactionTypeTransferTo/*2*/) {
            this.amount.setText(this.transaction.subTotalAsCurrency());
            this.amount.setTextColor(PocketMoneyThemes.redLabelColor());
        } else {
            this.amount.setText(this.transaction.subTotalAsCurrency());
            this.amount.setTextColor(PocketMoneyThemes.greenDepositColor());
        }
        if (willDisplay(Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_ID_FIELD), this.checkNumber)) {
            if (!Prefs.getBooleanPref(Prefs.TRANSACTIONS_TRUNCATE_ID) && this.transaction.getCheckNumber().length() > 7) {
                this.dateAndChecknumberLayout.setLayoutParams(new LayoutParams(-2, -1));
            }
            this.checkNumber.setText(this.transaction.getCheckNumber());
        }
        String categoryNotesString = "";
        if (Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_CATEGORY_FIELD)) {
            this.category.setTextColor(PocketMoneyThemes.alternateCellTextColor());
            if (this.transaction.getNumberOfSplits() > 1) {
                categoryNotesString = Locales.kLOC_GENERAL_SPLITS;
                if (this.transaction.getSubTotal() != this.transaction.getSplitsTotal()) {
                    Math.abs(this.transaction.getSubTotal() - this.transaction.getSplitsTotal());
                }
            } else {
                categoryNotesString = this.transaction.getCategory();
            }
        }
        if (Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_CLASS_FIELD)) {
            StringBuilder stringBuilder = new StringBuilder(String.valueOf(categoryNotesString));
            String str = (categoryNotesString.length() <= 0 || this.transaction.getClassName() == null || this.transaction.getClassName().length() <= 0) ? "" : "/";
            categoryNotesString = stringBuilder.append(str).append(this.transaction.getClassName()).toString();
        }
        if (Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_NOTES_FIELD)) {
            StringBuilder stringBuilder = new StringBuilder(String.valueOf(categoryNotesString));
            String str = (categoryNotesString.length() <= 0 || this.transaction.getMemo() == null || this.transaction.getMemo().length() <= 0) ? "" : " \ufffd ";
            categoryNotesString = stringBuilder.append(str).append(this.transaction.getMemo()).toString();
        }
        this.category.setText(categoryNotesString);
        if (willDisplay(Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_RUNNING_FIELD), this.runningTotal)) {
            this.runningTotal.setText(this.transaction.runningBalanceAsCurrency());
            if (Objects.requireNonNull(AccountDB.recordFor(this.transaction.getAccount())).balanceExceedsLimitWithRunningBalance(this.transaction.runningBalance)) {
                this.runningTotal.setTextColor(PocketMoneyThemes.redLabelColor());
            }
        }
        this.checkNumber.setTextColor(PocketMoneyThemes.alternateCellTextColor());
        this.category.setTextColor(PocketMoneyThemes.alternateCellTextColor());
        PMGlobal.programaticUpdate = true;
        this.selected.setChecked(this.transaction.getCleared());
        CheckBoxTint.colorCheckBox(this.selected);
        PMGlobal.programaticUpdate = false;
    }

    private boolean willDisplay(boolean show, View theView) {
        if (show) {
            theView.setVisibility(View.VISIBLE);
        } else {
            theView.setVisibility(View.INVISIBLE);
        }
        return show;
    }
}
