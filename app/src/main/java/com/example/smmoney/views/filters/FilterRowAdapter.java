package com.example.smmoney.views.filters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.FilterClass;

import java.util.ArrayList;
import java.util.Objects;

class FilterRowAdapter extends BaseAdapter {
    private FiltersMainActivity delegate;
    private FilterClass filter;
    private ArrayList<FilterClass> filterList = new ArrayList<>();
    private LayoutInflater inflater;
    private ListView theList;

    FilterRowAdapter(FiltersMainActivity theDelegate, FilterClass aFilter) {
        this.filter = aFilter;
        this.delegate = theDelegate;
        this.inflater = LayoutInflater.from(theDelegate);
        reloadData();
    }

    public void reloadData() {
        this.filterList.clear();
        ArrayList arrayList = new ArrayList();
        ArrayList<FilterClass> fList = FilterClass.query();
        ArrayList<String> nameList = new ArrayList<>();
        for (FilterClass filter : fList) {
            if (filter.getFilterName().length() > 0) {
                nameList.add(filter.getFilterName());
                this.filterList.add(filter);
            }
        }
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.filterList.size();
    }

    public Object getItem(int position) {
        return this.filterList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup arg2) {
        FilterRowHolder holder;
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.filter_row, null);
            holder = new FilterRowHolder();
            holder.title = convertView.findViewById(R.id.filterfiltername);
            holder.theRow = (FrameLayout) holder.title.getParent();
            holder.theRow.setOnClickListener(getClickListener());
            this.delegate.registerForContextMenu(holder.theRow);
            holder.editImage = convertView.findViewById(R.id.filterroweditimage);
            holder.editImage.setOnClickListener(getEditClickListener());
            convertView.setTag(holder);
        } else {
            holder = (FilterRowHolder) convertView.getTag();
        }
        holder.setFilter(this.filterList.get(position));
        holder.title.setText(holder.filter.getFilterName());
        if (position % 2 == 0) {
            convertView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        } else {
            convertView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
        }
        return convertView;
    }

    private OnClickListener getClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                FilterRowHolder vw = (FilterRowHolder) v.getTag();
                if (Objects.equals(vw.filter.getAccount(), Locales.kLOC_FILTERS_CURRENT_ACCOUNT)) {
                    vw.filter.setAccount(FilterRowAdapter.this.filter.getAccount());
                }
                FilterRowAdapter.this.delegate.filterSelected(vw.filter);
            }
        };
    }

    private OnClickListener getEditClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                FilterRowHolder vw = (FilterRowHolder) ((View) v.getParent()).getTag();
                Intent intent = new Intent(FilterRowAdapter.this.delegate, FilterEditActivity.class);
                intent.putExtra("Filter", vw.filter);
                FilterRowAdapter.this.delegate.startActivity(intent);
            }
        };
    }
}
