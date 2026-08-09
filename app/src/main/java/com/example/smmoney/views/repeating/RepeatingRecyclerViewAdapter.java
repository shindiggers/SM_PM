package com.example.smmoney.views.repeating;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.transactions.TransactionEditActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;

public class RepeatingRecyclerViewAdapter extends RecyclerView.Adapter<RepeatingRecyclerViewAdapter.RepeatingViewHolder> {
    private ArrayList<TransactionClass> elements = new ArrayList<>();
    private final Context mContext;
    private final LayoutInflater mInflater;

    private final View.OnClickListener postButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            RepeatingViewHolder holder = (RepeatingViewHolder) v.getTag();
            if (holder != null && holder.repeatingTransaction != null) {
                holder.repeatingTransaction.hydrated = false;
                if (holder.repeatingTransaction.getTransaction() != null) {
                    // Post the transaction immediately
                    holder.repeatingTransaction.postAndAdvanceTransaction(
                            holder.repeatingTransaction.getTransaction().getSubTotal(),
                            holder.repeatingTransaction.getNextTransactionDateAfter(holder.repeatingTransaction.lastProcessedDate)
                    );
                    holder.repeatingTransaction.saveToDatabase();
                    
                    Toast.makeText(mContext, "Transaction posted", Toast.LENGTH_LONG).show();

                    // Refresh the whole screen (balances, list re-ordering etc.)
                    if (mContext instanceof RepeatingActivity) {
                        ((RepeatingActivity) mContext).reloadData();
                    }
                }
            }
        }
    };

    public RepeatingRecyclerViewAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setElements(ArrayList<TransactionClass> aList) {
        this.elements = aList;
        // Sort by the calculated next occurrence date instead of the template's internal date
        Collections.sort(this.elements, (object1, object2) -> {
            RepeatingTransactionClass rt1 = new RepeatingTransactionClass(object1);
            rt1.hydrate();
            GregorianCalendar next1 = rt1.getNextTransactionDateAfter(rt1.lastProcessedDate);

            RepeatingTransactionClass rt2 = new RepeatingTransactionClass(object2);
            rt2.hydrate();
            GregorianCalendar next2 = rt2.getNextTransactionDateAfter(rt2.lastProcessedDate);

            if (next1 == null && next2 == null) return 0;
            if (next1 == null) return 1;
            if (next2 == null) return -1;

            return next1.compareTo(next2);
        });
        // Full refresh is intentional as the entire dataset has changed/been re-sorted
        notifyDataSetChanged();
    }

    public ArrayList<TransactionClass> getElements() {
        return this.elements;
    }

    @NonNull
    @Override
    public RepeatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.repeating_transaction_row, parent, false);
        return new RepeatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RepeatingViewHolder holder, int position) {
        TransactionClass transaction = elements.get(position);
        holder.bind(transaction, position);
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    public class RepeatingViewHolder extends RecyclerView.ViewHolder {
        TransactionClass transaction;
        RepeatingTransactionClass repeatingTransaction;
        TextView date;
        TextView payee;
        TextView amount;
        TextView frequency;
        TextView category;
        TextView account;
        MaterialButton postButton;
        View therow;

        RepeatingViewHolder(View v) {
            super(v);
            therow = v.findViewById(R.id.therow);
            date = v.findViewById(R.id.datetextview);
            payee = v.findViewById(R.id.payeetextview);
            amount = v.findViewById(R.id.amounttextview);
            frequency = v.findViewById(R.id.checknumbertextview);
            category = v.findViewById(R.id.categorytextview);
            account = v.findViewById(R.id.runningtotaltextview);
            postButton = v.findViewById(R.id.postbutton);

            therow.setOnClickListener(v1 -> {
                Intent i = new Intent(mContext, TransactionEditActivity.class);
                i.putExtra("Transaction", transaction);
                if (mContext instanceof RepeatingActivity) {
                    ((RepeatingActivity) mContext).launchEdit(i);
                } else {
                    mContext.startActivity(i);
                }
            });

            postButton.setTag(this);
            postButton.setOnClickListener(postButtonListener);
        }

        void bind(TransactionClass trans, int position) {
            this.transaction = trans;
            this.repeatingTransaction = new RepeatingTransactionClass(this.transaction);
            this.repeatingTransaction.hydrate();

            GregorianCalendar dateOfNextRepeat = this.repeatingTransaction.getNextTransactionDateAfter(this.repeatingTransaction.lastProcessedDate);
            GregorianCalendar today = CalExt.beginningOfToday();
            GregorianCalendar cutoff7Days = CalExt.endOfDay(CalExt.addDays((GregorianCalendar) today.clone(), 7));

            if (!dateOfNextRepeat.after(CalExt.endOfToday())) {
                // Due today or before: Muted Red
                this.postButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(mContext, R.color.theme_red_bar_color)));
            } else if (!dateOfNextRepeat.after(cutoff7Days)) {
                // If due in the next 7 days: Orange
                this.postButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(mContext, R.color.theme_orange_label_color)));
            } else {
                // If due more than 7 days away: Gray
                this.postButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(mContext, R.color.common_action_bar_splitter)));
            }

            String dateText = CalExt.descriptionWithShortDate(dateOfNextRepeat).replaceFirst("198", "8").replaceFirst("199", "9").replaceFirst("200", "0").replaceFirst("201", "1").replaceFirst("202", "2").replaceFirst("203", "3").replaceFirst("204", "4");
            this.date.setText(dateText);
            this.frequency.setText(this.repeatingTransaction.typeEveryAsString());
            
            if (this.transaction.isTransfer()) {
                this.payee.setText(String.format("%s <%s>", this.transaction.getPayee(), this.transaction.getTransferToAccount()));
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

            if (position % 2 == 0) {
                itemView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
            } else {
                itemView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
            }
        }
    }
}
