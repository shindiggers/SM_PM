package com.example.smmoney.views.lookups;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LookupRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SINGLE = 0;
    private static final int TYPE_MULTI = 1;

    private List<String> items = new ArrayList<>();
    private final Set<Integer> checkedPositions = new HashSet<>();
    private final Context context;
    private final LayoutInflater inflater;
    private final boolean isMultiSelect;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public LookupRecyclerViewAdapter(Context context, boolean isMultiSelect) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.isMultiSelect = isMultiSelect;
    }

    public void setItems(List<String> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public List<String> getItems() {
        return items;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItemChecked(int position, boolean checked) {
        if (checked) {
            checkedPositions.add(position);
        } else {
            checkedPositions.remove(position);
        }
        notifyItemChanged(position);
    }

    public boolean isItemChecked(int position) {
        return checkedPositions.contains(position);
    }

    public Set<Integer> getCheckedPositions() {
        return checkedPositions;
    }

    @Override
    public int getItemViewType(int position) {
        return isMultiSelect ? TYPE_MULTI : TYPE_SINGLE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MULTI) {
            View v = inflater.inflate(R.layout.lookup_multi_row, parent, false);
            return new MultiViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.lookup_row, parent, false);
            return new SingleViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String item = items.get(position);
        if (holder instanceof MultiViewHolder) {
            ((MultiViewHolder) holder).bind(item, position);
        } else {
            ((SingleViewHolder) holder).bind(item, position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SingleViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        SingleViewHolder(View v) {
            super(v);
            textView = (TextView) v;
            textView.setOnClickListener(v1 -> {
                if (listener != null) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }

        void bind(String text, int position) {
            textView.setText(text);
            textView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
            if (position % 2 == 0) {
                itemView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
            } else {
                itemView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
            }
        }
    }

    class MultiViewHolder extends RecyclerView.ViewHolder {
        CheckedTextView checkedTextView;

        MultiViewHolder(View v) {
            super(v);
            checkedTextView = (CheckedTextView) v;
            checkedTextView.setOnClickListener(v1 -> {
                if (listener != null) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }

        void bind(String text, int position) {
            checkedTextView.setText(text);
            checkedTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
            checkedTextView.setCheckMarkTintList(ColorStateList.valueOf(PocketMoneyThemes.currentTintColor()));
            checkedTextView.setChecked(checkedPositions.contains(position));
            
            if (position % 2 == 0) {
                itemView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
            } else {
                itemView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
            }
        }
    }
}
