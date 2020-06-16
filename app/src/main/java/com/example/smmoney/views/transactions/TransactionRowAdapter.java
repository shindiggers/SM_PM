package com.example.smmoney.views.transactions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.smmoney.R;
import com.example.smmoney.misc.PMGlobal;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.TransactionClass;

import java.util.ArrayList;

//import android.widget.CheckBox;
//import android.widget.LinearLayout;
//import android.widget.TextView;

class TransactionRowAdapter extends BaseAdapter {
    private ArrayList<TransactionClass> elements = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;//= LayoutInflater.from(this.mContext); //TODO Fix nullPointer exception which arises here!! UPDATE -> Moved the layout inflator to the constructor. seemed to fix the null pointer exception??

    TransactionRowAdapter(Context aContext) { // constructor for this class?? Think so...
        this.mContext = aContext;
        mInflater = (LayoutInflater.from(mContext));
    }

    public ArrayList<TransactionClass> getElements() {
        return this.elements;
    }

    public void setElements(ArrayList<TransactionClass> aList) {
        this.elements.clear();
        notifyDataSetChanged();
        this.elements = aList;
    }

    @Override
    public int getCount() {
        return this.elements == null ? 0 : this.elements.size();
    }

    @Override
    public Object getItem(int position) {
        return this.elements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TransactionRowHolder holder;
        TransactionClass transaction = this.elements.get(position);
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.transaction_row, parent, false);
            holder = new TransactionRowHolder();
            ((Activity) this.mContext).registerForContextMenu(convertView);
            convertView.setOnClickListener(getBtnClickListener());
            holder.date = convertView.findViewById(R.id.datetextview);
            holder.payee = convertView.findViewById(R.id.payeetextview);
            holder.amount = convertView.findViewById(R.id.amounttextview);
            holder.checkNumber = convertView.findViewById(R.id.checknumbertextview);
            holder.category = convertView.findViewById(R.id.categorytextview);
            holder.runningTotal = convertView.findViewById(R.id.runningtotaltextview);
            holder.selected = convertView.findViewById(R.id.selected);
            holder.selected.setOnCheckedChangeListener(getCheckListener());
            holder.dateAndChecknumberLayout = convertView.findViewById(R.id.dateandchecknumberlayout);
            convertView.setTag(holder);
        } else {
            holder = (TransactionRowHolder) convertView.getTag();
        }
        if (position % 2 == 0) {
            convertView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        } else {
            convertView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
        }
        holder.setTransaction(transaction, this.mContext);
        return convertView;
    }

    private OnCheckedChangeListener getCheckListener() {
        return new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!PMGlobal.programaticUpdate) {
                    TransactionRowHolder holder = (TransactionRowHolder) ((View) buttonView.getParent()).getTag();
                    holder.transaction.hydrate();
                    holder.transaction.setCleared(isChecked);
                    holder.transaction.saveToDatabase();
                    ((TransactionsActivity) TransactionRowAdapter.this.mContext).reloadData();
                    ((TransactionsActivity) TransactionRowAdapter.this.mContext).reloadBalanceBar();
                }
            }
        };
    }

    private OnClickListener getBtnClickListener() {
        return new OnClickListener() {
            public void onClick(View view) {
                TransactionRowHolder holder = (TransactionRowHolder) view.getTag();
                Intent i = new Intent(TransactionRowAdapter.this.mContext, TransactionEditActivity.class);
                i.putExtra("Transaction", holder.transaction);
                TransactionRowAdapter.this.mContext.startActivity(i);
            }
        };
    }
}
