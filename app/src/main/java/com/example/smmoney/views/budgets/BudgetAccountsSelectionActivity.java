package com.example.smmoney.views.budgets;

import android.annotation.SuppressLint;
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
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.views.CheckBoxTint;
import com.example.smmoney.views.PocketMoneyActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class BudgetAccountsSelectionActivity extends PocketMoneyActivity {

    private final List<AccountClass> allAccounts = new ArrayList<>();
    private final List<AccountClass> filteredAccounts = new ArrayList<>();
    private SelectionAdapter adapter;
    private TextView countSummaryTextView;
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_selection_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.BUDGET_ACCOUNTS_TITLE);
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
                filterAccounts();
                return true;
            }
        });

        loadAccounts();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void loadAccounts() {
        allAccounts.clear();
        allAccounts.addAll(AccountDB.queryOnViewType(Enums.kViewAccountsAll));
        filterAccounts();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterAccounts() {
        filteredAccounts.clear();
        for (AccountClass acc : allAccounts) {
            if (currentQuery.isEmpty() || acc.getAccount().toLowerCase().contains(currentQuery)) {
                filteredAccounts.add(acc);
            }
        }
        updateSummary();
        adapter.notifyDataSetChanged();
    }

    private void updateSummary() {
        int includedCount = 0;
        for (AccountClass acc : allAccounts) {
            if (acc.getIncludeInBudgets()) {
                includedCount++;
            }
        }
        countSummaryTextView.setText(getString(R.string.ITEMS_INCLUDED_COUNT, includedCount, allAccounts.size()));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAllIncluded(boolean included) {
        Database.execSQL("UPDATE accounts SET includeInBudgets=" + (included ? 1 : 0) + " WHERE deleted=0", null);
        for (AccountClass acc : allAccounts) {
            acc.setIncludeInBudgets(included);
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
            AccountClass acc = filteredAccounts.get(position);
            holder.title.setText(acc.getAccount());
            holder.title.setTextColor(PocketMoneyThemes.primaryCellTextColor());

            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(acc.typeAsString());
            holder.subtitle.setTextColor(PocketMoneyThemes.fieldLabelColor());

            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageResource(acc.getIconFileNameResourceIDUsingContext(BudgetAccountsSelectionActivity.this));

            holder.checkBox.setChecked(acc.getIncludeInBudgets());
            CheckBoxTint.colorCheckBox(holder.checkBox);

            holder.itemView.setOnClickListener(v -> {
                boolean newState = !acc.getIncludeInBudgets();
                acc.setIncludeInBudgets(newState);
                acc.saveToDatabase();
                holder.checkBox.setChecked(newState);
                updateSummary();
            });
        }

        @Override
        public int getItemCount() {
            return filteredAccounts.size();
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
