package com.example.smmoney.views.transactions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PMGlobal;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.CheckBoxTint;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Objects;

public class TransactionRecyclerViewAdapter extends RecyclerView.Adapter<TransactionRecyclerViewAdapter.TransactionViewHolder> {
    private ArrayList<TransactionClass> elements = new ArrayList<>();
    private final Context mContext;
    private final LayoutInflater mInflater;

    public TransactionRecyclerViewAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
    }

    public void setElements(ArrayList<TransactionClass> aList) {
        this.elements = aList;
        notifyDataSetChanged();
    }

    public ArrayList<TransactionClass> getElements() {
        return this.elements;
    }

    public int getCount() {
        return elements.size();
    }

    public Object getItem(int position) {
        if (position >= 0 && position < elements.size()) {
            return elements.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.transaction_row, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionClass transaction = elements.get(position);
        holder.bind(transaction, position);
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        TransactionClass transaction;
        TextView date;
        TextView payee;
        TextView amount;
        TextView checkNumber;
        TextView category;
        TextView runningTotal;
        CheckBox selected;
        View therow;

        TransactionViewHolder(View v) {
            super(v);
            therow = v.findViewById(R.id.therow);
            date = v.findViewById(R.id.datetextview);
            payee = v.findViewById(R.id.payeetextview);
            amount = v.findViewById(R.id.amounttextview);
            checkNumber = v.findViewById(R.id.checknumbertextview);
            category = v.findViewById(R.id.categorytextview);
            runningTotal = v.findViewById(R.id.runningtotaltextview);
            selected = v.findViewById(R.id.selected);

            therow.setOnClickListener(v1 -> {
                Intent i = new Intent(mContext, TransactionEditActivity.class);
                i.putExtra("Transaction", transaction);
                if (mContext instanceof TransactionsActivity) {
                    ((TransactionsActivity) mContext).editLauncher.launch(i);
                } else {
                    mContext.startActivity(i);
                }
            });

            selected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!PMGlobal.programaticUpdate) {
                    transaction.hydrate();
                    transaction.setCleared(isChecked);
                    transaction.saveToDatabase();
                    if (mContext instanceof TransactionsActivity) {
                        ((TransactionsActivity) mContext).reloadData();
                        ((TransactionsActivity) mContext).reloadBalanceBar();
                    }
                }
            });
        }

        void bind(TransactionClass trans, int position) {
            this.transaction = trans;
            
            // Ported logic from TransactionRowHolder.setTransaction
            if (willDisplay(Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_DATE_FIELD), this.date)) {
                this.date.setText(CalExt.descriptionWithShortDate(this.transaction.getDate()).replaceFirst("197", "7").replaceFirst("198", "8").replaceFirst("199", "9").replaceFirst("200", "0").replaceFirst("201", "1").replaceFirst("202", "2").replaceFirst("203", "3").replaceFirst("204", "4"));
                this.date.setTextColor(PocketMoneyThemes.alternateCellTextColor());
            }

            if (this.transaction.getType() != Enums.kTransactionTypeTransferFrom && this.transaction.getType() != Enums.kTransactionTypeTransferTo) {
                this.payee.setText(this.transaction.getPayee());
            } else if (Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_TRANSTOANDTO_FIELD)) {
                CharSequence payeeText = !this.transaction.getPayee().isEmpty() ? this.transaction.getPayee() : (this.transaction.getTransferToAccount() == null || this.transaction.getTransferToAccount().length() <= 0) ? "" : "<" + this.transaction.getTransferToAccount() + ">";
                this.payee.setText(payeeText);
            } else if (!(this.transaction.getTransferToAccount() == null || this.transaction.getTransferToAccount().isEmpty())) {
                this.payee.setText("<" + this.transaction.getTransferToAccount() + ">");
            }

            if (this.transaction.getDate().after(CalExt.endOfDay(new GregorianCalendar()))) {
                this.payee.setTextColor(PocketMoneyThemes.alternateCellTextColor());
            } else {
                this.payee.setTextColor(PocketMoneyThemes.primaryCellTextColor());
            }

            if (this.transaction.getType() == Enums.kTransactionTypeWithdrawal || this.transaction.getType() == Enums.kTransactionTypeTransferTo) {
                this.amount.setText(this.transaction.subTotalAsCurrency());
                this.amount.setTextColor(PocketMoneyThemes.redLabelColor());
            } else {
                this.amount.setText(this.transaction.subTotalAsCurrency());
                this.amount.setTextColor(PocketMoneyThemes.greenDepositColor());
            }

            if (willDisplay(Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_ID_FIELD), this.checkNumber)) {
                if (!Prefs.getBooleanPref(Prefs.TRANSACTIONS_TRUNCATE_ID) && this.transaction.getCheckNumber().length() > 7) {
                    this.checkNumber.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    this.date.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                this.checkNumber.setText(this.transaction.getCheckNumber());
            }

            String categoryNotesString = "";
            if (Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_CATEGORY_FIELD)) {
                this.category.setTextColor(PocketMoneyThemes.alternateCellTextColor());
                if (this.transaction.getNumberOfSplits() > 1) {
                    categoryNotesString = Locales.kLOC_GENERAL_SPLITS;
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
                AccountClass act = AccountDB.recordFor(this.transaction.getAccount());
                if (act != null && act.balanceExceedsLimitWithRunningBalance(this.transaction.runningBalance)) {
                    this.runningTotal.setTextColor(PocketMoneyThemes.redLabelColor());
                } else {
                    this.runningTotal.setTextColor(PocketMoneyThemes.alternateCellTextColor());
                }
            }

            this.checkNumber.setTextColor(PocketMoneyThemes.alternateCellTextColor());
            this.category.setTextColor(PocketMoneyThemes.alternateCellTextColor());
            
            PMGlobal.programaticUpdate = true;
            this.selected.setChecked(this.transaction.getCleared());
            CheckBoxTint.colorCheckBox(this.selected);
            PMGlobal.programaticUpdate = false;

            if (position % 2 == 0) {
                itemView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
            } else {
                itemView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
            }
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
}
