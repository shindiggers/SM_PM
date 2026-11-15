package com.example.smmoney.views.budgets;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.CategoryBudgetClass;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.CheckBoxTint;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.splits.SplitsActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

public class BudgetsEditActivity extends PocketMoneyActivity {
    private static final int MENU_SAVE = 1;

    private final ActivityResultLauncher<Intent> lookupLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != 0 && result.getData() != null) {
                    String selection = result.getData().getStringExtra("selection");
                    int type = result.getResultCode();
                    switch (type) {
                        case LookupsListActivity.CATEGORY_LOOKUP /*5*/ -> {
                            this.categoryEditText.setText(selection);
                            onCategorySelected(selection);
                        }
                        case LookupsListActivity.BUDGET_TYPE /*19*/ -> this.budgetTypeTextView.setText(selection);
                        case LookupsListActivity.BUDGET_PERIOD /*20*/ -> this.periodTextView.setText(selection);
                    }
                    getCells();
                }
            });

    // Models
    private CategoryClass category;
    private String oldCategory;
    private ArrayList<CategoryBudgetClass> categoryBudgetItems;
    private ArrayList<CategoryBudgetClass> deletedCategoryBudgetItems;

    // Card 1 Views
    private MaterialCardView cardGeneralSettings;
    private EditText categoryEditText;
    private TextView budgetTypeTextView;
    private TextView periodTextView;
    private CheckBox includeInBudgetsCheckBox;
    private CheckBox rolloverCheckBox;
    private CheckBox includeSubcategoriesCheckBox;

    // Card 2 Views
    private MaterialCardView cardBudgetSchedule;
    private LinearLayout scheduleIntervalsContainer;
    private TextView scheduleEmptyText;

    /**
     * UI Representation of an interval segment calculated from CategoryBudgetClass records.
     */
    public record ScheduleSegment(GregorianCalendar fromDate,
                                  @Nullable GregorianCalendar toDate,
                                  boolean isOngoing,
                                  double amount,
                                  boolean resetRollover,
                                  boolean isGap,
                                  @Nullable CategoryBudgetClass sourceRecord) {
    }

    /**
     * Encapsulates a simulated plan of schedule adjustments when adding/modifying an interval.
     */
    private static class AdjustmentPlan {
        final List<String> bulletPoints = new ArrayList<>();
        final List<CategoryBudgetClass> itemsToRemove = new ArrayList<>();
        final List<ItemShift> itemsToShift = new ArrayList<>();
        boolean requiresEndBoundary = false;
        GregorianCalendar endBoundaryDate = null;
        double endBoundaryRate = 0.0d;

        record ItemShift(CategoryBudgetClass item, GregorianCalendar newDate) {
        }

        boolean hasChanges() {
            return !bulletPoints.isEmpty();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.category = androidx.core.content.IntentCompat.getSerializableExtra(getIntent(), "Category", CategoryClass.class);

        if (this.category != null) {
            this.oldCategory = this.category.getCategory();
        } else {
            this.category = new CategoryClass();
            this.oldCategory = "";
        }

        setContentView(R.layout.budget_edit);
        this.categoryBudgetItems = CategoryBudgetClass.budgetItemsForCategory(this.category.getCategory());
        this.deletedCategoryBudgetItems = new ArrayList<>();

        // If category has no timeline items yet, create initial default from 1st of current month
        ensureInitialBudgetTimeline();

        setupViews();
        loadCells();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.BUDGETS_EDIT_TITLE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void ensureInitialBudgetTimeline() {
        if (this.categoryBudgetItems.isEmpty()) {
            CategoryBudgetClass initialItem = new CategoryBudgetClass();
            initialItem.setCategoryName(this.category.getCategory());

            // Default From Date: 1st of current month
            GregorianCalendar startOfMonth = new GregorianCalendar();
            startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
            startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
            startOfMonth.set(Calendar.MINUTE, 0);
            startOfMonth.set(Calendar.SECOND, 0);
            startOfMonth.set(Calendar.MILLISECOND, 0);
            initialItem.setDate(startOfMonth);

            double currentLimit = this.category.getBudgetLimit();
            initialItem.setBudgetLimit(currentLimit);
            initialItem.setResetRollover(false);

            this.categoryBudgetItems.add(initialItem);
        }
    }

    private void setupViews() {
        findViewById(R.id.parent_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());

        this.cardGeneralSettings = findViewById(R.id.card_general_settings);
        this.cardBudgetSchedule = findViewById(R.id.card_budget_schedule);
        applyCardThemes();

        int primaryRowSelector = PocketMoneyThemes.primaryRowSelector();

        // Category Picker
        View categoryRow = findViewById(R.id.categorybutton);
        categoryRow.setBackgroundResource(primaryRowSelector);
        categoryRow.setOnClickListener(v -> {
            getCells();
            Intent i = new Intent(BudgetsEditActivity.this, LookupsListActivity.class);
            i.putExtra("type", LookupsListActivity.CATEGORY_LOOKUP);
            lookupLauncher.launch(i);
        });
        this.categoryEditText = findViewById(R.id.categoryedittext);
        this.categoryEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.categoryEditText.setHintTextColor(PocketMoneyThemes.primaryHintTextColor());
        ((TextView) findViewById(R.id.category_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());

        // Budget Type
        View budgetTypeRow = findViewById(R.id.budgettypebutton);
        budgetTypeRow.setBackgroundResource(primaryRowSelector);
        budgetTypeRow.setOnClickListener(v -> {
            getCells();
            Intent i = new Intent(BudgetsEditActivity.this, LookupsListActivity.class);
            i.putExtra("type", LookupsListActivity.BUDGET_TYPE);
            lookupLauncher.launch(i);
        });
        this.budgetTypeTextView = findViewById(R.id.budgettexttextview);
        this.budgetTypeTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        ((TextView) findViewById(R.id.budgettypelabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());

        // Budget Interval (Period)
        View periodRow = findViewById(R.id.periodbutton);
        periodRow.setBackgroundResource(primaryRowSelector);
        periodRow.setOnClickListener(v -> {
            getCells();
            Intent i = new Intent(BudgetsEditActivity.this, LookupsListActivity.class);
            i.putExtra("type", LookupsListActivity.BUDGET_PERIOD);
            lookupLauncher.launch(i);
        });
        this.periodTextView = findViewById(R.id.periodtextview);
        this.periodTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        ((TextView) findViewById(R.id.periodlabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());

        // Drop-down icon tint
        int fieldLabelColor = PocketMoneyThemes.fieldLabelColor();
        ImageView iconView;
        if ((iconView = findViewById(R.id.category_drop_down)) != null) {
            iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
            iconView.setOnClickListener(v -> {
                getCells();
                Intent i = new Intent(BudgetsEditActivity.this, LookupsListActivity.class);
                i.putExtra("type", LookupsListActivity.CATEGORY_LOOKUP);
                lookupLauncher.launch(i);
            });
        }
        if ((iconView = findViewById(R.id.budget_type_drop_down)) != null) {
            iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        }
        if ((iconView = findViewById(R.id.period_drop_down)) != null) {
            iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        }

        setupCategoryAutoComplete();

        // Toggles & explanations
        View includeContainer = findViewById(R.id.include_in_budgets_container);
        includeContainer.setBackgroundResource(primaryRowSelector);
        this.includeInBudgetsCheckBox = findViewById(R.id.includeinbudgetscheckbox);
        CheckBoxTint.colorCheckBox(this.includeInBudgetsCheckBox);
        ((TextView) findViewById(R.id.includeinbudgetslabel)).setTextColor(PocketMoneyThemes.primaryCellTextColor());
        ((TextView) findViewById(R.id.includeinbudgetssubtitle)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        includeContainer.setOnClickListener(v -> {
            includeInBudgetsCheckBox.toggle();
            getCells();
        });

        View rolloverContainer = findViewById(R.id.rollover_container);
        rolloverContainer.setBackgroundResource(primaryRowSelector);
        this.rolloverCheckBox = findViewById(R.id.rollovercheckbox);
        CheckBoxTint.colorCheckBox(this.rolloverCheckBox);
        ((TextView) findViewById(R.id.rolloverlabel)).setTextColor(PocketMoneyThemes.primaryCellTextColor());
        ((TextView) findViewById(R.id.rolloversubtitle)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        rolloverContainer.setOnClickListener(v -> {
            rolloverCheckBox.toggle();
            getCells();
            renderScheduleTimeline();
        });

        View subcategoriesContainer = findViewById(R.id.subcategories_container);
        subcategoriesContainer.setBackgroundResource(primaryRowSelector);
        this.includeSubcategoriesCheckBox = findViewById(R.id.includesubcategoriescheckbox);
        CheckBoxTint.colorCheckBox(this.includeSubcategoriesCheckBox);
        ((TextView) findViewById(R.id.includesubcategorieslabel)).setTextColor(PocketMoneyThemes.primaryCellTextColor());
        ((TextView) findViewById(R.id.includesubcategoriessubtitle)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        subcategoriesContainer.setOnClickListener(v -> {
            includeSubcategoriesCheckBox.toggle();
            getCells();
        });

        // Schedule Card
        this.scheduleIntervalsContainer = findViewById(R.id.schedule_intervals_container);
        this.scheduleEmptyText = findViewById(R.id.schedule_empty_text);

        MaterialButton addIntervalButton = findViewById(R.id.add_interval_button);
        if (addIntervalButton != null) {
            int tintColor = PocketMoneyThemes.currentTintColor();
            addIntervalButton.setBackgroundColor(tintColor);
            int buttonTextColor = PocketMoneyThemes.isDarkTheme() ? 0xFF000000 : 0xFFFFFFFF;
            addIntervalButton.setTextColor(buttonTextColor);
            addIntervalButton.setIconTint(ColorStateList.valueOf(buttonTextColor));
            addIntervalButton.setOnClickListener(v -> onAddIntervalClicked());
        }
    }

    private void setupCategoryAutoComplete() {
        if (this.categoryEditText == null) return;

        int textColor = PocketMoneyThemes.primaryCellTextColor();
        int bgColor = PocketMoneyThemes.groupTableViewBackgroundColor();
        android.graphics.drawable.Drawable popupBg = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_dropdown_popup);
        if (popupBg instanceof android.graphics.drawable.GradientDrawable gd) {
            gd.setColor(bgColor);
            gd.setStroke((int) (1 * getResources().getDisplayMetrics().density), PocketMoneyThemes.fieldLabelColor() & 0x66FFFFFF);
        }

        if (this.categoryEditText instanceof android.widget.AutoCompleteTextView actv) {
            actv.setThreshold(1);
            if (popupBg != null && popupBg.getConstantState() != null) {
                actv.setDropDownBackgroundDrawable(popupBg.getConstantState().newDrawable().mutate());
            }
            actv.setDropDownVerticalOffset((int) (4 * getResources().getDisplayMetrics().density));

            ArrayList<String> categories = CategoryClass.allCategoryNamesInDatabase();
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, R.layout.dropdown_item_themed, categories) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (view instanceof TextView tv) {
                        tv.setTextColor(textColor);
                    }
                    return view;
                }
            };
            actv.setAdapter(adapter);

            actv.setOnItemClickListener((parent, view, position, id) -> {
                String selected = adapter.getItem(position);
                if (selected != null) {
                    onCategorySelected(selected);
                }
            });
        }
    }

    private void onCategorySelected(String categoryName) {
        int catID = CategoryClass.idForCategory(categoryName);
        if (catID != 0) {
            this.category = new CategoryClass(catID);
            this.oldCategory = this.category.getCategory();
            this.categoryBudgetItems = CategoryBudgetClass.budgetItemsForCategory(this.category.getCategory());
            ensureInitialBudgetTimeline();
            loadCells();
            renderScheduleTimeline();
        } else {
            this.category.setCategory(categoryName);
        }
    }

    private void applyCardThemes() {
        int cardBg = PocketMoneyThemes.cardBackgroundColor();
        int strokeColor = PocketMoneyThemes.cardStrokeColor();
        int dividerColor = PocketMoneyThemes.cardDividerColor();
        int headerTextColor = PocketMoneyThemes.fieldLabelColor();

        if (this.cardGeneralSettings != null) {
            this.cardGeneralSettings.setCardBackgroundColor(cardBg);
            this.cardGeneralSettings.setStrokeColor(strokeColor);
        }
        if (this.cardBudgetSchedule != null) {
            this.cardBudgetSchedule.setCardBackgroundColor(cardBg);
            this.cardBudgetSchedule.setStrokeColor(strokeColor);
        }

        // Apply divider colors
        int[] dividerIds = {
                R.id.card1_divider1, R.id.card1_divider2, R.id.card1_divider3,
                R.id.card1_divider4, R.id.card1_divider5, R.id.schedule_table_header_divider
        };
        for (int id : dividerIds) {
            View divider = findViewById(id);
            if (divider != null) {
                divider.setBackgroundColor(dividerColor);
            }
        }

        // Table headers text color and background matching the card
        View tableHeaders = findViewById(R.id.schedule_table_headers);
        if (tableHeaders != null) {
            tableHeaders.setBackgroundColor(cardBg);
        }
        TextView headerFrom = findViewById(R.id.header_from);
        if (headerFrom != null) headerFrom.setTextColor(headerTextColor);
        TextView headerTo = findViewById(R.id.header_to);
        if (headerTo != null) headerTo.setTextColor(headerTextColor);
        TextView headerAmount = findViewById(R.id.header_amount);
        if (headerAmount != null) headerAmount.setTextColor(headerTextColor);
        TextView headerReset = findViewById(R.id.header_reset);
        if (headerReset != null) headerReset.setTextColor(headerTextColor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderScheduleTimeline();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, MENU_SAVE, 0, SMMoney.getAppContext().getString(R.string.GENERAL_SAVE));
        item.setIcon(R.drawable.ic_save_white_24dp);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == MENU_SAVE) {
            saveAction();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCells() {
        this.categoryEditText.setText(this.category.getCategory());
        this.budgetTypeTextView.setText(this.category.typeAsString());
        this.periodTextView.setText(this.category.periodAsString());
        this.includeInBudgetsCheckBox.setChecked(this.category.getIncludeInBudgets());
        this.rolloverCheckBox.setChecked(this.category.getRollover());
        this.includeSubcategoriesCheckBox.setChecked(this.category.getIncludeSubcategories());
    }

    private void getCells() {
        this.category.setCategory(this.categoryEditText.getText().toString());
        this.category.setTypeFromString(this.budgetTypeTextView.getText().toString());
        this.category.setPeriodFromString(this.periodTextView.getText().toString());
        this.category.setIncludeInBudgets(this.includeInBudgetsCheckBox.isChecked());
        this.category.setRollover(this.rolloverCheckBox.isChecked());
        this.category.setIncludeSubcategories(this.includeSubcategoriesCheckBox.isChecked());

        // Keep category.budgetLimit synchronized with latest active interval for backward compatibility
        if (!this.categoryBudgetItems.isEmpty()) {
            this.categoryBudgetItems.sort(Comparator.comparing(CategoryBudgetClass::getDate));
            CategoryBudgetClass latest = this.categoryBudgetItems.get(this.categoryBudgetItems.size() - 1);
            this.category.setBudgetLimit(latest.getBudgetLimit());
        }
    }

    /**
     * Converts raw CategoryBudgetClass date points into a continuous timeline of ScheduleSegments,
     * identifying active intervals, inactive gaps (£0.00), and ongoing ranges.
     */
    private List<ScheduleSegment> calculateScheduleSegments() {
        List<ScheduleSegment> segments = new ArrayList<>();
        if (this.categoryBudgetItems.isEmpty()) {
            return segments;
        }

        // Sort chronological (Oldest to Newest)
        this.categoryBudgetItems.sort(Comparator.comparing(CategoryBudgetClass::getDate));

        for (int i = 0; i < this.categoryBudgetItems.size(); i++) {
            CategoryBudgetClass current = this.categoryBudgetItems.get(i);
            GregorianCalendar from = CalExt.beginningOfDay((GregorianCalendar) current.getDate().clone());
            double amount = current.getBudgetLimit();
            boolean reset = current.getResetRollover();
            boolean isGap = (amount <= 0.0001);

            if (i < this.categoryBudgetItems.size() - 1) {
                // To date is 1 day before the next entry's date
                CategoryBudgetClass next = this.categoryBudgetItems.get(i + 1);
                GregorianCalendar to = CalExt.endOfDay((GregorianCalendar) next.getDate().clone());
                to.add(Calendar.DAY_OF_MONTH, -1);

                segments.add(new ScheduleSegment(from, to, false, amount, reset, isGap, current));
            } else {
                // Last item is Ongoing
                segments.add(new ScheduleSegment(from, null, true, amount, reset, isGap, current));
            }
        }

        return segments;
    }

    private void renderScheduleTimeline() {
        this.scheduleIntervalsContainer.removeAllViews();
        List<ScheduleSegment> segments = calculateScheduleSegments();

        if (segments.isEmpty()) {
            this.scheduleEmptyText.setVisibility(View.VISIBLE);
            return;
        }
        this.scheduleEmptyText.setVisibility(View.GONE);

        // Display newest intervals at the top
        List<ScheduleSegment> displayList = new ArrayList<>(segments);
        Collections.reverse(displayList);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < displayList.size(); i++) {
            ScheduleSegment segment = displayList.get(i);
            @SuppressLint("InflateParams")
            View row = inflater.inflate(R.layout.item_budget_interval_row, null);

            // Alternating row background
            if (i % 2 == 1) {
                row.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
            } else {
                row.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
            }

            TextView fromDateView = row.findViewById(R.id.interval_from_date);
            TextView toDateView = row.findViewById(R.id.interval_to_date);
            TextView amountView = row.findViewById(R.id.interval_amount);
            ImageView resetTagView = row.findViewById(R.id.interval_reset_tag);
            ImageButton deleteButton = row.findViewById(R.id.interval_delete_button);

            fromDateView.setText(CalExt.descriptionWithMediumDate(segment.fromDate()));
            fromDateView.setTextColor(PocketMoneyThemes.primaryCellTextColor());

            if (segment.isOngoing()) {
                toDateView.setText(R.string.BUDGETS_ONGOING);
                toDateView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
                toDateView.setAlpha(0.6f);
            } else if (segment.toDate() != null) {
                toDateView.setText(CalExt.descriptionWithMediumDate(segment.toDate()));
                toDateView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
                toDateView.setAlpha(1.0f);
            }

            if (segment.isGap()) {
                amountView.setText(R.string.BUDGETS_INACTIVE_GAP);
                amountView.setTextColor(PocketMoneyThemes.fieldLabelColor());
            } else {
                amountView.setText(CurrencyExt.amountAsCurrency(segment.amount()));
                amountView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
            }

            // Rollover reset checkmark icon
            if (segment.resetRollover()) {
                resetTagView.setVisibility(View.VISIBLE);
                resetTagView.setColorFilter(PocketMoneyThemes.currentTintColor(), PorterDuff.Mode.SRC_IN);
            } else {
                resetTagView.setVisibility(View.GONE);
            }

            // Click to edit interval
            row.setOnClickListener(v -> onEditIntervalClicked(segment));

            // Delete action
            if (segment.sourceRecord() != null) {
                deleteButton.setColorFilter(PocketMoneyThemes.fieldLabelColor(), PorterDuff.Mode.SRC_IN);
                deleteButton.setOnClickListener(v -> confirmDeleteSegment(segment));
            } else {
                deleteButton.setVisibility(View.INVISIBLE);
            }

            this.scheduleIntervalsContainer.addView(row);
        }
    }

    private void onAddIntervalClicked() {
        GregorianCalendar defaultFrom;
        if (!this.categoryBudgetItems.isEmpty()) {
            this.categoryBudgetItems.sort(Comparator.comparing(CategoryBudgetClass::getDate));
            CategoryBudgetClass latest = this.categoryBudgetItems.get(this.categoryBudgetItems.size() - 1);
            defaultFrom = (GregorianCalendar) latest.getDate().clone();
            defaultFrom.add(Calendar.MONTH, 1);
            defaultFrom.set(Calendar.DAY_OF_MONTH, 1);
        } else {
            defaultFrom = new GregorianCalendar();
            defaultFrom.set(Calendar.DAY_OF_MONTH, 1);
        }

        double defaultAmount = 0.0d;
        if (!this.categoryBudgetItems.isEmpty()) {
            defaultAmount = this.categoryBudgetItems.get(this.categoryBudgetItems.size() - 1).getBudgetLimit();
        }

        BudgetIntervalBottomSheet bottomSheet = BudgetIntervalBottomSheet.newInstance(
                defaultFrom,
                null,
                true,
                defaultAmount,
                false,
                false
        );

        bottomSheet.setOnIntervalSavedListener((fromDate, toDate, isOngoing, amount, resetRollover) ->
                checkOverlapAndApply(fromDate, toDate, isOngoing, amount, resetRollover, null));

        bottomSheet.show(getSupportFragmentManager(), "add_interval_sheet");
    }

    private void onEditIntervalClicked(ScheduleSegment segment) {
        BudgetIntervalBottomSheet bottomSheet = BudgetIntervalBottomSheet.newInstance(
                segment.fromDate(),
                segment.toDate(),
                segment.isOngoing(),
                segment.amount(),
                segment.resetRollover(),
                true
        );

        bottomSheet.setOnIntervalSavedListener((fromDate, toDate, isOngoing, amount, resetRollover) ->
                checkOverlapAndApply(fromDate, toDate, isOngoing, amount, resetRollover, segment.sourceRecord()));

        bottomSheet.show(getSupportFragmentManager(), "edit_interval_sheet");
    }

    /**
     * Dry-runs and builds a deterministic plan of schedule adjustments when adding/modifying an interval.
     */
    private AdjustmentPlan planScheduleAdjustments(
            GregorianCalendar fromDate,
            @Nullable GregorianCalendar toDate,
            boolean isOngoing,
            @Nullable CategoryBudgetClass editingRecord) {

        AdjustmentPlan plan = new AdjustmentPlan();
        GregorianCalendar startDay = CalExt.beginningOfDay((GregorianCalendar) fromDate.clone());
        GregorianCalendar endDay = (!isOngoing && toDate != null)
                ? CalExt.beginningOfDay((GregorianCalendar) toDate.clone())
                : null;

        List<ScheduleSegment> currentSegments = calculateScheduleSegments();
        CategoryBudgetClass straddlingAfterItem = null;
        double rateAtExitBoundary = 0.0d;

        for (ScheduleSegment seg : currentSegments) {
            CategoryBudgetClass src = seg.sourceRecord();
            if (src == null || src == editingRecord) continue;

            GregorianCalendar segFrom = CalExt.beginningOfDay((GregorianCalendar) seg.fromDate().clone());
            GregorianCalendar segTo = seg.toDate() != null ? CalExt.beginningOfDay((GregorianCalendar) seg.toDate().clone()) : null;

            // --- 1. Check Preceding Intervals (moving start date earlier) ---
            if (segFrom.before(startDay)) {
                // If this preceding interval used to extend to or past startDay, it will now be truncated to (startDay - 1)
                if (segTo == null || !segTo.before(startDay)) {
                    GregorianCalendar newPrevEnd = (GregorianCalendar) startDay.clone();
                    newPrevEnd.add(Calendar.DAY_OF_MONTH, -1);
                    String oldToStr = (segTo != null) ? CalExt.descriptionWithMediumDate(segTo) : getString(R.string.BUDGETS_ONGOING);
                    plan.bulletPoints.add(getString(R.string.BUDGETS_OVERLAP_ITEM_PRECEDING,
                            CurrencyExt.amountAsCurrency(seg.amount()),
                            CalExt.descriptionWithMediumDate(newPrevEnd),
                            oldToStr));
                }
                // Track the rate active at the exit boundary if this segment spans past endDay
                if (endDay != null && (segTo == null || !segTo.before(endDay))) {
                    rateAtExitBoundary = seg.amount();
                }
                continue;
            }

            // --- 2. Check Subsequent or Enclosed Intervals ---
            if (isOngoing) {
                // Anything starting on or after startDay is superseded
                plan.itemsToRemove.add(src);
                String toStr = (segTo != null) ? CalExt.descriptionWithMediumDate(segTo) : getString(R.string.BUDGETS_ONGOING);
                plan.bulletPoints.add(getString(R.string.BUDGETS_OVERLAP_ITEM_REMOVED,
                        CalExt.descriptionWithMediumDate(segFrom), toStr, CurrencyExt.amountAsCurrency(seg.amount())));
            } else {
                // Segment is completely inside [startDay, endDay]
                if (segTo != null && !segTo.after(endDay)) {
                    plan.itemsToRemove.add(src);
                    plan.bulletPoints.add(getString(R.string.BUDGETS_OVERLAP_ITEM_REMOVED,
                            CalExt.descriptionWithMediumDate(segFrom), CalExt.descriptionWithMediumDate(segTo), CurrencyExt.amountAsCurrency(seg.amount())));
                }
                // Segment starts inside [startDay, endDay] but continues after endDay
                else if (!segFrom.after(endDay)) {
                    straddlingAfterItem = src;
                    rateAtExitBoundary = seg.amount();
                }
            }
        }

        // --- 3. Check Exit Boundary (day after endDay) ---
        if (!isOngoing && endDay != null) {
            GregorianCalendar dayAfter = CalExt.beginningOfDay((GregorianCalendar) endDay.clone());
            dayAfter.add(Calendar.DAY_OF_MONTH, 1);

            if (straddlingAfterItem != null) {
                plan.itemsToShift.add(new AdjustmentPlan.ItemShift(straddlingAfterItem, dayAfter));
                plan.bulletPoints.add(getString(R.string.BUDGETS_OVERLAP_ITEM_SHIFTED,
                        CurrencyExt.amountAsCurrency(straddlingAfterItem.getBudgetLimit()),
                        CalExt.descriptionWithMediumDate(dayAfter),
                        CalExt.descriptionWithMediumDate(straddlingAfterItem.getDate())));
            } else {
                // Check if an item already exists starting on dayAfter
                boolean existsAtDayAfter = false;
                for (CategoryBudgetClass item : this.categoryBudgetItems) {
                    if (item != editingRecord && !plan.itemsToRemove.contains(item) && CalExt.beginningOfDay(item.getDate()).equals(dayAfter)) {
                        existsAtDayAfter = true;
                        break;
                    }
                }
                if (!existsAtDayAfter) {
                    plan.requiresEndBoundary = true;
                    plan.endBoundaryDate = dayAfter;
                    plan.endBoundaryRate = rateAtExitBoundary;
                }
            }
        }

        return plan;
    }

    /**
     * Checks if applying the given interval will modify or shift adjacent/overlapping budget records,
     * and prompts the user with an explicit detailed bulleted confirmation dialog if so.
     */
    private void checkOverlapAndApply(
            GregorianCalendar fromDate,
            @Nullable GregorianCalendar toDate,
            boolean isOngoing,
            double amount,
            boolean resetRollover,
            @Nullable CategoryBudgetClass editingRecord) {

        AdjustmentPlan plan = planScheduleAdjustments(fromDate, toDate, isOngoing, editingRecord);

        if (plan.hasChanges()) {
            GregorianCalendar startDay = CalExt.beginningOfDay((GregorianCalendar) fromDate.clone());
            String endStr = (!isOngoing && toDate != null)
                    ? CalExt.descriptionWithMediumDate(toDate)
                    : getString(R.string.BUDGETS_ONGOING);

            StringBuilder msg = new StringBuilder();
            msg.append(getString(R.string.BUDGETS_OVERLAP_PROMPT_HEADER,
                    CalExt.descriptionWithMediumDate(startDay), endStr)).append("\n\n");

            for (String bullet : plan.bulletPoints) {
                msg.append(bullet).append("\n");
            }
            msg.append("\n").append(getString(R.string.BUDGETS_OVERLAP_PROMPT_FOOTER));

            new Builder(this, PocketMoneyThemes.dialogTheme())
                    .setTitle(R.string.BUDGETS_OVERLAP_CONFIRM_TITLE)
                    .setMessage(msg.toString())
                    .setPositiveButton(R.string.GENERAL_OK, (dialog, which) ->
                            executePlanAndApply(fromDate, amount, resetRollover, editingRecord, plan))
                    .setNegativeButton(R.string.GENERAL_CANCEL, null)
                    .show();
        } else {
            executePlanAndApply(fromDate, amount, resetRollover, editingRecord, plan);
        }
    }

    /**
     * Executes the planned shifts and removals, then applies the new/updated interval.
     */
    private void executePlanAndApply(
            GregorianCalendar fromDate,
            double amount,
            boolean resetRollover,
            @Nullable CategoryBudgetClass editingRecord,
            AdjustmentPlan plan) {

        GregorianCalendar startDay = CalExt.beginningOfDay((GregorianCalendar) fromDate.clone());

        // 1. Remove superseded items
        for (CategoryBudgetClass item : plan.itemsToRemove) {
            this.categoryBudgetItems.remove(item);
            this.deletedCategoryBudgetItems.add(item);
        }

        // 2. Apply planned shifts
        for (AdjustmentPlan.ItemShift shift : plan.itemsToShift) {
            shift.item().setDate(shift.newDate());
        }

        // 3. Update or insert the primary interval record
        if (editingRecord != null) {
            editingRecord.setDate(startDay);
            editingRecord.setBudgetLimit(amount);
            editingRecord.setResetRollover(resetRollover);
        } else {
            CategoryBudgetClass existingAtDate = null;
            for (CategoryBudgetClass item : this.categoryBudgetItems) {
                if (CalExt.beginningOfDay(item.getDate()).equals(startDay)) {
                    existingAtDate = item;
                    break;
                }
            }

            if (existingAtDate != null) {
                existingAtDate.setBudgetLimit(amount);
                existingAtDate.setResetRollover(resetRollover);
            } else {
                CategoryBudgetClass newItem = new CategoryBudgetClass();
                newItem.setCategoryName(this.category.getCategory());
                newItem.setDate(startDay);
                newItem.setBudgetLimit(amount);
                newItem.setResetRollover(resetRollover);
                this.categoryBudgetItems.add(newItem);
            }
        }

        // 4. Insert end boundary record if required
        if (plan.requiresEndBoundary && plan.endBoundaryDate != null) {
            insertBoundaryRecordAt(plan.endBoundaryDate, plan.endBoundaryRate);
        }

        // 5. Clean up any duplicate same-day entries
        cleanupRedundantTimelineItems();

        renderScheduleTimeline();
    }

    private void insertBoundaryRecordAt(GregorianCalendar boundaryDate, double rate) {
        CategoryBudgetClass found = null;
        for (CategoryBudgetClass item : this.categoryBudgetItems) {
            if (CalExt.beginningOfDay(item.getDate()).equals(boundaryDate)) {
                found = item;
                break;
            }
        }
        if (found == null) {
            CategoryBudgetClass newItem = new CategoryBudgetClass();
            newItem.setCategoryName(this.category.getCategory());
            newItem.setDate(boundaryDate);
            newItem.setBudgetLimit(rate);
            newItem.setResetRollover(false);
            this.categoryBudgetItems.add(newItem);
        }
    }

    private void cleanupRedundantTimelineItems() {
        this.categoryBudgetItems.sort(Comparator.comparing(CategoryBudgetClass::getDate));
        List<CategoryBudgetClass> toRemove = new ArrayList<>();

        // Remove duplicate same-day entries (keep the latest modified)
        for (int i = 0; i < this.categoryBudgetItems.size() - 1; i++) {
            CategoryBudgetClass c1 = this.categoryBudgetItems.get(i);
            CategoryBudgetClass c2 = this.categoryBudgetItems.get(i + 1);
            if (CalExt.beginningOfDay(c1.getDate()).equals(CalExt.beginningOfDay(c2.getDate()))) {
                toRemove.add(c1);
            }
        }

        for (CategoryBudgetClass item : toRemove) {
            this.categoryBudgetItems.remove(item);
            this.deletedCategoryBudgetItems.add(item);
        }
    }

    private void confirmDeleteSegment(ScheduleSegment segment) {
        if (segment.sourceRecord() == null) return;

        if (this.categoryBudgetItems.size() <= 1) {
            Toast.makeText(this, "A budget must have at least one timeline entry.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(R.string.GENERAL_DELETE)
                .setMessage("Delete this budget interval from " + CalExt.descriptionWithMediumDate(segment.fromDate()) + "?")
                .setPositiveButton(R.string.GENERAL_YES, (dialog, which) -> {
                    BudgetsEditActivity.this.categoryBudgetItems.remove(segment.sourceRecord());
                    BudgetsEditActivity.this.deletedCategoryBudgetItems.add(segment.sourceRecord());
                    BudgetsEditActivity.this.renderScheduleTimeline();
                })
                .setNegativeButton(R.string.GENERAL_NO, null)
                .show();
    }

    private void saveAction() {
        getCells();
        if (this.oldCategory == null || this.oldCategory.isEmpty()) {
            int catID = CategoryClass.idForCategory(this.category.getCategory());
            if (this.oldCategory == null || this.oldCategory.equalsIgnoreCase(this.category.getCategory())) {
                this.category = new CategoryClass(catID);
                getCells();
            }
            save();
            finish();
        } else if (this.category.getCategory().equalsIgnoreCase(this.oldCategory)) {
            save();
            finish();
        } else {
            CharSequence[] items = new CharSequence[]{
                    SMMoney.getAppContext().getString(R.string.LOOKUPS_POPUPLIST),
                    SMMoney.getAppContext().getString(R.string.LOOKUPS_EVERYWHERE)
            };
            Builder builder = new Builder(this, PocketMoneyThemes.dialogTheme());
            builder.setTitle(SMMoney.getAppContext().getString(R.string.LOOKUPS_CHANGEBODY));
            builder.setItems(items, (dialog, item) -> {
                int catID = CategoryClass.idForCategory(BudgetsEditActivity.this.category.getCategory());
                int oldCatID = CategoryClass.idForCategory(BudgetsEditActivity.this.oldCategory);
                if (catID != 0) {
                    BudgetsEditActivity.this.category = new CategoryClass(catID);
                    BudgetsEditActivity.this.getCells();
                }
                if (oldCatID != 0) {
                    new CategoryClass(oldCatID).deleteFromDatabase();
                    BudgetsEditActivity.this.category.categoryID = catID;
                }
                BudgetsEditActivity.this.save();
                switch (item) {
                    case PocketMoneyThemes.kThemeBlack /*0*/ -> BudgetsEditActivity.this.finish();
                    case SplitsActivity.RESULT_CHANGED /*1*/ -> {
                        TransactionClass.renameCategoryFromTo(BudgetsEditActivity.this.oldCategory, BudgetsEditActivity.this.category.getCategory());
                        BudgetsEditActivity.this.finish();
                    }
                }
            });
            builder.create().show();
        }
    }

    private void save() {
        this.category.saveToDatabase();
        for (CategoryBudgetClass item : this.categoryBudgetItems) {
            item.setCategoryName(this.category.getCategory());
            item.saveToDatabase();
        }
        for (CategoryBudgetClass bItem : this.deletedCategoryBudgetItems) {
            if (bItem.categoryBudgetID != 0) {
                bItem.deleteFromDatabase();
            }
        }
        if (this.category.getCategory() != null && !this.category.getCategory().equals(this.oldCategory)) {
            CategoryBudgetClass.renameBudgetItems(this.oldCategory, this.category.getCategory());
        }
    }
}
