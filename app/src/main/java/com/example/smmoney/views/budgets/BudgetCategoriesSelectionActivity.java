package com.example.smmoney.views.budgets;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.views.CheckBoxTint;
import com.example.smmoney.views.PocketMoneyActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class BudgetCategoriesSelectionActivity extends PocketMoneyActivity {

    private final List<CategoryClass> allCategories = new ArrayList<>();
    private final List<CategoryClass> filteredCategories = new ArrayList<>();
    private SelectionAdapter adapter;
    private TextView countSummaryTextView;
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_selection_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.BUDGET_CATEGORIES_TITLE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.parent_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        countSummaryTextView = findViewById(R.id.tv_count_summary);
        countSummaryTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());

        MaterialButton btnSelectAll = findViewById(R.id.btn_select_all);
        MaterialButton btnDeselectAll = findViewById(R.id.btn_deselect_all);
        btnSelectAll.setOnClickListener(v -> setAllIncluded(true));
        btnDeselectAll.setOnClickListener(v -> setAllIncluded(false));

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectionAdapter();
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.search_view);
        android.graphics.drawable.GradientDrawable searchBg = new android.graphics.drawable.GradientDrawable();
        searchBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        searchBg.setCornerRadius(getResources().getDisplayMetrics().density * 22f);
        searchBg.setColor(PocketMoneyThemes.cardBackgroundColor());
        searchBg.setStroke((int) getResources().getDisplayMetrics().density, PocketMoneyThemes.cardStrokeColor());
        searchView.setBackground(searchBg);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText == null ? "" : newText.trim().toLowerCase();
                filterCategories();
                return true;
            }
        });

        loadCategories();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void loadCategories() {
        allCategories.clear();
        Cursor c = Database.rawQuery("SELECT categoryID FROM categories WHERE deleted=0 AND category != '' ORDER BY UPPER(category)", null);
        if (c != null) {
            while (c.moveToNext()) {
                allCategories.add(new CategoryClass(c.getInt(0)));
            }
            c.close();
        }
        filterCategories();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterCategories() {
        filteredCategories.clear();
        for (CategoryClass cat : allCategories) {
            if (currentQuery.isEmpty() || cat.getCategory().toLowerCase().contains(currentQuery)) {
                filteredCategories.add(cat);
            }
        }
        updateSummary();
        adapter.notifyDataSetChanged();
    }

    private void updateSummary() {
        int includedCount = 0;
        for (CategoryClass cat : allCategories) {
            if (cat.getIncludeInBudgets()) {
                includedCount++;
            }
        }
        countSummaryTextView.setText(getString(R.string.ITEMS_INCLUDED_COUNT, includedCount, allCategories.size()));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAllIncluded(boolean included) {
        Database.execSQL("UPDATE categories SET includeInBudgets=" + (included ? 1 : 0) + " WHERE deleted=0", null);
        for (CategoryClass cat : allCategories) {
            cat.setIncludeInBudgets(included);
        }
        updateSummary();
        adapter.notifyDataSetChanged();
    }

    private class SelectionAdapter extends RecyclerView.Adapter<SelectionAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_selection, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CategoryClass cat = filteredCategories.get(position);
            holder.title.setText(cat.getCategory());
            holder.title.setTextColor(PocketMoneyThemes.primaryCellTextColor());

            String typeStr = (cat.getType() == Enums.kTransactionTypeDeposit) ? getString(R.string.BUDGETS_INCOME) : getString(R.string.BUDGETS_EXPENSES);
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(typeStr);
            holder.subtitle.setTextColor(PocketMoneyThemes.fieldLabelColor());

            holder.icon.setVisibility(View.GONE);

            holder.checkBox.setChecked(cat.getIncludeInBudgets());
            CheckBoxTint.colorCheckBox(holder.checkBox);

            holder.itemView.setOnClickListener(v -> {
                boolean newState = !cat.getIncludeInBudgets();
                cat.setIncludeInBudgets(newState);
                cat.saveToDatabase();
                holder.checkBox.setChecked(newState);
                updateSummary();
            });
        }

        @Override
        public int getItemCount() {
            return filteredCategories.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title;
            final TextView subtitle;
            final CheckBox checkBox;

            ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.item_icon);
                title = itemView.findViewById(R.id.item_title);
                subtitle = itemView.findViewById(R.id.item_subtitle);
                checkBox = itemView.findViewById(R.id.item_checkbox);
            }
        }
    }
}
