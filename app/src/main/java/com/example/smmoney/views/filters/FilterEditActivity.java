package com.example.smmoney.views.filters;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.views.FromToDateActivity;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Objects;

public class FilterEditActivity extends PocketMoneyActivity {
    private static final int MENU_APPLY = 1;
    private static final int MENU_SAVED_FILTERS = 2;
    private static final int MAX_EDIT_CHIPS = 20;

    private final ActivityResultLauncher<Intent> customDateLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String date = result.getData().getStringExtra("FromDate");
            GregorianCalendar gregorianCalendar = null;
            if (date != null && !date.equals("*")) {
                gregorianCalendar = CalExt.dateFromDescriptionWithMediumDate(date);
            }
            this.filter.setDateFrom(gregorianCalendar);
            date = result.getData().getStringExtra("ToDate");
            gregorianCalendar = null;
            if (date != null && !date.equals("*")) {
                gregorianCalendar = CalExt.dateFromDescriptionWithMediumDate(date);
            }
            this.filter.setDateTo(gregorianCalendar);
            this.datesTextView.setText(this.filter.customDateString());
        }
    });

    private final ActivityResultLauncher<Intent> lookupLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            int type = result.getResultCode();
            switch (type) {
                case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                    this.transactionTypeTextView.setText(selection);
                    break;
                case LookupsListActivity.FILTER_ACCOUNTS /*9*/:
                    this.currentAccountsSelection = (selection == null) ? "" : selection;
                    renderChipsForSection(this.accountsChipGroup, this.currentAccountsSelection, LookupsListActivity.FILTER_ACCOUNTS);
                    break;
                case LookupsListActivity.FILTER_DATES /*10*/:
                    if (selection != null && selection.contains(SMMoney.getAppContext().getString(R.string.FILTER_DATES_CUSTOM))) {
                        Intent i = new Intent(this, FromToDateActivity.class);
                        i.putExtra("FromDate", this.filter.getDateFrom() != null ? CalExt.descriptionWithMediumDate(this.filter.getDateFrom()) : "*");
                        i.putExtra("ToDate", this.filter.getDateTo() != null ? CalExt.descriptionWithMediumDate(this.filter.getDateTo()) : "*");
                        customDateLauncher.launch(i);
                    }
                    this.datesTextView.setText(selection);
                    this.filter.setDate(selection);
                    break;
                case LookupsListActivity.FILTER_PAYEES /*11*/:
                    this.currentPayeesSelection = (selection == null) ? "" : selection;
                    renderChipsForSection(this.payeesChipGroup, this.currentPayeesSelection, LookupsListActivity.FILTER_PAYEES);
                    break;
                case LookupsListActivity.FILTER_IDS /*12*/:
                    this.currentIdsSelection = (selection == null) ? "" : selection;
                    renderChipsForSection(this.idsChipGroup, this.currentIdsSelection, LookupsListActivity.FILTER_IDS);
                    break;
                case LookupsListActivity.FILTER_CLEARED /*13*/:
                    this.clearedTextView.setText(selection);
                    break;
                case LookupsListActivity.FILTER_CATEGORIES /*14*/:
                    this.currentCategoriesSelection = (selection == null) ? "" : selection;
                    renderChipsForSection(this.categoriesChipGroup, this.currentCategoriesSelection, LookupsListActivity.FILTER_CATEGORIES);
                    break;
                case LookupsListActivity.FILTER_CLASSES /*15*/:
                    this.currentClassesSelection = (selection == null) ? "" : selection;
                    renderChipsForSection(this.classesChipGroup, this.currentClassesSelection, LookupsListActivity.FILTER_CLASSES);
                    break;
            }
        }
    });

    private ChipGroup accountsChipGroup;
    private ChipGroup categoriesChipGroup;
    private ChipGroup classesChipGroup;
    private TextView clearedTextView;
    private FilterEditActivity currentActivity;
    private TextView datesTextView;
    private FilterClass filter;
    private EditText filterNameEditText;
    private ChipGroup idsChipGroup;
    private FrameLayout keyboardToolbar;
    private ChipGroup payeesChipGroup;
    private TextView transactionTypeTextView;

    private String currentAccountsSelection = "";
    private String currentPayeesSelection = "";
    private String currentIdsSelection = "";
    private String currentCategoriesSelection = "";
    private String currentClassesSelection = "";
    private FilterClass initialSnapshot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.filter = androidx.core.os.BundleCompat.getSerializable(extras, "Filter", FilterClass.class);
        }
        if (this.filter == null) {
            this.filter = new FilterClass();
        }
        this.filter.hydrate();
        this.initialSnapshot = this.filter.copy();

        this.currentActivity = this;
        setContentView(R.layout.filter_edit);
        loadInfo();
        setupButtons();

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasGenuineChanges()) {
                    new androidx.appcompat.app.AlertDialog.Builder(FilterEditActivity.this, PocketMoneyThemes.dialogTheme())
                            .setTitle(R.string.dialog_discard_changes_title)
                            .setMessage(R.string.dialog_discard_changes_message)
                            .setPositiveButton(R.string.dialog_discard_changes_positive, (dialog, which) -> finish())
                            .setNegativeButton(R.string.GENERAL_CANCEL, null)
                            .show();
                } else {
                    finish();
                }
            }
        });

        if (getSupportActionBar() != null) {
            String title = (this.filter.filterID == 0) ? SMMoney.getAppContext().getString(R.string.FILTER_NEW) : SMMoney.getAppContext().getString(R.string.TOOLS_FILTER_EDIT);
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem presetsItem = menu.add(0, MENU_SAVED_FILTERS, 0, R.string.FILTERS_APPLY_SAVED_FILTER);
        presetsItem.setIcon(R.drawable.ic_bookmarks_white_24dp);
        presetsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        MenuItem applyItem = menu.add(0, MENU_APPLY, 0, SMMoney.getAppContext().getString(R.string.GENERAL_APPLY));
        applyItem.setIcon(R.drawable.ic_check_white_24dp);
        applyItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == MENU_APPLY) {
            applyWithoutSaving();
            finish();
            return true;
        } else if (id == MENU_SAVED_FILTERS) {
            showSavedFiltersBottomSheet();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupButtons() {
        View v = findViewById(R.id.payees_row);
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(11);
        v = findViewById(R.id.ids_row);
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(12);
        v = findViewById(R.id.transaction_type_row);
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(8);
        v = findViewById(R.id.accounts_row);
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(9);
        v = findViewById(R.id.dates_row);
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(10);
        v = findViewById(R.id.cleared_row);
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(13);
        v = findViewById(R.id.categories_row);
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(14);
        v = findViewById(R.id.classes_row);
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(15);

        this.keyboardToolbar.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        ScrollView sv = findViewById(R.id.scroll_view);
        sv.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        findViewById(R.id.filter_edit_root).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());

        int fieldLabelColor = PocketMoneyThemes.fieldLabelColor();

        int[] dividerIds = {R.id.divider1, R.id.divider2, R.id.divider3, R.id.divider4, R.id.divider5, R.id.divider6, R.id.divider7, R.id.divider8, R.id.divider9};
        for (int id : dividerIds) {
            View divider = findViewById(id);
            if (divider != null) {
                divider.setBackgroundColor(fieldLabelColor);
                divider.setAlpha(0.3f);
            }
        }

        TextView label;
        label = findViewById(R.id.filter_name_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.filter_transaction_type_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.filter_accounts_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.filter_dates_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.filter_payees_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.filter_ids_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.filter_cleared_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.filter_category_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.filter_classes_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());

        int primaryHintTextColor = PocketMoneyThemes.primaryHintTextColor();
        this.filterNameEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.filterNameEditText.setHintTextColor(primaryHintTextColor);
        this.transactionTypeTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.datesTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.clearedTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());

        ((android.widget.ImageView) findViewById(R.id.transtype_arrow)).setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);
        ((android.widget.ImageView) findViewById(R.id.accounts_arrow)).setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);
        ((android.widget.ImageView) findViewById(R.id.dates_arrow)).setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);
        ((android.widget.ImageView) findViewById(R.id.payees_arrow)).setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);
        ((android.widget.ImageView) findViewById(R.id.ids_arrow)).setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);
        ((android.widget.ImageView) findViewById(R.id.cleared_arrow)).setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);
        ((android.widget.ImageView) findViewById(R.id.categories_arrow)).setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);
        ((android.widget.ImageView) findViewById(R.id.classes_arrow)).setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void loadInfo() {
        this.filterNameEditText = findViewById(R.id.filtereditname);
        this.transactionTypeTextView = findViewById(R.id.filteredittranstype);
        this.accountsChipGroup = findViewById(R.id.filtereditaccounts_chips);
        this.datesTextView = findViewById(R.id.filtereditdates);
        this.payeesChipGroup = findViewById(R.id.filtereditpayees_chips);
        this.idsChipGroup = findViewById(R.id.filtereditids_chips);
        this.clearedTextView = findViewById(R.id.filtereditcleared);
        this.categoriesChipGroup = findViewById(R.id.filtereditcategories_chips);
        this.classesChipGroup = findViewById(R.id.filtereditclasses_chips);
        this.keyboardToolbar = findViewById(R.id.keyboard_toolbar);

        this.filterNameEditText.setText(this.filter.getFilterName());
        this.transactionTypeTextView.setText(this.filter.typeAsString());
        this.datesTextView.setText(this.filter.isCustomDate() ? this.filter.customDateString() : this.filter.getDate());
        this.clearedTextView.setText(this.filter.clearedAsString());

        this.currentAccountsSelection = (this.filter.getAccount() == null || this.filter.getAccount().isEmpty())
                ? SMMoney.getAppContext().getString(R.string.FILTERS_ALL_ACCOUNTS) : this.filter.getAccount();
        renderChipsForSection(this.accountsChipGroup, this.currentAccountsSelection, LookupsListActivity.FILTER_ACCOUNTS);

        this.currentPayeesSelection = (this.filter.getPayee() == null || this.filter.getPayee().isEmpty())
                ? SMMoney.getAppContext().getString(R.string.FILTERS_ALL_PAYEES) : this.filter.getPayee();
        renderChipsForSection(this.payeesChipGroup, this.currentPayeesSelection, LookupsListActivity.FILTER_PAYEES);

        this.currentIdsSelection = (this.filter.getCheckNumber() == null || this.filter.getCheckNumber().isEmpty())
                ? SMMoney.getAppContext().getString(R.string.FILTERS_ALL_IDS) : this.filter.getCheckNumber();
        renderChipsForSection(this.idsChipGroup, this.currentIdsSelection, LookupsListActivity.FILTER_IDS);

        this.currentCategoriesSelection = (this.filter.getCategory() == null || this.filter.getCategory().isEmpty())
                ? SMMoney.getAppContext().getString(R.string.FILTERS_ALL_CATEGORIES) : this.filter.getCategory();
        renderChipsForSection(this.categoriesChipGroup, this.currentCategoriesSelection, LookupsListActivity.FILTER_CATEGORIES);

        this.currentClassesSelection = (this.filter.getClassName() == null || this.filter.getClassName().isEmpty())
                ? SMMoney.getAppContext().getString(R.string.FILTERS_ALL_CLASSES) : this.filter.getClassName();
        renderChipsForSection(this.classesChipGroup, this.currentClassesSelection, LookupsListActivity.FILTER_CLASSES);
    }

    private void renderChipsForSection(ChipGroup chipGroup, String currentSelection, int sectionType) {
        chipGroup.removeAllViews();
        String allLabel = getAllLabelForType(sectionType);

        if (currentSelection == null || currentSelection.trim().isEmpty() || currentSelection.equals(allLabel)) {
            // Render a single chip showing "All ..."
            Chip allChip = new Chip(this);
            allChip.setText(allLabel);
            allChip.setClickable(true);
            allChip.setFocusable(true);
            allChip.setChipBackgroundColor(ColorStateList.valueOf(PocketMoneyThemes.chipBackgroundColor()));
            allChip.setTextColor(PocketMoneyThemes.chipTextColor());
            allChip.setChipStrokeColor(ColorStateList.valueOf(PocketMoneyThemes.chipStrokeColor()));
            allChip.setChipStrokeWidth(1.5f);
            allChip.setOnClickListener(v -> launchLookupForSection(sectionType));
            chipGroup.addView(allChip);
            return;
        }

        ArrayList<String> items = FilterClass.parseItems(currentSelection);
        int count = Math.min(items.size(), MAX_EDIT_CHIPS);
        for (int i = 0; i < count; i++) {
            String itemText = items.get(i);
            Chip chip = new Chip(this);
            chip.setText(itemText);
            chip.setCloseIconVisible(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(PocketMoneyThemes.chipBackgroundColor()));
            chip.setTextColor(PocketMoneyThemes.chipTextColor());
            chip.setChipStrokeColor(ColorStateList.valueOf(PocketMoneyThemes.chipStrokeColor()));
            chip.setChipStrokeWidth(1.5f);
            chip.setCloseIconTint(ColorStateList.valueOf(PocketMoneyThemes.chipStrokeColor()));

            chip.setOnCloseIconClickListener(v -> removeSelectionItem(sectionType, itemText));
            chip.setOnClickListener(v -> launchLookupForSection(sectionType));
            chipGroup.addView(chip);
        }

        if (items.size() > MAX_EDIT_CHIPS) {
            int overflow = items.size() - MAX_EDIT_CHIPS;
            Chip overflowChip = new Chip(this);
            overflowChip.setText(getResources().getQuantityString(R.plurals.filters_overflow_more, overflow, overflow));
            overflowChip.setChipBackgroundColor(ColorStateList.valueOf(PocketMoneyThemes.currentTintColor()));
            overflowChip.setTextColor(Color.WHITE);
            overflowChip.setOnClickListener(v -> launchLookupForSection(sectionType));
            chipGroup.addView(overflowChip);
        }
    }

    private void removeSelectionItem(int sectionType, String itemToRemove) {
        String current = getSelectionForType(sectionType);
        ArrayList<String> items = FilterClass.parseItems(current);
        items.remove(itemToRemove);

        String newSelection;
        if (items.isEmpty()) {
            newSelection = getAllLabelForType(sectionType);
        } else {
            newSelection = String.join(", ", items);
        }

        setSelectionForType(sectionType, newSelection);
        ChipGroup group = getChipGroupForType(sectionType);
        if (group != null) {
            renderChipsForSection(group, newSelection, sectionType);
        }
    }

    private String getAllLabelForType(int sectionType) {
        return switch (sectionType) {
            case LookupsListActivity.FILTER_ACCOUNTS -> SMMoney.getAppContext().getString(R.string.FILTERS_ALL_ACCOUNTS);
            case LookupsListActivity.FILTER_PAYEES -> SMMoney.getAppContext().getString(R.string.FILTERS_ALL_PAYEES);
            case LookupsListActivity.FILTER_IDS -> SMMoney.getAppContext().getString(R.string.FILTERS_ALL_IDS);
            case LookupsListActivity.FILTER_CATEGORIES -> SMMoney.getAppContext().getString(R.string.FILTERS_ALL_CATEGORIES);
            case LookupsListActivity.FILTER_CLASSES -> SMMoney.getAppContext().getString(R.string.FILTERS_ALL_CLASSES);
            default -> "";
        };
    }

    private String getSelectionForType(int sectionType) {
        return switch (sectionType) {
            case LookupsListActivity.FILTER_ACCOUNTS -> this.currentAccountsSelection;
            case LookupsListActivity.FILTER_PAYEES -> this.currentPayeesSelection;
            case LookupsListActivity.FILTER_IDS -> this.currentIdsSelection;
            case LookupsListActivity.FILTER_CATEGORIES -> this.currentCategoriesSelection;
            case LookupsListActivity.FILTER_CLASSES -> this.currentClassesSelection;
            default -> "";
        };
    }

    private void setSelectionForType(int sectionType, String selection) {
        switch (sectionType) {
            case LookupsListActivity.FILTER_ACCOUNTS -> this.currentAccountsSelection = selection;
            case LookupsListActivity.FILTER_PAYEES -> this.currentPayeesSelection = selection;
            case LookupsListActivity.FILTER_IDS -> this.currentIdsSelection = selection;
            case LookupsListActivity.FILTER_CATEGORIES -> this.currentCategoriesSelection = selection;
            case LookupsListActivity.FILTER_CLASSES -> this.currentClassesSelection = selection;
        }
    }

    private ChipGroup getChipGroupForType(int sectionType) {
        return switch (sectionType) {
            case LookupsListActivity.FILTER_ACCOUNTS -> this.accountsChipGroup;
            case LookupsListActivity.FILTER_PAYEES -> this.payeesChipGroup;
            case LookupsListActivity.FILTER_IDS -> this.idsChipGroup;
            case LookupsListActivity.FILTER_CATEGORIES -> this.categoriesChipGroup;
            case LookupsListActivity.FILTER_CLASSES -> this.classesChipGroup;
            default -> null;
        };
    }

    private void launchLookupForSection(int sectionType) {
        Intent i = new Intent(this, LookupsListActivity.class);
        i.putExtra("type", sectionType);
        i.putExtra("isMultiSelect", true);
        i.putExtra("currentSelection", getSelectionForType(sectionType));
        lookupLauncher.launch(i);
    }

    private void saveInfo() {
        this.filter.setFilterName(this.filterNameEditText.getText().toString());
        this.filter.setTypeFromString(this.transactionTypeTextView.getText().toString());
        this.filter.setClearedFromString(this.clearedTextView.getText().toString());

        this.filter.setAccount(this.currentAccountsSelection.equals(SMMoney.getAppContext().getString(R.string.FILTERS_ALL_ACCOUNTS)) ? "" : this.currentAccountsSelection);
        this.filter.setPayee(this.currentPayeesSelection.equals(SMMoney.getAppContext().getString(R.string.FILTERS_ALL_PAYEES)) ? "" : this.currentPayeesSelection);
        this.filter.setCheckNumber(this.currentIdsSelection.equals(SMMoney.getAppContext().getString(R.string.FILTERS_ALL_IDS)) ? "" : this.currentIdsSelection);
        this.filter.setCategory(this.currentCategoriesSelection.equals(SMMoney.getAppContext().getString(R.string.FILTERS_ALL_CATEGORIES)) ? "" : this.currentCategoriesSelection);
        this.filter.setClassName(this.currentClassesSelection.equals(SMMoney.getAppContext().getString(R.string.FILTERS_ALL_CLASSES)) ? "" : this.currentClassesSelection);
    }

    private boolean hasGenuineChanges() {
        if (this.initialSnapshot == null || this.filter == null) {
            return false;
        }

        FilterClass current = this.filter.copy();
        current.setFilterName(this.filterNameEditText != null ? this.filterNameEditText.getText().toString() : "");
        if (this.transactionTypeTextView != null) {
            current.setTypeFromString(this.transactionTypeTextView.getText().toString());
        }
        if (this.clearedTextView != null) {
            current.setClearedFromString(this.clearedTextView.getText().toString());
        }
        current.setAccount(Objects.equals(this.currentAccountsSelection, SMMoney.getAppContext().getString(R.string.FILTERS_ALL_ACCOUNTS)) ? "" : this.currentAccountsSelection);
        current.setPayee(Objects.equals(this.currentPayeesSelection, SMMoney.getAppContext().getString(R.string.FILTERS_ALL_PAYEES)) ? "" : this.currentPayeesSelection);
        current.setCheckNumber(Objects.equals(this.currentIdsSelection, SMMoney.getAppContext().getString(R.string.FILTERS_ALL_IDS)) ? "" : this.currentIdsSelection);
        current.setCategory(Objects.equals(this.currentCategoriesSelection, SMMoney.getAppContext().getString(R.string.FILTERS_ALL_CATEGORIES)) ? "" : this.currentCategoriesSelection);
        current.setClassName(Objects.equals(this.currentClassesSelection, SMMoney.getAppContext().getString(R.string.FILTERS_ALL_CLASSES)) ? "" : this.currentClassesSelection);

        if (!Objects.equals(this.initialSnapshot.getFilterName(), current.getFilterName())) return true;
        if (this.initialSnapshot.getType() != current.getType()) return true;
        if (this.initialSnapshot.getCleared() != current.getCleared()) return true;
        if (!Objects.equals(this.initialSnapshot.getDate(), current.getDate())) return true;
        if (!Objects.equals(this.initialSnapshot.getAccount(), current.getAccount())) return true;
        if (!Objects.equals(this.initialSnapshot.getPayee(), current.getPayee())) return true;
        if (!Objects.equals(this.initialSnapshot.getCheckNumber(), current.getCheckNumber())) return true;
        if (!Objects.equals(this.initialSnapshot.getCategory(), current.getCategory())) return true;
        if (!Objects.equals(this.initialSnapshot.getClassName(), current.getClassName())) return true;

        if (this.initialSnapshot.isCustomDate()) {
            if (this.initialSnapshot.getDateFrom() != null && current.getDateFrom() != null &&
                    CalExt.daysBetween(this.initialSnapshot.getDateFrom(), current.getDateFrom()) != 0) {
                return true;
            }
            if ((this.initialSnapshot.getDateFrom() == null) != (current.getDateFrom() == null)) {
                return true;
            }
            if (this.initialSnapshot.getDateTo() != null && current.getDateTo() != null &&
                    CalExt.daysBetween(this.initialSnapshot.getDateTo(), current.getDateTo()) != 0) {
                return true;
            }
            return (this.initialSnapshot.getDateTo() == null) != (current.getDateTo() == null);
        }

        return false;
    }

    private void applyWithoutSaving() {
        saveInfo();
        this.filter.setCustomFilter(true);
        Intent i = new Intent();
        i.putExtra("Filter", this.filter);
        setResult(1, i);
    }

    // Suppressed: sheetView is a dialog root view passed directly to setContentView(), which provides the layout params
    @android.annotation.SuppressLint("InflateParams")
    private void showSavedFiltersBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog sheetDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_saved_filters_sheet, null);
        sheetDialog.setContentView(sheetView);

        // Apply theme-appropriate card background with rounded top corners
        android.graphics.drawable.GradientDrawable bgDrawable = new android.graphics.drawable.GradientDrawable();
        bgDrawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bgDrawable.setCornerRadii(new float[]{32, 32, 32, 32, 0, 0, 0, 0});
        bgDrawable.setColor(PocketMoneyThemes.cardBackgroundColor());
        sheetView.setBackground(bgDrawable);

        sheetDialog.setOnShowListener(dialog -> {
            com.google.android.material.bottomsheet.BottomSheetDialog d = (com.google.android.material.bottomsheet.BottomSheetDialog) dialog;
            android.widget.FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
                com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior =
                        com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int maxAllowedHeight = (int) (screenHeight * 0.60); // 60% of screen height
                behavior.setMaxHeight(maxAllowedHeight);
                behavior.setPeekHeight(maxAllowedHeight);
                behavior.setFitToContents(true);
                behavior.setSkipCollapsed(true);
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        TextView title = sheetView.findViewById(R.id.sheet_title);
        title.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        TextView empty = sheetView.findViewById(R.id.sheet_empty);
        empty.setTextColor(PocketMoneyThemes.fieldLabelColor());

        // "All Transactions" option
        View allTransRow = sheetView.findViewById(R.id.sheet_all_transactions_row);
        TextView allTransTitle = sheetView.findViewById(R.id.sheet_all_trans_title);
        TextView allTransSubtitle = sheetView.findViewById(R.id.sheet_all_trans_subtitle);
        allTransTitle.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        allTransSubtitle.setTextColor(PocketMoneyThemes.cardTextSecondaryColor());

        allTransRow.setOnClickListener(v -> {
            sheetDialog.dismiss();
            FilterClass allFilter = new FilterClass();
            allFilter.setFilterName(SMMoney.getAppContext().getString(R.string.ALL_TRANSACTIONS));
            allFilter.setAccount(SMMoney.getAppContext().getString(R.string.FILTERS_ALL_ACCOUNTS));
            allFilter.setCustomFilter(true);
            Intent i = new Intent();
            i.putExtra("Filter", allFilter);
            setResult(1, i);
            finish();
        });

        androidx.recyclerview.widget.RecyclerView rv = sheetView.findViewById(R.id.sheet_filter_list);
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // Query saved filters and exclude any empty/invalid filter names or default all accounts
        ArrayList<FilterClass> rawFilters = FilterClass.query();
        ArrayList<FilterClass> savedFilters = new ArrayList<>();
        for (FilterClass f : rawFilters) {
            f.hydrate();
            String name = f.getFilterName();
            if (name != null && !name.trim().isEmpty() && !name.equals(SMMoney.getAppContext().getString(R.string.ALL_TRANSACTIONS))) {
                savedFilters.add(f);
            }
        }

        if (savedFilters.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            rv.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter<SavedFilterSheetViewHolder>() {
                @androidx.annotation.NonNull
                @Override
                public SavedFilterSheetViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
                    View item = getLayoutInflater().inflate(R.layout.item_saved_filter_sheet_row, parent, false);
                    return new SavedFilterSheetViewHolder(item);
                }

                @Override
                public void onBindViewHolder(@androidx.annotation.NonNull SavedFilterSheetViewHolder holder, int position) {
                    FilterClass item = savedFilters.get(position);
                    holder.name.setText(item.getFilterName());
                    holder.name.setTextColor(PocketMoneyThemes.primaryCellTextColor());
                    holder.summary.setText(item.generateSummaryString());
                    holder.summary.setTextColor(PocketMoneyThemes.cardTextSecondaryColor());

                    holder.itemView.setOnClickListener(v -> {
                        sheetDialog.dismiss();
                        FilterClass selected = item.copy();
                        selected.setCustomFilter(true);
                        Intent i = new Intent();
                        i.putExtra("Filter", selected);
                        setResult(1, i);
                        finish();
                    });
                }

                @Override
                public int getItemCount() {
                    return savedFilters.size();
                }
            });
        }

        sheetDialog.show();
    }

    private static class SavedFilterSheetViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final TextView name;
        final TextView summary;

        SavedFilterSheetViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.filter_sheet_row_name);
            this.summary = itemView.findViewById(R.id.filter_sheet_row_summary);
        }
    }

    private OnClickListener getLookupListClickListener() {
        return view -> {
            int tag = (Integer) view.getTag();
            if (tag == 10) { // Dates
                Intent i = new Intent(FilterEditActivity.this.currentActivity, LookupsListActivity.class);
                i.putExtra("type", tag);
                i.putExtra("FromDate", FilterEditActivity.this.filter.getDateFrom() != null ? CalExt.descriptionWithMediumDate(FilterEditActivity.this.filter.getDateFrom()) : "*");
                i.putExtra("ToDate", FilterEditActivity.this.filter.getDateTo() != null ? CalExt.descriptionWithMediumDate(FilterEditActivity.this.filter.getDateTo()) : "*");
                lookupLauncher.launch(i);
            } else if (tag == 8 || tag == 13) { // Transaction Type or Cleared
                Intent i = new Intent(FilterEditActivity.this.currentActivity, LookupsListActivity.class);
                i.putExtra("type", tag);
                lookupLauncher.launch(i);
            } else { // Multi-select ChipGroup sections
                launchLookupForSection(tag);
            }
        };
    }
}
