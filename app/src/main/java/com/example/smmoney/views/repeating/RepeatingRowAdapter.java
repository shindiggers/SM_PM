package com.example.smmoney.views.repeating;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.transactions.TransactionEditActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class RepeatingRowAdapter extends BaseAdapter {
    private ArrayList<TransactionClass> elements;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final OnClickListener postButtonListener = new OnClickListener() {
        public void onClick(View v) {
            RepeatingRowHolder holder = (RepeatingRowHolder) ((View) v.getParent().getParent()).getTag();
            holder.repeatingTransaction.hydrated = false;
            if (holder.repeatingTransaction.getTransaction() != null) {
                RepeatingTransactionClass repeaterSM;
                repeaterSM = new RepeatingTransactionClass(holder.repeatingTransaction.getTransaction());
                repeaterSM.postAndAdvanceTransaction(repeaterSM.getTransaction().getSubTotal(), repeaterSM.getTransaction().getDate());
                repeaterSM.saveToDatabase();
                Toast.makeText(mContext, "Transaction posted", Toast.LENGTH_LONG).show();
                holder.setTransaction(repeaterSM.getTransaction(), mContext);
                //Intent intent = new Intent(RepeatingRowAdapter.this.mContext, TransactionEditActivity.class);
                //intent.putExtra("repeatingTransaction", holder.repeatingTransaction);
                //intent.putExtra("Posting", true);
                //RepeatingRowAdapter.this.mContext.startActivity(intent);
            }
        }
    };

    RepeatingRowAdapter(Context aContext) {
        this.mContext = aContext;
        this.elements = new ArrayList<>();
        this.mInflater = LayoutInflater.from(this.mContext);
    }

    public ArrayList<TransactionClass> getElements() {
        return this.elements;
    }

    public void setElements(ArrayList<TransactionClass> aList) {
        this.elements.clear();
        this.elements = aList;
        Collections.sort(this.elements, new Comparator<TransactionClass>() {
            public int compare(TransactionClass object1, TransactionClass object2) {
                return object1.getDate().before(object2.getDate()) ? -1 : 1;
            }
        });
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.elements.size();
    }

    public Object getItem(int position) {
        return this.elements.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TransactionClass transaction = this.elements.get(position);
        RepeatingRowHolder holder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.repeating_transaction_row, parent, false);
            holder = new RepeatingRowHolder();
            ((Activity) this.mContext).registerForContextMenu(convertView);
            convertView.setOnClickListener(getBtnClickListener());
            holder.date = convertView.findViewById(R.id.datetextview);
            holder.payee = convertView.findViewById(R.id.payeetextview);
            holder.amount = convertView.findViewById(R.id.amounttextview);
            holder.frequency = convertView.findViewById(R.id.checknumbertextview);
            holder.category = convertView.findViewById(R.id.categorytextview);
            holder.account = convertView.findViewById(R.id.runningtotaltextview);
            holder.postButton = convertView.findViewById(R.id.postbutton);
            holder.postButton.setOnClickListener(this.postButtonListener);
            convertView.findViewById(R.id.selected).setVisibility(View.GONE);
            convertView.setTag(holder);
        } else {
            holder = (RepeatingRowHolder) convertView.getTag();
        }
        if (position % 2 == 0) {
            convertView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        } else {
            convertView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
        }
        holder.setTransaction(transaction, this.mContext);
        return convertView;
    }

    private OnClickListener getBtnClickListener() {
        return new OnClickListener() {
            public void onClick(View view) {
                RepeatingRowHolder holder = (RepeatingRowHolder) view.getTag();
                Intent i = new Intent(RepeatingRowAdapter.this.mContext, TransactionEditActivity.class);
                i.putExtra("Transaction", holder.transaction);
                RepeatingRowAdapter.this.mContext.startActivity(i);
            }
        };
    }
}
