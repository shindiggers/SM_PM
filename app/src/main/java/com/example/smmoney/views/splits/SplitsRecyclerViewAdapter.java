package com.example.smmoney.views.splits;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.SplitsClass;
import com.example.smmoney.records.TransactionClass;

import java.util.List;

public class SplitsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_SPLIT = 0;
    public static final int TYPE_ADD_PLACEHOLDER = 1;
    public static final int TYPE_REMAINDER_PLACEHOLDER = 2;

    private final Context context;
    private final LayoutInflater inflater;
    private final TransactionClass transaction;
    private List<SplitsClass> items;

    public SplitsRecyclerViewAdapter(Context context, TransactionClass transaction) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.transaction = transaction;
        this.items = transaction.getSplits();
    }

    public void setItems(List<SplitsClass> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        double remainder = transaction.getSubTotal() - splitsSum();
        boolean hasRemainder = Math.abs(remainder) > 0.009;
        
        if (items.isEmpty()) {
            return 1; // Only "Add first split"
        }
        
        return items.size() + (hasRemainder ? 2 : 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < items.size()) {
            return TYPE_SPLIT;
        }
        
        if (items.isEmpty()) {
            return TYPE_ADD_PLACEHOLDER;
        }

        double remainder = transaction.getSubTotal() - splitsSum();
        boolean hasRemainder = Math.abs(remainder) > 0.009;
        
        if (hasRemainder && position == items.size()) {
            return TYPE_REMAINDER_PLACEHOLDER;
        }
        
        return TYPE_ADD_PLACEHOLDER;
    }

    private double splitsSum() {
        double sum = 0;
        for (SplitsClass s : items) sum += s.getAmount();
        return sum;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SPLIT) {
            View v = inflater.inflate(R.layout.split_row, parent, false);
            return new SplitViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.lookup_row, parent, false);
            return new AddViewHolder(v, viewType == TYPE_REMAINDER_PLACEHOLDER);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SplitViewHolder) {
            ((SplitViewHolder) holder).bind(items.get(position), position);
        } else {
            ((AddViewHolder) holder).bind(position);
        }
    }

    class SplitViewHolder extends RecyclerView.ViewHolder {
        TextView category;
        TextView amount;
        TextView memo;
        TextView theClass;
        SplitsClass split;

        SplitViewHolder(View v) {
            super(v);
            category = v.findViewById(R.id.categorytextview);
            amount = v.findViewById(R.id.amounttextview);
            memo = v.findViewById(R.id.memotextview);
            theClass = v.findViewById(R.id.classtextview);
            
            v.setOnClickListener(v1 -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && context instanceof SplitsActivity) {
                    Intent i = new Intent(context, SplitsEditActivity.class);
                    i.putExtra("Split", items.get(pos));
                    i.putExtra("Transaction", transaction);
                    i.putExtra("SplitIndex", pos);
                    ((SplitsActivity) context).editSplitLauncher.launch(i);
                }
            });
        }

        void bind(SplitsClass s, int position) {
            this.split = s;
            if (this.split.isTransfer()) {
                String transferToAccount = context.getString(R.string.splits_transfer_to_account, this.split.getTransferToAccount());
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

            if (position % 2 == 0) {
                itemView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
            } else {
                itemView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
            }
        }
    }

    class AddViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        boolean isRemainder;

        AddViewHolder(View v, boolean isRemainder) {
            super(v);
            this.textView = (TextView) v;
            this.isRemainder = isRemainder;
            this.textView.setOnClickListener(v1 -> {
                if (context instanceof SplitsActivity) {
                    ((SplitsActivity) context).newSplitAction(isRemainder);
                }
            });
        }

        void bind(int position) {
            String label;
            if (isRemainder) {
                double remainder = transaction.getSubTotal() - splitsSum();
                label = "+ Allocate remainder (" + CurrencyExt.amountAsCurrency(remainder) + ") to...";
                textView.setTextColor(PocketMoneyThemes.greenDepositColor());
            } else {
                if (items.isEmpty()) {
                    label = "Tap to enter data for first split item";
                } else {
                    label = "+ Add another split";
                }
                textView.setTextColor(PocketMoneyThemes.currentTintColor());
            }
            textView.setText(label);
            
            if (position % 2 == 0) {
                itemView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
            } else {
                itemView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
            }
        }
    }
}
