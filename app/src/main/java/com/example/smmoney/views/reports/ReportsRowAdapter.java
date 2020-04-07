package com.example.smmoney.views.reports;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.support.v4.widget.CompoundButtonCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.transactions.TransactionsActivity;

import java.util.ArrayList;

class ReportsRowAdapter extends BaseAdapter {
    private ArrayList<ReportItem> elements = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater; // = LayoutInflater.from(this.mContext); TODO Fix nullPointer exception which arises here!! UPDATE -> Moved the layout inflator to the constructor. seemed to fix the null pointer exception??

    ReportsRowAdapter(Context aContext) {
        this.mContext = aContext;
        mInflater = (LayoutInflater.from(mContext));
    }

    public ArrayList<ReportItem> getElements() {
        return this.elements;
    }

    public void setElements(ArrayList<ReportItem> aList) {
        if (this.elements != aList) {
            this.elements.clear();
            this.elements = aList;
        }
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
        ReportItem report = this.elements.get(position);
        ReportsRowHolder holder = new ReportsRowHolder();
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.reports_row, null);
            holder.theRow = convertView.findViewById(R.id.therow);
            holder.theRow.setOnClickListener(getBtnClickListener());
            holder.checked = convertView.findViewById(R.id.checked);
            holder.checked.setOnCheckedChangeListener(getCheckListener());
            holder.expense = convertView.findViewById(R.id.expensetextview);
            holder.amount = convertView.findViewById(R.id.amounttextview);
            holder.checked.setButtonDrawable(Resources.getSystem().getIdentifier("btn_check_material_anim", "drawable", "android"));
            int[][] states = new int[][]{
                    /*new int[] {-android.R.attr.state_enabled},*/ // disabled
                    new int[]{-android.R.attr.state_checked}, // unchecked
                    /*new int[] { android.R.attr.state_pressed},*/  // pressed
                    new int[]{android.R.attr.state_enabled} // enabled
            };
            int[] colors = new int[]{
                    PocketMoneyThemes.chkBoxColorUnchecked(),
                    PocketMoneyThemes.chkBoxColorChecked()
            };
            ColorStateList colorStateList = new ColorStateList(states, colors);
            CompoundButtonCompat.setButtonTintList(holder.checked, colorStateList);
            convertView.setTag(holder);
        } else {
            holder = (ReportsRowHolder) convertView.getTag();
        }
        if (position % 2 == 0) {
            convertView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        } else {
            convertView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
        }
        holder.setReport(report);
        return convertView;
    }

    private OnCheckedChangeListener getCheckListener() {
        return new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((ReportsRowHolder) ((View) buttonView.getParent()).getTag()).report.checked = isChecked;
                ((ReportsActivity) ReportsRowAdapter.this.mContext).reloadData();
            }
        };
    }

    private OnClickListener getBtnClickListener() {
        return new OnClickListener() {
            public void onClick(View view) {
                ReportsRowHolder holder = (ReportsRowHolder) view.getTag();
                Intent i = new Intent(ReportsRowAdapter.this.mContext, TransactionsActivity.class);
                i.putExtra("Filter", ((ReportsActivity) ReportsRowAdapter.this.mContext).getFilterForReport(holder.report));
                ReportsRowAdapter.this.mContext.startActivity(i);
            }
        };
    }
}
