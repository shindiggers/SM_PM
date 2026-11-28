package com.example.smmoney.views.filters;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.FilterClass;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FilterRowAdapter extends RecyclerView.Adapter<FilterRowHolder> {
    private static final int MAX_QUICK_VIEW_CHIPS = 5;

    private final FiltersMainActivity delegate;
    private final FilterClass currentFilter;
    private final ArrayList<FilterClass> filterList = new ArrayList<>();
    private final LayoutInflater inflater;
    private final Set<Integer> expandedPositions = new HashSet<>();

    public FilterRowAdapter(FiltersMainActivity theDelegate, FilterClass aFilter) {
        this.currentFilter = aFilter;
        this.delegate = theDelegate;
        this.inflater = LayoutInflater.from(theDelegate);
        reloadData();
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void reloadData() {
        this.filterList.clear();
        ArrayList<FilterClass> fList = FilterClass.query();
        for (FilterClass f : fList) {
            if (!f.getFilterName().isEmpty()) {
                this.filterList.add(f);
            }
        }
        // Full refresh is appropriate as the entire list is replaced
        notifyDataSetChanged();
    }

    public List<FilterClass> getElements() {
        return this.filterList;
    }

    @Override
    public int getItemCount() {
        return this.filterList.size();
    }

    @NonNull
    @Override
    public FilterRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = this.inflater.inflate(R.layout.filter_row, parent, false);
        return new FilterRowHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterRowHolder holder, int position) {
        FilterClass filterItem = this.filterList.get(position);
        holder.applyTheme();
        holder.title.setText(filterItem.getFilterName());
        holder.subtitle.setText(filterItem.generateSummaryString());

        if (position % 2 == 0) {
            holder.itemView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        } else {
            holder.itemView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
        }

        boolean isExpanded = expandedPositions.contains(position);
        holder.quickViewCard.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.quickViewCard.setCardBackgroundColor(PocketMoneyThemes.cardBackgroundColor());

        if (isExpanded) {
            holder.editImage.setImageResource(R.drawable.ic_outline_arrow_drop_up_circle_24);
            populateQuickView(holder, filterItem);
        } else {
            holder.editImage.setImageResource(R.drawable.ic_outline_arrow_drop_down_circle_24);
        }

        holder.editImage.setColorFilter(PocketMoneyThemes.fieldLabelColor(), PorterDuff.Mode.SRC_IN);
        holder.editImage.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                if (expandedPositions.contains(pos)) {
                    expandedPositions.remove(pos);
                } else {
                    expandedPositions.add(pos);
                }
                notifyItemChanged(pos);
            }
        });

        // Tapping anywhere in the expanded card view opens FilterEditActivity
        holder.quickViewCard.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && pos < filterList.size()) {
                FilterClass clickedFilter = filterList.get(pos);
                Intent intent = new Intent(delegate, FilterEditActivity.class);
                intent.putExtra("Filter", clickedFilter);
                delegate.filterEditLauncher.launch(intent);
            }
        });

        holder.headerRow.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && pos < filterList.size()) {
                FilterClass clickedFilter = filterList.get(pos);
                if (currentFilter != null && Objects.equals(clickedFilter.getAccount(), SMMoney.getAppContext().getString(R.string.FILTERS_CURRENT_ACCOUNT))) {
                    clickedFilter.setAccount(currentFilter.getAccount());
                }
                delegate.filterSelected(clickedFilter);
            }
        });
    }

    private void populateQuickView(FilterRowHolder holder, FilterClass filterItem) {
        filterItem.hydrate();

        // 1. General Chips (Dates, Type, Cleared)
        holder.generalChips.removeAllViews();
        if (filterItem.getDate() != null && !filterItem.getDate().isEmpty() && !filterItem.getDate().equals(SMMoney.getAppContext().getString(R.string.FILTER_DATES_ALL))) {
            String dateLabel = filterItem.isCustomDate() ? filterItem.customDateString() : filterItem.getDate();
            addChip(holder.generalChips, dateLabel, filterItem, false);
        }
        if (filterItem.getType() != Enums.kTransactionTypeAll) {
            String typeLabel = switch (filterItem.getType()) {
                case Enums.kTransactionTypeWithdrawal -> SMMoney.getAppContext().getString(R.string.GENERAL_WITHDRAWAL);
                case Enums.kTransactionTypeDeposit -> SMMoney.getAppContext().getString(R.string.GENERAL_DEPOSIT);
                case Enums.kTransactionTypeTransferTo, Enums.kTransactionTypeTransferFrom -> SMMoney.getAppContext().getString(R.string.GENERAL_TRANSFER);
                default -> "";
            };
            if (!typeLabel.isEmpty()) {
                addChip(holder.generalChips, typeLabel, filterItem, false);
            }
        }
        if (filterItem.getCleared() != Enums.kClearedDoesntMatter) {
            String clearedLabel = (filterItem.getCleared() == Enums.kClearedCleared)
                    ? SMMoney.getAppContext().getString(R.string.GENERAL_CLEARED)
                    : SMMoney.getAppContext().getString(R.string.FILTER_UNCLEARED);
            addChip(holder.generalChips, clearedLabel, filterItem, false);
        }

        if (holder.generalChips.getChildCount() > 0) {
            holder.generalLabel.setVisibility(View.VISIBLE);
            holder.generalLabel.setTextColor(PocketMoneyThemes.cardTextSecondaryColor());
            holder.generalChips.setVisibility(View.VISIBLE);
        } else {
            holder.generalLabel.setVisibility(View.GONE);
            holder.generalChips.setVisibility(View.GONE);
        }

        // 2. Accounts
        populateSection(holder.accountsLabel, holder.accountsChips, filterItem.getAccount(),
                SMMoney.getAppContext().getString(R.string.FILTERS_ALL_ACCOUNTS),
                SMMoney.getAppContext().getString(R.string.FILTERS_CURRENT_ACCOUNT),
                SMMoney.getAppContext().getString(R.string.GENERAL_ACCOUNT),
                SMMoney.getAppContext().getString(R.string.GENERAL_ACCOUNTS), filterItem);

        // 3. Categories
        populateSection(holder.categoriesLabel, holder.categoriesChips, filterItem.getCategory(),
                SMMoney.getAppContext().getString(R.string.FILTERS_ALL_CATEGORIES), null,
                SMMoney.getAppContext().getString(R.string.GENERAL_CATEGORY),
                SMMoney.getAppContext().getString(R.string.GENERAL_CATEGORY_TITLE), filterItem);

        // 4. Payees
        populateSection(holder.payeesLabel, holder.payeesChips, filterItem.getPayee(),
                SMMoney.getAppContext().getString(R.string.FILTERS_ALL_PAYEES), null,
                SMMoney.getAppContext().getString(R.string.GENERAL_PAYEE),
                SMMoney.getAppContext().getString(R.string.GENERAL_PAYEE_TITLE), filterItem);

        // 5. Classes
        populateSection(holder.classesLabel, holder.classesChips, filterItem.getClassName(),
                SMMoney.getAppContext().getString(R.string.FILTERS_ALL_CLASSES), null,
                SMMoney.getAppContext().getString(R.string.GENERAL_CLASS),
                SMMoney.getAppContext().getString(R.string.GENERAL_CLASSES), filterItem);

        // 6. IDs
        populateSection(holder.idsLabel, holder.idsChips, filterItem.getCheckNumber(),
                SMMoney.getAppContext().getString(R.string.FILTERS_ALL_IDS), null,
                SMMoney.getAppContext().getString(R.string.GENERAL_ID),
                SMMoney.getAppContext().getString(R.string.GENERAL_ID_TITLE), filterItem);
    }

    private void populateSection(TextView label, ChipGroup chipGroup, String value, String allValue1, String allValue2,
                                String singularTitle, String pluralTitle, FilterClass filterItem) {
        chipGroup.removeAllViews();
        if (value == null || value.trim().isEmpty() || value.equals(allValue1) || Objects.equals(value, allValue2)) {
            label.setVisibility(View.GONE);
            chipGroup.setVisibility(View.GONE);
            return;
        }

        ArrayList<String> items = FilterClass.parseItems(value);
        if (items.isEmpty()) {
            label.setVisibility(View.GONE);
            chipGroup.setVisibility(View.GONE);
            return;
        }

        label.setText(items.size() == 1 ? singularTitle : pluralTitle);
        label.setVisibility(View.VISIBLE);
        label.setTextColor(PocketMoneyThemes.cardTextSecondaryColor());
        chipGroup.setVisibility(View.VISIBLE);

        int count = Math.min(items.size(), MAX_QUICK_VIEW_CHIPS);
        for (int i = 0; i < count; i++) {
            addChip(chipGroup, items.get(i), filterItem, false);
        }

        if (items.size() > MAX_QUICK_VIEW_CHIPS) {
            int overflow = items.size() - MAX_QUICK_VIEW_CHIPS;
            addChip(chipGroup, this.delegate.getResources().getQuantityString(R.plurals.filters_overflow_more, overflow, overflow), filterItem, true);
        }
    }

    private void addChip(ChipGroup chipGroup, String text, FilterClass filterItem, boolean isOverflow) {
        Chip chip = new Chip(this.delegate);
        chip.setText(text);
        chip.setTextSize(12f);
        chip.setClickable(true);
        chip.setFocusable(true);

        int tintColor = PocketMoneyThemes.currentTintColor();
        if (isOverflow) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(tintColor));
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(PocketMoneyThemes.chipBackgroundColor()));
            chip.setTextColor(PocketMoneyThemes.chipTextColor());
            chip.setChipStrokeColor(ColorStateList.valueOf(PocketMoneyThemes.chipStrokeColor()));
            chip.setChipStrokeWidth(1.5f);
        }

        chip.setOnClickListener(v -> {
            Intent intent = new Intent(delegate, FilterEditActivity.class);
            intent.putExtra("Filter", filterItem);
            delegate.filterEditLauncher.launch(intent);
        });

        chipGroup.addView(chip);
    }
}
