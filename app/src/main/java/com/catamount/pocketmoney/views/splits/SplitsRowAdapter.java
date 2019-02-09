package com.catamount.pocketmoney.views.splits;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.records.SplitsClass;
import com.catamount.pocketmoney.records.TransactionClass;
import java.util.ArrayList;

public class SplitsRowAdapter extends BaseAdapter {
    private ArrayList<SplitsClass> elements = new ArrayList();
    private Context mContext;
    private LayoutInflater mInflater = LayoutInflater.from(this.mContext);
    private TransactionClass transaction;

    public SplitsRowAdapter(Context aContext, TransactionClass aTrans) {
        this.mContext = aContext;
        this.transaction = aTrans;
    }

    public ArrayList<SplitsClass> getElements() {
        return this.elements;
    }

    public void setElements(ArrayList<SplitsClass> aList) {
        this.elements = aList;
    }

    public int getCount() {
        return this.elements.size();
    }

    public Object getItem(int position) {
        return this.elements.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SplitsRowHolder holder;
        SplitsClass split = this.elements.get(position);
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.split_row, null);
            holder = new SplitsRowHolder();
            ((Activity) this.mContext).registerForContextMenu(convertView);
            convertView.setOnClickListener(getBtnClickListener());
            holder.amount = convertView.findViewById(R.id.amounttextview);
            holder.category = convertView.findViewById(R.id.categorytextview);
            holder.theClass = convertView.findViewById(R.id.classtextview);
            holder.memo = convertView.findViewById(R.id.memotextview);
            convertView.setTag(holder);
        } else {
            holder = (SplitsRowHolder) convertView.getTag();
        }
        if (position % 2 == 0) {
            convertView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        } else {
            convertView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
        }
        holder.setSplit(split, this.mContext);
        return convertView;
    }

    private OnClickListener getBtnClickListener() {
        return new OnClickListener() {
            public void onClick(View view) {
                SplitsRowHolder holder = (SplitsRowHolder) view.getTag();
                Intent i = new Intent(SplitsRowAdapter.this.mContext, SplitsEditActivity.class);
                i.putExtra("Split", holder.split);
                i.putExtra("Transaction", SplitsRowAdapter.this.transaction);
                i.putExtra("SplitIndex", SplitsRowAdapter.this.elements.indexOf(holder.split));
                ((Activity) SplitsRowAdapter.this.mContext).startActivityForResult(i, 3);
            }
        };
    }
}
